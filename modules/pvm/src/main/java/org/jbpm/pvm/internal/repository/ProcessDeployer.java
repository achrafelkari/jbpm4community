/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jbpm.pvm.internal.repository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessDefinitionQuery;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.migration.InstanceMigrator;
import org.jbpm.pvm.internal.migration.MigrationDescriptor;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;
import org.jbpm.pvm.internal.session.RepositorySession;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;


/**
 * @author Tom Baeyens
 */
public class ProcessDeployer implements Deployer {
  
  private static final Log log = Log.getLog(ProcessDeployer.class.getName());

  protected String extension;
  protected Parser parser;

  public ProcessDeployer(String extension, Parser parser) {
    this.extension = extension;
    this.parser = parser;
  }

  @SuppressWarnings("unchecked")
  public void deploy(DeploymentImpl deployment) {
    
    for (String resourceName: deployment.getResourceNames()) {
      
      if (resourceName.endsWith(extension)) {
        byte[] bytes = deployment.getBytes(resourceName);
        InputStream inputStream = new ByteArrayInputStream(bytes);
        Parse parse = parser.createParse();
        parse.contextMapPut(Parse.CONTEXT_KEY_DEPLOYMENT, deployment);
        parse.setProblems(deployment.getProblems());
        parse.setInputStream(inputStream);
        parse.execute();
        List<ProcessDefinitionImpl> processDefinitions = (List<ProcessDefinitionImpl>) parse.getDocumentObject();
        if (processDefinitions!=null) {
          for (ProcessDefinitionImpl processDefinition : processDefinitions) {
            if ((processDefinition != null) && (processDefinition.getName() != null)) {
              processDefinition.setSuspended(deployment.isSuspended());

              String imageResourceName = resourceName.substring(0, resourceName.lastIndexOf(extension)) + ".png";
              if (deployment.getResourceNames().contains(imageResourceName)) {
                processDefinition.setImageResourceName(imageResourceName);
              }

              processDefinition.setDeploymentDbid(deployment.getDbid());

              String processDefinitionName = processDefinition.getName();
              if (deployment.hasObjectProperties(processDefinitionName)) {
                String key = deployment.getProcessDefinitionKey(processDefinitionName);
                String id = deployment.getProcessDefinitionId(processDefinitionName);
                Long version = deployment.getProcessDefinitionVersion(processDefinitionName);
                processDefinition.setId(id);
                processDefinition.setKey(key);
                processDefinition.setVersion(version.intValue());
            
              } else {
                checkKey(processDefinition, deployment);
                checkVersion(processDefinition, deployment);
                checkId(processDefinition, deployment);

                deployment.setProcessDefinitionId(processDefinitionName, processDefinition.getId());
                deployment.setProcessDefinitionKey(processDefinitionName, processDefinition.getKey());
                deployment.setProcessDefinitionVersion(processDefinitionName, new Long(processDefinition.getVersion()));
                
                //execute migration
                Map<ProcessDefinition, MigrationDescriptor> migrations = (Map<ProcessDefinition, MigrationDescriptor>)parse.contextMapGet(Parse.CONTEXT_KEY_MIGRATIONS);
                if (migrations != null) {
                  MigrationDescriptor migrationDescriptor = migrations.get(processDefinition);
                  if (migrationDescriptor != null) {
                    InstanceMigrator.migrateAll(processDefinition, migrationDescriptor);
                  }
                }
                
              }

              deployment.addObject(processDefinitionName, processDefinition);
            }
          }
        }
      }
    }
  }
  
  protected void checkKey(ProcessDefinitionImpl processDefinition, DeploymentImpl deployment) {
    String processDefinitionName = processDefinition.getName();
    String processDefinitionKey = processDefinition.getKey();

    // if no key was specified in the jpdl process file
    if (processDefinitionKey==null) {
      // derive the key from the name
      // replace any non-word character with an underscore
      processDefinitionKey = processDefinitionName.replaceAll("[^\\p{L}\\p{N}]", "_");
      processDefinition.setKey(processDefinitionKey);
    }
    
    RepositorySession repositorySession = EnvironmentImpl.getFromCurrent(RepositorySession.class);

    List<ProcessDefinition> existingProcesses = repositorySession.createProcessDefinitionQuery()
        .processDefinitionName(processDefinitionName)
        .list();
    
    for (ProcessDefinition existingProcess: existingProcesses) {
      if (!processDefinitionKey.equals(existingProcess.getKey())) {
        deployment.addProblem("invalid key '"+processDefinitionKey+"' in process "+processDefinition.getName()+".  Existing process has name '"+processDefinitionName+"' and key '"+existingProcess.getKey()+"'");
      }
    }

    existingProcesses = repositorySession.createProcessDefinitionQuery()
        .processDefinitionKey(processDefinitionKey)
        .list();
    
    for (ProcessDefinition existingProcess: existingProcesses) {
      if (!processDefinitionName.equals(existingProcess.getName())) {
        deployment.addProblem("invalid name '"+processDefinitionName+"' in process "+processDefinition.getName()+".  Existing process has name '"+existingProcess.getName()+"' and key '"+processDefinitionKey+"'");
      }
    }
  }

  protected void checkId(ProcessDefinitionImpl processDefinition, DeploymentImpl deployment) {
    String id = processDefinition.getId();
    if (id==null) {
      id = processDefinition.getKey()+"-"+processDefinition.getVersion();
      if (log.isTraceEnabled()) log.trace("created id '"+id+"' for "+processDefinition);
      processDefinition.setId(id);
    }
    
    RepositorySession repositorySession = EnvironmentImpl.getFromCurrent(RepositorySession.class);
    ProcessDefinition existingProcessDefinition = repositorySession.createProcessDefinitionQuery()
        .processDefinitionId(id)
        .uniqueResult();
    if (existingProcessDefinition != null) {
      deployment.addProblem("process '" + id + "' already exists");
    }
  }
  
  protected void checkVersion(ProcessDefinitionImpl processDefinition, DeploymentImpl deployment) {
    int version = processDefinition.getVersion();
    String key = processDefinition.getKey();
    if (version==ProcessDefinitionImpl.UNASSIGNED_VERSION) {
      RepositorySession repositorySession = EnvironmentImpl.getFromCurrent(RepositorySession.class);
      
      ProcessDefinition latestDeployedVersion = repositorySession
          .createProcessDefinitionQuery()
          .processDefinitionKey(key)
          .orderDesc(ProcessDefinitionQuery.PROPERTY_VERSION)
          .page(0, 1)
          .uniqueResult();

      if (latestDeployedVersion!=null) {
        version = latestDeployedVersion.getVersion() + 1;
      } else {
        version = 1;
      }
      if (log.isTraceEnabled()) log.trace("assigning version "+version+" to process definition "+key);
      processDefinition.setVersion(version);
    }
  }

  public void updateResource(DeploymentImpl deployment, String resourceName, byte[] bytes) {
    deployment.addResourceFromInputStream(resourceName, new ByteArrayInputStream(bytes));
  }
}

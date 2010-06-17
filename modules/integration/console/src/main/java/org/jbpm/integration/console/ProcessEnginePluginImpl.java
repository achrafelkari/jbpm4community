/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jbpm.integration.console;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.jboss.bpm.console.client.model.DeploymentRef;
import org.jboss.bpm.console.client.model.JobRef;
import org.jboss.bpm.console.server.plugin.ProcessEnginePlugin;
import org.jbpm.api.Deployment;
import org.jbpm.api.ManagementService;
import org.jbpm.api.NewDeployment;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessDefinitionQuery;
import org.jbpm.api.RepositoryService;
import org.jbpm.api.job.Job;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 * @author jbarrez
 */
public class ProcessEnginePluginImpl extends JBPMIntegration implements ProcessEnginePlugin {

  public ProcessEnginePluginImpl() {
    super();
  }

  public List<DeploymentRef> getDeployments() {
    List<DeploymentRef> results = new ArrayList<DeploymentRef>();

    RepositoryService repositoryService = this.processEngine.getRepositoryService();
    List<Deployment> dpls = repositoryService.createDeploymentQuery().list();

    for (Deployment dpl : dpls) {
      DeploymentRef deploymentRef = ModelAdaptor.adoptDeployment(dpl);

      // active processes for deployment
      ProcessDefinitionQuery pdQuery = repositoryService.createProcessDefinitionQuery();
      pdQuery.deploymentId(dpl.getId());
      List<ProcessDefinition> activePds = pdQuery.list();

      for (ProcessDefinition procDef : activePds) {
        deploymentRef.getDefinitions().add(procDef.getId());
      }

      // suspended processes for deployment
      List<ProcessDefinition> suspendedPds = 
        repositoryService.createProcessDefinitionQuery()
                         .deploymentId(dpl.getId())
                         .suspended()
                         .list();

      for (ProcessDefinition procDef : suspendedPds) {
        deploymentRef.getDefinitions().add(procDef.getId());
      }

      results.add(deploymentRef);
    }

    return results;
  }
  
  public void deleteDeployment(String id) {
    RepositoryService repositoryService = this.processEngine.getRepositoryService();
    repositoryService.deleteDeploymentCascade(id);
  }

  public void suspendDeployment(String id, boolean isSuspended) {
    RepositoryService repositoryService = this.processEngine.getRepositoryService();
    if (isSuspended)
      repositoryService.suspendDeployment(id);
    else
      repositoryService.resumeDeployment(id); 
  }

  public List<JobRef> getJobs() {
    ManagementService mgmtService = this.processEngine.getManagementService();
    List<JobRef> results = new ArrayList<JobRef>();

    // timers
    List<Job> timers = mgmtService.createJobQuery().timers().list();
    for (Job t : timers) {
      JobRef ref = ModelAdaptor.adoptJob(t);
      ref.setType("timer");
      results.add(ref);
    }

    // messages
    List<Job> msgs = mgmtService.createJobQuery().messages().list();
    for (Job t : msgs) {
      JobRef ref = ModelAdaptor.adoptJob(t);
      ref.setType("message");
      results.add(ref);
    }

    return results;
  }

  public void executeJob(String jobId) {
    ManagementService mgmtService = this.processEngine.getManagementService();
    mgmtService.executeJob(jobId);
  }
  
  public String deployFile(File processFile) {
    RepositoryService repositoryService = this.processEngine.getRepositoryService();
    NewDeployment deployment = repositoryService.createDeployment();
    deployment.setName(processFile.getName());
    deployment.setTimestamp(System.currentTimeMillis());
    
    if (processFile.getName().endsWith(".xml")) {
      
      deployment.addResourceFromFile(processFile);
      
    } else if (processFile.getName().endsWith("ar")) {
      
      try {
        FileInputStream fileInputStream = new FileInputStream(processFile);
        ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
        deployment.addResourcesFromZipInputStream(zipInputStream);
      } catch (Exception e) {
        throw new RuntimeException("couldn't read business archive "+processFile, e);
      }

    } 
    
    return deployment.deploy();
  }
}

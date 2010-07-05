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
package org.jbpm.pvm.internal.cmd;

import java.util.List;

import org.hibernate.Session;

import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.repository.DeploymentImpl;
import org.jbpm.pvm.internal.repository.RepositoryCache;
import org.jbpm.pvm.internal.session.DbSession;
import org.jbpm.pvm.internal.session.RepositorySession;

/**
 * @author Tom Baeyens
 */
public class DeleteDeploymentCmd implements Command<Object> {

  private static final Log log = Log.getLog(DeleteDeploymentCmd.class.getName());
  
  private static final long serialVersionUID = 1L;
  
  String deploymentId;
  boolean cascade;
  
  public DeleteDeploymentCmd(String deploymentId) {
    this.deploymentId = deploymentId;
  }
  
  public DeleteDeploymentCmd(String deploymentId, boolean cascade) {
    this.deploymentId = deploymentId;
    this.cascade = cascade;
  }

  public Object execute(Environment environment) throws Exception {
    RepositorySession repositorySession = environment.get(RepositorySession.class);
    List<ProcessDefinition> processDefinitions = repositorySession.createProcessDefinitionQuery()
      .deploymentId(deploymentId)
      .list();
    
    DbSession dbSession = environment.get(DbSession.class); 

    for (ProcessDefinition processDefinition: processDefinitions) {
      String processDefinitionId = processDefinition.getId();
      List<String> processInstanceIds = dbSession.findProcessInstanceIds(processDefinitionId);
      
      if (cascade) {
        for (String processInstanceId: processInstanceIds) {
          dbSession.deleteProcessInstance(processInstanceId, true);
        }

        dbSession.deleteProcessDefinitionHistory(processDefinitionId);
        
      } else {
        if (!processInstanceIds.isEmpty()) {
          throw new JbpmException("cannot delete deployment "+deploymentId+": still executions for "+processDefinition+": "+processInstanceIds);
        }
      }
    }
    
    Session session = environment.get(Session.class);
    DeploymentImpl deployment = (DeploymentImpl) session.load(DeploymentImpl.class, Long.parseLong(deploymentId));
    log.debug("deleting deployment "+deploymentId);
    session.delete(deployment);
    
    RepositoryCache repositoryCache = environment.get(RepositoryCache.class);
    repositoryCache.remove(deploymentId);

    return null;
  }
}

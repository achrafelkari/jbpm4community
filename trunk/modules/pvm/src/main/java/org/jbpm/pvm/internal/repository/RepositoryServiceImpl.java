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
import java.util.Set;

import org.jbpm.api.DeploymentQuery;
import org.jbpm.api.NewDeployment;
import org.jbpm.api.ProcessDefinitionQuery;
import org.jbpm.api.RepositoryService;
import org.jbpm.api.model.ActivityCoordinates;
import org.jbpm.pvm.internal.cmd.CreateDeploymentQueryCmd;
import org.jbpm.pvm.internal.cmd.CreateProcessDefinitionQueryCmd;
import org.jbpm.pvm.internal.cmd.DeleteDeploymentCmd;
import org.jbpm.pvm.internal.cmd.GetActivityCoordinatesCmd;
import org.jbpm.pvm.internal.cmd.GetDeploymentResourceNamesCmd;
import org.jbpm.pvm.internal.cmd.GetResourceAsStreamCmd;
import org.jbpm.pvm.internal.cmd.GetStartActivityNamesCmd;
import org.jbpm.pvm.internal.cmd.GetStartFormResourceNameCmd;
import org.jbpm.pvm.internal.cmd.ResumeDeploymentCmd;
import org.jbpm.pvm.internal.cmd.SuspendDeploymentCmd;
import org.jbpm.pvm.internal.cmd.UpdateDeploymentResourceCmd;
import org.jbpm.pvm.internal.query.DeploymentQueryImpl;
import org.jbpm.pvm.internal.query.ProcessDefinitionQueryImpl;
import org.jbpm.pvm.internal.svc.AbstractServiceImpl;

/**
 * @author Tom Baeyens
 */
public class RepositoryServiceImpl extends AbstractServiceImpl implements RepositoryService {

  public NewDeployment createDeployment() {
    return new DeploymentImpl(commandService);
  }

  public void suspendDeployment(String deploymentId) {
    commandService.execute(new SuspendDeploymentCmd(deploymentId));
  }

  public void resumeDeployment(String deploymentId) {
    commandService.execute(new ResumeDeploymentCmd(deploymentId));
  }

  public void deleteDeployment(String deploymentId) {
    commandService.execute(new DeleteDeploymentCmd(deploymentId));
  }

  public void deleteDeploymentCascade(String deploymentId) {
    commandService.execute(new DeleteDeploymentCmd(deploymentId, true));
  }

  public Set<String> getResourceNames(String deploymentId) {
    return commandService.execute(new GetDeploymentResourceNamesCmd(deploymentId));
  }

  public InputStream getResourceAsStream(String deploymentId, String resource) {
    byte[] bytes = commandService.execute(new GetResourceAsStreamCmd(deploymentId, resource));
    if (bytes!=null) {
      return new ByteArrayInputStream(bytes);
    }
    return null;
  }

  public ProcessDefinitionQuery createProcessDefinitionQuery() {
    ProcessDefinitionQueryImpl query = commandService.execute(new CreateProcessDefinitionQueryCmd());
    query.setCommandService(commandService);
    return query;
  }

  public DeploymentQuery createDeploymentQuery() {
    DeploymentQueryImpl query = commandService.execute(new CreateDeploymentQueryCmd());
    query.setCommandService(commandService);
    return query;
  }

  public ActivityCoordinates getActivityCoordinates(String processDefinitionId, String activityName) {
    return commandService.execute(new GetActivityCoordinatesCmd(processDefinitionId, activityName));
  }

  public List<String> getStartActivityNames(String processDefinitionId) {
    return commandService.execute(new GetStartActivityNamesCmd(processDefinitionId));
  }

  public String getStartFormResourceName(String processDefinitionId, String activityName) {
    return commandService.execute(new GetStartFormResourceNameCmd(processDefinitionId, activityName));
  }

  public void updateDeploymentResource(String deploymentId, String resourceName, InputStream inputStream) {
    commandService.execute(new UpdateDeploymentResourceCmd(deploymentId, resourceName, inputStream));
  }
}

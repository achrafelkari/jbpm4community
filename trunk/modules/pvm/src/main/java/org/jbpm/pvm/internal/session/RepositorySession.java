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
package org.jbpm.pvm.internal.session;

import org.jbpm.api.NewDeployment;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;
import org.jbpm.pvm.internal.query.ProcessDefinitionQueryImpl;
import org.jbpm.pvm.internal.repository.DeploymentImpl;


/**
 * @author Tom Baeyens
 */
public interface RepositorySession {

  String deploy(NewDeployment deployment);
  
  DeploymentImpl getDeployment(String deploymentId);

  Object getObject(String deploymentId, String objectName);

  byte[] getBytes(String deploymentId, String resourceName);

  /** create a process definition query */
  ProcessDefinitionQueryImpl createProcessDefinitionQuery();

  ProcessDefinitionImpl findProcessDefinitionById(String processDefinitionId);

  ProcessDefinitionImpl findProcessDefinitionByKey(String processDefinitionKey);
  
  ProcessDefinitionImpl findLatestProcessDefinitionByName(String processDefinitionName);

  void updateDeploymentResource(String deploymentId, String resourceName, byte[] bytes);
}

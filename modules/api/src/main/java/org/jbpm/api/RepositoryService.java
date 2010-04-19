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
package org.jbpm.api;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.jbpm.api.model.ActivityCoordinates;


/** exposes the repository of deployments.
 * 
 * <p>Deployments contain a set of named resources.  Those 
 * resources can represent process definitions, forms, images and so on.
 * </p>
 * 
 * <p>The repository contains and manages the process definitions.
 * </p>
 * 
 * @author Tom Baeyens
 */
public interface RepositoryService {

  /** create a new deployment.  The deployment will only be deployed into 
   * the repository after invoking the {@link NewDeployment#deploy()}.  */
  NewDeployment createDeployment();

  /** similar to deleting a deployment.  The difference is that 
   * all the data remains in the database.  So that the suspending 
   * can be undone with {@link #resumeDeployment(String)}. */
  void suspendDeployment(String deploymentId);
  
  /** resume a suspended deployment. */
  void resumeDeployment(String deploymentId);
  
  /** deletes a deployment if the process definitions don't have 
   * running executions.  history information (if any) remains 
   * in the database. */
  void deleteDeployment(String deploymentId);
  
  /** deletes deployment, contained process definitions, related process instances 
   * and their history information */
  void deleteDeploymentCascade(String deploymentId);
  
  /** Returns all the resources stored in the deployment with the given id. */
  Set<String> getResourceNames(String deploymentId);
  
  /** obtain an InputStream to a resource in a deployment */
  InputStream getResourceAsStream(String deploymentId, String resourceName);

  /** create a query for process definitions */
  ProcessDefinitionQuery createProcessDefinitionQuery();

  /** create a query for deployments */
  DeploymentQuery createDeploymentQuery();

  /** find all the activity names of the start activities for a given 
   * process definition.  Only top level activities are scanned.  
   * Every activity that doesn't have incoming transitions is considered 
   * a start activity. For the moment, jPDL only supports one 
   * start activity.  Start activity names can be null. */
  List<String> getStartActivityNames(String processDefinitionId);
  
  /** the resource name for the given start activity. It is assumed that 
   * the ProcessDefinition is already retrieved.  From the ProcessDefinition,
   * the {@link ProcessDefinition#getId()} and the {@link ProcessDefinition#getDeploymentId()}
   * can be retrieved.  The activityName can be obtained via {@link #getStartActivityNames(String)}.
   * An InputStream for the resource can be obtained with {@link #getResourceAsStream(String, String)}  */
  String getStartFormResourceName(String processDefinitionId, String activityName);
  
  /** the coordinates for the activity on 
   * {@link ProcessDefinition#getImageResourceName() the process image}. */
  ActivityCoordinates getActivityCoordinates(String processDefinitionId, String activityName);
}

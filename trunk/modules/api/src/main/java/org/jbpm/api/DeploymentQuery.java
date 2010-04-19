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

import java.util.List;

/** find existing deployments in the repository.
 * 
 * @author Tom Baeyens
 */
public interface DeploymentQuery {
  
  String PROPERTY_TIMESTAMP = "timestamp";
  String PROPERTY_STATE = "state";

  /** only include a specific deployment by id */
  DeploymentQuery deploymentId(String id);
  
  /** only select suspended deployments */
  DeploymentQuery suspended();
  
  /** only select non-suspended deployments */
  DeploymentQuery notSuspended();

  /** order results ascending for property {@link #PROPERTY_TIMESTAMP} 
   * or {@link #PROPERTY_STATE} */
  DeploymentQuery orderAsc(String property);

  /** order results descending for property {@link #PROPERTY_TIMESTAMP} 
   * or {@link #PROPERTY_STATE} */
  DeploymentQuery orderDesc(String property);

  /** only select a specific page */ 
  DeploymentQuery page(int firstResult, int maxResults);
  
  /** execute the query and get the result list */ 
  List<Deployment> list();
  
  /** execute the query and get the unique result */ 
  Deployment uniqueResult();
  
  /** execute a count(*) query and returns number of results */ 
  long count();
}

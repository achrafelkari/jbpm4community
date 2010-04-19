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

import java.io.Serializable;


/** a graphical process which is deployed in the {@link RepositoryService}.
 * 
 * @author Tom Baeyens
 */
public interface ProcessDefinition extends Serializable {

  /** the short display name given to this process definition. 
   * Multiple process definitions can have the same as long 
   * as they are given a different {@link #getVersion() version}.  */
  String getName();

  /** the user defined short representation of the name. Just 
   * like the name, multiple versions of a process definition 
   * can have the same key. */
  String getKey();

  /** the unique id for this process definition that is used as a reference in the service methods. */
  String getId();

  /** automatically assigned during deployment of a process that 
   * represents the sequence number for process definitions with 
   * the same {@link #getName() name}. */ 
  int getVersion();
  
  /** references the deployment in which this process definition is
   * deployed. */
  String getDeploymentId();
  
  /** the name of the resource in the deployment which contains the image 
   * file for this process */
  String getImageResourceName();

  /** description of the process definition */
  String getDescription();
  
  /** Returns whether this process definition is currently suspended. */
  boolean isSuspended();
  
}

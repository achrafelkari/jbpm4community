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
package org.jbpm.pvm.internal.client;

import org.jbpm.api.Execution;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.pvm.internal.model.OpenProcessDefinition;


/** adds factory methods for creating and beginning new process instances.
 * 
 * @author Tom Baeyens
 */
public interface ClientProcessDefinition extends OpenProcessDefinition {

  /** creates a new process instances. The returned process instance 
   * is not started yet.  This way, 
   * {@link OpenExecution#setVariable(String, Object) variables can be set} 
   * before execution is started.  Invoke {@link ClientProcessInstance#start()} 
   * to start execution of the process. */
  ClientProcessInstance createProcessInstance();
  
  /** creates a new process instances with a given key. The returned process instance 
   * is not started yet.  This way, 
   * {@link OpenExecution#setVariable(String, Object) variables can be set} 
   * before execution is started.  Invoke {@link ClientProcessInstance#start()} 
   * to start execution of the process. 
   * @param key is a user provided reference that uniquely identifies this 
   * process instance in the scope of the process name. */
  ClientProcessInstance createProcessInstance(String key);
  
  /** creates a new process instances with a given key. The returned process instance 
   * is not started yet.  This way, 
   * {@link OpenExecution#setVariable(String, Object) variables can be set} 
   * before execution is started.  Invoke {@link ClientProcessInstance#start()} 
   * to start execution of the process. 
   * @param key is a user provided reference that uniquely identifies this 
   * process instance in the scope of the process name. key is allowed to be null.*/
  ClientProcessInstance createProcessInstance(String key, Execution superProcessExecution);

  /** creates the process instance and immediately start its execution. */
  ClientProcessInstance startProcessInstance();
  
  /** creates the process instance with the given key and immediately start its 
   * execution.
   * @param key is a user provided reference that uniquely identifies this 
   * process instance in the scope of the process name. */
  ClientExecution startProcessInstance(String key);
}

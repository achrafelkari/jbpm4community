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
package org.jbpm.api.model;

import java.util.Map;
import java.util.Set;

import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;

/** execution that opens up access to the related 
 * objects in the execution and process definition 
 * model.
 * 
 * This execution exposes the execution hierarchy,
 * variable access and associated timers.
 * 
 * Open refers to the relations being accessible.  This is related 
 * to hibernate's lazy loading capabilities.  That requires an active 
 * session.  Inside process execution, there is such an active session
 * and hence the relations can be exposed.  But for the client of the 
 * service methods, it's not sure if the session is still active.  
 * That is why the relations are not exposed in the return values 
 * of service methods.  
 * 
 * @author Tom Baeyens
 */
public interface OpenExecution extends Execution {
  
  /** the current activity */
  Activity getActivity();
  
  /** update the state */
  void setState(String state);

  /** the related sub process execution. */
  OpenExecution getSubProcessInstance();
  
  // variable access //////////////////////////////////////////////////////////

  /** retrieve the value for the given key.
   * The value can be null.
   * If there is no value for the given key, the returned 
   * value will also be null.  The value for key <code>null</code>
   * will always be null as null keys are not allowed. */  
  Object getVariable(String key);

  /** updates or creates a variable for the given value.
   * Values are allowed to be null.
   * @throws JbpmException if key is null. */
  void setVariable(String key, Object value);

  /** {@link #setVariable(String, Object) sets} all given variables.
   * Existing key-value pairs for which there is no key in the provided 
   * variables will <b>not</b> be removed. 
   * @throws JbpmException is variables is not null and if null is present 
   * as a key in the provided variables map. */
  void setVariables(Map<String, ?> variables);

  /** indicates presenve of the given key. 
   * No exception will be thrown if key is null. 
   * @return true if the key is present and false if the key doesn't exist 
   * or if key is null. */
  boolean hasVariable(String key);
  
  /** remove the key-value pair for the given key from this scope.
   * No exception will be thrown when the variable is not present.
   * @return whether a variable was actually found and removed. */
  boolean removeVariable(String key);
  
  /** removes all variables in this scope */
  void removeVariables();

  /** indicates if there are keys in this scope. */ 
  boolean hasVariables();
  
  /** a non-null set that contains all the keys present in this scope.
   * Even if there are no variable keys, an empty, non-null set will 
   * be returned. */
  Set<String> getVariableKeys();
  
  /** a non-null map containing all the key-value pairs in this scope. 
   * Even if there are no variable keys, an empty, non-null map will 
   * be returned. */
  Map<String, ?> getVariables();
  
  /** create a new variable in this execution scope and determine 
   * the type automagically. */
  void createVariable(String key, Object value);

  // priority /////////////////////////////////////////////////////////////////
  
  /** setter for the priority.  The default priority is 0, which means 
   * NORMAL. Other recognized named priorities are HIGHEST (2), HIGH (1), 
   * LOW (-1) and LOWEST (-2). For the rest, the user can set any other 
   * priority integer value, but then, the UI will have to display it as 
   * an integer and not the named value.*/
  void setPriority(int priority);
  
  // execution hierarchy access ///////////////////////////////////////////////
  
  /** the main path of execution in the execution tree structure.  The 
   * process instance is the root of the execution tree.  */
  OpenProcessInstance getProcessInstance();

  /** the parent execution in the execution structure.  
   * Null will be returned in case this execution itself is the 
   * process instance. */ 
  OpenExecution getParent();

  /** the child execution for the given name or null in case such execution doesn't exist. */ 
  OpenExecution getExecution(String name);
  
  /** find the execution in the given activity or null if no such activity exists */
  OpenExecution findActiveExecutionIn(String activityName);
}

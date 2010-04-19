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

import java.util.Map;

import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.api.activity.ExternalActivityBehaviour;
import org.jbpm.api.model.OpenExecution;

/** view upon an {@link Execution path of execution} exposed to 
 * external clients.
 * 
 * @author Tom Baeyens
 */
public interface ClientExecution extends OpenExecution {
  
  // ending an execution //////////////////////////////////////////////////////
  
  /** ends this execution and all of its child executions.
   * 
   * <p>All child executions will be ended and removed.  This execution
   * will not be removed from its parent.</p>
   * 
   * <p>This method should not be called in {@link ActivityBehaviour}s.  It can be called from 
   * outside the process execution and in {@link ExternalActivityBehaviour}s. </p> */
  void end();

  /** ends this execution and all it's child executions with a user defined 
   * status. */
  void end(String state);

  // signal ///////////////////////////////////////////////////////////////////

  /** feeds a external trigger into this execution.
   * 
   * <p>Typically a signal causes the execution to proceed, but that doesn't necessarily 
   * has to be the case .  The {@link ExternalActivityBehaviour} is responsible for interpreting 
   * the signal and acting upon it.
   * </p>
   * 
   * <p>A signal can optionally be given {@link #signal(String) a signal name}, 
   * {@link #signal(Map) a map of parameters} or {@link #signal(String, Map) both}.
   * </p>
   * 
   * <p>Since it's an external trigger, this method requires that this execution is 
   * waiting for an external trigger.  So this method must be called as an external client
   * and can not be called while this execution is executing.  In an {@link ActivityBehaviour} for 
   * example you're not allowed to call the signal on the execution cause it is executing.  
   * But you are allowed to invoke this method on any other execution (at least, if that 
   * one is waiting for an external trigger).</p>
   * 
   * <p>Typically a signal will cause the execution to start executing, but that is 
   * not a must.  What happens with this signal is defined in the 
   * {@link ExternalActivityBehaviour#signal(Execution, String, Map)} of 
   * the current activity. </p>
   *  
   * @see #signal(String) */
  void signal();
  
  /** feeds a named {@link #signal() external trigger} into the execution.
   * 
   * <p>In each state, a number of things can happen.  The signal parameter specifies 
   * which of these things is happening.  It's somewhat similar to a method name in 
   * the invocation of an object.
   * </p>
   * 
   * @see #signal() See the unnamed signal for more information
   */
  void signal(String signalName);

  /** feeds {@link #signal() an external trigger} into the execution with parameters.
   * 
   * @see #signal() See the unnamed signal for more information
   */
  void signal(Map<String, ?> parameters);

  /** feeds a named {@link #signal() external trigger} into the execution with parameters.
   *
   * <p>In each state, a number of things can happen.  The signal parameter specifies 
   * which of these things is happening.  It's somewhat similar to a method name in 
   * the invocation of an object.
   * </p>
   * 
   * <p>The parameters parameter provide extra information to the signal.
   * Typically, the parameters are set as variables but 
   * the process language can overwrite that behaviour in the current activity.  
   * See {@link ExternalActivityBehaviour#signal(Execution, String, Map)} for more information. 
   * </p>
   * 
   * @see #signal() See the unnamed signal for more information
   */
  void signal(String signalName, Map<String, ?> parameters);

  /** feeds a external trigger into the given execution.
   * 
   * <p>Typically a signal causes the execution to proceed, but that doesn't necessarily 
   * has to be the case .  The {@link ExternalActivityBehaviour} is responsible for interpreting 
   * the signal and acting upon it.
   * </p>
   * 
   * <p>A signal can optionally be given {@link #signal(String) a signal name}, 
   * {@link #signal(Map) a map of parameters} or {@link #signal(String, Map) both}.
   * </p>
   * 
   * <p>Since it's an external trigger, this method requires that this execution is 
   * waiting for an external trigger.  So this method must be called as an external client
   * and can not be called while this execution is executing.  In an {@link ActivityBehaviour} for 
   * example you're not allowed to call the signal on the execution cause it is executing.  
   * But you are allowed to invoke this method on any other execution (at least, if that 
   * one is waiting for an external trigger).</p>
   * 
   * <p>Typically a signal will cause the execution to start executing, but that is 
   * not a must.  What happens with this signal is defined in the 
   * {@link ExternalActivityBehaviour#signal(Execution, String, Map)} of 
   * the current activity. </p>
   *  
   * @see #signal(String) */
  void signal(Execution execution);
  
  /** feeds a named {@link #signal() external trigger} into a given execution.
   * 
   * <p>In each state, a number of things can happen.  The signal parameter specifies 
   * which of these things is happening.  It's somewhat similar to a method name in 
   * the invocation of an object.
   * </p>
   * 
   * @see #signal() See the unnamed signal for more information
   */
  void signal(String signalName, Execution execution);

  /** feeds {@link #signal() an external trigger} into a given execution with parameters.
   * 
   * @see #signal() See the unnamed signal for more information
   */
  void signal(Map<String, ?> parameters, Execution execution);

  /** feeds a named {@link #signal() external trigger} into a given execution with parameters.
   *
   * <p>In each state, a number of things can happen.  The signal parameter specifies 
   * which of these things is happening.  It's somewhat similar to a method name in 
   * the invocation of an object.
   * </p>
   * 
   * <p>The parameters parameter provide extra information to the signal.
   * Typically, the parameters are set as variables but 
   * the process language can overwrite that behaviour in the current activity.  
   * See {@link ExternalActivityBehaviour#signal(Execution, String, Map)} for more information. 
   * </p>
   * 
   * @see #signal() See the unnamed signal for more information
   */
  void signal(String signalName, Map<String, ?> parameters, Execution execution);

  
  /** suspends this execution and all it's child executions.  Human tasks 
   * of a suspended execution shouldn't show up in people's task list and 
   * timers of suspended executions shouldn't fire. 
   * @throws JbpmException if this execution is already suspended. */   
  void suspend();

  /** resumes an execution.  Inverse of {@link #suspend()}. 
   * @throws JbpmException if this execution is not suspended. */ 
  void resume();
}

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
package org.jbpm.api.activity;

import java.util.Map;

import org.jbpm.api.Execution;
import org.jbpm.api.ExecutionService;


/** extends {@link ActivityBehaviour} for handling external triggers after a wait state.
 * 
 * @author Tom Baeyens
 */
public interface ExternalActivityBehaviour extends ActivityBehaviour {
  
  /** handles an external signal.  
   * 
   * <p>An external signal that comes into an execution 
   * through one of the {@link ExecutionService#signalExecutionById(String)} methods. 
   * It will be delegated to the activity in which the execution is positioned when it 
   * receives the external trigger.
   * </p>  
   * 
   * <p>The signal method implements how the activity will react on that signal.  
   * For example, the outgoing transition could be taken that corresponds with the 
   * given signal.   
   * </p>
   * 
   * @param execution the {@link Execution} for which the signal is given
   * 
   * @param signalName is an abstract text that can be associated with a signal.  this 
   *   corresponds to e.g. a method name in a java class interface.  The implementation 
   *   can decide e.g. to use the signal to identify the outgoing transition.
   *   
   * @param parameters is extra information that can be provided with a signal.
   *   In this way, it is somewhat similar to the parameters that can be fed into a method 
   *   invocation through reflection.
   *   
   * @throws Exception to indicate any kind of failure.  Note that exceptions are 
   *   considered non recoverable.  After an Exception, the execution should not be 
   *   used any more and if this is during a transaction, the transaction should be 
   *   rolled back. */
  void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception;
}

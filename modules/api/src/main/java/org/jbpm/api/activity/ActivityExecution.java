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

import org.jbpm.api.Execution;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.JbpmException;
import org.jbpm.api.model.OpenExecution;


/** view upon an {@link Execution} exposed to 
 * {@link ActivityBehaviour} implementations.
 *  
 * @author Tom Baeyens
 */
public interface ActivityExecution extends OpenExecution {
  
  /** represents the current position in the process by indicating the 
   * name of the current activity. */
  String getActivityName();

  
  // wait state behaviour /////////////////////////////////////////////////////
  
  /** makes this execution wait in the current activity until an external trigger is given 
   * with one of the {@link ExecutionService#signalExecutionById(String) signal} methods. */
  void waitForSignal();

  // taking a transition //////////////////////////////////////////////////////
  
  /** takes the default transition.  
   * 
   * <p>This method can only be called from inside
   * {@link ExternalActivityBehaviour} implementations.
   * </p>
   * 
   * @throws JbpmException in case there is no default transition in the current activity 
   * or in case this method is called from inside an {@link ActivityBehaviour} */
  void takeDefaultTransition();

  /** takes the outgoing transition with the given name. 
   * 
   * <p>This method can only be called from inside 
   * {@link ExternalActivityBehaviour} implementations.</p>
   * 
   * <p>Transitions will be looked up recursively starting from the 
   * {@link #getActivityName() current activity} and then up the activity-parent-hierarchy</p>
   * 
   * @param transitionName is the name of the transition to take.  A null value will 
   * match the first unnamed transition.
   *   
   * @throws JbpmException in case no such transition is found in {@link #getActivityName() the current activity} 
   * or in case this method is called from inside an {@link ActivityBehaviour}.*/
  void take(String transitionName);

  // execute a child activity /////////////////////////////////////////////////////
  
  /** executes the given nested activity. 
   * 
   * <p>The activityName is looked up in the current activity's nested activities.</p>
   * 
   * <p>This method can only be called from inside {@link ExternalActivityBehaviour} 
   * implementations.</p> */
  void execute(String activityName);
  
  // ending an execution //////////////////////////////////////////////////////
  
  /** ends this execution and all of its child executions.
   * 
   * <p>The execution will be removed from it's parent.  Potentially this can cause 
   * a parent execution to start executing in case this is the last concurrent 
   * execution for which the parent is waiting.</p> 
   */
  void end();

  /** ends this execution and all it's child executions with a user defined 
   * status.  
   * 
   * <p>It is not recommended to use any of 
   * {@link Execution the defined statuses in Execution} as that may case unpredictable 
   * side effects.</p>
   *  
   * <p>The execution will be removed from it's parent.</p> */
  void end(String state);
  
  // extra state information methods //////////////////////////////////////////
  
  /** setter for the priority.  The default priority is 0, which means 
   * NORMAL. Other recognized named priorities are HIGHEST (2), HIGH (1), 
   * LOW (-1) and LOWEST (-2). For the rest, the user can set any other 
   * priority integer value, but then, the UI will have to display it as 
   * an integer and not the named value.*/
  void setPriority(int priority);

}

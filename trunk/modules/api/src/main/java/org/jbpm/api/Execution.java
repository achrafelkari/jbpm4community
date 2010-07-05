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
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jbpm.api.model.OpenExecution;

/** a runtime path of execution.
 * 
 * <h3 id="state">State of an execution</h3>
 * 
 * <p>The state of an execution is either active or locked.  An active execution is either 
 * executing or waiting for an external trigger.  If an execution is not in {@link #STATE_ACTIVE_ROOT}
 * or {@link #STATE_ACTIVE_CONCURRENT} then it is locked.  A locked execution is read only.  
 * </p>
 * 
 * <p>When a new execution is created, it is in {@link #STATE_ACTIVE_ROOT}.  
 * {@link #STATE_ACTIVE_ROOT Some STATE_* constants} are provided that represent the 
 * most commonly used locked states.  But the state '...' in the picture indicates 
 * that any string can be provided as the state in the lock method.
 * </p>
 * 
 * <p>If an execution is locked, methods that change the execution will throw
 * a {@link JbpmException} and the message will reference the actual locking state.
 * {@link OpenExecution#setVariable(String, Object) updating variables}, 
 * {@link OpenExecution#setPriority(int) updating priority} are not considered to change an 
 * execution.   
 * </p>    
 * 
 * <p>Make sure that comparisons between {@link #getState()} and the 
 * {@link #STATE_ACTIVE_ROOT STATE_* constants} are  
 * done with .equals and not with '==' because if executions are 
 * loaded from persistent storage, a new string is created instead 
 * of the constants.
 * </p>
 * 
 * <h3>Comments</h3>
 * 
 * @author Tom Baeyens
 */
public interface Execution extends Serializable {

  /** between creation of a new process instance and the start of that 
   * process instance.  Typically this is only a very short period that is
   * not observable thourgh the service interfaces.   */
  String STATE_CREATED = "created";
  
  /** single (non-concurrent) path of execution that is an active indicator 
   * of the current position in the diagram.  jBPM can be executing automatic 
   * activities or some external entity might be responsible for continuing the 
   * execution (wait state for jBPM).  An active execution is always 
   * a leaf in the execution tree.  
   * This is the normal state of an execution and the initial state 
   * when creating a new execution. Make sure that comparisons are 
   * done with .equals and not with '==' because if executions are 
   * loaded from persistent storage, a new string is created instead 
   * of the constants. */
  String STATE_ACTIVE_ROOT = "active-root";

  /** concurrent path of execution that is an active indicator 
   * of the current position in the diagram.  The parent of an active 
   * concurrent execution is always an inactive concurrent root.  jBPM can 
   * be executing automatic 
   * activities or some external entity might be responsible for continuing the 
   * execution (wait state for jBPM).
   * Make sure that comparisons are 
   * done with .equals and not with '==' because if executions are 
   * loaded from persistent storage, a new string is created instead 
   * of the constants. */
  String STATE_ACTIVE_CONCURRENT = "active-concurrent";

  /** parent of concurrent child executions.  
   * When an execution has concurrent child executions, it implies that 
   * this execution can't be active.  For example, at a fork, the parent
   * execution can wait inactively in the fork being till all the
   * child executions are joined.  Only leaves of the
   * execution tree can be active. Make sure that comparisons are 
   * done with .equals and not with '==' because if executions are 
   * loaded from persistent storage, a new string is created instead 
   * of the constants. */
  String STATE_INACTIVE_CONCURRENT_ROOT = "inactive-concurrent-root";
  
  /** parent of a scoped execution.  This execution is inactive, 
   * but points to the parent scope like e.g. a group.
   * This execution has exactly 1 child execution.  That indicates 
   * the state inside of the scope.
   * Make sure that comparisons are 
   * done with .equals and not with '==' because if executions are 
   * loaded from persistent storage, a new string is created instead 
   * of the constants. */
  String STATE_INACTIVE_SCOPE = "inactive-scope";

  /** concurrent execution that is inactively waiting in a join  
   * until other concurrent executions arrive.
   * Make sure that comparisons are 
   * done with .equals and not with '==' because if executions are 
   * loaded from persistent storage, a new string is created instead 
   * of the constants. */
  String STATE_INACTIVE_JOIN = "inactive-join";

  /** indicates that this execution is temporary suspended.  Human tasks of 
   * a suspended execution
   * shouldn't show up in people's task list and timers of suspended
   * executions shouldn't fire and the execution is locked. Make sure that comparisons are 
   * done with .equals and not with '==' because if executions are 
   * loaded from persistent storage, a new string is created instead 
   * of the constants. */
  String STATE_SUSPENDED = "suspended";
  
  /** indicates that this execution is doing an asynchronous continuation. */
  String STATE_ASYNC = "async";

  /** this execution has ended. Make sure that comparisons are 
   * done with .equals and not with '==' because if executions are 
   * loaded from persistent storage, a new string is created instead 
   * of the constants. */
  String STATE_ENDED = "ended";

  /** the externally given name or id of this execution. The id of a main 
   * path of execution is null.   Can be used to differentiate concurrent 
   * paths of execution e.g. the shipping and billing paths.  */
  String getName();

  /** the optional user provided business key that is unique within one 
   * process definition.  This could be for instance the order number.  
   * It's a user defined identifier for one execution within the scope of 
   * a single process definition. */
  String getKey();
  
  /** a globally unique identifier for this execution that is used as a reference in the service methods. */
  String getId();
  
  /** the <a href="#state">state</a> of this execution.  */
  String getState();

  /** 
   * Returns whether this execution is a process instance.
   * Note that we cannot use 'isProcessInstance', since this
   * would clash with 'getProcessInstance()' when using expressions.
   * (see JBPM-2494)
   * */
  boolean getIsProcessInstance();

  /** is this execution ended */
  boolean isEnded();

  /** is this execution suspended ? */
  boolean isSuspended();

  /** indicates low priorities with negative values and high priorities 
   * with positive values.  The default priority is 0, which means 
   * NORMAL. Other recognized named priorities are HIGHEST (2), HIGH (1), 
   * LOW (-1) and LOWEST (-2). For the rest, the user can set any other 
   * priority integer value, but then, the UI will have to display it as 
   * an integer and not the named value.*/
  int getPriority();
  
  // execution hierarchy access ///////////////////////////////////////////////
  
  /** the main path of execution in the <a href="package-summary.html#basicexecutionstructure">execution 
   * structure</a>.  Null will be returned in case this execution itself is the 
   * main execution path.  */
  Execution getProcessInstance();
  
  /**
   * the sub path of execution in the <a href="package-summary.html#basicexecutionstructure">execution 
   * structure</a>. Null will be returned in case there is no sub process execution. 
   */
  Execution getSubProcessInstance();

  /** the parent execution in the <a href="package-summary.html#basicexecutionstructure">execution 
   * structure</a>.  Null will be returned in case this execution itself is the 
   * main execution path. */ 
  Execution getParent();

  /** the child executions in the <a href="package-summary.html#basicexecutionstructure">execution 
   * structure</a>.  Can be null and can be an empty collection. */ 
  Collection<? extends Execution> getExecutions();
  
  /** maps child execution names to execution objects.  In case multiple executions 
   * have the same name, the first one is taken.  Can be null or can be an empty 
   * map.  The first execution without a name is also included with null as the key.
   */ 
  Map<String, ? extends Execution> getExecutionsMap();
  
  /** the child execution for the given name or null in case such execution doesn't exist. */ 
  Execution getExecution(String name);
  
  /** indicates if this execution has a child execution with the given executionName */
  boolean hasExecution(String executionName);
  
  /** search for an execution that is active and in the given activityName.
   * Returns null in case there is no such execution.
   * @see #findActiveActivityNames() */
  Execution findActiveExecutionIn(String activityName);
  
  /** get the set of all activities that are active. 
   * Returns an empty set in case there are no activities active. 
   * @see #findActiveExecutionIn(String) */
  Set<String> findActiveActivityNames();
  
  /** is the given activity active.
   * This execution and its child executions are searched.  If this is a 
   * process instance, this means that the whole process instance is searched. */
  boolean isActive(String activityName);
  
  /** id of the process definition used for this execution */
  String getProcessDefinitionId();
}

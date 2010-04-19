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
package org.jbpm.api.history;

import java.util.Date;

import org.jbpm.api.Execution;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessInstance;

/** one particular instance of a process definition.
 * 
 * Every {@link ProcessInstance} will have one HistoryProcessInstance
 * associated.  The difference is that the ProcessInstance will be 
 * deleted when it is ended, whereas the history information will remain 
 * in the DB.  That keeps the runtime DB healthy and performing well.
 *  
 * @author Tom Baeyens
 */
public interface HistoryProcessInstance {
  
  /** when the full process instance has come to an end */
  String STATE_ENDED = "ended";

  /** when the full process instance is still active */
  String STATE_ACTIVE = "active";

  /** the process instance id (== the root execution id) */
  String getProcessInstanceId();

  /** reference to the process definition */
  String getProcessDefinitionId();

  /** unique user provided business key 
   * (could be null if no such key is provided in 
   * {@link ExecutionService#startProcessInstanceById(String, java.util.Map, String)}) */
  String getKey();

  /** {@link #STATE_ACTIVE} or {@link #STATE_ENDED} (this more coarse grained 
   * state then {@link Execution#getState()}) */ 
  String getState();

  /** when the process instance started */
  Date getStartTime();

  /** when the process instance ended (only not null if the 
   * process instance already ended) */
  Date getEndTime();

  /** duration of the process instance in milliseconds or null 
   * if the process instance has not yet ended */
  Long getDuration();
  
  /** Returns the name of the end state that was reached when the process was ended.
   */
  String getEndActivityName();
}
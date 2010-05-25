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

import org.jbpm.api.task.Task;

/** history record for a task instance.  
 * 
 * In contrast to the {@link Task}, this entity will remain 
 * after the task has been completed for history purposes.
 *  
 * @author Tom Baeyens
 */
public interface HistoryTask {

  String STATE_COMPLETED = "completed";
  String STATE_OBSOLETE = "obsolete";

  /** the unique id for this task that is used as a reference in the service methods */
  String getId();

  /** the execution that was related to this activity occurrence */
  String getExecutionId();

  /** time when the activity was entered */
  Date getCreateTime();

  /** might be null in case the activity is still active */
  Date getEndTime();

  /** duration in milliseconds */
  long getDuration();

  /** history task state */  
  String getState();

  /** userId of the person that is responsible for this task */  
  String getAssignee();

  /** the outcome of this task */
  String getOutcome();
}
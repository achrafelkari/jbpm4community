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
package org.jbpm.api.job;

import java.util.Date;

import org.jbpm.api.Execution;

/** base class for timers and messages.
 * 
 * @author Tom Baeyens
 */
public interface Job {

  /** unique id for this job that is used as a reference in the service methods */
  String getId();

  /** job executor identification that has acquired this job and is going to execute it */
  String getLockOwner();

  /** in case this is a timer, it is the time that the timer should fire, in case this 
   * is a message, it is null. */
  Date getDueDate();

  /** in case this is a timer, it is the time that the timer should fire, in case this 
   * is a message, it is null.
   * @deprecated call {@link #getDueDate()} instead */
  @Deprecated
  Date getDuedate();

  /** exception that occurred during the last execution of this job.  The transaction 
   * of the job execution is rolled back.  A synchronization is used to create 
   * a separate transaction to update the exception and decrement the retries. */
  String getException();

  /** number of retries left.  This is only decremented when an exception occurs during job 
   * execution.  The transaction of the job execution is rolled back.  A synchronization is used to create 
   * a separate transaction to update the exception and decrement the retries. */
  int getRetries();

  /** indicates if this job should be executed separate from any other job 
   * in the same process instance */
  boolean isExclusive();

  /** the related execution */
  Execution getExecution();

  /** the related process instance */
  Execution getProcessInstance();

  Date getLockExpirationTime();

}
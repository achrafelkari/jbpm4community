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
package org.jbpm.pvm.internal.jobexecutor;

import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.jbpm.internal.log.Log;

/** listener that can be registered as a listener to the transaction
 * to notify the job executor of added jobs so that the dispatcher
 * thread can wake up.
 *  
 * @author Tom Baeyens
 */
public class JobAddedNotification implements Synchronization {
  
  private static final Log log = Log.getLog(JobAddedNotification.class.getName());
  
  JobExecutor jobExecutor;
  
  public JobAddedNotification(JobExecutor jobExecutor) {
    this.jobExecutor = jobExecutor;
  }

  public void afterCompletion(int status) {
    if (status == Status.STATUS_COMMITTED) {
      log.trace("notifying job executor of added message");
      jobExecutor.jobWasAdded();
    }
  }

  public void beforeCompletion() {
  }
  
  public String toString() {
    return "job-added-notification";
  }
}

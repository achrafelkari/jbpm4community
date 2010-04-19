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
package org.jbpm.pvm.internal.session;

import java.util.List;

import org.jbpm.api.Execution;
import org.jbpm.api.job.Timer;

/**
 * @author Tom Baeyens, Pascal Verdage
 */
public interface TimerSession {

  /** Schedule the execution of a timer.
   * 
   * @param timerImpl
   *          the timer to be executed.
   * @throws IllegalArgumentException
   *           if the timer is null or if its activity is null or if its duedate
   *           is null or if its duedate is negative or if its duedate is past
   *           or if its duedate is equals to Long.MAX_VALUE
   */
  void schedule(Timer timer);

  /** Cancels a timer. <br />
   * If a transaction is in progress, the cancellation will be effective at the
   * end of the transaction. If the timer has been created in the same
   * transaction or if there is no transaction, the cancellation is immediately
   * effective. <br />
   * If the timer is executing when the cancellation becomes effective, the
   * execution in progress will not be stopped.
   * 
   * @param timerImpl
   *          the timer to be cancelled.
   */
  void cancel(Timer timer);

  /** retrieve all the outstanding timers associated for the given execution */ 
  List<Timer> findTimersByExecution(Execution execution);
}

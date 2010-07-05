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
package org.jbpm.pvm.internal.job;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.job.Timer;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.cal.Duration;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.id.DbidGenerator;
import org.jbpm.pvm.internal.jobexecutor.JobAddedNotification;
import org.jbpm.pvm.internal.jobexecutor.JobExecutor;
import org.jbpm.pvm.internal.model.ObservableElement;
import org.jbpm.pvm.internal.session.TimerSession;
import org.jbpm.pvm.internal.tx.Transaction;
import org.jbpm.pvm.internal.util.Clock;

/**
 * a runtime timer instance.
 * 
 * @author Tom Baeyens
 * @author Pascal Verdage
 * @author Alejandro Guizar
 * @author Ronald Van Kuijk
 * @author Maciej Swiderski
 */
public class TimerImpl extends JobImpl implements Timer {

  private static final long serialVersionUID = 1L;
  private static final Log log = Log.getLog(TimerImpl.class.getName());

  private final static String dateFormat = "yyyy-MM-dd HH:mm:ss";

  protected String signalName;
  protected String eventName;
  protected String repeat;

  public static final String EVENT_TIMER = "timer";

  public TimerImpl() {
  }

  public void schedule() {
    this.dbid = DbidGenerator.getDbidGenerator().getNextId();
    TimerSession timerSession = EnvironmentImpl.getFromCurrent(TimerSession.class);
    timerSession.schedule(this);
  }

  public void setDueDateDescription(String dueDateDescription) {
    if (dueDateDescription != null) {
      dueDate = Duration.calculateDueDate(dueDateDescription);
    }
  }

  public Boolean execute(Environment environment) throws Exception {
    if (log.isDebugEnabled())
      log.debug("executing " + this);

    if (environment == null) {
      throw new JbpmException("environment is null");
    }

    if (signalName != null) {
      if (log.isDebugEnabled()) {
        log.debug("feeding timer signal " + signalName + " into " + execution);
      }

      // feed expiration signal
      execution.signal(signalName);
    }

    if (eventName != null) {
      ObservableElement eventSource = execution.getActivity();
      if (log.isDebugEnabled())
        log.debug("firing event " + eventName + " into " + eventSource);
      execution.fire(eventName, eventSource);
    }

    // if there is no repeat on this timer
    if (repeat == null) {
      // have the timer deleted
      return true;
    }

    // suppose that it took the timer runner thread a very long time to execute the timers
    // then the repeat action duedate could already have passed
    do {
      setDueDateDescription(repeat);
    } while (dueDate.getTime() <= Clock.getTime().getTime());

    if (log.isDebugEnabled())
      log.debug("rescheduled " + this + " for " + formatDueDate(dueDate));

    // release the lock on the timer
    release();

    // notify the jobExecutor at the end of the transaction
    JobExecutor jobExecutor = environment.get(JobExecutor.class);
    if (jobExecutor != null) {
      Transaction transaction = environment.get(Transaction.class);
      if (transaction == null) {
        throw new JbpmException("no transaction in environment");
      }
      JobAddedNotification jobNotificator = new JobAddedNotification(jobExecutor);
      transaction.registerSynchronization(jobNotificator);
    }
    // do not delete this timer
    return false;
  }

  public void validate() {
    if (getExecution() == null) {
      throw new JbpmException("timer has no execution specified");
    }
    if (getSignalName() == null && getEventName() == null) {
      throw new JbpmException("timer has no signalName or eventName specified");
    }
    if (getDueDate() == null) {
      throw new JbpmException("timer scheduled at null date");
    }
  }

  @Override
  public String toString() {
    StringBuilder text = new StringBuilder();
    text.append("timer[");
    text.append(dbid);
    if (dueDate != null) {
      text.append('|').append(formatDueDate(dueDate));
    }
    if (signalName != null) {
      text.append('|').append(signalName);
    }
    if (eventName != null) {
      text.append('|');
      text.append(eventName);
    }
    text.append("]");
    return text.toString();
  }

  public static String formatDueDate(Date date) {
    return new SimpleDateFormat(dateFormat).format(date);
  }

  public String getSignalName() {
    return signalName;
  }
  public void setSignalName(String signalName) {
    this.signalName = signalName;
  }
  public String getEventName() {
    return eventName;
  }
  public void setEventName(String eventName) {
    this.eventName = eventName;
  }
  public String getRepeat() {
    return repeat;
  }
  public void setRepeat(String repeat) {
    this.repeat = repeat;
  }
}

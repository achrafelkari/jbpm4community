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
package org.jbpm.pvm.internal.history;

import java.io.Serializable;

import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;

/** base class for process logs.  Process logs are dispatched to the
 * {@link HistorySession} that is configured in the environment.
 *
 * <p>ProcessLogs that are send to a LogSession is the probe mechanism
 * provided to listen into the process execution progress and details.
 * It is the mechanism to collect process history and process statistics.
 * </p>
 *
 * @author Tom Baeyens
 * @author Huisheng Xu
 */
public abstract class HistoryEvent implements Serializable {

  private static final long serialVersionUID = 1L;

  protected ExecutionImpl execution;

  public ExecutionImpl getExecution() {
    return execution;
  }

  public void setExecution(ExecutionImpl execution) {
    this.execution = execution;
  }

  public abstract void process();

  public static void fire(HistoryEvent historyEvent) {
    fire(historyEvent, null);
  }

  public static void fire(HistoryEvent historyEvent, ExecutionImpl execution) {
    EnvironmentImpl environment = EnvironmentImpl.getCurrent();
    if (environment!=null) {

      HistorySession historySession = environment.get(HistorySession.class);
      if (historySession!=null) {
        historyEvent.setExecution(execution);
        historySession.process(historyEvent);
      }
    }
  }
}

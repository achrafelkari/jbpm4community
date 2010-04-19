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
package org.jbpm.pvm.internal.history.events;

import org.hibernate.Session;
import org.jbpm.api.history.HistoryTask;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.history.HistoryEvent;
import org.jbpm.pvm.internal.history.model.HistoryTaskImpl;
import org.jbpm.pvm.internal.history.model.HistoryTaskInstanceImpl;
import org.jbpm.pvm.internal.util.Clock;



/**
 * @author Tom Baeyens
 */
public class TaskComplete extends HistoryEvent {

  private static final long serialVersionUID = 1L;
  
  protected String outcome;

  public TaskComplete(String outcome) {
    this.outcome = outcome;
  }

  public void process() {
    Session session = EnvironmentImpl.getFromCurrent(Session.class);
    Long historyActivityInstanceDbId = execution.getHistoryActivityInstanceDbid();
    HistoryTaskInstanceImpl historyTaskInstance = (HistoryTaskInstanceImpl) 
        session.load(HistoryTaskInstanceImpl.class, historyActivityInstanceDbId); 
    historyTaskInstance.setEndTime(Clock.getTime());
    historyTaskInstance.setTransitionName(outcome);

    HistoryTaskImpl historyTask = historyTaskInstance.getHistoryTask();
    historyTask.setOutcome(outcome);
    historyTask.setEndTime(Clock.getTime());
    historyTask.setState(HistoryTask.STATE_COMPLETED);

    session.update(historyTaskInstance);
  }

  public String getOutcome() {
    return outcome;
  }
  
}

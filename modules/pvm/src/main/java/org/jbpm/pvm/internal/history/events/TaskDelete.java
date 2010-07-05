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

import org.jbpm.api.JbpmException;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.history.model.HistoryActivityInstanceImpl;
import org.jbpm.pvm.internal.history.model.HistoryTaskImpl;
import org.jbpm.pvm.internal.history.model.HistoryTaskInstanceImpl;
import org.jbpm.pvm.internal.session.DbSession;
import org.jbpm.pvm.internal.task.TaskImpl;
import org.jbpm.pvm.internal.util.Clock;


/**
 * @author Tom Baeyens
 */
public class TaskDelete extends ActivityEnd {

  private static final long serialVersionUID = 1L;

  protected TaskImpl task;
  protected String reason;
  
  public TaskDelete(TaskImpl task, String reason) {
    if (task.getExecution()!=null) {
      throw new JbpmException("tasks related to an execution must be completed. they cannot just be deleted");
    }
      
    this.task = task;
    this.reason = reason;
  }

  protected void updateHistoryActivityInstance(HistoryActivityInstanceImpl historyActivityInstance) {
    super.updateHistoryActivityInstance(historyActivityInstance);

    DbSession dbSession = EnvironmentImpl.getFromCurrent(DbSession.class);
    HistoryTaskImpl historyTask = dbSession.get(HistoryTaskImpl.class, task.getDbid());
    historyTask.setState(reason);
    historyTask.setEndTime(Clock.getTime());
  }

  protected Class<? extends HistoryActivityInstanceImpl> getHistoryActivityInstanceClass() {
    return HistoryTaskInstanceImpl.class;
  }
  
  public TaskImpl getTask() {
    return task;
  }

  public String getReason() {
    return reason;
  }
  
}

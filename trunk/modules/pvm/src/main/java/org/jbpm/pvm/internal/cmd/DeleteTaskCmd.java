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
package org.jbpm.pvm.internal.cmd;

import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.history.model.HistoryTaskImpl;
import org.jbpm.pvm.internal.session.DbSession;
import org.jbpm.pvm.internal.task.TaskImpl;

/**
 * @author Tom Baeyens
 * @author Alejandro Guizar
 */
public class DeleteTaskCmd extends AbstractCommand<Void> {

  private static final long serialVersionUID = 1L;

  protected long taskDbid;
  protected String reason;
  protected boolean deleteHistory = false;

  public DeleteTaskCmd(String taskId) {
    this.taskDbid = Long.parseLong(taskId);
  }

  public DeleteTaskCmd(String taskId, String reason) {
    this.taskDbid = Long.parseLong(taskId);
    this.reason = reason;
  }

  public DeleteTaskCmd(String taskId, boolean deleteHistory) {
    this.taskDbid = Long.parseLong(taskId);
    this.deleteHistory = deleteHistory;
  }

  public Void execute(Environment environment) throws Exception {
    DbSession dbSession = environment.get(DbSession.class);
    TaskImpl task = dbSession.get(TaskImpl.class, taskDbid);
    if (task!=null) {
      task.delete(reason);
      dbSession.delete(task);
      if (deleteHistory) {
        HistoryTaskImpl historyTask = dbSession.get(HistoryTaskImpl.class, taskDbid);
        if (historyTask!=null) {
          dbSession.delete(historyTask);
        }
      }
    } else {
      throw new JbpmException("task "+taskDbid+" doesn't exist");
    }
    return null;
  }
}

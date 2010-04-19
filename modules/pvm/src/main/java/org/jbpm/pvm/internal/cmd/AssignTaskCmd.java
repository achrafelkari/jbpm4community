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
import org.jbpm.pvm.internal.history.HistoryEvent;
import org.jbpm.pvm.internal.history.events.TaskAssign;
import org.jbpm.pvm.internal.session.DbSession;
import org.jbpm.pvm.internal.task.TaskImpl;


/**
 * @author Tom Baeyens
 */
public class AssignTaskCmd extends AbstractCommand<Void> {

  private static final long serialVersionUID = 1L;
  
  protected String taskId;
  protected String userId;
  protected boolean take;

  public AssignTaskCmd(String taskId, String userId) {
    this.taskId = taskId;
    this.userId = userId;
  }

  public AssignTaskCmd(String taskId, String userId, boolean take) {
    this.taskId = taskId;
    this.userId = userId;
    this.take = take;
  }

  public Void execute(Environment environment) {
    DbSession dbSession = environment.get(DbSession.class);
    TaskImpl task = dbSession.get(TaskImpl.class, Long.parseLong(taskId));
    if (task==null) {
      throw new JbpmException("task "+taskId+" does not exist");
    }
    if (take) {
      task.take(userId);
    } else {
      task.setAssignee(userId, true);
    }
    
    HistoryEvent.fire(new TaskAssign(task, userId));

    return null;
  }
}

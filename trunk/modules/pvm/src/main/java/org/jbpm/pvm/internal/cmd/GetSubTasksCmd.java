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

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import org.jbpm.api.cmd.Environment;
import org.jbpm.api.task.Task;
import org.jbpm.pvm.internal.task.TaskImpl;
import org.jbpm.pvm.internal.util.CollectionUtil;

/**
 * @author Tom Baeyens
 */
public class GetSubTasksCmd extends AbstractCommand<List<Task>> {
  
  private static final long serialVersionUID = 1L;

  String parentTaskId;
  
  public GetSubTasksCmd(String parentTaskId) {
    this.parentTaskId = parentTaskId;
  }

  public List<Task> execute(Environment environment) throws Exception {
    List<?> tasks = environment.get(Session.class).createCriteria(TaskImpl.class)
      .createAlias("superTask", "super")
      .add(Restrictions.eq("super.dbid", Long.parseLong(parentTaskId)))
      .list();
    return CollectionUtil.checkList(tasks, Task.class);
  }
}

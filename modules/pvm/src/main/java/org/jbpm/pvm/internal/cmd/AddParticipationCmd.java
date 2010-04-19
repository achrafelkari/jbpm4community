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

import org.hibernate.Session;
import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.task.TaskImpl;

/**
 * @author Tom Baeyens
 */
public class AddParticipationCmd extends AbstractCommand<Object> {
  
  private static final long serialVersionUID = 1L;

  protected String taskId;
  protected String swimlaneId;
  protected String userId;
  protected String groupId;
  protected String type;
  
  public AddParticipationCmd(String taskId, String swimlaneId, String userId, String groupId, String type) {
    this.taskId = taskId;
    this.swimlaneId = swimlaneId;
    this.userId = userId;
    this.groupId = groupId;
    this.type = type;
  }

  public Object execute(Environment environment) throws Exception {
    Session session = environment.get(Session.class);
    
    if (taskId!=null) {
      TaskImpl task = (TaskImpl) session.get(TaskImpl.class, Long.parseLong(taskId));
      if (task==null) {
        throw new JbpmException("task "+taskId+" was not found");
      }

      task.addParticipation(userId, groupId, type);
    }

    /*
    if (swimlaneDbid!=null) {
      SwimlaneImpl swimlane = (TaskImpl) session.get(SwimlaneImpl.class, swimlaneDbid);
      if (swimlane==null) {
        throw new JbpmException("swimlane "+swimlaneDbid+" was not found");
      }

      swimlane.addRole(identityType, identityId, roleName);
    }
    */

    return null;
  }
}

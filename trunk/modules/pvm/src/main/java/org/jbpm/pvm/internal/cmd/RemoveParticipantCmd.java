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

import java.util.HashSet;
import java.util.Set;

import org.hibernate.Session;
import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.task.ParticipationImpl;
import org.jbpm.pvm.internal.task.TaskImpl;


/**
 * @author Tom Baeyens
 */
public class RemoveParticipantCmd extends AbstractCommand<Object> {

  private static final long serialVersionUID = 1L;
  
  protected Long taskDbid;
  protected Long swimlaneId;
  protected String userId;
  protected String groupId;
  protected String participationType;
  
  public RemoveParticipantCmd(String taskId, String swimlaneId, String userId, String groupId, String participationType) {
    this.swimlaneId = (swimlaneId!=null ? Long.parseLong(swimlaneId) : null );
    this.taskDbid = (taskId!=null ? Long.parseLong(taskId) : null );
    this.userId = userId;
    this.groupId = groupId;
    this.participationType = participationType;
  }

  public Object execute(Environment environment) throws Exception {
    Session session = environment.get(Session.class);

    if (taskDbid!=null) {
      TaskImpl task = (TaskImpl) session.get(TaskImpl.class, taskDbid);
      if (task==null) {
        throw new JbpmException("task "+taskDbid+" was not found");
      }

      Set<ParticipationImpl> participations = new HashSet<ParticipationImpl>(task.getParticipations());
      for (ParticipationImpl participation : participations) {
        boolean userMatch = userId!=null ? userId.equals(participation.getUserId()) : false;
        boolean groupMatch = groupId!=null ? groupId.equals(participation.getGroupId()) : false;
        if ( ( userMatch || groupMatch ) 
             && participation.getType().equals(participationType)
           ) {
          task.removeParticipant(participation);
        }
      }
    }

    return null;
  }
}

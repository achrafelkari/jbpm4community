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

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.task.Participation;
import org.jbpm.pvm.internal.task.ParticipationImpl;
import org.jbpm.pvm.internal.util.CollectionUtil;

/**
 * @author Tom Baeyens
 */
public class GetParticipantsCmd extends AbstractCommand<List<Participation>> {
  
  private static final long serialVersionUID = 1L;

  protected String taskId;
  protected String swimlaneId;
  
  public GetParticipantsCmd(String taskId, String swimlaneId) {
    if (taskId==null) {
      throw new JbpmException("taskId is null");
    }
    this.taskId = taskId;
    this.swimlaneId = swimlaneId;
  }

  public List<Participation> execute(Environment environment) throws Exception {
    Criteria criteria = environment.get(Session.class).createCriteria(ParticipationImpl.class);
    
    if (taskId!=null) {
      criteria.add(Restrictions.eq("task.dbid", Long.parseLong(taskId)));
    } else if (swimlaneId!=null) {
      criteria.add(Restrictions.eq("swimlane.dbid", Long.parseLong(swimlaneId)));
    } else {
      throw new JbpmException("no task nor swimlane specified");
    }

    List<?> participations = criteria.list();
    return CollectionUtil.checkList(participations, Participation.class);
  }
}

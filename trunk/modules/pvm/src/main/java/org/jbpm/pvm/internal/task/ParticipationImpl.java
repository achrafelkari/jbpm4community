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
package org.jbpm.pvm.internal.task;

import java.io.Serializable;

import org.jbpm.api.task.Participation;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.id.DbidGenerator;


/** the relation between a task and a person.
 * @author Tom Baeyens
 */
public class ParticipationImpl implements Serializable, Participation
{

  private static final long serialVersionUID = 1L;

  protected long dbid;
  protected int dbversion;
  
  protected String userId;
  protected String groupId; 
  protected String type;
  protected TaskImpl task;
  protected SwimlaneImpl swimlane;
  
  public ParticipationImpl() {
  }
  
  public ParticipationImpl(String userId, String groupId, String type) {
    this.dbid = EnvironmentImpl.getFromCurrent(DbidGenerator.class).getNextId();
    this.userId = userId;
    this.groupId = groupId;
    this.type = type;
  }

  // customized getters and setters ///////////////////////////////////////////
  
  public String getId() {
    return Long.toString(dbid);
  }

  // getters and setters //////////////////////////////////////////////////////

  public TaskImpl getTask() {
    return task;
  }
  public void setTask(TaskImpl task) {
    this.task = task;
  }
  public long getDbid() {
    return dbid;
  }
  public SwimlaneImpl getSwimlane() {
    return swimlane;
  }
  public void setSwimlane(SwimlaneImpl swimlane) {
    this.swimlane = swimlane;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public String getUserId() {
    return userId;
  }
  public void setUserId(String userId) {
    this.userId = userId;
  }
  public String getGroupId() {
    return groupId;
  }
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }
}

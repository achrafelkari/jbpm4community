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
import java.util.HashSet;
import java.util.Set;

import org.jbpm.api.JbpmException;
import org.jbpm.api.task.Assignable;
import org.jbpm.api.task.Participation;
import org.jbpm.api.task.Swimlane;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.util.EqualsUtil;

/**
 * runtime process role for a specific process instance.
 */
public class SwimlaneImpl implements Serializable, Swimlane, Assignable {

  private static final long serialVersionUID = 1L;

  protected long dbid;
  protected int dbversion;
  protected String name = null;
  protected String assignee = null;
  protected ExecutionImpl execution = null;
  protected Set<ParticipationImpl> participations = new HashSet<ParticipationImpl>();
  protected SwimlaneDefinitionImpl swimlaneDefinition = null;
  
  // cloning //////////////////////////////////////////////////////////////////
  
  /** initialize this swimlane's assignee and participations from the 
   * other swimlane. */
  public void initialize(SwimlaneImpl other) {
    this.assignee = other.getAssignee();
    if (other.getParticipations()!=null) {
      for (ParticipationImpl otherParticipation: other.getParticipations()) {
        addParticipation(
          otherParticipation.getUserId(), 
          otherParticipation.getGroupId(), 
          otherParticipation.getType()
        );
      }
    }
  }
  
  // assignment methods ///////////////////////////////////////////////////////

  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  // participations /////////////////////////////////////////////////////////////
  
  public Set<ParticipationImpl> getParticipations() {
    return participations;
  }

  public void addCandidateGroup(String groupId) {
    addParticipation(null, groupId, Participation.CANDIDATE);
  }

  public void addCandidateUser(String userId) {
    addParticipation(userId, null, Participation.CANDIDATE);
  }

  public Participation addParticipation(String userId, String groupId, String type) {
    return addParticipation(new ParticipationImpl(userId, groupId, type));
  }
  
  private Participation addParticipation(ParticipationImpl participation) {
    participation.setSwimlane(this);
    participations.add(participation);
    return participation;
  }

  public void removeParticipant(ParticipationImpl participant) {
    if (participant==null) {
      throw new JbpmException("participant is null");
    }
    if (participations.remove(participant)) {
      participant.setSwimlane(null);
    }
  }
  
  // equals ///////////////////////////////////////////////////////////////////
  // hack to support comparing hibernate proxies against the real objects
  // since this always falls back to ==, we don't need to overwrite the hashcode
  public boolean equals(Object o) {
    return EqualsUtil.equals(this, o);
  }
  
  public String toString() {
    return "Swimlane("+name+")";
  }
  
  // customized getters and setters ///////////////////////////////////////////
  
  public String getId() {
    return Long.toString(dbid);
  }

  // getters and setters //////////////////////////////////////////////////////

  public long getDbid() {
    return dbid;
  }
  public String getName() {
    return name;
  }
  public SwimlaneDefinitionImpl getSwimlaneDefinition() {
    return swimlaneDefinition;
  }
  public void setSwimlaneDefinition(SwimlaneDefinitionImpl swimlaneDefinition) {
    this.swimlaneDefinition = swimlaneDefinition;
  }
  public void setName(String name) {
    this.name = name;
  }
  public ExecutionImpl getExecution() {
    return execution;
  }
  public void setExecution(ExecutionImpl execution) {
    this.execution = execution;
  }
  public void setDbid(long dbid) {
    this.dbid = dbid;
  }
}

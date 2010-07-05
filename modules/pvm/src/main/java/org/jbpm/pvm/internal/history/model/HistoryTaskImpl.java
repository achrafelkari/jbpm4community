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
package org.jbpm.pvm.internal.history.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.jbpm.api.history.HistoryTask;
import org.jbpm.pvm.internal.task.TaskImpl;
import org.jbpm.pvm.internal.util.Clock;
import org.jbpm.pvm.internal.util.EqualsUtil;

/**
 * @author Tom Baeyens
 */
public class HistoryTaskImpl implements Serializable, HistoryTask {

  private static final long serialVersionUID = 1L;

  protected long dbid;
  protected int dbversion;

  protected String executionId;
  protected String assignee;
  protected String state;
  protected String outcome;
  protected int priority;
  protected Date duedate;
  protected Date createTime;
  protected Date endTime;
  protected long duration;
  protected int nextDetailIndex = 1;

  protected Set<HistoryTaskImpl> subTasks = new HashSet<HistoryTaskImpl>();

  /** only here to get hibernate cascade */
  protected Set<HistoryVariableImpl> historyVariables;

  /** only here to get hibernate cascade */
  protected Set<HistoryDetailImpl> details = new HashSet<HistoryDetailImpl>();

  public HistoryTaskImpl() {
  }

  public HistoryTaskImpl(TaskImpl task) {
    this.dbid = task.getDbid();
    this.assignee = task.getAssignee();
    this.priority = task.getPriority();
    this.duedate = task.getDuedate();
    this.createTime = Clock.getTime();
  }

  public void updated(TaskImpl task) {
    if ( (assignee==null && task.getAssignee()!=null)
         || (assignee!=null) && (!assignee.equals(task.getAssignee()))
       ) {
      addDetail(new HistoryTaskAssignmentImpl(assignee, task.getAssignee()));
      this.assignee = task.getAssignee();
    }
    if (priority!=task.getPriority()) {
      addDetail(new HistoryPriorityUpdateImpl(priority, task.getPriority()));
      this.priority = task.getPriority();
    }
    if ( (duedate==null && task.getDuedate()!=null)
         || (duedate!=null) && (!duedate.equals(task.getDuedate()))
       ) {
      addDetail(new HistoryTaskDuedateUpdateImpl(duedate, task.getDuedate()));
      this.duedate = task.getDuedate();
    }
  }

  // details //////////////////////////////////////////////////////////////////
  
  public void addDetail(HistoryDetailImpl detail) {
    detail.setHistoryTask(this, nextDetailIndex);
    this.details.add(detail);
    nextDetailIndex++;
  }

  // subtasks /////////////////////////////////////////////////////////////////
  
  public void addSubTask(HistoryTaskImpl historyTask) {
    subTasks.add(historyTask);
  }

  // equals ///////////////////////////////////////////////////////////////////
  // hack to support comparing hibernate proxies against the real objects
  // since this always falls back to ==, we don't need to overwrite the hashcode
  public boolean equals(Object o) {
    return EqualsUtil.equals(this, o);
  }
  
  // customized getters and setters ///////////////////////////////////////////
  
  public String getId() {
    return Long.toString(dbid);
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
    this.duration = endTime.getTime() - createTime.getTime();
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getState() {
    return state;
  }
  public String getAssignee() {
    return assignee;
  }
  public String getOutcome() {
    return outcome;
  }
  public void setOutcome(String outcome) {
    this.outcome = outcome;
  }
  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }
  public Date getCreateTime() {
    return createTime;
  }
  public Date getEndTime() {
    return endTime;
  }
  public long getDuration() {
    return duration;
  }
  public void setDuration(long duration) {
    this.duration = duration;
  }
  public String getExecutionId() {
    return executionId;
  }
  public void setState(String state) {
    this.state = state;
  }
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
}

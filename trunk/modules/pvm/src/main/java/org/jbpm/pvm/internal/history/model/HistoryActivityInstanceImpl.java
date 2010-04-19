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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbpm.api.history.HistoryActivityInstance;
import org.jbpm.api.history.HistoryProcessInstance;
import org.jbpm.pvm.internal.id.DbidGenerator;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;

/** base activity instance class.
 *  
 * @author Tom Baeyens
 */
public class HistoryActivityInstanceImpl implements HistoryActivityInstance, Serializable {

  private static final long serialVersionUID = 1L;

  protected long dbid;
  protected int dbversion;

  protected HistoryProcessInstance historyProcessInstance;
  protected String executionId;
  protected ActivityImpl activity;
  protected String type;
  protected String activityName;

  protected Date startTime;
  protected Date endTime;
  protected long duration;
  
  protected String transitionName;

  protected int nextDetailIndex = 1;

  /** only here to get hibernate cascade */
  protected Set<HistoryDetailImpl> details = new HashSet<HistoryDetailImpl>();

  
  public HistoryActivityInstanceImpl() {
  }

  public HistoryActivityInstanceImpl(HistoryProcessInstance historyProcessInstanceImpl, ExecutionImpl execution) {
    this.historyProcessInstance = historyProcessInstanceImpl;
    this.activity = execution.getActivity();
    this.executionId = execution.getId();
    this.activityName = activity.getName();
    this.startTime = execution.getHistoryActivityStart();
    this.dbid = DbidGenerator.getDbidGenerator().getNextId(); 
  }
  
  // details //////////////////////////////////////////////////////////////////
  
  public void addDetail(HistoryDetailImpl detail) {
    detail.setHistoryActivityInstance(this, nextDetailIndex);
    nextDetailIndex++;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
    this.duration = endTime.getTime() - startTime.getTime();
  }
  
  public long getDbid() {
    return dbid;
  }
  public ActivityImpl getActivity() {
    return activity;
  }
  public String getActivityName() {
    return activityName;
  }
  public Date getStartTime() {
    return startTime;
  }
  public Date getEndTime() {
    return endTime;
  }
  public long getDuration() {
    return duration;
  }
  public HistoryProcessInstance getHistoryProcessInstance() {
    return historyProcessInstance;
  }
  public String getExecutionId() {
    return executionId;
  }
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  public String getTransitionName() {
    return transitionName;
  }
  public void setTransitionName(String transitionName) {
    this.transitionName = transitionName;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public List<String> getTransitionNames() {
    // TODO: expand for multiple outgoing transitions.
    // Currently not possible, since only one transition name is stored.
    if (transitionName != null) {
      List<String> transitionNames = new ArrayList<String>();
      transitionNames.add(transitionName);
      return transitionNames;
    }
    return Collections.emptyList();
  }
}

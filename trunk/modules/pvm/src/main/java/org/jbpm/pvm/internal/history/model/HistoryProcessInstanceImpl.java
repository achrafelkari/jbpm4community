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

import org.jbpm.api.history.HistoryProcessInstance;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;
import org.jbpm.pvm.internal.util.Clock;


/**
 * @author Tom Baeyens
 */
public class HistoryProcessInstanceImpl implements HistoryProcessInstance, Serializable {
  
  private static final long serialVersionUID = 1L;

  protected long dbid;
  protected int dbversion;
  
  protected ProcessDefinitionImpl processDefinition;
  protected String processDefinitionId;
  protected String processInstanceId;
  protected String key;
  protected String state;
  protected String endActivityName;
  protected Date startTime;
  protected Date endTime;
  protected Long duration;
  protected int nextDetailIndex = 1;

  /** only here to get hibernate cascade */
  protected Set<HistoryActivityInstanceImpl> historyActivityInstances;

  /** only here to get hibernate cascade */
  protected Set<HistoryVariableImpl> historyVariables;

  /** only here to get hibernate cascade */
  protected Set<HistoryDetailImpl> details = new HashSet<HistoryDetailImpl>();

  
  public void addDetail(HistoryDetailImpl detail) {
    detail.setHistoryProcessInstance(this, nextDetailIndex);
    nextDetailIndex++;
  }

  public HistoryProcessInstanceImpl() {
  }

  public HistoryProcessInstanceImpl(ExecutionImpl processInstance) {
    this.dbid = processInstance.getDbid();
    this.processDefinition = processInstance.getProcessDefinition();
    this.processDefinitionId = processDefinition.getId();
    this.processInstanceId = processInstance.getId();
    this.key = processInstance.getKey();
    this.state = "active";
    this.startTime = Clock.getTime();
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
    this.duration = endTime.getTime() - startTime.getTime();
    this.state = "ended";
  }

  public Date getEndTime() {
    return endTime;
  }
  public long getDbid() {
    return dbid;
  }
  public ProcessDefinitionImpl getProcessDefinition() {
    return processDefinition;
  }
  public Date getStartTime() {
    return startTime;
  }
  public Long getDuration() {
    return duration;
  }
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getKey() {
    return key;
  }
  public String getState() {
    return state;
  }
  public void setState(String state) {
    this.state = state;
  }
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }
  public String getEndActivityName() {
    return endActivityName;
  }
  public void setEndActivityName(String endActivityName) {
    this.endActivityName = endActivityName;
  }
  public Set<HistoryVariableImpl> getHistoryVariables() {
    return historyVariables;
  }
}

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
import java.util.HashSet;
import java.util.Set;

import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.type.Variable;


/**
 * @author Tom Baeyens
 */
public class HistoryVariableImpl implements Serializable {

  private static final long serialVersionUID = 1L;

  protected long dbid;
  protected int dbversion;
  
  protected HistoryProcessInstanceImpl historyProcessInstance;
  protected HistoryTaskImpl historyTask;

  protected String processInstanceId;
  protected String executionId;
  protected String variableName;
  protected String value;
  protected int nextDetailIndex = 1;

  /** only here to get hibernate cascade */
  protected Set<HistoryDetailImpl> details = new HashSet<HistoryDetailImpl>();

  protected HistoryVariableImpl() {
  }
  
  public HistoryVariableImpl(HistoryProcessInstanceImpl historyProcessInstance, HistoryTaskImpl historyTask, Variable variable) {
    this.dbid = variable.getDbid();

    this.historyProcessInstance = historyProcessInstance;
    this.historyTask = historyTask;

    ExecutionImpl processInstance = variable.getProcessInstance();
    if (processInstance!=null) {
      this.processInstanceId = processInstance.getId();
    }
    // this.executionId = variable.getExecution().getId();

    this.variableName = variable.getKey();
    this.value = variable.getTextValue();
  }
  
  public void updated(Variable variable) {
    String newValue = variable.getTextValue();
    if ( (value==null && newValue!=null)
         || (value!=null && (!value.equals(newValue)))
       ) {
      this.value = newValue;
      addDetail(new HistoryVariableUpdateImpl(value, newValue));
    }
  }
  
  public void addDetail(HistoryDetailImpl detail) {
    detail.setHistoryVariable(this, nextDetailIndex);
    nextDetailIndex++;
  }

  
  public long getDbid() {
    return dbid;
  }
  public int getDbversion() {
    return dbversion;
  }
  public HistoryProcessInstanceImpl getHistoryProcessInstance() {
    return historyProcessInstance;
  }
  public HistoryTaskImpl getHistoryTask() {
    return historyTask;
  }
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getExecutionId() {
    return executionId;
  }
  public String getVariableName() {
    return variableName;
  }
  public String getValue() {
    return value;
  }
  public int getNextDetailIndex() {
    return nextDetailIndex;
  }
  public Set<HistoryDetailImpl> getDetails() {
    return details;
  }
}

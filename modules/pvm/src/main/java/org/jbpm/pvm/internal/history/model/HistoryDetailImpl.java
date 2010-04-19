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

import java.util.Date;

import org.jbpm.api.history.HistoryDetail;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.id.DbidGenerator;
import org.jbpm.pvm.internal.util.Clock;
import org.jbpm.pvm.internal.util.EqualsUtil;

public class HistoryDetailImpl implements HistoryDetail {

  private static final long serialVersionUID = 1L;

  protected long dbid;
  protected int dbversion;
  protected String userId = null;
  protected Date time = null;
  
  protected HistoryProcessInstanceImpl historyProcessInstance;
  protected Integer historyProcessInstanceIndex;

  protected HistoryActivityInstanceImpl historyActivityInstance;
  protected Integer historyActivityInstanceIndex;
  
  protected HistoryTaskImpl historyTask;
  protected Integer historyTaskIndex;

  protected HistoryVariableImpl historyVariable;
  protected Integer historyVariableIndex;

  protected HistoryDetailImpl() {
  }

  // dummy string constructor to differentiate from  
  // the default constructor which is used by hibernate 
  protected HistoryDetailImpl(String dummy) {
    this.dbid = DbidGenerator.getDbidGenerator().getNextId(); 
    this.time = Clock.getTime();
    
    EnvironmentImpl environment = EnvironmentImpl.getCurrent();
    if (environment!=null) {
      this.userId = environment.getAuthenticatedUserId();
    }
  }
  
  // equals ///////////////////////////////////////////////////////////////////
  // hack to support comparing hibernate proxies against the real objects
  // since this always falls back to ==, we don't need to overwrite the hashcode
  public boolean equals(Object o) {
    return EqualsUtil.equals(this, o);
  }

  // cusomtized getters and setters ////////////////////////////////////////////

  public String getId() {
    return Long.toString(dbid);
  }

  // getters and setters //////////////////////////////////////////////////////

  public long getDbid() {
    return dbid;
  }
  public String getUserId() {
    return userId;
  }
  public Date getTime() {
    return time;
  }
  public void setUserId(String userId) {
    this.userId = userId;
  }
  public void setTime(Date time) {
    this.time = time;
  }

  public void setHistoryProcessInstance(HistoryProcessInstanceImpl historyProcessInstance, int historyProcessInstanceIndex) {
    this.historyProcessInstance = historyProcessInstance;
    this.historyProcessInstanceIndex = historyProcessInstanceIndex;
  }

  public void setHistoryActivityInstance(HistoryActivityInstanceImpl historyActivityInstance, int historyActivityInstanceIndex) {
    this.historyActivityInstance = historyActivityInstance;
    this.historyActivityInstanceIndex = historyActivityInstanceIndex;
  }

  public void setHistoryTask(HistoryTaskImpl historyTask, int historyTaskIndex) {
    this.historyTask = historyTask;
    this.historyTaskIndex = historyTaskIndex;
  }

  public void setHistoryVariable(HistoryVariableImpl historyVariable, int historyVariableIndex) {
    this.historyVariable = historyVariable;
    this.historyVariableIndex = historyVariableIndex;
  }
}

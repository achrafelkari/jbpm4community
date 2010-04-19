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
package org.jbpm.pvm.internal.model;

import java.io.Serializable;
import java.util.Date;

import org.jbpm.api.JbpmException;


/**
 * @author Tom Baeyens
 * @author Pascal Verdage
 */
public class TimerDefinitionImpl implements Serializable {

  private static final long serialVersionUID = 1L;

  protected long dbid;
  protected int dbversion;
  protected String dueDateDescription;
  protected Date dueDate;
  protected String cronExpression;
  protected String repeat;
  protected Boolean isExclusive;
  protected Integer retries;
  protected String eventName;
  protected String signalName;

  public TimerDefinitionImpl() {
  }

  public String getRepeat() {
    return repeat;
  }
  public void setRepeat(String repeat) {
    if (cronExpression != null) {
      throw new JbpmException("Can't use 'repeat' and 'cronExpression' together for same timer");
    }
    this.repeat = repeat;
  }
  public long getDbid() {
    return dbid;
  }
  public String getDueDateDescription() {
    return dueDateDescription;
  }
  public void setDueDateDescription(String dueDateDescription) {
    this.dueDateDescription = dueDateDescription;
  }
  public Boolean isExclusive() {
    return isExclusive;
  }
  public void setExclusive(Boolean isExclusive) {
    this.isExclusive = isExclusive;
  }
  public Integer getRetries() {
    return retries;
  }
  public void setRetries(Integer retries) {
    this.retries = retries;
  }
  public String getSignalName() {
    return signalName;
  }
  public void setSignalName(String signalName) {
    this.signalName = signalName;
  }
  public String getEventName() {
    return eventName;
  }
  public void setEventName(String eventName) {
    this.eventName = eventName;
  }
  public Date getDueDate() {
    return dueDate;
  }
  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }
  public String getCronExpression() {
    return cronExpression;
  }
  public void setCronExpression(String cronExpression)  {
    if (repeat != null) {
      throw new JbpmException("Can't use 'repeat' and 'cronExpression' together for same timer");
    }
    this.cronExpression = cronExpression;
  }
}

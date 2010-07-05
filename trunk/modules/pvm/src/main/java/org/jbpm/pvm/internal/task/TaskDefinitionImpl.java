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

import java.util.ArrayList;
import java.util.List;

import org.jbpm.pvm.internal.util.Priority;

/**
 * defines a task and how the actor(s) must be calculated at runtime.
 */
public class TaskDefinitionImpl extends AssignableDefinitionImpl {
  
  private static final long serialVersionUID = 1L;

  protected String dueDateDescription;
  protected String formResourceName;
  protected List<TaskDefinitionImpl> subTaskDefinitions = new ArrayList<TaskDefinitionImpl>();
  protected int priority = Priority.NORMAL;
  protected SwimlaneDefinitionImpl swimlaneDefinition;

  public int getPriority() {
    return priority;
  }
  public void setPriority(int priority) {
    this.priority = priority;
  }
  public List<TaskDefinitionImpl> getSubTaskDefinitions() {
    return subTaskDefinitions;
  }
  public void setSubTaskDefinitions(List<TaskDefinitionImpl> subTaskDefinitions) {
    this.subTaskDefinitions = subTaskDefinitions;
  }
  public SwimlaneDefinitionImpl getSwimlaneDefinition() {
    return swimlaneDefinition;
  }
  public void setSwimlaneDefinition(SwimlaneDefinitionImpl swimlaneDefinition) {
    this.swimlaneDefinition = swimlaneDefinition;
  }
  public String getDueDateDescription() {
    return dueDateDescription;
  }
  public void setDueDateDescription(String dueDateDescription) {
    this.dueDateDescription = dueDateDescription;
  }
  public String getFormResourceName() {
    return formResourceName;
  }
  public void setFormResourceName(String form) {
    this.formResourceName = form;
  }
}

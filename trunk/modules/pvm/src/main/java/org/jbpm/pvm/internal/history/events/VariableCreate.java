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
package org.jbpm.pvm.internal.history.events;

import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.history.HistoryEvent;
import org.jbpm.pvm.internal.history.model.HistoryProcessInstanceImpl;
import org.jbpm.pvm.internal.history.model.HistoryTaskImpl;
import org.jbpm.pvm.internal.history.model.HistoryVariableImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.session.DbSession;
import org.jbpm.pvm.internal.task.TaskImpl;
import org.jbpm.pvm.internal.type.Variable;


/**
 * @author Tom Baeyens
 */
public class VariableCreate extends HistoryEvent {

  private static final long serialVersionUID = 1L;

  Variable variable;
  
  public VariableCreate(Variable variable) {
    this.variable = variable;
  }

  public void process() {
    DbSession dbSession = EnvironmentImpl.getFromCurrent(DbSession.class);
    
    dbSession.save(variable);

    HistoryProcessInstanceImpl historyProcessInstance = null;
    ExecutionImpl processInstance = variable.getProcessInstance();
    if (processInstance!=null) {
      long processInstanceDbid = processInstance.getDbid();
      historyProcessInstance = dbSession.get(HistoryProcessInstanceImpl.class, processInstanceDbid);
    }
    
    HistoryTaskImpl historyTask = null;
    TaskImpl task = variable.getTask();
    if (task!=null) {
      long taskDbid = task.getDbid();
      historyTask = dbSession.get(HistoryTaskImpl.class, taskDbid);
    }
    
    HistoryVariableImpl historyVariable = new HistoryVariableImpl(historyProcessInstance, historyTask, variable);
    dbSession.save(historyVariable);
  }

  public Variable getVariable() {
    return variable;
  }
  
}

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

import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessInstance;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.history.HistoryEvent;
import org.jbpm.pvm.internal.history.model.HistoryProcessInstanceImpl;
import org.jbpm.pvm.internal.history.model.HistoryProcessInstanceMigrationImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.session.DbSession;

/**
 * @author Koen Aers
 */
public class ProcessInstanceMigration extends HistoryEvent {

  private static final long serialVersionUID = 1L;

  ProcessInstance processInstance;
  ProcessDefinition processDefinition;
  
  public ProcessInstanceMigration(ProcessInstance processInstance, ProcessDefinition processDefinition) {
    this.processInstance = processInstance;
    this.processDefinition = processDefinition;
  }

  public void process() {
    if (processDefinition == null || processInstance == null || !(processInstance instanceof ExecutionImpl)) return;
    
    DbSession dbSession = EnvironmentImpl.getFromCurrent(DbSession.class);

    HistoryProcessInstanceImpl historyProcessInstance = null;
    long processInstanceDbid = ((ExecutionImpl)processInstance).getDbid();
    historyProcessInstance = dbSession.get(HistoryProcessInstanceImpl.class, processInstanceDbid);
    if (historyProcessInstance == null) return;
    
    String oldVersion = historyProcessInstance.getProcessDefinitionId();
    String newVersion = processDefinition.getId();
    
    historyProcessInstance.setProcessDefinitionId(newVersion);
    HistoryProcessInstanceMigrationImpl historyProcessInstanceMigration = 
        new HistoryProcessInstanceMigrationImpl(oldVersion, newVersion);
    historyProcessInstance.addDetail(historyProcessInstanceMigration);
    
    dbSession.save(historyProcessInstanceMigration);
  }
  
  public ProcessInstance getProcessInstance() {
    return processInstance;
  }

  public ProcessDefinition getProcessDefinition() {
    return processDefinition;
  }
  
}

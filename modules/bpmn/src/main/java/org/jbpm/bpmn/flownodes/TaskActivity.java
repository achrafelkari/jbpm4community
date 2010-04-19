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
package org.jbpm.bpmn.flownodes;

import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.task.TaskDefinitionImpl;


/**
 * @author Tom Baeyens
 * @author Ronald van Kuijk (kukeltje)
 */
public class TaskActivity extends BpmnActivity {

  private static final long serialVersionUID = 1L;
  
  private static final Log log = Log.getLog(TaskActivity.class.getName());

  protected TaskDefinitionImpl taskDefinition;
  
  public void execute(ActivityExecution execution) {
    execute((ExecutionImpl)execution);
  }

  public void execute(ExecutionImpl execution) {

    proceed(execution, findOutgoingSequenceFlow(execution, CONDITIONS_CHECKED));
    
  }
  
  public TaskDefinitionImpl getTaskDefinition() {
    return taskDefinition;
  }
  public void setTaskDefinition(TaskDefinitionImpl taskDefinition) {
    this.taskDefinition = taskDefinition;
  }
}
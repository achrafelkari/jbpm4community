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

import java.util.Collection;

import org.jbpm.bpmn.model.BpmnProcessDefinition;
import org.jbpm.pvm.internal.model.ExecutionImpl;

/**
 * @author Joram Barrez
 */
public class InclusiveGatewayActivity extends AbstractMergingGatewayActivity {

  private static final long serialVersionUID = 1L;
  
  public void fork(ExecutionImpl execution) { 
    proceed(execution, findOutgoingSequenceFlow(execution, CONDITIONS_CHECKED));
  }
  
  /**
   * Section 14.3.2 of the BPMN 2.0 specification.
   * 
   * The Inclusive Gateway is activated if 
   *   - At least one incoming sequence flow has at least one Token and 
   *   - for each empty incoming sequence flow, there is no Token in the graph anywhere 
   *     upstream of this sequence flow, i.e., there is no directed path (formed by Sequence Flow) 
   *     from a Token to this sequence flow unless 
   *       - the path visits the inclusive gateway or 
   *       - the path visits a node that has a directed path to a non-empty incoming sequence 
   *         flow of the inclusive gateway. 
   */
  protected boolean isComplete(ExecutionImpl incomingExecution) {
    String currentActivityId = incomingExecution.getActivityName(); // id is stored in the name attribute
    Collection<ExecutionImpl> allExecutions = incomingExecution.getProcessInstance().getExecutions();
    BpmnProcessDefinition processDefinition = (BpmnProcessDefinition) incomingExecution.getProcessDefinition();
  
    for (ExecutionImpl execution : allExecutions) {
      if (incomingExecution.getParent().equals(execution.getParent())) {
        String activityId = execution.getActivityName(); // id is stored in the name attribute
        if (activityId != null && !currentActivityId.equals(activityId)) {
          if (processDefinition.isReachable(activityId, currentActivityId)) {
            return false;
          }
        }
      }
    }
    
    return true;
  }

}

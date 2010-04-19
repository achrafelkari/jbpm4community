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
package org.jbpm.test.history;

import java.util.List;

import org.jbpm.api.Execution;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.history.HistoryProcessInstance;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class EndProcessInstanceTest extends JbpmTestCase {

  public void testCancelProcessInstance() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='wait' />" +
      "  </start>" +
      "  <state name='wait'>" +
      "    <transition to='end' />" +
      "  </state>" +
      "  <end name='end' />" +
      "</process>"
    );

    Execution execution = executionService.startProcessInstanceByKey("ICL");
    String processInstanceId = execution.getId();
    executionService.endProcessInstance(processInstanceId, "cancel");

    List<ProcessInstance> processInstances = executionService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .list();
    assertEquals(0, processInstances.size());

    HistoryProcessInstance historyProcessInstance = historyService.createHistoryProcessInstanceQuery()
      .processInstanceId(processInstanceId)
      .uniqueResult();

    assertNotNull(historyProcessInstance.getStartTime());
    assertNotNull(historyProcessInstance.getEndTime());
    assertEquals("cancel", historyProcessInstance.getState());
  }
  
  // Test case for JBPM-2319
  public void testProcessInstanceEndState() {
    deployJpdlXmlString(
      "<process name='test1'>" +
      "  <start g='4,289,48,48' name='start1'>" +
      "    <transition name='to task1' to='task1' g='-44,-18'/>" +
      "  </start>" +
      "  <task name='task1' g='152,277,92,52'>" +
      "    <transition name='Reject (risk)' to='Rejected by risk' g='-42,-18'/>" +
      "    <transition name='Reject (reporting)' to='Rejected by reporting' g='-42,-18'/>" +
      "  </task>" +
      "  <end name='Rejected by risk' g='328,232,48,48'/>" +
      "  <end name='Rejected by reporting' g='335,347,48,48'/>" +
      "</process>"      
    );
    
    // Start a process instance and complete the task which ends the process instance
    ProcessInstance pi = executionService.startProcessInstanceByKey("test1");
    Task task = taskService.createTaskQuery()
                           .processInstanceId(pi.getId())
                           .uniqueResult();
    taskService.completeTask(task.getId(), "Reject (risk)");
    assertProcessInstanceEnded(pi);
    
    // Check the state of the process instance
    HistoryProcessInstance historyPi = historyService.createHistoryProcessInstanceQuery()
                                                     .processInstanceId(pi.getId())
                                                     .uniqueResult();
    assertEquals("ended", historyPi.getState());
  }
  
}

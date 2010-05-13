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
/**
 * 
 */
package org.jbpm.test.task;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;


/**
 * Testcase to check if properties can be resolved through a {@link Task}.
 * 
 * @author Joram Barrez
 * @author Ronald van Kuijk
 */
public class TaskPropertiesTest extends JbpmTestCase {
  
  private static final String ACTOR = "johnDoe";
  
  private static final String ACTOR2 = "johnSmoe";
  
  private static final String PROCESS =
    "<process name='VacationTrip'>" +
    "  <start>" +
    "    <transition to='f' />" +
    "  </start>" +
    "  <fork name='f'>" +
    "    <transition to='select destination' />" +
    "    <transition to='work hard for the money' />" +
    "  </fork>" +
    "  <task name='select destination' assignee='" + ACTOR + "'>" +
    " <description>Description for 'select destination' with #{timeframe}</description>" +
    "    <transition to='wait' />" +
    "  </task>" +
    "  <task name='work hard for the money' assignee='" + ACTOR2 + "_not_the_same'>" +
    "    <transition to='wait' />" +
    "  </task>" +
    "  <state name='wait'/>" +
    "</process>";
  
  public void testGetActivityName() {
    Task task = startProcessInstanceAndReturnTaskFor(ACTOR);
    assertEquals("select destination", task.getActivityName()); 
  }
   
  public void testGetDescription() {
    Task task = startProcessInstanceAndReturnTaskFor(ACTOR);
    assertEquals("Description for 'select destination' with Springbreak", task.getDescription());
  }
  
  public void testGetProcessDefinitionThroughTask() {
    Task task = startProcessInstanceAndReturnTaskFor(ACTOR);
    
    String pdId = executionService.findExecutionById(task.getExecutionId()).getProcessDefinitionId();
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                                                           .processDefinitionId(pdId)
                                                           .uniqueResult();
    assertEquals("VacationTrip", processDefinition.getName());   
  }
  
  private Task startProcessInstanceAndReturnTaskFor(String actor) {
    deployJpdlXmlString(PROCESS);
    Map<String, String> vars = new HashMap<String, String>();
    vars.put("timeframe", "Springbreak");
    executionService.startProcessInstanceByKey("VacationTrip", vars);
 
    return taskService.findPersonalTasks(actor).get(0);
  }

}

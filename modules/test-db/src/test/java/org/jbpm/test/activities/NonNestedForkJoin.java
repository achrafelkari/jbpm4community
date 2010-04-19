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
package org.jbpm.test.activities;

import org.jbpm.api.ProcessInstance;
import org.jbpm.api.TaskQuery;
import org.jbpm.api.task.Task;

import org.jbpm.test.JbpmTestCase;

/**
 *  Testcase for a not fully nested fork/join JBPM-2570
 * 
 * @author Ronald van Kuijk
 */
public class NonNestedForkJoin extends JbpmTestCase {
    
    public void testNotFullyNestedWithSwimlanes () {

    	deployJpdlXmlString("<?xml version='1.0' encoding='UTF-8'?>"
                + "<process name='JBPM-2570'> "
                + "	<swimlane candidate-groups='A' name='A'/>"
                + "	<swimlane candidate-groups='B' name='B'/>"
                + "	<swimlane candidate-groups='C' name='C'/>"
                + "	<start name='start1'>"
                + "		<transition to='fork1'/>"
                + "	</start>"
                + "	<fork name='fork1'>"
                + "		<transition to='task1a'/>"
                + "		<transition to='task1b'/>"
                + "		<transition to='task1c'/>"
                + "	</fork>"
                + "	<task name='task1a' swimlane='A'>"
                + "		<transition to='join1'/>"
                + "	</task>"
                + "	<task name='task1b' swimlane='B'>"
                + "		<transition to='join1'/>"
                + "	</task>"
                + "	<task name='task1c' swimlane='C'>"
                + "		<transition to='join2'/>"
                + "	</task>"
                + "	<join name='join1'>"
                + "		<transition to='task2a'/>"
                + "	</join>"
                + "	<task name='task2a' swimlane='B'>"
                + "		<transition to='join2'/>"
                + "	</task>"
                + "	<join name='join2'>"
                + "		<transition to='task3a'/>"
                + "	</join>"
                + "	<task name='task3a'>"
                + "		<transition to='end1'/>"
                + "	</task>"             
                + "	<end name='end1'/>"
                + "</process>");

    	   
        ProcessInstance processInstance = executionService.startProcessInstanceByKey("JBPM-2570");
   
        String processInstanceId = processInstance.getId();

        TaskQuery taskQuery = taskService.createTaskQuery();
        taskQuery.processInstanceId(processInstanceId);
        
        assertEquals(3, taskQuery.list().size());
        
        assertActivityActive(processInstanceId, "task1a");        
        Task t1a = getTask(taskQuery.list(),"task1a");        
        taskService.assignTask(t1a.getId(), "For SLA");
        taskService.completeTask(t1a.getId());
        
        assertActivityActive(processInstanceId, "task1b");
        Task t1b = getTask(taskQuery.list(),"task1b");
        taskService.assignTask(t1b.getId(), "For SLB");
        taskService.completeTask(t1b.getId());

        assertActivitiesActive(processInstanceId, "task1c", "task2a");

        assertNotActivityActive(processInstanceId, "task3a");
        
        assertActivityActive(processInstanceId, "task2a");
        taskService.completeTask(getTask(taskQuery.list(),"task2a").getId());
        
        Task t1c = getTask(taskQuery.list(),"task1c");        
        taskService.assignTask(t1c.getId(), "For SLC");
        taskService.completeTask(t1c.getId());
        
        assertActivitiesActive(processInstanceId, "task3a");
        
        Task t3a = getTask(taskQuery.list(),"task3a");
        taskService.completeTask(t3a.getId());        
        
        assertProcessInstanceEnded(processInstance);
    }    
    
}

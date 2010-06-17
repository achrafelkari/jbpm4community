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
package org.jbpm.test.activity.task;

import java.util.List;

import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;

/**
 * JBPM-2570.
 *
 * @author Huisheng Xu
 */
public class ForkSwimlaneTest extends JbpmTestCase {
    private static final String PROCESS_XML = ""
        + "<process name='ForkSwimlane' xmlns='http://jbpm.org/4.3/jpdl'>"
        + "   <swimlane candidate-groups='A' name='A'/>"
        + "   <swimlane candidate-groups='B' name='B'/>"
        + "   <start name='start'>"
        + "      <transition to='task1A'/>"
        + "   </start>"
        + "   <task name='task1A' swimlane='A'>"
        + "      <transition to='task1B'/>"
        + "   </task>"
        + "   <task name='task1B' swimlane='B'>"
        + "      <transition to='fork'/>"
        + "   </task>"
        + "   <fork name='fork'>"
        + "      <transition to='task2A'/>"
        + "      <transition to='task2B'/>"
        + "   </fork>"
        + "   <task name='task2A' swimlane='A'>"
        + "      <transition to='join'/>"
        + "   </task>"
        + "   <task name='task2B' swimlane='B'>"
        + "      <transition to='join'/>"
        + "   </task>"
        + "   <join name='join'>"
        + "      <transition to='end'/>"
        + "   </join>"
        + "   <end name='end'/>"
        + "</process>";

    protected void setUp() throws Exception {
        super.setUp();

        String groupA = identityService.createGroup("A");
        String groupB = identityService.createGroup("B");

        identityService.createUser("lingo", "lingo", "lingo");
        identityService.createMembership("lingo", groupA, "lingo-groupA");
        identityService.createMembership("lingo", groupB, "lingo-groupB");
        this.deployJpdlXmlString(PROCESS_XML);
    }

    protected void tearDown() throws Exception {
        identityService.deleteGroup("A");
        identityService.deleteGroup("B");
        identityService.deleteUser("lingo");
        super.tearDown();
    }

    public void testSwimlane() {
        executionService.startProcessInstanceByKey("ForkSwimlane");
        takeAndCompleteTask("lingo");
        takeAndCompleteTask("lingo");
        completeTask("lingo");
        completeTask("lingo");
    }

    protected void takeAndCompleteTask(String username) {
        List<Task> tasks = taskService.findGroupTasks(username);
        Task task = tasks.get(0);
        taskService.takeTask(task.getId(), username);
        taskService.completeTask(task.getId());
    }

    protected void completeTask(String username) {
        List<Task> tasks = taskService.findPersonalTasks(username);
        Task task = tasks.get(0);
        taskService.completeTask(task.getId());
    }
}

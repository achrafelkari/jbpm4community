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
 package org.jbpm.test.activity.forkjoin;

import org.jbpm.api.ProcessInstance;
import org.jbpm.api.listener.EventListener;
import org.jbpm.api.listener.EventListenerExecution;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.test.JbpmTestCase;

import java.util.Set;

/**
 * JBPM-2832, JBPM-2833.
 *
 * @author Huisheng Xu
 */
public class ForkResultsInNullEndActivityTest extends JbpmTestCase {
    public static final String XML_FORK_JOIN = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<process name=\"ForkTest\" xmlns=\"http://jbpm.org/4.3/jpdl\">\n" +
        "  <start name=\"preparing test\">\n" +
        "    <on event='end'>" +
        "      <event-listener class='org.jbpm.test.activity.forkjoin.ForkResultsInNullEndActivityTest$StartEventListener'/>" +
        "    </on>" +
        "    <transition to=\"fork\"/>\n" +
        "  </start>\n" +
        "  <fork name=\"fork\">\n" +
        "    <transition to=\"state1\"/>\n" +
        "    <transition to=\"state2\"/>\n" +
        "    <transition to=\"cancel\"/>\n" +
        "  </fork>\n" +
        "  <state name=\"cancel\">\n" +
        "    <transition to=\"cancelled\"/>\n" +
        "  </state>\n" +
        "  <state name=\"state1\">\n" +
        "    <transition to=\"states completed\"/>\n" +
        "  </state>\n" +
        "  <state name=\"state2\">\n" +
        "    <transition to=\"states completed\"/>\n" +
        "  </state>\n" +
        "  <join multiplicity=\"2\" name=\"states completed\">\n" +
        "    <transition to=\"completed\"/>\n" +
        "  </join>\n" +
        "  <end name=\"cancelled\"/>\n" +
        "  <end name=\"completed\"/>\n" +
        "</process>";

    public static final String XML_SUB_PROCESS = "" +
        "<process name='sub_process' xmlns='http://jbpm.org/4.3/jpdl'>" +
        "  <start>" +
        "    <transition to='sub'/>" +
        "  </start>" +
        "  <sub-process name='sub' sub-process-key='ForkTest' outcome='#{result}'>" +
        "    <transition name='to A' to='A'/>" +
        "    <transition name='to B' to='B'/>" +
        "    <transition name='to C' to='C'/>" +
        "  </sub-process>" +
        "  <state name='A'/>" +
        "  <state name='B'/>" +
        "  <state name='C'/>" +
        "</process>";

    private String deploymentId;

    protected void setUp() throws Exception {
        super.setUp();

        // Deploys the process
        deploymentId = repositoryService.createDeployment()
            .addResourceFromString("ForkTest.jpdl.xml", XML_FORK_JOIN)
            .addResourceFromString("sub_process.jpdl.xml", XML_SUB_PROCESS)
            .deploy();
    }

    protected void tearDown() throws Exception {
        repositoryService.deleteDeploymentCascade(deploymentId);
        super.tearDown();
    }

    private void verifyInitialActivities(ProcessInstance proc) {
        String[] expectedStartActivities = {"state1", "state2", "cancel"};
        Set<String> foundStartActivities = proc.findActiveActivityNames();
        for (String act : expectedStartActivities) {
            assertTrue(foundStartActivities.contains(act));
        }
    }

    public void t2estCompleteJoin() {
        ProcessInstance proc = executionService.startProcessInstanceByKey("ForkTest");
        verifyInitialActivities(proc);

        String state1Id = proc.findActiveExecutionIn("state1").getId();
        proc = executionService.signalExecutionById(state1Id);

        String state2Id = proc.findActiveExecutionIn("state2").getId();
        proc = executionService.signalExecutionById(state2Id);

        assertExecutionEnded(proc.getId());

        //the name of the end activity should now be set on the execution
        ExecutionImpl executionImpl = (ExecutionImpl) proc;
        assertEquals("completed", executionImpl.getActivityName());

        assertEquals("completed", historyService.createHistoryProcessInstanceQuery()
                                                .uniqueResult()
                                                .getEndActivityName());
    }

    public void t2estCancelWithoutJoin() {
        ProcessInstance proc = executionService.startProcessInstanceByKey("ForkTest");
        verifyInitialActivities(proc);

        String driveTruckExecutionId = proc.findActiveExecutionIn("cancel").getId();
        proc = executionService.signalExecutionById(driveTruckExecutionId);

        assertExecutionEnded(proc.getId());

        //the name of the end activity should now be set on the execution
        ExecutionImpl executionImpl = (ExecutionImpl) proc;
        assertEquals("cancelled", executionImpl.getActivityName());

        assertEquals("cancelled", historyService.createHistoryProcessInstanceQuery()
                                                .uniqueResult()
                                                .getEndActivityName());
    }

    public void testForkAndSubProcess() {
        ProcessInstance mainProcess = executionService.startProcessInstanceByKey("sub_process");

        ProcessInstance subProcess = executionService.createProcessInstanceQuery()
            .orderDesc("dbid")
            .page(0, 1)
            .uniqueResult();

        String driveTruckExecutionId = subProcess.findActiveExecutionIn("cancel").getId();
        subProcess = executionService.signalExecutionById(driveTruckExecutionId);

        mainProcess = executionService.findProcessInstanceById(mainProcess.getId());
        assertEquals("A", mainProcess.findActiveActivityNames().iterator().next());
    }

    public static class StartEventListener implements EventListener {
        public void notify(EventListenerExecution execution) {
            execution.setVariable("result", "to A");
        }
    }

}

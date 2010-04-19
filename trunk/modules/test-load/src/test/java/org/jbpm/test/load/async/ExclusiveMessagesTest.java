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
package org.jbpm.test.load.async;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.jbpm.api.Execution;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.cmd.StartProcessInstanceCmd;
import org.jbpm.pvm.internal.job.CommandMessage;
import org.jbpm.pvm.internal.jobexecutor.JobExecutor;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;
import org.jbpm.pvm.internal.session.MessageSession;

/**
 * @author Tom Baeyens
 */
public class ExclusiveMessagesTest extends JobExecutorTestCase {

  static Map<String, Set<Long>> exclusiveThreadIds;

  static int nbrOfTestMessagesPerExecution = 5;
  static int nbrOfTestExecutions = 20;

  public void setUp() throws Exception {
    super.setUp();

    exclusiveThreadIds = new HashMap<String, Set<Long>>();
  }

  public void testDecisionMessageProcessing() {
    insertDecisionTestMessages();

    JobExecutor jobExecutor = processEngine.get(JobExecutor.class);
    jobExecutor.start();
    try {

      waitTillNoMoreMessages();

    } finally {
      jobExecutor.stop(true);
    }

    commandService.execute(new Command<Object>() {

      public Object execute(Environment environment) throws Exception {
        // exclusiveMessageIds maps execution keys to a set of thread ids.
        // the idea is that for each execution, all the exclusive jobs will
        // be executed by 1 thread sequentially.

        for (int i = 0; i < nbrOfTestExecutions; i++) {
          String executionKey = "execution-" + i;
          Set<Long> threadIds = exclusiveThreadIds.get(executionKey);
          assertNotNull("no thread id set for " + executionKey + " in: " + exclusiveThreadIds, threadIds);
          assertEquals("exclusive messages for " + executionKey + " have been executed by multiple threads: " + threadIds, 1, threadIds.size());
        }
        return null;
      }
    });
  }

  public static class WaitState implements ExternalActivityBehaviour {

    private static final long serialVersionUID = 1L;

    public void execute(ActivityExecution execution) throws Exception {
      execution.waitForSignal();
    }
    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
      execution.take(signalName);
    }
  }

  public void insertDecisionTestMessages() {
    commandService.execute(new Command<Object>() {
      public Object execute(Environment environment) throws Exception {
        ProcessDefinitionImpl processDefinition = (ProcessDefinitionImpl) ProcessDefinitionBuilder
        .startProcess("excl")
          .startActivity("wait", WaitState.class)
            .initial()
          .endActivity()
        .endProcess();
        processDefinition.setId("excl:1");
        
        Session session = environment.get(Session.class);
        session.save(processDefinition);
        return null;
      }
    });

    commandService.execute(new Command<Object>() {

      public Object execute(Environment environment) throws Exception {
        MessageSession messageSession = environment.get(MessageSession.class);
        for (int i = 0; i < nbrOfTestExecutions; i++) {
          Execution execution = new StartProcessInstanceCmd("excl:1", null, "execution-" + i).execute(environment);

          for (int j = 0; j < nbrOfTestMessagesPerExecution; j++) {
            CommandMessage exclusiveTestMessage = ExclusiveTestCommand.createMessage(execution);
            messageSession.send(exclusiveTestMessage);
          }
        }
        return null;
      }
    });
  }

}

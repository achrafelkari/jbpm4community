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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.cmd.VoidCommand;
import org.jbpm.test.JbpmTestCase;

/**
 * @author Tom Baeyens
 * @author Alejandro Guizar
 */
public class ExclusiveMessagesTest extends JbpmTestCase {

  // exclusiveThreads maps process instance keys to a set of thread names.
  // the idea is that for each execution, all the exclusive jobs will
  // be executed by one thread sequentially.
  static final Map<String, Set<String>> exclusiveThreads = new HashMap<String, Set<String>>();

  static final int nbrOfTestExecutions = 20;

  public void testExclusiveMessageProcessing() {
    insertExclusiveTestMessages();
    waitTillNoMoreMessages();

    processEngine.execute(new VoidCommand() {

      private static final long serialVersionUID = 1L;

      protected void executeVoid(Environment environment) {
        for (int i = 0; i < nbrOfTestExecutions; i++) {
          String processInstanceKey = Integer.toString(i);
          Set<String> threadNames = exclusiveThreads.get(processInstanceKey);
          assertNotNull("no thread name set for "
            + processInstanceKey
            + " in: "
            + exclusiveThreads, threadNames);
          assertEquals("exclusive messages for "
            + processInstanceKey
            + " have been executed by multiple threads: "
            + threadNames, 1, threadNames.size());
        }
      }
    });
  }

  private void insertExclusiveTestMessages() {
    deployJpdlXmlString("<process name='excl'>"
      + "  <start>"
      + "    <transition to='f'/>"
      + "  </start>"
      + "  <fork name='f'>"
      + "    <on event='end' continue='exclusive' />"
      + "    <transition to='a'/>"
      + "    <transition to='b'/>"
      + "    <transition to='c'/>"
      + "    <transition to='d'/>"
      + "    <transition to='e'/>"
      + "  </fork>"
      + "  <custom name='a' class='"
      + ExclusiveActivity.class.getName()
      + "'/>"
      + "  <custom name='b' class='"
      + ExclusiveActivity.class.getName()
      + "'/>"
      + "  <custom name='c' class='"
      + ExclusiveActivity.class.getName()
      + "'/>"
      + "  <custom name='d' class='"
      + ExclusiveActivity.class.getName()
      + "'/>"
      + "  <custom name='e' class='"
      + ExclusiveActivity.class.getName()
      + "'/>"
      + "</process>");

    for (int i = 0; i < nbrOfTestExecutions; i++) {
      executionService.startProcessInstanceByKey("excl", Integer.toString(i));
    }
  }

  public static class ExclusiveActivity implements ExternalActivityBehaviour {

    private static final long serialVersionUID = 1L;

    public void execute(ActivityExecution execution) throws Exception {
      String processInstanceKey = execution.getProcessInstance().getKey();
      Set<String> threadNames = ExclusiveMessagesTest.exclusiveThreads.get(processInstanceKey);
      if (threadNames == null) {
        threadNames = new HashSet<String>();
        ExclusiveMessagesTest.exclusiveThreads.put(processInstanceKey, threadNames);
      }
      threadNames.add(Thread.currentThread().getName());
    }

    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters)
      throws Exception {
    }
  }
}

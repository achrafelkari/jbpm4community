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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.model.OpenProcessDefinition;

/**
 * @author Tom Baeyens
 */
public class ContinuationTest extends JobExecutorTestCase {

  // private static final Log log = Log.getLog(ContinuationTest.class.getName());

  static Recorder recorder = new Recorder();

  int nbrOfExecutions = 100;

  public void testContinuations() {
    
    try {
      deployProcess();
      startExecutions();
      jobExecutor.start();
      waitTillNoMoreMessages();

    } finally {
      jobExecutor.stop(true);
    }

    List<String> expectedLogs = new ArrayList<String>();
    expectedLogs.add("execute(start)");
    expectedLogs.add("execute(a)");
    expectedLogs.add("execute(b)");
    expectedLogs.add("execute(c)");
    expectedLogs.add("execute(end)");
    
    assertEquals(nbrOfExecutions, recorder.executionEvents.size());
    for (List<String> executionLogs : recorder.executionEvents.values()) {
      assertEquals(expectedLogs, executionLogs);
    }
  }

  public void deployProcess() {
    commandService.execute(new Command<Object>() {
      public Object execute(Environment environment) throws Exception {
        OpenProcessDefinition processDefinition = ProcessDefinitionBuilder.startProcess("continuations")
          .key("continuations")
          .startActivity("start", AutomaticActivity.class)
            .initial()
            .asyncExecute()
            .transition("a")
          .endActivity()
          .startActivity("a", AutomaticActivity.class)
            .asyncExecute()
            .transition("b")
          .endActivity()
          .startActivity("b", AutomaticActivity.class)
            .asyncExecute()
            .transition("c")
          .endActivity()
          .startActivity("c", AutomaticActivity.class)
            .asyncExecute()
            .transition("end")
          .endActivity()
          .startActivity("end", WaitState.class)
          .endActivity()
        .endProcess();
        
        Session session = environment.get(Session.class);
        session.save(processDefinition);
        return null;
      }
    });
  }

  public void startExecutions() {
    for (int i = 0; i < nbrOfExecutions; i++) {
      executionService.startProcessInstanceByKey("continuations");
    }
  }
}

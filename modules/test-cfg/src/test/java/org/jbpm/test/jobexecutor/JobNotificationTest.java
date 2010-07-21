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
package org.jbpm.test.jobexecutor;

import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.jobexecutor.JobAdditionNotifier;
import org.jbpm.pvm.internal.jobexecutor.JobExecutor;
import org.jbpm.test.JbpmCustomCfgTestCase;

/**
 * Merge job notification booleans.
 * 
 * @author Alejandro Guizar
 * @see <a href="https://jira.jboss.org/browse/JBPM-1453">JBPM-1453</a>
 */
public class JobNotificationTest extends JbpmCustomCfgTestCase {

  static int notifications;

  public void testJobNotification() {
    JobExecutor jobExecutor = processEngine.get(JobExecutor.class);
    assert jobExecutor.getNbrOfThreads() > 1 : jobExecutor.getNbrOfThreads();

    deployJpdlXmlString("<process name='JobNotification'>"
      + "  <on event='timeout'>"
      + "    <timer duedate='1 second'/>"
      + "    <script expr='do nothing' />"
      + "  </on>"
      + "  <start>"
      + "    <transition to='f' />"
      + "  </start>"
      + "  <fork name='f'>"
      + "    <transition name='a' to='a' continue='async' />"
      + "    <transition name='b' to='b' continue='async' />"
      + "  </fork>"
      + "  <state name='a' />"
      + "  <state name='b' />"
      + "</process>");

    jobExecutor.start();
    try {
      String processInstanceId = executionService.startProcessInstanceByKey("JobNotification")
        .getId();
      waitTillNoMoreMessages();
      assertActivitiesActive(processInstanceId, "a", "b");
    }
    finally {
      jobExecutor.stop(true);
    }

    GetEnvironmentObject<JobAdditionNotifier> command = new GetEnvironmentObject<JobAdditionNotifier>(JobAdditionNotifier.class);
    JobAdditionNotifier notifier = processEngine.execute(command);
    assertNotSame(notifier, processEngine.execute(command));
  }

  public static class Executor extends JobExecutor {
    private static final long serialVersionUID = 1L;

    @Override
    public void jobWasAdded() {
      super.jobWasAdded();
      notifications++;
    }
  }

  private static class GetEnvironmentObject<T> implements Command<T> {
    private final Class<T> type;
    private static final long serialVersionUID = 1L;

    GetEnvironmentObject(Class<T> type) {
      this.type = type;
    }

    public T execute(Environment environment) throws Exception {
      return environment.get(type);
    }
  }
}

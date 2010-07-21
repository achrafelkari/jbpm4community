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

import java.util.Arrays;
import java.util.List;

import org.jbpm.test.JbpmTestCase;

/**
 * @author Tom Baeyens
 */
public class ContinuationTest extends JbpmTestCase {

  static final Recorder recorder = new Recorder();
  static final int nbrOfExecutions = 100;

  public void testContinuations() {
    deployProcess();
    startExecutions();
    waitTillNoMoreMessages();

    assertEquals(nbrOfExecutions, recorder.executionEvents.size());
    List<String> expectedLogs = Arrays.asList("execute(a)", "execute(b)", "execute(c)", "execute(end)");
    for (List<String> executionLogs : recorder.executionEvents.values()) {
      assertEquals(expectedLogs, executionLogs);
    }
  }

  void deployProcess() {
    deployJpdlXmlString("<process name='continuations'>"
      + "<start>"
      + "  <transition to='a'/>"
      + "</start>"
      + "<custom name='a' class='"
      + AutomaticActivity.class.getName()
      + "' continue='async'>"
      + "  <transition to='b'/>"
      + "</custom>"
      + "<custom name='b' class='"
      + AutomaticActivity.class.getName()
      + "' continue='async'>"
      + "  <transition to='c'/>"
      + "</custom>"
      + "<custom name='c' class='"
      + AutomaticActivity.class.getName()
      + "' continue='async'>"
      + "  <transition to='end'/>"
      + "</custom>"
      + "<custom name='end' class='"
      + WaitState.class.getName()
      + "'/>"
      + "</process>");
  }

  void startExecutions() {
    for (int i = 0; i < nbrOfExecutions; i++) {
      executionService.startProcessInstanceByKey("continuations");
    }
  }
}

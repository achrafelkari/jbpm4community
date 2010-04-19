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
package org.jbpm.test.historysessionchain;

import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmCustomCfgTestCase;

/**
 * Simple test for the <history-session-chain> construct.
 * Currently in this test, only the delegation to the custom
 * history event listeners is tested.
 * 
 * @author Joram Barrez
 */
public class HistorySessionChainTest extends JbpmCustomCfgTestCase {
  
  private static final String TEST_PROCESS =
    "<process name='testProcess'>" +
    "  <start>" +
    "    <transition to='wait' />" +
    "  </start>" +
    "  <state name='wait'>" +
    "    <transition to='theEnd' />" +
    "  </state>" +
    "  <end name='theEnd' />" +
    "</process>";
  
  public void testCustomHistoryEventListeners() {
    deployJpdlXmlString(TEST_PROCESS);
    ProcessInstance pi = executionService.startProcessInstanceByKey("testProcess");
    
    // After process start, the ProcessStartListener should have been notified
    assertEquals(1, DummyProcessStartListener.nrOfProcessesStarted);
    assertEquals(0, DummyProcessEndListener.nrOfProcessesEnded);
    
    // After process end the ProcessEndListener should have been notified
    executionService.signalExecutionById(pi.findActiveExecutionIn("wait").getId());
    assertProcessInstanceEnded(pi);
    assertEquals(1, DummyProcessStartListener.nrOfProcessesStarted);
    assertEquals(1, DummyProcessEndListener.nrOfProcessesEnded);
  }

}

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

import org.jbpm.api.Execution;
import org.jbpm.test.JbpmTestCase;

/**
 * Test case for basic functionality of Join activity.
 * 
 * @author jbarrez
 */
public class JoinTest extends JbpmTestCase {
  
  
  public void testMultiplicityLessThanIncomingTransitions() {
    deployJpdlXmlString(
            "<process name='multiplicityProcess'>" +
            "  <start>" +
            "    <transition to='theFork' />" +
            "  </start>" +
            "  <fork name='theFork'>" +
            "    <transition to='stateOne' />" +
            "    <transition to='stateTwo' />" +
            "    <transition to='stateThree' />" +
            "  </fork>" +
            "  <state name='stateOne'>" +
            "    <transition to='theJoin' />" +
            "  </state> " +
            "  <state name='stateTwo'>" +
            "    <transition to='theJoin' />" +
            "  </state> " +
            "  <state name='stateThree'>" +
            "    <transition to='theJoin' />" +
            "  </state> " +
            "  <join name='theJoin' multiplicity='2'>" +
            "    <transition to='end' />" +
            "  </join>" +
            "  <end name='end' />" +
            "</process>"
          );
    String processInstanceId = executionService.startProcessInstanceByKey("multiplicityProcess").getId();
    assertActivitiesActive(processInstanceId, "stateOne", "stateTwo", "stateThree");
    
    // Signalling 'stateOne' will should not end the process instance 
    Execution executionInStateOne = executionService.findExecutionById(processInstanceId)
                                                    .findActiveExecutionIn("stateOne");
    executionService.signalExecutionById(executionInStateOne.getId());
    assertProcessInstanceActive(processInstanceId);
    
    // Signalling 'stateTwo' should end the process instance
    Execution executionInStateTwo = executionService.findExecutionById(processInstanceId)
                                                    .findActiveExecutionIn("stateTwo");
    executionService.signalExecutionById(executionInStateTwo.getId());
    assertProcessInstanceEnded(processInstanceId);
  }

  public void testNoMultiplicity() {
    deployJpdlXmlString(
            "<process name='multiplicityProcess'>" +
            "  <start>" +
            "    <transition to='theFork' />" +
            "  </start>" +
            "  <fork name='theFork'>" +
            "    <transition to='stateOne' />" +
            "    <transition to='stateTwo' />" +
            "    <transition to='stateThree' />" +
            "  </fork>" +
            "  <state name='stateOne'>" +
            "    <transition to='theJoin' />" +
            "  </state> " +
            "  <state name='stateTwo'>" +
            "    <transition to='theJoin' />" +
            "  </state> " +
            "  <state name='stateThree'>" +
            "    <transition to='theJoin' />" +
            "  </state> " +
            "  <join name='theJoin'>" +
            "    <transition to='end' />" +
            "  </join>" +
            "  <end name='end' />" +
            "</process>"
          );
    
    String processInstanceId = executionService.startProcessInstanceByKey("multiplicityProcess").getId();
    assertActivitiesActive(processInstanceId, "stateOne", "stateTwo", "stateThree");
    
    executionService.signalExecutionById(executionService.findExecutionById(processInstanceId).findActiveExecutionIn("stateOne").getId());
    assertProcessInstanceActive(processInstanceId);
    executionService.signalExecutionById(executionService.findExecutionById(processInstanceId).findActiveExecutionIn("stateTwo").getId());
    assertProcessInstanceActive(processInstanceId);
    executionService.signalExecutionById(executionService.findExecutionById(processInstanceId).findActiveExecutionIn("stateThree").getId());
    assertProcessInstanceEnded(processInstanceId);
  }
  
}

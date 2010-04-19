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

import java.util.HashMap;
import java.util.Map;

import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class GroupBasicsTest extends JbpmTestCase {

  public void testSimplestGroup() {
    deployJpdlXmlString(
      "<process name='Group'>" +
      "  <start>" +
      "    <transition to='group' />" +
      "  </start>" +
      "  <group name='group'>" +
      "    <start>" +
      "      <transition to='a' />" +
      "    </start>" +
      "    <state name='a'>" +
      "      <transition to='done' />" +
      "    </state>" +
      "    <end name='done' />" +
      "    <transition to='end' />" +
      "  </group>" +
      "  <end name='end' />" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("Group");
    assertTrue(processInstance.isActive("a"));
    
    processInstance = executionService.signalExecutionById(processInstance.getId());
    assertTrue(processInstance.isEnded());
  }

  public void testGroupWithoutStartActivity() {
    deployJpdlXmlString(
      "<process name='Group'>" +
      "  <start>" +
      "    <transition to='group' />" +
      "  </start>" +
      "  <group name='group'>" +
      "    <state name='a'>" +
      "      <transition to='done' />" +
      "    </state>" +
      "    <end name='done' />" +
      "    <transition to='end' />" +
      "  </group>" +
      "  <end name='end' />" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("Group");
    assertTrue(processInstance.isActive("a"));
    
    processInstance = executionService.signalExecutionById(processInstance.getId());
    assertTrue(processInstance.isEnded());
  }

  public void testGroupMultipleEntryStartActivities() {
    deployJpdlXmlString(
      "<process name='Group'>" +
      "  <start>" +
      "    <transition to='choose' />" +
      "  </start>" +
      "  <decision name='choose' expr='#{theWayToGo}'>" +
      "    <transition name='left' to='left' />" +
      "    <transition name='right' to='right' />" +
      "  </decision>" +
      "  <group name='group'>" +
      "    <start name='left'>" +
      "      <transition to='a' />" +
      "    </start>" +
      "    <start name='right'>" +
      "      <transition to='b' />" +
      "    </start>" +
      "    <state name='a'>" +
      "      <transition to='done' />" +
      "    </state>" +
      "    <state name='b'>" +
      "      <transition to='done' />" +
      "    </state>" +
      "    <end name='done' />" +
      "    <transition to='end' />" +
      "  </group>" +
      "  <end name='end' />" +
      "</process>"
    );

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("theWayToGo", "left");
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("Group", variables);
    assertTrue(processInstance.isActive("a"));
    
    processInstance = executionService.signalExecutionById(processInstance.getId());
    assertTrue(processInstance.isEnded());

    variables.put("theWayToGo", "right");
    processInstance = executionService.startProcessInstanceByKey("Group", variables);
    assertTrue(processInstance.isActive("b"));
    
    processInstance = executionService.signalExecutionById(processInstance.getId());
    assertTrue(processInstance.isEnded());
  }

  public void testGroupDirectEntryTransition() {
    deployJpdlXmlString(
      "<process name='Group'>" +
      "  <start>" +
      "    <transition to='direct' />" +
      "  </start>" +
      "  <group name='group'>" +
      "    <start name='direct'>" +
      "      <transition to='a' />" +
      "    </start>" +
      "    <state name='a'>" +
      "      <transition to='done' />" +
      "    </state>" +
      "    <end name='done' />" +
      "    <transition to='end' />" +
      "  </group>" +
      "  <end name='end' />" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("Group");
    assertTrue(processInstance.isActive("a"));
    
    processInstance = executionService.signalExecutionById(processInstance.getId());
    assertTrue(processInstance.isEnded());
  }

  public void testGroupWithoutEndActivity() {
    deployJpdlXmlString(
      "<process name='Group'>" +
      "  <start>" +
      "    <transition to='group' />" +
      "  </start>" +
      "  <group name='group'>" +
      "    <start>" +
      "      <transition to='a' />" +
      "    </start>" +
      "    <state name='a' />" +
      "    <transition to='end' />" +
      "  </group>" +
      "  <end name='end' />" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("Group");
    assertTrue(processInstance.isActive("a"));
   
    processInstance = executionService.signalExecutionById(processInstance.getId());
    assertTrue(processInstance.isEnded());
  }

  public void testGroupMultipleExitEndActivities() {
    deployJpdlXmlString(
      "<process name='Group'>" +
      "  <start>" +
      "    <transition to='group' />" +
      "  </start>" +
      "  <group name='group'>" +
      "    <start>" +
      "      <transition to='a' />" +
      "    </start>" +
      "    <state name='a'>" +
      "      <transition name='a' to='exitA' />" +
      "      <transition name='b' to='exitB' />" +
      "    </state>" +
      "    <end name='exitA'>" +
      "      <transition to='endA' />" +
      "    </end>" +
      "    <end name='exitB'>" +
      "      <transition to='endB' />" +
      "    </end>" +
      "  </group>" +
      "  <end name='endA' />" +
      "  <end name='endB' />" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("Group");
    assertTrue(processInstance.isActive("a"));
    
    processInstance = executionService.signalExecutionById(processInstance.getId(), "a");
    assertTrue(processInstance.isEnded());
    
    processInstance = executionService.startProcessInstanceByKey("Group");
    assertTrue(processInstance.isActive("a"));
    
    processInstance = executionService.signalExecutionById(processInstance.getId(), "b");
    assertTrue(processInstance.isEnded());
  }

  public void testGroupDirectExitTransition() {
    deployJpdlXmlString(
      "<process name='Group'>" +
      "  <start>" +
      "    <transition to='group' />" +
      "  </start>" +
      "  <group name='group'>" +
      "    <start>" +
      "      <transition to='a' />" +
      "    </start>" +
      "    <state name='a'>" +
      "      <transition to='end' />" +
      "    </state>" +
      "  </group>" +
      "  <end name='end' />" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("Group");
    assertTrue(processInstance.isActive("a"));
    
    processInstance = executionService.signalExecutionById(processInstance.getId());
    assertTrue(processInstance.isEnded());
  }
}

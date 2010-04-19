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
package org.jbpm.test.execution;

import org.jbpm.api.Execution;
import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.activities.PassThroughActivity;


/**
 * @author Tom Baeyens
 */
public class ConcurrentEndTest extends JbpmTestCase {

  public void testConcurrentEndScenario1() {
    deployJpdlXmlString(
      "<process name='ConcurrentEnd'>" +
      "  <start>" +
      "    <transition to='f' />" +
      "  </start>" +
      "  <fork name='f'>" +
      "    <transition to='a' />" +
      "    <transition to='end' />" +
      "  </fork>" +
      "  <state name='a' />" +
      "  <end name='end' />" +
      "</process>"
    );
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ConcurrentEnd");
    assertEquals(Execution.STATE_ENDED, processInstance.getState());
  }

  public void testConcurrentEndScenario2() {
    deployJpdlXmlString(
      "<process name='ConcurrentEnd'>" +
      "  <start>" +
      "    <transition to='f' />" +
      "  </start>" +
      "  <fork name='f'>" +
      "    <transition to='end' />" +
      "    <transition to='a' />" +
      "  </fork>" +
      "  <state name='a' />" +
      "  <end name='end' />" +
      "</process>"
    );
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ConcurrentEnd");
    assertEquals(Execution.STATE_ENDED, processInstance.getState());
  }
  
  public void testConcurrentEndScenario3() {
    deployJpdlXmlString(
      "<process name='ConcurrentEnd'>" +
      " <start>" +
      " <transition to='f' />" +
      " </start>" +
      " <fork name='f'>" +
      " <transition to='a' />" +
      " <transition to='b' />" +
      " </fork>" +
      " <custom name='a' class='"+PassThroughActivity.class.getName()+"'>" +
      " <transition to='join' />" +
      " </custom>" +
      " <custom name='b' class='"+PassThroughActivity.class.getName()+"'>" +
      " <transition to='join' />" +
      " </custom>" +
      " <join name='join'>" +
      " <transition to='end' />" +
      " </join>" +
      " <end name='end' />" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ConcurrentEnd");
    assertEquals(Execution.STATE_ENDED, processInstance.getState());
  } 
}

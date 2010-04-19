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
/**
 * 
 */
package org.jbpm.test.activities;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmTestCase;


/**
 * Test case for fork/join constructs in one transaction
 * 
 * @author Joram Barrez
 */
public class ForkJoinInSameTransactionTest extends JbpmTestCase {
  
  /**
   * Test for JBPM-2286
   */
  public void testForkToJoinInOneTransaction() {
    deployJpdlXmlString(
            "<process name='ForkJoinInOneTransaction'>" +
            "  <start>" +
            "    <transition to='theFork' />" +
            "  </start>" +
            "  <fork name='theFork'>" +
            "    <transition to='theJoin' />" +
            "    <transition to='theJoin' />" +
            "  </fork>" +
            "  <join name='theJoin'>" +
            "    <transition to='end' />" +
            "  </join>" +
            "  <end name='end' />" +
            "</process>"
          );
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ForkJoinInOneTransaction");
    assertProcessInstanceEnded(processInstance);
  }
  
  /**
   * Test for JBPM-2286
   */
  public void testForkToJoinAfterStateInOneTransaction() {
    deployJpdlXmlString(
            "<process name='ForkJoinInOneTransaction'>" +
            "  <start>" +
            "    <transition to='wait' />" +
            "  </start>" +
            "  <state name='wait'>" +
            "    <transition name='go on' to='theFork' />" +
            "  </state>" +
            "  <fork name='theFork'>" +
            "    <transition to='theJoin' />" +
            "    <transition to='theJoin' />" +
            "  </fork>" +
            "  <join name='theJoin'>" +
            "    <transition to='end' />" +
            "  </join>" +
            "  <end name='end' />" +
            "</process>"
          );
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ForkJoinInOneTransaction");
    executionService.signalExecutionById(processInstance.getId(), "go on");
    assertProcessInstanceEnded(processInstance);
  }
  
  /**
   * Test for JBPM-2286
   */
  public void testForkToJoinWithActivitiesInOneTransaction() {
    deployJpdlXmlString(
            "<process name='ForkJoinInOneTransaction'>" +
            "  <start>" +
            "    <transition to='theFork' />" +
            "  </start>" +
            "  <fork name='theFork'>" +
            "    <transition to='left' />" +
            "    <transition to='right' />" +
            "  </fork>" +
            "  <custom name='left' class='org.jbpm.test.activities.PassThroughActivity' >" +
            "    <transition to='theJoin' />" +
            "  </custom>" + 
            "  <custom name='right' class='org.jbpm.test.activities.PassThroughActivity' >" +
            "    <transition to='theJoin' />" +
            "  </custom>" + 
            "  <join name='theJoin'>" +
            "    <transition to='end' />" +
            "  </join>" +
            "  <end name='end' />" +
            "</process>"
          );

    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ForkJoinInOneTransaction");
    assertProcessInstanceEnded(processInstance);
  }
  
  /**
   * Test for JBPM-2286
   */
  public void testForkToJoinInLoopInOneTransaction() {
    deployJpdlXmlString(
            "<process name='ForkJoinInOneTransaction'>" +
            "  <start>" +
            "    <transition to='checkLoop' />" +
            "  </start>" +
            "  <decision name='checkLoop'>" +
            "    <transition to='end'>" +
            "      <condition expr='#{testVar &gt; 10}' />" +
            "    </transition>" +
            "    <transition to='theFork' />" +
            "  </decision>" +
            "  <fork name='theFork'>" +
            "    <transition to='left' />" +
            "    <transition to='right' />" +
            "  </fork>" +
            "  <custom name='left' class='org.jbpm.test.activities.PassThroughActivity' >" +
            "    <transition to='theJoin' />" +
            "  </custom>" + 
            "  <custom name='right' class='org.jbpm.test.activities.PassThroughActivity' >" +
            "    <transition to='theJoin' />" +
            "  </custom>" + 
            "  <join name='theJoin'>" +
            "    <transition to='incrementLoopVar' />" +
            "  </join>" +
            "  <custom name='incrementLoopVar' class='org.jbpm.test.activities.IncrementVariableActivity' >" +
            "    <transition to='checkLoop' />" +
            "  </custom>" + 
            "  <end name='end' />" +
            "</process>"
          );

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("testVar", new Integer(0));
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ForkJoinInOneTransaction", vars);
    assertProcessInstanceEnded(processInstance);
  }

}

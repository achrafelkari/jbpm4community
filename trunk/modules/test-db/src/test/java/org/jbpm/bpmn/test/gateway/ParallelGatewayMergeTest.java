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
package org.jbpm.bpmn.test.gateway;

import java.util.Arrays;

import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.assertion.CollectionAssertions;

/**
 * Test case for the merging behaviour of the parallel gateway.
 * 
 * @author Tom Baeyens
 */
public class ParallelGatewayMergeTest extends JbpmTestCase {
  
  /* Test process with parallel gateway that has 3 incoming and 2 outgoing sequence flow */
  private static final String TEST_SIMPLE_MERGE_PROCESS = 
    "<definitions>" +
    "  <process id='simpleMerge'>" +
    "    <startEvent id='theStart' />" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='parallelGateway' />" +
    "    <sequenceFlow id='flow2' sourceRef='theStart' targetRef='parallelGateway' />" +
    "    <sequenceFlow id='flow3' sourceRef='theStart' targetRef='parallelGateway' />" +
    "    <parallelGateway id='parallelGateway' />" +
    "    <sequenceFlow id='flow4' sourceRef='parallelGateway' targetRef='wait1' />" +
    "    <sequenceFlow id='flow5' sourceRef='parallelGateway' targetRef='wait2' />" +
    "    <receiveTask id='wait1' />" +
    "    <sequenceFlow id='flow6' sourceRef='wait1' targetRef='theEnd' />" +
    "    <receiveTask id='wait2' />" +
    "    <sequenceFlow id='flow7' sourceRef='wait2' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' >" +
    "      <terminateEventDefinition/>" +
    "    </endEvent>" +
    "  </process>" +
    "</definitions>";
  
  /* 
   * Test process with parallel gateway that has three outgoing sequence flow.
   * Two of those sequence flow are merged before the resulting sequence flow is merged
   * with the one remaining sequence flow
   */
  private static final String TEST_NESTED_MERGE_PROCESS = 
    "<definitions>" +
    "  <process id='nestedMerge'>" +
    "    <startEvent id='theStart' />" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='outerFork' />" +
    "    <parallelGateway id='outerFork' />" +
    "    <sequenceFlow id='flow2' sourceRef='outerFork' targetRef='innerJoin' />" +
    "    <sequenceFlow id='flow3' sourceRef='outerFork' targetRef='innerJoin' />" +
    "    <sequenceFlow id='flow4' sourceRef='outerFork' targetRef='wait1' />" +
    "    <receiveTask id='wait1' />" +
    "    <sequenceFlow id='flow5' sourceRef='wait1' targetRef='outerJoin' />" +
    "    <parallelGateway id='innerJoin' />" +
    "    <sequenceFlow id='flow6' sourceRef='innerJoin' targetRef='wait2' />" +
    "    <receiveTask id='wait2' />" +
    "    <sequenceFlow id='flow7' sourceRef='wait2' targetRef='outerJoin' />" +
    "    <parallelGateway id='outerJoin' />" +
    "    <sequenceFlow id='flow8' sourceRef='outerJoin' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' />" +
    "  </process>" +
    "</definitions>";
  
  /* 
   * Test process with parallel gateway that has three outgoing sequence flow.
   * Two of those sequence flow are merged before and split again into three sequence flow.
   * The one remaining sequence flow, will at last be merged with these three sequence flow,
   * which means that the outer join has 4 incoming sequence flow.
   */
  private static final String TEST_NESTED_MERGE_PROCESS_2 = 
    "<definitions>" +
    "  <process id='nestedMerge2' >" +
    "    <startEvent id='theStart' />" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='outerFork' />" +
    "    <parallelGateway id='outerFork' />" +
    "    <sequenceFlow id='flow2' sourceRef='outerFork' targetRef='wait' />" +
    "    <sequenceFlow id='flow3' sourceRef='outerFork' targetRef='innerJoin' />" +
    "    <sequenceFlow id='flow4' sourceRef='outerFork' targetRef='innerJoin' />" +
    "    <receiveTask id='wait' />" +
    "    <sequenceFlow id='flow5' sourceRef='wait' targetRef='outerJoin' />" +
    "    <parallelGateway id='innerJoin' />" +
    "    <sequenceFlow id='flow6' sourceRef='innerJoin' targetRef='outerJoin' />" +
    "    <sequenceFlow id='flow7' sourceRef='innerJoin' targetRef='outerJoin' />" +
    "    <sequenceFlow id='flow8' sourceRef='innerJoin' targetRef='outerJoin' />" +
    "    <parallelGateway id='outerJoin' />" +
    "    <sequenceFlow id='flow10' sourceRef='outerJoin' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' />" +
    "  </process>" +
    "</definitions>";

  
  public void testSimpleParallelMerge() {
    deployBpmn2XmlString(TEST_SIMPLE_MERGE_PROCESS);
    ProcessInstance pi = executionService.startProcessInstanceByKey("simpleMerge");
    pi.findActiveActivityNames();
    CollectionAssertions.assertContainsSameElements(pi.findActiveActivityNames(), Arrays.asList("wait1", "wait2"));
  }
  
  public void testNestedParallelMerge() {
    deployBpmn2XmlString(TEST_NESTED_MERGE_PROCESS);
    
    ProcessInstance pi = executionService.startProcessInstanceByKey("nestedMerge");
    CollectionAssertions.assertContainsSameElements(pi.findActiveActivityNames(), Arrays.asList("wait1", "wait2"));
    
    executionService.signalExecutionById(pi.findActiveExecutionIn("wait1").getId());
    executionService.signalExecutionById(pi.findActiveExecutionIn("wait2").getId());
    assertProcessInstanceEnded(pi);
  }
  
  public void testNestedParallelMerge2() {
    deployBpmn2XmlString(TEST_NESTED_MERGE_PROCESS_2);
    
    ProcessInstance pi = executionService.startProcessInstanceByKey("nestedMerge2");
    CollectionAssertions.assertContainsSameElements(pi.findActiveActivityNames(), Arrays.asList("wait"));
    executionService.signalExecutionById(pi.findActiveExecutionIn("wait").getId());
    assertProcessInstanceEnded(pi);
    
  }

}

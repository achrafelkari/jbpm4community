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

import java.util.HashMap;
import java.util.Map;

import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.assertion.CollectionAssertions;

/**
 * @author Joram Barrez
 */
public class InclusiveGatewayTest extends JbpmTestCase {
  
  /*
   * Process with a simple inclusive split: 3 outgoing sequence flow (incl. 1 that always will be taken)
   */
  private static final String SIMPLE_SPLIT =
    "<definitions xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
    "  <process id='simpleInclusiveSplit' >" +
    "    <startEvent id='theStart' />" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='inclusiveGateway' />" +
    "    <inclusiveGateway id='inclusiveGateway' />" +
    "    <sequenceFlow id='flow2' sourceRef='inclusiveGateway' targetRef='wait1' >" +
    "      <conditionExpression xsi:type='tFormalExpression'>${var &gt; 5}</conditionExpression>" +
    "    </sequenceFlow>" +
    "    <sequenceFlow id='flow3' sourceRef='inclusiveGateway' targetRef='wait2' />" + // wait2 will always be reached since it has no condition
    "    <sequenceFlow id='flow4' sourceRef='inclusiveGateway' targetRef='wait3' >" +
    "      <conditionExpression xsi:type='tFormalExpression'>${var &gt; 10}</conditionExpression>" +
    "    </sequenceFlow>" +
    "    <receiveTask id='wait1' />" +
    "    <sequenceFlow id='flow5' sourceRef='wait1' targetRef='theEnd' />" +
    "    <receiveTask id='wait2' />" +
    "    <sequenceFlow id='flow6' sourceRef='wait2' targetRef='theEnd' />" +
    "    <receiveTask id='wait3' />" +
    "    <sequenceFlow id='flow7' sourceRef='wait3' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' >" +
    "      <terminateEventDefinition/>" +
    "    </endEvent>" +
    "  </process>" +
    "</definitions>";
  
  /* 
   * Copy of the SIMPLE_SPLIT_PROCESS, where the 'always' outgoing sequence flow is replaced
   * by a default sequence flow.
   */
  private static final String SIMPLE_SPLIT_WITH_DEFAULT =
    "<definitions xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
    "  <process id='simpleInclusiveSplitWithDefault' >" +
    "    <startEvent id='theStart' />" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='inclusiveGateway' />" +
    "    <inclusiveGateway id='inclusiveGateway' default='flow3' />" +
    "    <sequenceFlow id='flow2' sourceRef='inclusiveGateway' targetRef='wait1' >" +
    "      <conditionExpression xsi:type='tFormalExpression'>${var &gt; 5}</conditionExpression>" +
    "    </sequenceFlow>" +
    "    <sequenceFlow id='flow3' sourceRef='inclusiveGateway' targetRef='wait2' />" +
    "    <sequenceFlow id='flow4' sourceRef='inclusiveGateway' targetRef='wait3' >" +
    "      <conditionExpression xsi:type='tFormalExpression'>${var &gt; 10}</conditionExpression>" +
    "    </sequenceFlow>" +
    "    <receiveTask id='wait1' />" +
    "    <sequenceFlow id='flow5' sourceRef='wait1' targetRef='theEnd' />" +
    "    <receiveTask id='wait2' />" +
    "    <sequenceFlow id='flow6' sourceRef='wait2' targetRef='theEnd' />" +
    "    <receiveTask id='wait3' />" +
    "    <sequenceFlow id='flow7' sourceRef='wait3' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' >" +
    "      <terminateEventDefinition/>" +
    "    </endEvent>" +
    "  </process>" +
    "</definitions>";
  
  /*
   * Simple process testing split and merge behaviour of the inclusive gateway.
   * The first inclusive gateway has 3 outgoing sequence flow, that are merged later by
   * a merging sequence flow.
   */
  private static final String SIMPLE_SPLIT_AND_MERGE =
    "<definitions xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
    "  <process id='simpleSplitAndMerge' >" +
    "    <startEvent id='theStart' />" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='inclusiveSplit' />" +
    "    <inclusiveGateway id='inclusiveSplit' default='flow3' />" +
    "    <sequenceFlow id='flow2' sourceRef='inclusiveSplit' targetRef='wait1' >" +
    "      <conditionExpression xsi:type='tFormalExpression'>${var &gt; 5}</conditionExpression>" +
    "    </sequenceFlow>" +
    "    <sequenceFlow id='flow3' sourceRef='inclusiveSplit' targetRef='wait2' />" + 
    "    <sequenceFlow id='flow4' sourceRef='inclusiveSplit' targetRef='wait3' >" +
    "      <conditionExpression xsi:type='tFormalExpression'>${var &gt; 10}</conditionExpression>" +
    "    </sequenceFlow>" +
    "    <receiveTask id='wait1' />" +
    "    <sequenceFlow id='flow5' sourceRef='wait1' targetRef='inclusiveJoin' />" +
    "    <receiveTask id='wait2' />" +
    "    <sequenceFlow id='flow6' sourceRef='wait2' targetRef='inclusiveJoin' />" +
    "    <receiveTask id='wait3' />" +
    "    <sequenceFlow id='flow7' sourceRef='wait3' targetRef='inclusiveJoin' />" +
    "    <inclusiveGateway id='inclusiveJoin' />" +
    "    <sequenceFlow id='flow8' sourceRef='inclusiveJoin' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' />" +
    "  </process>" +
    "</definitions>";
  
  /*
   * See test/resources/nested_inclusive_gateway_test.png 
   */
  private static final String NESTED_INCLUSIVE_SPLIT = 
      "<definitions xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
      "  <process id='nestedInclusiveSplit' >" +
      "    <startEvent id='theStart' />" +
      "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='inclusiveSplit' />" +
      // Inclusive Split
      "    <inclusiveGateway id='inclusiveSplit' />" +
      "    <sequenceFlow id='flow2' sourceRef='inclusiveSplit' targetRef='wait1' >" +
      "      <conditionExpression xsi:type='tFormalExpression'>${var &gt; 5}</conditionExpression>" +
      "    </sequenceFlow>" +
      "    <sequenceFlow id='flow3' sourceRef='inclusiveSplit' targetRef='wait2' />" + 
      "    <sequenceFlow id='flow4' sourceRef='inclusiveSplit' targetRef='wait3' >" +
      "      <conditionExpression xsi:type='tFormalExpression'>${var &gt; 10}</conditionExpression>" +
      "    </sequenceFlow>" +
      // var > 5
      "    <receiveTask id='wait1' />" +
      "    <sequenceFlow id='flow5' sourceRef='wait1' targetRef='wait4' >" +
      "      <conditionExpression xsi:type='tFormalExpression'>${var == 6}</conditionExpression>" +
      "    </sequenceFlow>" +
      "    <sequenceFlow id='flow6a' sourceRef='wait1' targetRef='innerInclusiveSplit' >" +
      "      <conditionExpression xsi:type='tFormalExpression'>${var &gt;= 7}</conditionExpression>" +
      "    </sequenceFlow>" +
      "    <sequenceFlow id='flow6b' sourceRef='wait1' targetRef='innerInclusiveSplit' >" +
      "      <conditionExpression xsi:type='tFormalExpression'>${var &gt;= 9}</conditionExpression>" +
      "    </sequenceFlow>" +
      // var == 6
      "    <receiveTask id='wait4' />" +
      "    <sequenceFlow id='flow7' sourceRef='wait4' targetRef='inclusiveMerge' />" +
      // var >= 7 -> nested inclusive split
      "    <inclusiveGateway id='innerInclusiveSplit' default='flow10'/>" +
      "    <sequenceFlow id='flow8' sourceRef='innerInclusiveSplit' targetRef='wait5' >" +
      "      <conditionExpression xsi:type='tFormalExpression'>${var &gt;= 8}</conditionExpression>" +
      "    </sequenceFlow>" +
      "    <sequenceFlow id='flow9' sourceRef='innerInclusiveSplit' targetRef='wait6' >" +
      "      <conditionExpression xsi:type='tFormalExpression'>${var &gt;= 9}</conditionExpression>" +
      "    </sequenceFlow>" +
      "    <sequenceFlow id='flow10' sourceRef='innerInclusiveSplit' targetRef='wait7' />" +
      "    <receiveTask id='wait5' />" +
      "    <sequenceFlow id='flow11' sourceRef='wait5' targetRef='inclusiveMerge' />" +
      "    <receiveTask id='wait6' />" +
      "    <sequenceFlow id='flow12' sourceRef='wait6' targetRef='inclusiveMerge' />" +
      "    <receiveTask id='wait7' />" +
      "    <sequenceFlow id='flow13' sourceRef='wait7' targetRef='inclusiveMerge' />" +
      // 'always' sequence flow on outer inclusive split
      "    <receiveTask id='wait2' />" +
      "    <sequenceFlow id='flow14' sourceRef='wait2' targetRef='inclusiveMerge' />" +
      "    <receiveTask id='wait3' />" +
      // var > 10
      "    <sequenceFlow id='flow15' sourceRef='wait3' targetRef='inclusiveMerge' />" +
      "    <inclusiveGateway id='inclusiveMerge' />" +
      "    <sequenceFlow id='flow16' sourceRef='inclusiveMerge' targetRef='theEnd' />" +
      "    <endEvent id='theEnd' />" +
      "  </process>" +
      "</definitions>";
  
  public void testSimpleSplit() {
    deployBpmn2XmlString(SIMPLE_SPLIT);
    
    // A var value < 5 will trigger all outgoing sequence flow
    startAndVerifySimpleSplitProcess("simpleInclusiveSplit", 15, "wait1", "wait2", "wait3");
    
    // A var value 0 < x < 10 will trigger only two sequence flow
    startAndVerifySimpleSplitProcess("simpleInclusiveSplit", 7, "wait1", "wait2");
    
    // A var value < 5 trigger only one sequence flow (the one without a condition)
    startAndVerifySimpleSplitProcess("simpleInclusiveSplit", 3, "wait2");
  }
  
  public void testSimpleSplitWithDefault() {
    deployBpmn2XmlString(SIMPLE_SPLIT_WITH_DEFAULT);
    
    // A var value < 5 will trigger all outgoing sequence flow, but not the default one
    startAndVerifySimpleSplitProcess("simpleInclusiveSplitWithDefault", 15, "wait1", "wait3");
    
    // A var value 0 < x < 10 will trigger only one sequence flow
    startAndVerifySimpleSplitProcess("simpleInclusiveSplitWithDefault", 7, "wait1");
    
    // A var value < 5 trigger only one sequence flow (the default one)
    startAndVerifySimpleSplitProcess("simpleInclusiveSplitWithDefault", 3, "wait2");
  }
  
  public void testSimpleSplitAndMerge() {
    deployBpmn2XmlString(SIMPLE_SPLIT_AND_MERGE);
    
    ProcessInstance pi = startAndVerifySimpleSplitProcess("simpleSplitAndMerge", 17, "wait1", "wait3");
    executionService.signalExecutionById(pi.findActiveExecutionIn("wait1").getId());
    assertProcessInstanceActive(pi);
    executionService.signalExecutionById(pi.findActiveExecutionIn("wait3").getId());
    assertProcessInstanceEnded(pi);
  }
  
  public void testNestedInclusiveSplit() {
    deployBpmn2XmlString(NESTED_INCLUSIVE_SPLIT);
    
    // If var == 6, then the nested inclusive split will not be reached
    // The inclusive merge will then have to merge two incoming sequence flow
    ProcessInstance pi = startAndVerifySimpleSplitProcess("nestedInclusiveSplit", 6, "wait1", "wait2");
    pi = executionService.signalExecutionById(pi.findActiveExecutionIn("wait1").getId());
    assertActivitiesActive(pi.getId(), "wait2", "wait4");
    pi = executionService.signalExecutionById(pi.findActiveExecutionIn("wait2").getId());
    pi = executionService.signalExecutionById(pi.findActiveExecutionIn("wait4").getId());
    assertProcessInstanceEnded(pi);
    
    // If var == 9, the inclusive split will be reached and it will produce 2 outgoing sequence flow.
    // The inclusive merge will have to merge three incoming sequence flow
    pi = startAndVerifySimpleSplitProcess("nestedInclusiveSplit", 9, "wait1", "wait2");
    pi = executionService.signalExecutionById(pi.findActiveExecutionIn("wait1").getId());
    assertActivitiesActive(pi.getId(), "wait2", "wait5", "wait6");
    pi = executionService.signalExecutionById(pi.findActiveExecutionIn("wait5").getId());
    pi = executionService.signalExecutionById(pi.findActiveExecutionIn("wait6").getId());
    pi = executionService.signalExecutionById(pi.findActiveExecutionIn("wait2").getId());
    assertProcessInstanceEnded(pi);
  }
  
  private ProcessInstance startAndVerifySimpleSplitProcess(String processKey, Integer varValue, String ... expectedActivities) {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", varValue);
    ProcessInstance pi = executionService.startProcessInstanceByKey(processKey, vars);
    CollectionAssertions.assertContainsSameElements(pi.findActiveActivityNames(), expectedActivities);
    return pi;
  }
  
}

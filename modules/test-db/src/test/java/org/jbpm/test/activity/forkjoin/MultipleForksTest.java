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
package org.jbpm.test.activity.forkjoin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.assertion.CollectionAssertions;


/**
 * @author Tom Baeyens
 */
public class MultipleForksTest extends JbpmTestCase {

  public void testConcurrencyGraphBased() {
    deployJpdlXmlString(
      "<process name='ConcurrencyGraphBased'>" +
      "  <start>" +
      "    <transition to='fork'/>" +
      "  </start>" +
      "  <fork name='fork'>" +
      "    <transition to='fork2' />" +
      "    <transition to='send invoice' />" +
      "  </fork>" +
      "  <fork name='fork2'>" +
      "    <transition to='load truck'/>" +
      "    <transition to='print shipping documents' />" +
      "  </fork>" +
      "  <state name='send invoice'>" +
      "    <transition to='final join' />" +
      "  </state>" +
      "  <state name='load truck'>" +
      "    <transition to='shipping join' />" +
      "  </state>" +
      "  <state name='print shipping documents'>" +
      "    <transition to='shipping join' />" +
      "  </state>" +
      "  <join name='shipping join'>" +
      "    <transition to='drive truck to destination'/>" +
      "  </join>" +
      "  <state name='drive truck to destination'>" +
      "    <transition to='final join' />" +
      "  </state>" +
      "  <join name='final join'>" +
      "    <transition to='end'/>" +
      "  </join>" +
      "  <end name='end' />" +
      "</process>"
    );
        
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ConcurrencyGraphBased");
    String pid = processInstance.getId();
    
    Set<String> expectedActivityNames = new HashSet<String>();
    expectedActivityNames.add("send invoice");
    expectedActivityNames.add("load truck");
    expectedActivityNames.add("print shipping documents");
    
    assertEquals(expectedActivityNames, processInstance.findActiveActivityNames());
    
    assertNotNull(processInstance.findActiveExecutionIn("send invoice"));
    assertNotNull(processInstance.findActiveExecutionIn("load truck"));
    assertNotNull(processInstance.findActiveExecutionIn("print shipping documents"));
    
    String sendInvoiceExecutionId = processInstance.findActiveExecutionIn("send invoice").getId();
    processInstance = executionService.signalExecutionById(sendInvoiceExecutionId);

    expectedActivityNames.remove("send invoice");
    assertEquals(expectedActivityNames, processInstance.findActiveActivityNames());

    assertNotNull(processInstance.findActiveExecutionIn("load truck"));
    assertNotNull(processInstance.findActiveExecutionIn("print shipping documents"));

    String loadTruckExecutionId = processInstance.findActiveExecutionIn("load truck").getId();
    processInstance = executionService.signalExecutionById(loadTruckExecutionId);

    expectedActivityNames.remove("load truck");
    assertEquals(expectedActivityNames, processInstance.findActiveActivityNames());

    assertNotNull(processInstance.findActiveExecutionIn("print shipping documents"));
    
    String printShippingDocumentsId = processInstance.findActiveExecutionIn("print shipping documents").getId();
    processInstance = executionService.signalExecutionById(printShippingDocumentsId);

    expectedActivityNames.remove("print shipping documents");
    expectedActivityNames.add("drive truck to destination");
    assertEquals(expectedActivityNames, processInstance.findActiveActivityNames());

    assertNotNull(processInstance.findActiveExecutionIn("drive truck to destination"));

    String driveTruckExecutionId = processInstance.findActiveExecutionIn("drive truck to destination").getId();
    processInstance = executionService.signalExecutionById(driveTruckExecutionId);

    assertNull("execution "+pid+" should not exist", executionService.findExecutionById(pid));
  }
  
  // Test for JBPM-2754
  public void testNestedForks() {
    deployJpdlXmlString(
      "<process name='nestedForks'>" +
      "  <start>" +
      "    <transition to='outerFork' />" +
      "  </start>" +
      "  <fork name='outerFork'>" +
      "    <transition to='passthrough1' />" +
      "    <transition to='passthrough2' />" +
      "    <transition to='wait' />" +
      "  </fork>" +
      "  <state name='wait'>" +
      "    <transition to='outerJoin' />" +
      "  </state>" +
      "  <passthrough name='passthrough1'>" +
      "    <transition to='innerJoin' />" +
      "  </passthrough>" +
      "  <passthrough name='passthrough2'>" +
      "    <transition to='innerJoin' />" +
      "  </passthrough>" +
      "  <join name='innerJoin'>" +
      "    <transition to='innerFork' />" +
      "  </join>" +
      "  <fork name='innerFork' >" +
      "    <transition to='passthrough3'/>" +
      "    <transition to='passthrough4'/>" +
      "    <transition to='passthrough5'/>" +
      "  </fork>" +
      "  <passthrough name='passthrough3'>" +
      "    <transition to='outerJoin' />" +
      "  </passthrough>" +
      "  <passthrough name='passthrough4'>" +
      "    <transition to='outerJoin' />" +
      "  </passthrough>" +
      "  <passthrough name='passthrough5'>" +
      "    <transition to='outerJoin' />" +
      "  </passthrough>" +
      "  <join name='outerJoin'>" +
      "    <transition to='theEnd' />" +
      "  </join>" +
      "  <end name='theEnd' />" + 
      "</process>");
    
    ProcessInstance pi = executionService.startProcessInstanceByKey("nestedForks");
    CollectionAssertions.assertContainsSameElements(pi.findActiveActivityNames(), Arrays.asList("wait"));
    executionService.signalExecutionById(pi.findActiveExecutionIn("wait").getId());
    assertProcessInstanceEnded(pi);
  }

}

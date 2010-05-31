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
package org.jbpm.test.activity.subprocess;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.api.ProcessInstance;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.test.JbpmTestCase;


/**
 * Test case for signal of subprocess state activities.
 * 
 * @author Maciej Swiderski
 */
public class SubProcessSignalTest extends JbpmTestCase {
  
  
  
  private static final String SUB_PROCESS_WITH_WAIT_STATE =
    "<process name='SubProcessReview'>" +
    "  <start>" +
    "    <transition to='wait'/>" +
    "  </start>" +
    "  <state name='wait'>" +
    "    <transition name='wait2' to='wait2'/>" +
    "  </state>" +
    "  <state name='wait2'>" +
    "    <transition name='ok' to='ok'/>" +
    "  </state>" +
    "  <end name='ok' />" +
    "</process>";  
  
  
  
  private static final String MAIN_PROCESS_SUB_EL_ID =
    "<process name='mainProcess'>" +
    "  <start>" +
    "    <transition to='review' />" +
    "  </start>" +
    "  <sub-process name='review' sub-process-id='#{dynamic_subprocess}'>" +
    "    <transition name='ok' to='next step'/>" +
    "    <transition name='nok' to='update'/>" +
    "    <transition name='reject' to='close'/>" +
    "  </sub-process>" +
    "  <state name='next step'>" +
    "    <transition name='close' to='close'/>" +
    "  </state>" +
    "  <state name='update'>" +
    "    <transition name='close' to='close'/>" +
    "  </state>" +
    "  <end name='close'/>" +
    "</process>"; 
  
  
  public void testSubProcessWithStateFailure() {
    deployJpdlXmlString(SUB_PROCESS_WITH_WAIT_STATE);
    deployJpdlXmlString(MAIN_PROCESS_SUB_EL_ID);
    
    Map<String, String> vars = new HashMap<String, String>();
    vars.put("dynamic_subprocess", "SubProcessReview-1");
  
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("mainProcess", vars);
   
    try {
      processInstance = executionService.signalExecutionById(processInstance.getId());
      
      fail("Should fail since signal was made on a process with subprocess stil active");
     } catch (Exception e) {
       
       //expected exception since we are signaling main process while sub process is not finished
       assertTrue(e.getMessage().indexOf("has running subprocess") != -1);
       
       // clean up to let other tests execute
       executionService.signalExecutionById(processInstance.getSubProcessInstance().getId());
       executionService.signalExecutionById(processInstance.getSubProcessInstance().getId());
       
       processInstance = executionService.signalExecutionById(processInstance.getId());
       
    }
    
  }
  
  public void testSubProcessWithStateSuccess() {
    deployJpdlXmlString(SUB_PROCESS_WITH_WAIT_STATE);
    deployJpdlXmlString(MAIN_PROCESS_SUB_EL_ID);
    
    Map<String, String> vars = new HashMap<String, String>();
    vars.put("dynamic_subprocess", "SubProcessReview-1");
   
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("mainProcess", vars);
    String subId = processInstance.getSubProcessInstance().getId();
    executionService.signalExecutionById(processInstance.getSubProcessInstance().getId());
    executionService.signalExecutionById(processInstance.getSubProcessInstance().getId());
    
    processInstance = executionService.signalExecutionById(processInstance.getId());
    
    assertProcessInstanceEnded(processInstance); 
    ProcessInstance subProcessInstance = executionService.findProcessInstanceById(subId);
    assertNull(subProcessInstance);
    
  }
  
}

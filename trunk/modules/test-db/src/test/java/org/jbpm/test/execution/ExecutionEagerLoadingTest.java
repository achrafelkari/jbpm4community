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
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class ExecutionEagerLoadingTest extends JbpmTestCase {

  public void testEagerLoading() {
    deployJpdlXmlString(
      "<process name='p'>" +
      "  <start>" +
      "    <transition to='f' />" +
      "  </start>" +
      "  <fork name='f'>" +
      "    <transition to='f1' />" +
      "    <transition to='f2' />" +
      "  </fork>" +
      "  <fork name='f1'>" +
      "    <transition to='s11' />" +
      "    <transition to='s12' />" +
      "  </fork>" +
      "  <fork name='f2'>" +
      "    <transition to='s21' />" +
      "    <transition to='s22' />" +
      "  </fork>" +
      "  <state name ='s11' />" +
      "  <state name ='s12' />" +
      "  <state name ='s21' />" +
      "  <state name ='s22' />" +
      "</process>"
    );
  
    Execution processInstance = executionService.startProcessInstanceByKey("p");
   
    processInstance = executionService.findExecutionById(processInstance.getId());
    
    assertNotNull(processInstance.findActiveExecutionIn("s11"));
    assertNotNull(processInstance.findActiveExecutionIn("s12"));
    assertNotNull(processInstance.findActiveExecutionIn("s21"));
    assertNotNull(processInstance.findActiveExecutionIn("s22"));

    processInstance = executionService.findExecutionById(processInstance.getId());

    assertEquals(4, processInstance.getExecutions().size());

    processInstance = executionService.findExecutionById(processInstance.getId());

    assertEquals(1, processInstance.getExecutionsMap().size());

    processInstance = executionService.findExecutionById(processInstance.getId());

    assertNotNull(processInstance.getExecution(null));
  }
}

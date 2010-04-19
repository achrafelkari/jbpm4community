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
package org.jbpm.test.execution;

import org.jbpm.api.Execution;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Joram Barrez
 */
public class ExecutionExpressionTest extends JbpmTestCase {
  
  public ExecutionExpressionTest() { }
  
  public void testProcessInstanceExpression() {
    
    deployJpdlXmlString(
        "<process name='p'>" +
        "  <start>" +
        "    <transition to='java_activity' />" +
        "  </start>" +
        "  <java name='java_activity' " +
        "        class='" + this.getClass().getName() + "' " +
        "        method='assertIsExecution'>" +
        "    <arg>" +
        "      <object expr='#{execution.processInstance}' />" +
        "    </arg>" + 
        "    <transition to='theEnd' />" +
        "  </java>" +
        "  <end name='theEnd' />" +
        "</process>"
      );
    
    executionService.startProcessInstanceByKey("p");
  }
  
  public void testIsProcessInstanceExpression() {
    
    deployJpdlXmlString(
        "<process name='p'>" +
        "  <start>" +
        "    <transition to='java_activity' />" +
        "  </start>" +
        "  <java name='java_activity' " +
        "        class='" + this.getClass().getName() + "' " +
        "        method='assertIsTrue'>" +
        "    <arg>" +
        "      <object expr='#{execution.isProcessInstance}' />" +
        "    </arg>" + 
        "    <transition to='theEnd' />" +
        "  </java>" +
        "  <end name='theEnd' />" +
        "</process>"
      );
    
    executionService.startProcessInstanceByKey("p");
  }
  

  public void assertIsExecution(Object obj) {
    assertTrue(obj instanceof Execution);
  }
  
  public void assertIsTrue(Boolean trueOrFalse) {
    assertTrue(trueOrFalse);
  }

}

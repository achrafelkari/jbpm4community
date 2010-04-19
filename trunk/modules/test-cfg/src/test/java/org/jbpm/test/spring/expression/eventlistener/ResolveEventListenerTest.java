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
package org.jbpm.test.spring.expression.eventlistener;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.api.ProcessInstance;
import org.jbpm.test.AbstractTransactionalSpringJbpmTestCase;

/**
 * Tests if event listeners can be resolved when they are defined as Spring beans.
 * 
 * Test originally created for JBPM-2529
 * 
 * @author Joram Barrez
 */
public class ResolveEventListenerTest extends AbstractTransactionalSpringJbpmTestCase {
  
  private final String TEST_PROCESS =
    "<?xml version='1.0' encoding='UTF-8'?>" +
    "<process name='testProcess'>" +
    "  <start name='start'>" +
    "    <transition name='to a' to='a' />" +
    "  </start>" +
    "  <state name='a'>" +
    "    <on event='start'>" +
    "      <event-listener expr='${myEventListener}' />" +
    "    </on>" +
    "    <transition name='to end' to='theEnd' />" +
    "  </state>" +
    "  <end name='theEnd' />" +
    "</process>"; 

  public void testResolveEventListener() {
    
    deployJpdlXmlString(TEST_PROCESS);
    
    // We start a process and insert a variable with value 1234
    Map<String, Object> vars = new HashMap<String, Object>();
    final String var = "testVar";
    final Integer varValue = new Integer(1234);
    vars.put(var, varValue);
    
    ProcessInstance pi = executionService.startProcessInstanceByKey("testProcess", vars);
    assertTrue(pi.isActive("a"));
    
    // The event listener should've added 1 to the variable value
    Integer value = (Integer) executionService.getVariable(pi.getId(), var);
    assertEquals(new Integer(varValue + 1), value);
  }

}

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
package org.jbpm.test.variables;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class BasicVariablesTest extends JbpmTestCase {

  public void testStartProcessInstanceWithoutVariables() {
    deployJpdlXmlString(
      "<process name='var'>" +
      "  <start name='a'>" +
      "    <transition to='b' />" +
      "  </start>" +
      "  <state name='b'/>" +
      "</process>"
    );
    
    executionService.startProcessInstanceByKey("var", "one");
    Set<String> variableNames = executionService.getVariableNames("var.one");
    assertNotNull(variableNames);
    assertEquals(0, variableNames.size());
  }

  public void testStartProcessInstanceWithThreeVariables() {
    deployJpdlXmlString(
      "<process name='var'>" +
      "  <start name='a'>" +
      "    <transition to='b' />" +
      "  </start>" +
      "  <state name='b'/>" +
      "</process>"
    );
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("customer", "John Doe");
    variables.put("type", "Accident");
    variables.put("amount", new Float(763.74));

    Set<String> expectedVariableNames = new HashSet<String>(variables.keySet());
    Map<String, Object> expectedVariables = new HashMap<String, Object>(variables);

    executionService.startProcessInstanceByKey("var", variables, "one");
    
    Set<String> variableNames = executionService.getVariableNames("var.one");
    assertNotNull(variableNames);
    
    assertEquals(expectedVariableNames, variableNames);

    variables = executionService.getVariables("var.one", variableNames);

    assertEquals(expectedVariables, variables);
  }

  public void testSetAndUpdateVariable() {
    deployJpdlXmlString(
      "<process name='var'>" +
      "  <start name='a'>" +
      "    <transition to='b' />" +
      "  </start>" +
      "  <state name='b'/>" +
      "</process>"
    );
    
    executionService.startProcessInstanceByKey("var", "one");
    executionService.setVariable("var.one", "msg", "hello");
    assertEquals("hello", executionService.getVariable("var.one", "msg"));
    executionService.setVariable("var.one", "msg", "world");
    assertEquals("world", executionService.getVariable("var.one", "msg"));
  }

  public void testUpdateVariableToDifferentType() {
    deployJpdlXmlString(
      "<process name='var'>" +
      "  <start name='a'>" +
      "    <transition to='b' />" +
      "  </start>" +
      "  <state name='b'/>" +
      "</process>"
    );
    
    executionService.startProcessInstanceByKey("var", "one");
    executionService.setVariable("var.one", "msg", "hello");
    assertEquals("hello", executionService.getVariable("var.one", "msg"));
    executionService.setVariable("var.one", "msg", new Integer(5));
    assertEquals(new Integer(5), executionService.getVariable("var.one", "msg"));
  }
}

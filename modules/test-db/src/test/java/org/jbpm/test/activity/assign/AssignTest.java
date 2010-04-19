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
package org.jbpm.test.activity.assign;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class AssignTest extends JbpmTestCase {

  public void testValueExpressionToVar() {
    deployJpdlXmlString(
      "<process name='AssignTest'>" +
      "  <start>" +
      "    <transition to='resolve' />" +
      "  </start>" +
      "  <assign name='resolve' expr='#{person.name}' to-var='result'>" +
      "    <transition to='wait' />" +
      "  </assign>" +
      "  <state name='wait' />" +
      "</process>"
    );
    
    Person person = new Person();
    person.setName("johndoe");

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("person", person);
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("AssignTest", variables);
    executionService.signalExecutionById(processInstance.getId());
    
    assertEquals("johndoe", executionService.getVariable(processInstance.getId(), "result"));
  }

  public void testMethodExpressionToVar() {
    deployJpdlXmlString(
      "<process name='AssignTest'>" +
      "  <start>" +
      "    <transition to='resolve' />" +
      "  </start>" +
      "  <assign name='resolve' expr='#{person.hello()}' to-var='result'>" +
      "    <transition to='wait' />" +
      "  </assign>" +
      "  <state name='wait' />" +
      "</process>"
    );
    
    Person person = new Person();
    person.setName("johndoe");

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("person", person);
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("AssignTest", variables);
    executionService.signalExecutionById(processInstance.getId());
    
    assertEquals("goodby", executionService.getVariable(processInstance.getId(), "result"));
  }

  public void testMethodWithParameterExpressionToVar() {
    deployJpdlXmlString(
      "<process name='AssignTest'>" +
      "  <start>" +
      "    <transition to='resolve' />" +
      "  </start>" +
      "  <assign name='resolve' expr=\"#{person.hello('Joe')}\" to-var='result'>" +
      "    <transition to='wait' />" +
      "  </assign>" +
      "  <state name='wait' />" +
      "</process>"
    );
    
    Person person = new Person();
    person.setName("johndoe");

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("person", person);
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("AssignTest", variables);
    executionService.signalExecutionById(processInstance.getId());
    
    assertEquals("Hi, Joe", executionService.getVariable(processInstance.getId(), "result"));
  }

  public void testWireObjectToExpression() {
    deployJpdlXmlString(
      "<process name='AssignTest'>" +
      "  <start>" +
      "    <transition to='resolve' />" +
      "  </start>" +
      "  <assign name='resolve' to-expr='#{person.address.street}'>" +
      "    <string value='gasthuisstraat' />" +
      "    <transition to='wait' />" +
      "  </assign>" +
      "  <state name='wait' />" +
      "</process>"
    );
    
    Person person = new Person();
    person.setName("johndoe");

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("person", person);
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("AssignTest", variables);
    executionService.signalExecutionById(processInstance.getId());
    
    person = (Person) executionService.getVariable(processInstance.getId(), "person");
    assertEquals("gasthuisstraat", person.getAddress().getStreet());
  }
}

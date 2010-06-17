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

import java.util.Collections;
import java.util.Map;

import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmTestCase;

/**
 * @author Tom Baeyens
 */
public class AssignTest extends JbpmTestCase {

  public void testFromExprToVar() {
    deployJpdlXmlString("<process name='AssignTest' xmlns='http://jbpm.org/jpdl/4.4'>"
      + "  <start>"
      + "    <transition to='resolve' />"
      + "  </start>"
      + "  <assign name='resolve' from-expr='#{person.name}' to-var='result'>"
      + "    <transition to='wait' />"
      + "  </assign>"
      + "  <state name='wait' />"
      + "</process>");

    Person person = new Person();
    person.setName("johndoe");
    Map<String, ?> variables = Collections.singletonMap("person", person);

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("AssignTest", variables);
    assertEquals("johndoe", executionService.getVariable(processInstance.getId(), "result"));
  }

  public void testFromMethodExprToVar() {
    deployJpdlXmlString("<process name='AssignTest' xmlns='http://jbpm.org/jpdl/4.4'>"
      + "  <start>"
      + "    <transition to='resolve' />"
      + "  </start>"
      + "  <assign name='resolve' from-expr='#{person.toString()}' to-var='result'>"
      + "    <transition to='wait' />"
      + "  </assign>"
      + "  <state name='wait' />"
      + "</process>");

    Person person = new Person();
    person.setName("johndoe");
    Map<String, ?> variables = Collections.singletonMap("person", person);

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("AssignTest", variables);
    assertEquals("Person(johndoe)", executionService.getVariable(processInstance.getId(), "result"));
  }

  public void testFromMethodParamExprToVar() {
    deployJpdlXmlString("<process name='AssignTest' xmlns='http://jbpm.org/jpdl/4.4'>"
      + "  <start>"
      + "    <transition to='resolve' />"
      + "  </start>"
      + "  <assign name='resolve' from-expr=\"#{person.sayHi('Joe')}\" to-var='result'>"
      + "    <transition to='wait' />"
      + "  </assign>"
      + "  <state name='wait' />"
      + "</process>");

    Person person = new Person();
    person.setName("johndoe");
    Map<String, ?> variables = Collections.singletonMap("person", person);

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("AssignTest", variables);
    assertEquals("Hi, Joe", executionService.getVariable(processInstance.getId(), "result"));
  }

  public void testFromDescToExpr() {
    deployJpdlXmlString("<process name='AssignTest' xmlns='http://jbpm.org/jpdl/4.4'>"
      + "  <start>"
      + "    <transition to='resolve' />"
      + "  </start>"
      + "  <assign name='resolve' to-expr='#{person.address.street}'>"
      + "    <from><string value='gasthuisstraat' /></from>"
      + "    <transition to='wait' />"
      + "  </assign>"
      + "  <state name='wait' />"
      + "</process>");

    Person person = new Person();
    person.setName("johndoe");
    Map<String, ?> variables = Collections.singletonMap("person", person);

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("AssignTest", variables);
    person = (Person) executionService.getVariable(processInstance.getId(), "person");
    assertEquals("gasthuisstraat", person.getAddress().getStreet());
  }

  public void testFromVarToVar() {
    deployJpdlXmlString("<process name='AssignTest' xmlns='http://jbpm.org/jpdl/4.4'>"
      + "  <start>"
      + "    <transition to='resolve' />"
      + "  </start>"
      + "  <assign name='resolve' from-var='person' to-var='result'>"
      + "    <transition to='wait' />"
      + "  </assign>"
      + "  <state name='wait' />"
      + "</process>");

    Person person = new Person();
    person.setName("johndoe");
    Map<String, ?> variables = Collections.singletonMap("person", person);

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("AssignTest", variables);
    person = (Person) executionService.getVariable(processInstance.getId(), "result");
    assertEquals("johndoe", person.getName());
  }
}

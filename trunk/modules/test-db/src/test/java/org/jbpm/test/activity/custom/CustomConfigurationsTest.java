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
package org.jbpm.test.activity.custom;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jbpm.api.ProcessInstance;
import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class CustomConfigurationsTest extends JbpmTestCase {
  
  public static class MyCustomAutomatic implements ActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) throws Exception {
    }
  }

  public void testCustomAutomaticClass() {
    deployJpdlXmlString(
        "<process name='CustomClass'>" +
        "  <start>" +
        "    <transition to='c' />" +
        "  </start>" +
        "  <custom name='c' class='"+MyCustomAutomatic.class.getName()+"'>" +
        "    <transition to='wait' />" +
        "  </custom>" +
        "  <state name='wait'/>" +
        "</process>"
    );
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("CustomClass");

    assertTrue(processInstance.findActiveActivityNames().contains("wait"));
  }

  public static class MyCustomWait implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) throws Exception {
      execution.waitForSignal();
    }
    public void signal(ActivityExecution execution, String signalName, Map<String, ? > parameters) throws Exception {
      execution.take("3");
    }
  }

  public void testCustomWaitClass() {
    deployJpdlXmlString(
        "<process name='CustomClass'>" +
        "  <start>" +
        "    <transition to='c' />" +
        "  </start>" +
        "  <custom name='c' class='"+MyCustomWait.class.getName()+"'>" +
        "    <transition name='1' to='the 1st way' />" +
        "    <transition name='2' to='the 2nd way' />" +
        "    <transition name='3' to='the 3rd way' />" +
        "  </custom>" +
        "  <state name='the 1st way' />" +
        "  <state name='the 2nd way' />" +
        "  <state name='the 3rd way' />" +
        "</process>"
    );
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("CustomClass");

    Set<String> findActiveActivityNames = processInstance.findActiveActivityNames();
    assertTrue(findActiveActivityNames.contains("c"));
    
    processInstance = executionService.signalExecutionById(processInstance.getId());
    
    findActiveActivityNames = processInstance.findActiveActivityNames();
    assertTrue(findActiveActivityNames.contains("the 3rd way"));
  }

  public static class MyCustomAutomaticWithFieldAndProperty implements ActivityBehaviour {
    private static final long serialVersionUID = 1L;
    String text;
    Long objectLong;

    int basicInt;
    Integer objectInt;
    long basicLong;
    float basicFloat;
    Float objectFloat;
    double basicDouble;
    Double objectDouble;
    boolean basicBooleanTrue;
    boolean basicBooleanFalse;
    Boolean objectBooleanTrue;
    Boolean objectBooleanFalse;
    
    public void execute(ActivityExecution execution) throws Exception {
      execution.setVariable("text", text);
      execution.setVariable("number", objectLong);
      execution.setVariable("basicInt", basicInt);
      execution.setVariable("objectInt", objectInt);
      execution.setVariable("basicLong", basicLong);
      execution.setVariable("basicFloat", basicFloat);
      execution.setVariable("objectFloat", objectFloat);
      execution.setVariable("basicDouble", basicDouble);
      execution.setVariable("objectDouble", objectDouble);
      execution.setVariable("basicBooleanTrue", basicBooleanTrue);
      execution.setVariable("basicBooleanFalse", basicBooleanFalse);
      execution.setVariable("objectBooleanTrue", objectBooleanTrue);
      execution.setVariable("objectBooleanFalse", objectBooleanFalse);
    }
    public void setNumberProperty(Long number) {
      this.objectLong = number;
    }
  }

  public void testCustomAutomaticClassWithInjections() {
    deployJpdlXmlString(
        "<process name='CustomClass'>" +
        "  <start>" +
        "    <transition to='c' />" +
        "  </start>" +
        "  <custom name='c' class='"+MyCustomAutomaticWithFieldAndProperty.class.getName()+"'>" +
        "    <field name='text'><string value='hi' /></field>" +
        "    <property name='numberProperty'><long value='5' /></property>" +
        "    <field name='basicInt'><int value='6' /></field>" +
        "    <field name='objectInt'><int value='7' /></field>" +
        "    <field name='basicLong'><long value='99999999999' /></field>" +
        "    <field name='basicFloat'><float value='99.99' /></field>" +
        "    <field name='objectFloat'><float value='88.88' /></field>" +
        "    <field name='basicDouble'><double value='9999999999.99' /></field>" +
        "    <field name='objectDouble'><double value='8888888888.88' /></field>" +
        "    <field name='basicBooleanTrue'><true /></field>" +
        "    <field name='basicBooleanFalse'><false /></field>" +
        "    <field name='objectBooleanTrue'><true /></field>" +
        "    <field name='objectBooleanFalse'><false /></field>" +
        "    <transition to='wait' />" +
        "  </custom>" +
        "  <state name='wait'/>" +
        "</process>"
    );
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("CustomClass");

    assertTrue(processInstance.findActiveActivityNames().contains("wait"));
    
    assertEquals("hi", executionService.getVariable(processInstance.getId(), "text"));
    assertEquals(new Long(5), executionService.getVariable(processInstance.getId(), "number"));

    assertEquals(new Integer("6"), executionService.getVariable(processInstance.getId(), "basicInt"));
    assertEquals(new Integer("7"), executionService.getVariable(processInstance.getId(), "objectInt"));
    assertEquals(99999999999L, executionService.getVariable(processInstance.getId(), "basicLong"));
    assertEquals(new Float("99.99"), executionService.getVariable(processInstance.getId(), "basicFloat"));
    assertEquals(new Float("88.88"), executionService.getVariable(processInstance.getId(), "objectFloat"));
    assertEquals(new Double("9999999999.99"), executionService.getVariable(processInstance.getId(), "basicDouble"));
    assertEquals(new Double("8888888888.88"), executionService.getVariable(processInstance.getId(), "objectDouble"));
    assertEquals(Boolean.TRUE, executionService.getVariable(processInstance.getId(), "basicBooleanTrue"));
    assertEquals(Boolean.FALSE, executionService.getVariable(processInstance.getId(), "basicBooleanFalse"));
    assertEquals(Boolean.TRUE, executionService.getVariable(processInstance.getId(), "objectBooleanTrue"));
    assertEquals(Boolean.FALSE, executionService.getVariable(processInstance.getId(), "objectBooleanFalse"));
  }


  public void testCustomAutomaticExpr() {
    deployJpdlXmlString(
        "<process name='CustomExpr'>" +
        "  <start>" +
        "    <transition to='c' />" +
        "  </start>" +
        "  <custom name='c' expr='#{custombehaviour}'>" +
        "    <transition to='wait' />" +
        "  </custom>" +
        "  <state name='wait'/>" +
        "</process>"
    );

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("custombehaviour", new MyCustomAutomatic());
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("CustomExpr", variables);

    assertTrue(processInstance.findActiveActivityNames().contains("wait"));
  }

  public void testCustomWaitExpr() {
    deployJpdlXmlString(
        "<process name='CustomExpr'>" +
        "  <start>" +
        "    <transition to='c' />" +
        "  </start>" +
        "  <custom name='c' expr='#{custombehaviour}'>" +
        "    <transition name='1' to='the 1st way' />" +
        "    <transition name='2' to='the 2nd way' />" +
        "    <transition name='3' to='the 3rd way' />" +
        "  </custom>" +
        "  <state name='the 1st way' />" +
        "  <state name='the 2nd way' />" +
        "  <state name='the 3rd way' />" +
        "</process>"
    );
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("custombehaviour", new MyCustomWait());
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("CustomExpr", variables);

    Set<String> findActiveActivityNames = processInstance.findActiveActivityNames();
    assertTrue(findActiveActivityNames.contains("c"));
    
    processInstance = executionService.signalExecutionById(processInstance.getId());
    
    findActiveActivityNames = processInstance.findActiveActivityNames();
    assertTrue(findActiveActivityNames.contains("the 3rd way"));
  }

  public static class ActivityBehaviourWithId implements ActivityBehaviour {
    private static final long serialVersionUID = 1L;
    private static int nextObjectId = 1;
    int objectId = nextObjectId++;
    public void execute(ActivityExecution execution) throws Exception {
      execution.setVariable("objectId", objectId);
    }
  }

  public void testCustomUserObjectCachingDefault() {
    ActivityBehaviourWithId.nextObjectId = 1;
    deployJpdlXmlString(
        "<process name='CustomClass'>" +
        "  <start>" +
        "    <transition to='c' />" +
        "  </start>" +
        "  <custom name='c' class='"+ActivityBehaviourWithId.class.getName()+"'>" +
        "    <transition to='wait' />" +
        "  </custom>" +
        "  <state name='wait'/>" +
        "</process>"
    );
    
    String pid = executionService.startProcessInstanceByKey("CustomClass").getId();
    assertEquals(1, executionService.getVariable(pid, "objectId"));
    
    pid = executionService.startProcessInstanceByKey("CustomClass").getId();
    assertEquals(1, executionService.getVariable(pid, "objectId"));
    
    pid = executionService.startProcessInstanceByKey("CustomClass").getId();
    assertEquals(1, executionService.getVariable(pid, "objectId"));
  }

  public void testCustomUserObjectCachingDisabled() {
    ActivityBehaviourWithId.nextObjectId = 1;
    deployJpdlXmlString(
        "<process name='CustomClass'>" +
        "  <start>" +
        "    <transition to='c' />" +
        "  </start>" +
        "  <custom name='c' cache='false' class='"+ActivityBehaviourWithId.class.getName()+"'>" +
        "    <transition to='wait' />" +
        "  </custom>" +
        "  <state name='wait'/>" +
        "</process>"
    );
    
    String pid = executionService.startProcessInstanceByKey("CustomClass").getId();
    assertEquals(1, executionService.getVariable(pid, "objectId"));
    
    pid = executionService.startProcessInstanceByKey("CustomClass").getId();
    assertEquals(2, executionService.getVariable(pid, "objectId"));
    
    pid = executionService.startProcessInstanceByKey("CustomClass").getId();
    assertEquals(3, executionService.getVariable(pid, "objectId"));
  }
}

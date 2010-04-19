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
package org.jbpm.bpmn;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.DocumentFactory;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.TaskQuery;
import org.jbpm.api.task.Task;
import org.jbpm.bpmn.parser.BpmnParser;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.xml.Problem;
import org.jbpm.test.JbpmTestCase;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Tom Baeyens
 * @author Ronald van Kuijk (kukeltje)
 */
public class ExclusiveGatewayTest extends JbpmTestCase {

  static BpmnParser bpmnParser = new BpmnParser();

  public List<Problem> parse(String resource) {

    List<Problem> problems = bpmnParser.createParse().setResource(resource).execute().getProblems();

    return problems;
  }

  public void testNormal() {

    List<Problem> problems = parse("org/jbpm/bpmn/exclusiveGateway.bpmn.xml");

    if (!problems.isEmpty()) {
      fail("No problems should have occured. Problems: " + problems);
    }
  }
  
  public void testNormalXPath() {

    List<Problem> problems = parse("org/jbpm/bpmn/exclusiveGatewayXPath.bpmn.xml");

    if (!problems.isEmpty()) {
      fail("No problems should have occured. Problems: " + problems);
    }
  }
  
  public void testNormalExecuteDecisionCondition() {

    String deploymentId = repositoryService.createDeployment().addResourceFromClasspath("org/jbpm/bpmn/exclusiveGateway.bpmn.xml").deploy();

    try {
        Map variables = new HashMap();
        
        variables.put("test", "value");
        
        ProcessInstance pi = executionService.startProcessInstanceByKey("ExclusiveGateway", variables );
        String pid = pi.getId();
        
        TaskQuery taskQuery = taskService.createTaskQuery();
        List<Task> allTasks = taskQuery.list();

        assertEquals(1, allTasks.size());
        assertEquals("doSomething", allTasks.get(0).getActivityName());
        
        taskService.completeTask( allTasks.get(0).getId());

        // process instance should be ended
        pi = executionService.findProcessInstanceById(pid);
        assertNull(pi);
        
    }
    finally {
        repositoryService.deleteDeploymentCascade(deploymentId);
    }
  }
  
  public void testNormalExecuteDefault() {

    String deploymentId = repositoryService.createDeployment().addResourceFromClasspath("org/jbpm/bpmn/exclusiveGateway.bpmn.xml").deploy();

    try {
        Map variables = new HashMap();
        
        variables.put("test", "no value");
        
        ProcessInstance pi = executionService.startProcessInstanceByKey("ExclusiveGateway", variables );
        String pid = pi.getId();
        
        TaskQuery taskQuery = taskService.createTaskQuery();
        List<Task> allTasks = taskQuery.list();

        assertEquals(1, allTasks.size());
        assertEquals("doSomethingElse", allTasks.get(0).getActivityName());
        
        taskService.completeTask( allTasks.get(0).getId());

        // process instance should be ended
        pi = executionService.findProcessInstanceById(pid);
        assertNull(pi);
        
    }
    finally {
        repositoryService.deleteDeploymentCascade(deploymentId);
    }
  }
  
  public void testNormalExecuteDecisionConditionXPath() {

    String deploymentId = repositoryService.createDeployment().addResourceFromClasspath("org/jbpm/bpmn/exclusiveGatewayXPath.bpmn.xml").deploy();

    try {
        Map variables = new HashMap();
        Document objectData;
        
        objectData = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this.getClass().getResourceAsStream("xmlTestDocument.xml"));
        variables.put("justadocument", objectData);
        variables.put("test", "just");
        
        ProcessInstance pi = executionService.startProcessInstanceByKey("ExclusiveGateway", variables );
        String pid = pi.getId();
        
        TaskQuery taskQuery = taskService.createTaskQuery();
        List<Task> allTasks = taskQuery.list();

        assertEquals(1, allTasks.size());
        assertEquals("doSomething", allTasks.get(0).getActivityName());
        
        taskService.completeTask( allTasks.get(0).getId());

        // process instance should be ended
        pi = executionService.findProcessInstanceById(pid);
        assertNull(pi);
        
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
    finally {
        repositoryService.deleteDeploymentCascade(deploymentId);
    }
  }


  public void testNonBoundDefault() {

    List<Problem> problems = parse("org/jbpm/bpmn/exclusiveGatewayNonBoundDefault.bpmn.xml");

    if ((problems == null) || (problems.isEmpty())) {
      fail("expected problems during parse");
    } else {
      assertTextPresent("exclusiveGateway 'Just a gateway' default sequenceFlow 'flow5' does not exist or is not related to this node", problems.get(0).getMsg());
    }
  }
  
  public void testNonExistingDefault() {

    List<Problem> problems = parse("org/jbpm/bpmn/exclusiveGatewayNonExistingDefault.bpmn.xml");

    if ((problems == null) || (problems.isEmpty())) {
      fail("expected problems during parse");
    } else {
      assertTextPresent("cvc-id.1: There is no ID/IDREF binding for IDREF 'flow666'", problems.get(0).getMsg());
    }
  }
  
  public void testMixedValid() {

    List<Problem> problems = parse("org/jbpm/bpmn/exclusiveGatewayMixedValid.bpmn.xml");

    if (!problems.isEmpty()) {
      fail("No problems should have occured. Problems: " + problems);
    }
  }

  
  public void testMixedInvalid() {

    List<Problem> problems = parse("org/jbpm/bpmn/exclusiveGatewayMixedInvalid.bpmn.xml");

    if ((problems == null) || (problems.isEmpty())) {
      fail("expected problems during parse");
    } else {
      assertTextPresent("exclusiveGateway 'Just a gateway' has the wrong number of incomming (1) and outgoing (2) transitions for gatewayDirection='mixed'", problems.get(0).getMsg());
    }
  }
  
  public void testConvergingInvalid() {

    List<Problem> problems = parse("org/jbpm/bpmn/exclusiveGatewayConvergingInvalid.bpmn.xml");

    if ((problems == null) || (problems.isEmpty())) {
      fail("expected problems during parse");
    } else {
      assertTextPresent("Gateway 'Just a gateway' has the wrong number of incomming (1) and outgoing (2) transitions for gatewayDirection='converging'", problems.get(0).getMsg());
    }
  }

  public void testDivergingInvalid() {

    List<Problem> problems = parse("org/jbpm/bpmn/exclusiveGatewayDivergingInvalid.bpmn.xml");

    if ((problems == null) || (problems.isEmpty())) {
      fail("expected problems during parse");
    } else {
      assertTextPresent("exclusiveGateway 'Just a gateway' has the wrong number of incomming (2) and outgoing (2) transitions for gatewayDirection='diverging'", problems.get(0).getMsg());
    }
  }
  
  public void testInvalidConditionExpression() {

    List<Problem> problems = parse("org/jbpm/bpmn/exclusiveGatewayInvalidConditionExpression.bpmn.xml");

    if ((problems == null) || (problems.isEmpty())) {
      fail("expected problems during parse");
    } else {
      assertTextPresent("Type of the conditionExpression on sequenceFlow with id=flow2 is of onsupported type 'bpmn:tExpression", problems.get(0).getMsg());
    }
  }

}

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
package org.jbpm.test.eventlistener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.api.ProcessInstance;
import org.jbpm.api.listener.EventListener;
import org.jbpm.api.listener.EventListenerExecution;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class EventListenerTest extends JbpmTestCase {
  
  public static class ProcessStartListener implements EventListener {
    private static final long serialVersionUID = 1L;
    public void notify(EventListenerExecution execution) {
      execution.setVariable("isInvoked", "true");
    }
  }

  public void testProcessStartListener() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <on event='start'>" +
      "    <event-listener class='"+ProcessStartListener.class.getName()+"' />" +
      "  </on>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a' />" +
      "</process>"
    );
    
    String processInstanceId = executionService.startProcessInstanceByKey("ICL").getId();

    assertEquals("true", executionService.getVariable(processInstanceId, "isInvoked"));
    
    executionService.setVariable(processInstanceId, "isInvoked", "false");
    
    executionService.signalExecutionById(processInstanceId);
    
    assertEquals("false", executionService.getVariable(processInstanceId, "isInvoked"));
  }

  public static class PropagationEnabledListener implements EventListener {
    private static final long serialVersionUID = 1L;
    public void notify(EventListenerExecution execution) {
      Integer invocations = (Integer) execution.getVariable("invocations");
      if (invocations==null) {
        execution.setVariable("invocations", 1);
      } else {
        execution.setVariable("invocations", invocations+1);
      }
    }
  }

  public void testProcessStartListenerPropagation() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <on event='start'>" +
      "    <event-listener propagation='enabled' class='"+PropagationEnabledListener.class.getName()+"' />" +
      "  </on>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition to='b' />" +
      "  </state>" +
      "  <state name='b' />" +
      "</process>"
    );
    
    String processInstanceId = executionService.startProcessInstanceByKey("ICL").getId();

    // the PropagationEnabledListener is invoked once for the start event 
    // of the process and once for the start event of activity a
    assertEquals(2, executionService.getVariable(processInstanceId, "invocations"));
    
    executionService.signalExecutionById(processInstanceId);
    
    // the listener is invoked once more for the start of activity b
    assertEquals(3, executionService.getVariable(processInstanceId, "invocations"));
  }

  public static class ActivityStartListener implements EventListener {
    private static final long serialVersionUID = 1L;
    public void notify(EventListenerExecution execution) {
      execution.setVariable("isInvoked", "true");
    }
  }

  public void testActivityStartListener() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='s' />" +
      "  </start>" +
      "  <state name='s'>" +
      "    <on event='start'>" +
      "      <event-listener class='"+ActivityStartListener.class.getName()+"' />" +
      "    </on>" +
      "    <transition to='end' />" +
      "  </state>" +
      "  <state name='end' />" +
      "</process>"
    );
    
    String processInstanceId = executionService.startProcessInstanceByKey("ICL").getId();
    
    assertEquals("true", executionService.getVariable(processInstanceId, "isInvoked"));
  }

  public static class ActivityEndListener implements EventListener {
    private static final long serialVersionUID = 1L;
    public void notify(EventListenerExecution execution) {
      execution.setVariable("isInvoked", "true");
    }
  }

  public void testActivityEndListener() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='s' />" +
      "  </start>" +
      "  <state name='s'>" +
      "    <on event='end'>" +
      "      <event-listener class='"+ActivityEndListener.class.getName()+"' />" +
      "    </on>" +
      "    <transition to='end' />" +
      "  </state>" +
      "  <state name='end' />" +
      "</process>"
    );
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ICL");
    String processInstanceId = processInstance.getId();

    assertNull(executionService.getVariable(processInstanceId, "isInvoked"));
    
    executionService.signalExecutionById(processInstance.getId());

    assertEquals("true", executionService.getVariable(processInstanceId, "isInvoked"));
  }

  public static class TransitionListener implements EventListener {
    private static final long serialVersionUID = 1L;
    public void notify(EventListenerExecution execution) {
      execution.setVariable("isInvoked", "true");
    }
  }

  public void testTransitionListener() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='end'>" +
      "      <event-listener class='"+TransitionListener.class.getName()+"' />" +
      "    </transition>" +
      "  </start>" +
      "  <state name='end' />" +
      "</process>"
    );
    
    String processInstanceId = executionService.startProcessInstanceByKey("ICL").getId();
    
    assertEquals("true", executionService.getVariable(processInstanceId, "isInvoked"));
  }

  public static class OrderListener implements EventListener {
    private static final long serialVersionUID = 1L;
    int i;
    public void notify(EventListenerExecution execution) {
      List<Integer> order = (List<Integer>) execution.getVariable("order");
      order.add(i);
      execution.setVariable("order", order);
    }
  }

  public void testEventOrdering() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='end'>" +
      "      <event-listener class='"+OrderListener.class.getName()+"'>" +
      "        <field name='i'><int value='1' /></field> " +
      "      </event-listener>" +
      "      <event-listener class='"+OrderListener.class.getName()+"'>" +
      "        <field name='i'><int value='2' /></field> " +
      "      </event-listener>" +
      "      <event-listener class='"+OrderListener.class.getName()+"'>" +
      "        <field name='i'><int value='3' /></field> " +
      "      </event-listener>" +
      "      <event-listener class='"+OrderListener.class.getName()+"'>" +
      "        <field name='i'><int value='4' /></field> " +
      "      </event-listener>" +
      "      <event-listener class='"+OrderListener.class.getName()+"'>" +
      "        <field name='i'><int value='5' /></field> " +
      "      </event-listener>" +
      "    </transition>" +
      "  </start>" +
      "  <state name='end' />" +
      "</process>"
    );
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("order", new ArrayList<Integer>());
    String processInstanceId = executionService.startProcessInstanceByKey("ICL", variables).getId();
    
    List<Integer> expectedOrder = new ArrayList<Integer>();
    expectedOrder.add(1);
    expectedOrder.add(2);
    expectedOrder.add(3);
    expectedOrder.add(4);
    expectedOrder.add(5);
    
    assertEquals(expectedOrder, executionService.getVariable(processInstanceId, "order"));
  }

  public void testProcessStartListenerExpr() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <on event='start'>" +
      "    <event-listener expr='#{processstartlistener}' />" +
      "  </on>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a' />" +
      "</process>"
    );
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("processstartlistener", new ProcessStartListener());
    String processInstanceId = executionService.startProcessInstanceByKey("ICL", variables).getId();

    assertEquals("true", executionService.getVariable(processInstanceId, "isInvoked"));
    
    executionService.setVariable(processInstanceId, "isInvoked", "false");
    
    executionService.signalExecutionById(processInstanceId);
    
    assertEquals("false", executionService.getVariable(processInstanceId, "isInvoked"));
  }

  public void testActivityStartListenerExpr() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='s' />" +
      "  </start>" +
      "  <state name='s'>" +
      "    <on event='start'>" +
      "      <event-listener expr='#{activitystartlistener}' />" +
      "    </on>" +
      "    <transition to='end' />" +
      "  </state>" +
      "  <state name='end' />" +
      "</process>"
    );
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("activitystartlistener", new ActivityStartListener());
    String processInstanceId = executionService.startProcessInstanceByKey("ICL", variables).getId();
    
    assertEquals("true", executionService.getVariable(processInstanceId, "isInvoked"));
  }

  public void testActivityEndListenerExpr() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='s' />" +
      "  </start>" +
      "  <state name='s'>" +
      "    <on event='end'>" +
      "      <event-listener expr='#{activityendlistener}' />" +
      "    </on>" +
      "    <transition to='end' />" +
      "  </state>" +
      "  <state name='end' />" +
      "</process>"
    );
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("activityendlistener", new ActivityEndListener());
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ICL", variables);
    String processInstanceId = processInstance.getId();

    assertNull(executionService.getVariable(processInstanceId, "isInvoked"));
    
    executionService.signalExecutionById(processInstance.getId());

    assertEquals("true", executionService.getVariable(processInstanceId, "isInvoked"));
  }

}

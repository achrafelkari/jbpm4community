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

import org.jbpm.api.ProcessInstance;
import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class SerializedVariableUpdateTest extends JbpmTestCase {
  
  public static class UpdateAndReplace implements ActivityBehaviour {

    private static final long serialVersionUID = 1L;

    public void execute(ActivityExecution execution) throws Exception {
      Set<String> messages = (Set<String>) execution.getVariable("messages");
      messages.clear();
      messages.add("i");
      messages.add("was");
      messages.add("updated");

      Set<String> newMessagesObject = new HashSet<String>();
      newMessagesObject.add("completely");
      newMessagesObject.add("new");
      newMessagesObject.add("object");
      execution.setVariable("messages", newMessagesObject);
    }
    
  }

  public void testSerializableVariableUpdate() {
    deployJpdlXmlString(
      "<process name='SerializedVariableUpdate'>" +
      "  <start>" +
      "    <transition to='wait before' />" +
      "  </start>" +
      "  <state name='wait before'>" +
      "    <transition to='update' />" +
      "  </state>" +
      "  <custom name='update' class='"+UpdateAndReplace.class.getName()+"'>" +
      "    <transition to='wait after' />" +
      "  </custom>" +
      "  <state name='wait after'/>" +
      "</process>"
    );

    Set<String> messages = new HashSet<String>();
    messages.add("serialize");
    messages.add("me");

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("messages", messages);
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("SerializedVariableUpdate", variables);
    String pid = processInstance.getId();
    executionService.signalExecutionById(pid);

    Set<String> expectedMessages = new HashSet<String>();
    expectedMessages.add("completely");
    expectedMessages.add("new");
    expectedMessages.add("object");

    messages = (Set<String>) executionService.getVariable(pid, "messages");
    assertEquals(expectedMessages, messages);
  }


  public static class ReadOnly implements ActivityBehaviour {
    private static final long serialVersionUID = 1L;

    public void execute(ActivityExecution execution) throws Exception {
      execution.getVariable("messages");
      execution.getVariable("messages");
    }
  }
  
  public void testReadOnly() {
    deployJpdlXmlString(
      "<process name='SerializedVariableUpdate'>" +
      "  <start>" +
      "    <transition to='wait before' />" +
      "  </start>" +
      "  <state name='wait before'>" +
      "    <transition to='update' />" +
      "  </state>" +
      "  <custom name='update' class='"+ReadOnly.class.getName()+"'>" +
      "    <transition to='wait after' />" +
      "  </custom>" +
      "  <state name='wait after'/>" +
      "</process>"
    );
  
    Set<String> messages = new HashSet<String>();
    messages.add("serialize");
    messages.add("me");
  
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("messages", messages);
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("SerializedVariableUpdate", variables);
    String pid = processInstance.getId();
    executionService.signalExecutionById(pid);
  
    Set<String> expectedMessages = new HashSet<String>();
    expectedMessages.add("serialize");
    expectedMessages.add("me");
  
    messages = (Set<String>) executionService.getVariable(pid, "messages");
    assertEquals(expectedMessages, messages);
    
    // TODO find some way to assert that NO variable update is issued to the history service
    // for now, i just did that check once manually in the debugger :-)  11/12/2009
  }

}

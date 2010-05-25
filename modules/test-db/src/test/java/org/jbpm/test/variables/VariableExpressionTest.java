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
package org.jbpm.test.variables;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Joram Barrez
 * @author Maciej Swiderski
 */
public class VariableExpressionTest extends JbpmTestCase {
  
  public void testExpression() {
    deployJpdlXmlString(
            "<process name='theProcess'>" +
            "  <start name='theStart'>" +
            "    <transition to='incrementCounter' />" +
            "  </start>" +
            "  <custom name='incrementCounter' class='" + MyJavaActivity.class.getName() + "'>" +
            "   <transition to='decideToGoFurther' />" +
            "  </custom>" +
            "  <decision name='decideToGoFurther'>" +
            "    <transition to='waitHere'>" +
            "      <condition expr='#{counter == 10}' />" + 
            "    </transition>" +
            "    <transition to='incrementCounter'>" +
            "      <condition expr='#{counter &lt; 10}' />" +
            "    </transition>" +
            "  </decision>" +
            "  <state name='waitHere'>" +
            "    <transition to='theEnd' />" +
            "  </state>" +
            "  <end name='theEnd' />" +
            "</process>"
          );
    
    Map<String, Integer> vars = new HashMap<String, Integer>();
    vars.put("counter", 0);
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("theProcess", vars);
    assertActivityActive(processInstance.getId(), "waitHere");
    
    Integer counter = (Integer) executionService.getVariable(processInstance.getId(), "counter");
    assertEquals(new Integer(10), counter);
  }
  
  public void testNullValueExpression() {
    deployJpdlXmlString(
            "<process name='theProcess'>" +
            "  <start name='theStart'>" +
            "    <transition to='decideToGoFurther' />" +
            "  </start>" +
            "  <custom name='incrementCounter' class='" + MyJavaActivity.class.getName() + "'>" +
            "   <transition to='decideToGoFurther' />" +
            "  </custom>" +
            "  <decision name='decideToGoFurther'>" +
            "    <transition to='waitHere'>" +
            "      <condition expr='#{counter==null}' />" + 
            "    </transition>" +
            "    <transition to='incrementCounter'>" +
            "      <condition expr='#{counter &lt; 10}' />" +
            "    </transition>" +
            "  </decision>" +
            "  <state name='waitHere'>" +
            "    <transition to='theEnd' />" +
            "  </state>" +
            "  <end name='theEnd' />" +
            "</process>"
          );
    
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("counter", null);
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("theProcess", vars);
    assertActivityActive(processInstance.getId(), "waitHere");
    
    Object value = executionService.getVariable(processInstance.getId(), "counter");
    assertEquals(null, value);
  }
  
  public void testMissingVariableExpression() {
    deployJpdlXmlString(
            "<process name='theProcess'>" +
            "  <start name='theStart'>" +
            "    <transition to='decideToGoFurther' />" +
            "  </start>" +
            "  <custom name='incrementCounter' class='" + MyJavaActivity.class.getName() + "'>" +
            "   <transition to='decideToGoFurther' />" +
            "  </custom>" +
            "  <decision name='decideToGoFurther'>" +
            "    <transition to='waitHere'>" +
            "      <condition expr='#{counter==null}' />" + 
            "    </transition>" +
            "    <transition to='incrementCounter'>" +
            "      <condition expr='#{counter &lt; 10}' />" +
            "    </transition>" +
            "  </decision>" +
            "  <state name='waitHere'>" +
            "    <transition to='theEnd' />" +
            "  </state>" +
            "  <end name='theEnd' />" +
            "</process>"
          );
    
    Map<String, Object> vars = new HashMap<String, Object>();
    try {
      ProcessInstance processInstance = executionService.startProcessInstanceByKey("theProcess", vars);

      fail("Variable counter is not set, should fail");
    } catch (JbpmException e) {

      assertTrue(e.getMessage().indexOf("Cannot find property counter") != -1);
    }

  }
  
  
  public static class MyJavaActivity implements ActivityBehaviour {

    private static final long serialVersionUID = 1L;

    public void execute(ActivityExecution execution) throws Exception {
      Integer counter = (Integer) execution.getVariable("counter");
      counter++;
      execution.setVariable("counter", counter);
    }
    
  }

}

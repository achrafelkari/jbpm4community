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
package org.jbpm.bpmn.test.gateway;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.api.history.HistoryActivityInstance;
import org.jbpm.api.history.HistoryActivityInstanceQuery;
import org.jbpm.test.JbpmTestCase;

/**
 * Test case for the convering (merge) behaviour of an exclusive gateway.
 * 
 * @author Tom Baeyens
 */
public class ExclusiveGatewayMergeTest extends JbpmTestCase {
  
  private static final String TEST_PROCESS = 
    "<definitions xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
    "  <process id='exclusiverMerge' >" +
    "    <startEvent id='theStart' />" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='decision' />" +
    "    <sequenceFlow id='flow2' sourceRef='theStart' targetRef='decision' />" +
    "    <exclusiveGateway id='decision' />" +
    "    <sequenceFlow id='flow2' sourceRef='decision' targetRef='theEnd1' >" +
    "      <conditionExpression xsi:type='tFormalExpression'>${var &gt;= 10}</conditionExpression>" +
    "    </sequenceFlow>" +
    "    <sequenceFlow id='flow3' sourceRef='decision' targetRef='theEnd2' >" +
    "      <conditionExpression xsi:type='tFormalExpression'>${var &lt;= 10}</conditionExpression>" +
    "    </sequenceFlow>" +
    "    <endEvent id='theEnd1' />" +
    "    <endEvent id='theEnd2' />" +
    "  </process>" +
    "</definitions>";
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    deployBpmn2XmlString(TEST_PROCESS);
  }
  
  public void testExclusiveMerge() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);
    executionService.startProcessInstanceByKey("exclusiverMerge", vars);
    
    HistoryActivityInstanceQuery query = historyService.createHistoryActivityInstanceQuery().activityName("decision");
    List<HistoryActivityInstance> historyActivities = query.list();
    assertEquals(2, historyActivities.size());
    assertEquals("flow3", historyActivities.get(0).getTransitionNames().get(0));
    assertEquals("flow3", historyActivities.get(1).getTransitionNames().get(0));
  }

}

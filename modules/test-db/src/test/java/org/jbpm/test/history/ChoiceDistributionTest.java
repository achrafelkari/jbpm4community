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
package org.jbpm.test.history;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class ChoiceDistributionTest extends JbpmTestCase {

  public void testDecisionExpression() {
    deployJpdlXmlString(
      "<process name='Transport Selection' key='TRS'>" +
      "  <start>" +
      "    <transition to='How far?' />" +
      "  </start>" +
      "  <decision name='How far?' expr='#{distance}'>" +
      "    <transition name='far'           to='Big car' />" +
      "    <transition name='nearby'        to='Small car' />" +
      "    <transition name='other country' to='Airoplane' />" +
      "  </decision>" +
      "  <state name='Big car' />" +
      "  <state name='Small car' />" +
      "  <state name='Airoplane' />" +
      "</process>"
    );

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("distance", "far");
    executionService.startProcessInstanceByKey("TRS", variables);

    variables.put("distance", "nearby");
    executionService.startProcessInstanceByKey("TRS", variables);
    executionService.startProcessInstanceByKey("TRS", variables);
    executionService.startProcessInstanceByKey("TRS", variables);

    variables.put("distance", "other country");
    executionService.startProcessInstanceByKey("TRS", variables);
    executionService.startProcessInstanceByKey("TRS", variables);

    Map<String, Integer> choiceDistribution = historyService.choiceDistribution("TRS-1", "How far?");
    
    assertEquals(1, (int)choiceDistribution.get("far"));
    assertEquals(3, (int)choiceDistribution.get("nearby"));
    assertEquals(2, (int)choiceDistribution.get("other country"));
      
  }


}

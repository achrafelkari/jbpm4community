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
package org.jbpm.pvm.builder;

import java.util.List;
import java.util.Map;

import org.jbpm.api.model.Transition;
import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.test.BaseJbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class BuilderTest extends BaseJbpmTestCase {
  
  public void testBuilderInitialActivity() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess("p")
      .startActivity("start")
        .initial()
      .endActivity()
    .endProcess();
 
    ActivityImpl start = (ActivityImpl) processDefinition.getInitial();
    assertNotNull(start);
    assertEquals("start", start.getName());
  }

  public void testBuilderActivityProperties() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess("p")
      .startActivity("start")
        .startBehaviour(TestBehaviourBuilder.class)
          .testActivityProperty("some cfg value")
        .endBehaviour()
      .endActivity()
    .endProcess();
    
    ActivityImpl decisionActivity = (ActivityImpl) processDefinition.getActivity("start");
    assertNotNull(decisionActivity);
    assertEquals("start", decisionActivity.getName());
  }

  public void testBuilderMultipleOutgoingFlows() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess("p")
      .startActivity("x")
        .transition("a")
        .transition("b", "to b")
        .startFlow("c")
          .name("to c")
        .endFlow()
        .transition("x")
      .endActivity()
      .startActivity("a")
        .transition("b")
      .endActivity()
      .startActivity("b")
      .endActivity()
      .startActivity("c")
      .endActivity()
    .endProcess();

    ActivityImpl decisionActivity = (ActivityImpl) processDefinition.getActivity("x");
    assertNotNull(decisionActivity);
    
    List<Transition> outgoingTransitions = decisionActivity.getOutgoingTransitions();
    assertNotNull(outgoingTransitions);
    assertEquals("expected 4 transitions: "+outgoingTransitions, 4, outgoingTransitions.size());
    Transition toA = outgoingTransitions.get(0);
    assertNull(toA.getName());
    assertEquals("a", toA.getDestination().getName());
    
    Transition toB = outgoingTransitions.get(1);
    assertEquals("to b", toB.getName());
    assertEquals("b", toB.getDestination().getName());

    Transition toC = outgoingTransitions.get(2);
    assertEquals("to c", toC.getName());
    assertEquals("c", toC.getDestination().getName());
    
    Transition toX = outgoingTransitions.get(3);
    assertNull(toX.getName());
    assertEquals("x", toX.getDestination().getName());
    
    Map<String, Transition> outgoingTransitionsMap = decisionActivity.getOutgoingTransitionsMap();
    assertSame(toA, outgoingTransitionsMap.get(null));
    assertSame(toB, outgoingTransitionsMap.get("to b"));
    assertSame(toC, outgoingTransitionsMap.get("to c"));
    
    ActivityImpl b = (ActivityImpl) processDefinition.getActivity("b");
    List<Transition> incomingTransitions = b.getIncomingTransitions();
    assertNotNull(incomingTransitions);
    assertEquals("x", incomingTransitions.get(0).getSource().getName());
    assertEquals("a", incomingTransitions.get(1).getSource().getName());
    assertEquals(2, incomingTransitions.size());
  }

  public void testBuilderCompositeActivities() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess()
      .startActivity("1")
        .startActivity("1.1")
          .transition("1.2.1", "to onedottwodotone")
        .endActivity()
        .startActivity("1.2")
          .startActivity("1.2.1")
          .endActivity()
          .startActivity("1.2.2")
            .initial()
            .transition("1", "to one")
          .endActivity()
          .startActivity("1.2.3")
          .endActivity()
        .endActivity()
        .startActivity("1.3")
        .endActivity()
      .endActivity()
    .endProcess();

    ActivityImpl activity1 = (ActivityImpl) processDefinition.findActivity("1");
    ActivityImpl activity11 = (ActivityImpl) processDefinition.findActivity("1.1");
    ActivityImpl activity12 = (ActivityImpl) processDefinition.findActivity("1.2");
    ActivityImpl activity121 = (ActivityImpl) processDefinition.findActivity("1.2.1");
    ActivityImpl activity122 = (ActivityImpl) processDefinition.findActivity("1.2.2");
    ActivityImpl activity123 = (ActivityImpl) processDefinition.findActivity("1.2.3");
    ActivityImpl activity13 = (ActivityImpl) processDefinition.findActivity("1.3");
    
    assertNotNull(activity1);
    assertNotNull(activity11);
    assertNotNull(activity12);
    assertNotNull(activity121);
    assertNotNull(activity122);
    assertNotNull(activity123);
    assertNotNull(activity13);

    assertSame(activity1, processDefinition.getActivities().get(0));
    assertEquals(1, processDefinition.getActivities().size());

    assertSame(activity11, activity1.getActivities().get(0));
    assertSame(activity12, activity1.getActivities().get(1));
    assertSame(activity13, activity1.getActivities().get(2));
    assertEquals(3, activity1.getActivities().size());

    assertSame(activity121, activity12.getActivities().get(0));
    assertSame(activity122, activity12.getActivities().get(1));
    assertSame(activity123, activity12.getActivities().get(2));
    assertEquals(3, activity12.getActivities().size());
    
    assertSame(processDefinition, activity1.getParent());
    assertSame(activity1, activity11.getParent());
    assertSame(activity1, activity12.getParent());
    assertSame(activity12, activity121.getParent());
    assertSame(activity12, activity122.getParent());
    assertSame(activity12, activity123.getParent());
    assertSame(activity1, activity13.getParent());
    
    assertSame(activity1, activity122.getOutgoingTransition("to one").getDestination());
    assertSame(activity121, activity11.getOutgoingTransition("to onedottwodotone").getDestination());
  }
}

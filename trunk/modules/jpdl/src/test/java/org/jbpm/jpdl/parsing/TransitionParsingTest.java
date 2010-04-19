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
package org.jbpm.jpdl.parsing;

import java.util.List;

import org.jbpm.api.model.Activity;
import org.jbpm.api.model.Transition;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.xml.Problem;


/**
 * @author Tom Baeyens
 */
public class TransitionParsingTest extends JpdlParseTestCase {

  public void testTransition() {
    ClientProcessDefinition processDefinition = parse(
      "<process name='p'>" +
      "  <start name='a'>" +
      "    <transition to='b' />" +
      "  </start>" +
      "  <state name='b' />" +
      "</process>"
    );
    Activity a = processDefinition.getActivity("a");
    assertNotNull(a);
    Transition transition = a.getOutgoingTransitions().get(0);
    assertNotNull(transition);
    Activity b = processDefinition.getActivity("b");
    assertNotNull(b);
    assertSame(b, transition.getDestination());
  }

  public void testSelfTransition() {
    ClientProcessDefinition processDefinition = parse(
      "<process name='p'>" +
      "  <start name='a'>" +
      "    <transition to='a' />" +
      "  </start>" +
      "</process>"
    );
    Activity a = processDefinition.getActivity("a");
    assertNotNull(a);
    Transition transition = a.getOutgoingTransitions().get(0);
    assertNotNull(transition);
    assertSame(a, transition.getDestination());
  }

  public void testMissingTransitionDestination() {
    List<Problem> problems = parseProblems(
      "<process name='p'>" +
      "  <start name='a'>" +
      "    <transition />" +
      "  </start>" +
      "</process>"
    );
    assertTextPresent("attribute <transition to=\"...\" is required", problems.get(0).getMsg());
  }

  public void testEmptyTransitionDestination() {
    List<Problem> problems = parseProblems(
      "<process name='p'>" +
      "  <start name='a'>" +
      "    <transition to='' />" +
      "  </start>" +
      "</process>"
    );
    assertTextPresent("attribute <transition to=\"\" is empty", problems.get(0).getMsg());
  }

  public void testInvalidTransitionDestination() {
    List<Problem> problems = parseProblems(
      "<process name='p'>" +
      "  <start name='a'>" +
      "    <transition to='unexisting' />" +
      "  </start>" +
      "</process>"
    );
    assertTextPresent("attribute <transition to=\"unexisting\" doesn't reference an existing activity name", problems.get(0).getMsg());
  }

  public void testMultipleOutgoingTransitions() {
    ClientProcessDefinition processDefinition = parse(
      "<process name='p'>" +
      "  <start name='a'>" +
      "    <transition to='b' />" +
      "    <transition to='b' />" +
      "    <transition to='c' />" +
      "  </start>" +
      "  <state name='b' />" +
      "  <state name='c' />" +
      "</process>"
    );
    Activity a = processDefinition.getActivity("a");
    Activity b = processDefinition.getActivity("b");
    Activity c = processDefinition.getActivity("c");

    assertSame(b, a.getOutgoingTransitions().get(0).getDestination());
    assertSame(b, a.getOutgoingTransitions().get(1).getDestination());
    assertSame(c, a.getOutgoingTransitions().get(2).getDestination());
  }

  public void testMultipleIncomingTransitions() {
    ClientProcessDefinition processDefinition = parse(
      "<process name='p'>" +
      "  <start name='a'>" +
      "    <transition to='c' />" +
      "    <transition to='c' />" +
      "  </start>" +
      "  <state name='b'>" +
      "    <transition to='c' />" +
      "  </state>" +
      "  <state name='c' />" +
      "</process>"
    );
    Activity a = processDefinition.getActivity("a");
    Activity b = processDefinition.getActivity("b");
    Activity c = processDefinition.getActivity("c");

    assertSame(c, a.getOutgoingTransitions().get(0).getDestination());
    assertSame(c, a.getOutgoingTransitions().get(1).getDestination());
    assertSame(c, b.getOutgoingTransitions().get(0).getDestination());
  }

}

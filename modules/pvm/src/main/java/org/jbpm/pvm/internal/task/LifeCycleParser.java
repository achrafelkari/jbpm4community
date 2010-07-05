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
package org.jbpm.pvm.internal.task;

import java.util.List;

import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;
import org.jbpm.pvm.internal.model.TransitionImpl;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;
import org.w3c.dom.Element;

/**
 * @author Tom Baeyens
 */
public class LifeCycleParser extends Parser {

  public Object parseDocumentElement(Element element, Parse parse) {
    ProcessDefinitionImpl lifeCycleProcess = new LifeCycle();
    lifeCycleProcess.setName("TaskLifeCycle");
    
    List<Element> stateElements = XmlUtil.elements(element, "state");
    for (Element stateElement: stateElements) {
      parseState(stateElement, lifeCycleProcess, parse);
    }
    for (Element stateElement: stateElements) {
      parseTransitions(stateElement, lifeCycleProcess, parse);
    }
    
    String initialStateName = null;
    if (element.hasAttribute("initial")) {
      initialStateName = element.getAttribute("initial");
      ActivityImpl initial = lifeCycleProcess.getActivity(initialStateName);
      if (initial!=null) {
        lifeCycleProcess.setInitial(initial);
      } else {
        parse.addProblem("initial "+initialStateName+" doesn't exist", element);
      }
    }
    
    return lifeCycleProcess;
  }

  public void parseTransitions(Element element, ProcessDefinitionImpl lifeCycleProcess, Parse parse) {
    if (! element.hasAttribute("name")) {
      parse.addProblem("state doesn't have a name: "+XmlUtil.toString(element), element);
      return;
    }
    String stateName = element.getAttribute("name");
    ActivityImpl state = lifeCycleProcess.getActivity(stateName);
    
    List<Element> transitionElements = XmlUtil.elements(element, "transition");
    for (Element transitionElement: transitionElements) {
      parseTransition(transitionElement, state, parse);
    }
  }

  public void parseTransition(Element element, ActivityImpl state, Parse parse) {
    if (! element.hasAttribute("name")) {
      parse.addProblem("transition doesn't have a name: "+XmlUtil.toString(element), element);
      return;
    }
    String transitionName = element.getAttribute("name");
    if (! element.hasAttribute("to")) {
      parse.addProblem("transition "+transitionName+" doesn't have a to attribute indicating the destination state: "+XmlUtil.toString(element), element);
      return;
    }
    String destinationName = element.getAttribute("to");
    ActivityImpl destination = state.getProcessDefinition().getActivity(destinationName);
    if (destination!=null) {
      TransitionImpl transition = state.createOutgoingTransition();
      transition.setDestination(destination);
      transition.setName(transitionName);
    } else {
      parse.addProblem("destination "+destinationName+" of "+transitionName+" is unknown", element);
    }
  }

  public void parseState(Element element, ProcessDefinitionImpl lifeCycleProcess, Parse parse) {
    if (! element.hasAttribute("name")) {
      parse.addProblem("state doesn't have an id: "+XmlUtil.toString(element), element);
      return;
    }
    String stateName = element.getAttribute("name");
    ActivityImpl state = lifeCycleProcess.createActivity(stateName);
    state.setActivityBehaviour(new LifeCycleState());
  }
}

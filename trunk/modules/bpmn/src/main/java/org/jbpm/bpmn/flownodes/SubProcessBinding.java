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
package org.jbpm.bpmn.flownodes;

import java.util.HashSet;
import java.util.Set;

import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.api.model.Activity;
import org.jbpm.api.model.Transition;
import org.jbpm.bpmn.model.BpmnProcessDefinition;
import org.jbpm.bpmn.parser.BpmnParser;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.xml.Parse;
import org.w3c.dom.Element;

/**
 * @author Joram Barrez
 */
public class SubProcessBinding extends BpmnBinding {
  
  protected static final String TAG_NAME = "subProcess";

  public SubProcessBinding() {
    super(TAG_NAME);
  }
  
  public Object parse(Element element, Parse parse, BpmnParser bpmnParser) {
    SubProcessActivity subProcessActivity = new SubProcessActivity();
    
    ActivityImpl activity = parse.contextStackFind(ActivityImpl.class);
    bpmnParser.parseActivities(element, parse, activity);
    bpmnParser.parseSequenceFlow(element, parse, parse.contextStackFind(BpmnProcessDefinition.class));
    
    validateStartActivities(activity, parse);
    validateAllSequenceFlow(activity, parse);
    
    subProcessActivity.setDefault(getDefault());
    return subProcessActivity;
  }
  
  /**
   * Only none start activities and activities without incoming sequence flow
   * are allowed as start activities in a sub-process.
   */
  protected void validateStartActivities(ActivityImpl subProcessActivity, Parse parse) {
    for (Activity childActivity : subProcessActivity.getActivities()) {
      if (childActivity.getIncomingTransitions().isEmpty()) {

        ActivityBehaviour activityBehaviour = ((ActivityImpl) childActivity).getActivityBehaviour();
        if ( (activityBehaviour instanceof BpmnEvent)
                && !(activityBehaviour instanceof NoneStartEventActivity) ) {
          parse.addProblem("Only none start events are allowed in an embedded sub process. " +
                  "Event " + childActivity.getName() + " has no incoming sequence flow.");
        } 
        
      }
    }
  }
  
  /**
   * Sequence flow are not allowed to cross the sub-process boundary.
   * Exception to that rule is sequence flow which have as target a none start event (which actually
   * could graphically be viewed as boundary events).
   */
  protected void validateAllSequenceFlow(ActivityImpl subProcessActivity, Parse parse) {
    
    // collect all child activity ids
    Set<String> childActivityIds = new HashSet<String>();
    for (Activity childActivity : subProcessActivity.getActivities()) {
      childActivityIds.add(childActivity.getName());
    }
    
    // Verify source/target of all sequenceflow
    for (Activity childActivity : subProcessActivity.getActivities()) {
      for (Transition incomingTransition : childActivity.getIncomingTransitions()) {
        validateSequenceFlow(incomingTransition, childActivityIds, parse);          
      }
      for (Transition outgoingTransition : subProcessActivity.getOutgoingTransitions()) {
        validateSequenceFlow(outgoingTransition, childActivityIds, parse);
      }
    }
  }
  
  protected void validateSequenceFlow(Transition transition, Set<String> subProcessActivityIds, Parse parse) {
    if (!subProcessActivityIds.contains(transition.getSource().getName())) {
      parse.addProblem("Invalid sequence flow " + transition.getName() 
              + ": cannot cross sub-process boundaries from " + transition.getSource().getName()
              + " into the sub process activity.");
    }
    if (!subProcessActivityIds.contains(transition.getDestination().getName())) {
      parse.addProblem("Invalid sequence flow " + transition.getName() 
              + ": cannot cross sub-process boundaries to " + transition.getSource().getName()
              + " from within the sub process activity.");
    }
  }

}

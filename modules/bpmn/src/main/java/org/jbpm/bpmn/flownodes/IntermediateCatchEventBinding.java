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

import org.jbpm.bpmn.parser.BpmnParser;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.EventImpl;
import org.jbpm.pvm.internal.model.TimerDefinitionImpl;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.xml.Parse;
import org.w3c.dom.Element;

/**
 * @author Joram Barrez
 */
public class IntermediateCatchEventBinding extends BpmnBinding {
  
  public IntermediateCatchEventBinding() {
    super("intermediateCatchEvent");
  }
  
  public Object parse(Element element, Parse parse, BpmnParser bpmnParser) {
    String id = XmlUtil.attribute(element, "id");
    Element eventDefinition = XmlUtil.element(element);
    if ("timerEventDefinition".equals(eventDefinition.getNodeName())) {
      return createIntermediateTimerCatchEvent(id, eventDefinition, bpmnParser, parse);
    } else {
      parse.addProblem("Invalid eventDefinition type : " + eventDefinition.getNodeName());
    }
    
    return null;
  }
  
  protected IntermediateCatchTimerEvent createIntermediateTimerCatchEvent(String catchEventId, 
          Element eventDefinitionElement, BpmnParser parser, Parse parse) {
    
    IntermediateCatchTimerEvent intermediateCatchTimerEvent = new IntermediateCatchTimerEvent();
    TimerDefinitionImpl timerDefinition = parser.parseTimerEventDefinition(eventDefinitionElement, parse, catchEventId);
    
    // Attach the timerDefinition to the current activity.
    // That way, the PVM will automatically pcik it up when the execution arrives in the activity.
    ActivityImpl activity = parse.contextStackFind(ActivityImpl.class);
    activity.addTimerDefinition(timerDefinition);
    
    // The timer will fire an event on which this class will listen.
    String eventName = "INTERMEDIATE_TIMER_" + catchEventId;
    timerDefinition.setEventName(eventName);
    EventImpl timerEvent = activity.createEvent(eventName);
    timerEvent.createEventListenerReference(intermediateCatchTimerEvent);
   
    return intermediateCatchTimerEvent;
  }

}

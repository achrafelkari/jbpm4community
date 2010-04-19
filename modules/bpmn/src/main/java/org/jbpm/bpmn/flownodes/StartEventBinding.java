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

import java.util.List;

import org.jbpm.bpmn.model.BpmnProcessDefinition;
import org.jbpm.bpmn.parser.BpmnParser;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.job.StartProcessTimer;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.TimerDefinitionImpl;
import org.jbpm.pvm.internal.session.DbSession;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.xml.Parse;
import org.w3c.dom.Element;



public class StartEventBinding extends BpmnBinding {
  
  private static final Log LOG = Log.getLog(StartEventBinding.class.getName());

  public StartEventBinding() {
	  super("startEvent");
  }
  
  public Object parse(Element element, Parse parse, BpmnParser bpmnParser) {
    ActivityImpl startActivity = parse.contextStackFind(ActivityImpl.class);
    BpmnProcessDefinition processDefinition = parse.contextStackFind(BpmnProcessDefinition.class);
    
    if (processDefinition.getInitial()==null) {
      processDefinition.setInitial(startActivity);
      
    } else if (startActivity.getParentActivity()==null) {
      parse.addProblem("multiple start events not yet supported", element);
    }
    
    String id = XmlUtil.attribute(element, "id", true, parse);
    Element eventDefinition = XmlUtil.element(element);
    if (eventDefinition != null && "timerEventDefinition".equals(eventDefinition.getNodeName())) {
      return createTimerStartEvent(processDefinition, eventDefinition, id, bpmnParser, parse);
    } else if (eventDefinition != null){
      parse.addProblem("Invalid eventDefinition type : " + eventDefinition.getNodeName());
    }
    
    return new NoneStartEventActivity(); // default
  }
  
  /**
   * Timer start event
   */
  protected TimerStartEventActivity createTimerStartEvent(BpmnProcessDefinition processDefinition, 
          Element timerEventDefinition, String eventId, BpmnParser parser, Parse parse) {
    
    TimerStartEventActivity timerStartEvent = new TimerStartEventActivity();
    TimerDefinitionImpl timerDefinition = parser.parseTimerEventDefinition(timerEventDefinition, parse, eventId);
    
    if (timerDefinition == null) { // problem explanation will already be added to parse, no need to do it here
      return null;
    }
    
    StartProcessTimer startProcessTimer = new StartProcessTimer();
    startProcessTimer.setProcessDefinitionName(processDefinition.getName());
    
    if (timerDefinition.getDueDate() != null) {
      startProcessTimer.setDuedate(timerDefinition.getDueDate());
    } else if (timerDefinition.getDueDateDescription() != null) {
      startProcessTimer.setIntervalExpression(timerDefinition.getDueDateDescription());
    } else if (timerDefinition.getCronExpression() != null) {
      startProcessTimer.setIntervalExpression(timerDefinition.getCronExpression());
    }
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Scheduling a new start process timer for process definition " 
              + processDefinition.getName());
    }
    
    deleteStartProcessTimers(processDefinition.getName()); // Only the latest procDef should have a timer start
    startProcessTimer.schedule();
    return timerStartEvent;
  }
  
  /**
   * Deletes all existing timer start events for a given process definition.
   * 
   * This is required when a new version of a process definition with a timer start is deployed:
   * only the latest may version may be started by the timer start event.
   */
  protected void deleteStartProcessTimers(String processDefinitionName) {
    DbSession dbSession = EnvironmentImpl.getCurrent().get(DbSession.class);
    List<StartProcessTimer> existingTimers = dbSession.findStartProcessTimers(processDefinitionName);
    for (StartProcessTimer spt : existingTimers) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Removing existing start process timer: " + spt);
      }
      dbSession.delete(spt);
    }
  }
  
}

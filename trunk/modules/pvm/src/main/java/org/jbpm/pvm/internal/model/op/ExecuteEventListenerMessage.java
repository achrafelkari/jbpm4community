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
package org.jbpm.pvm.internal.model.op;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.job.MessageImpl;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.EventImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.model.ObservableElementImpl;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;
import org.jbpm.pvm.internal.model.TransitionImpl;

/**
 * @author Tom Baeyens
 */
public class ExecuteEventListenerMessage extends MessageImpl {

  private static final String KEY_EVENT_COMPLETED_OPERATION = "ECO";
  private static final String KEY_EVENT_LISTENER_INDEX = "ELI";
  private static final String KEY_EVENT_NAME = "EN";
  private static final String KEY_EVENT_OBSERVABLE_PARENT_LEVEL = "EOPL";
  private static final String KEY_TRANSITION_SOURCE_INDEX = "TSI";
  private static final String KEY_TRANSITION_SOURCE = "TS";
  private static final String KEY_EVENT_SOURCE_TRANSITION = "EST";
  private static final String KEY_EVENT_SOURCE_ACTIVITY = "ESA";
  private static final String KEY_EVENT_SOURCE_PROCESS_DEFINITION = "ESPD";
  private static final String KEY_STATE = "S";
  
  private static final long serialVersionUID = 1L;

  public ExecuteEventListenerMessage() {
  }

  public ExecuteEventListenerMessage(ExecutionImpl execution) {
    super(execution);
    
    Map<String, Object> asyncExecutionInfo = new HashMap<String, Object>();
    
    TransitionImpl transition = execution.getTransition();
    if (transition!=null) {
      ActivityImpl source = transition.getSource();
      asyncExecutionInfo.put(KEY_TRANSITION_SOURCE, source.getName());
      asyncExecutionInfo.put(KEY_TRANSITION_SOURCE_INDEX, source.getOutgoingTransitions().indexOf(transition));
    }

    asyncExecutionInfo.put(KEY_STATE, execution.getState());

    ObservableElementImpl eventSource = execution.getEventSource();
    if (eventSource instanceof ProcessDefinitionImpl) {
      asyncExecutionInfo.put(KEY_EVENT_SOURCE_PROCESS_DEFINITION, null);
      
    } else if (eventSource instanceof ActivityImpl) {
      asyncExecutionInfo.put(KEY_EVENT_SOURCE_ACTIVITY, eventSource.getName());
      
    } else if (eventSource instanceof TransitionImpl) {
      asyncExecutionInfo.put(KEY_EVENT_SOURCE_TRANSITION, null);
    }

    EventImpl event = execution.getEvent();
    asyncExecutionInfo.put(KEY_EVENT_OBSERVABLE_PARENT_LEVEL, getEventObservableParentLevel(eventSource, event.getObservableElement()));
    asyncExecutionInfo.put(KEY_EVENT_NAME, event.getName());
    
    asyncExecutionInfo.put(KEY_EVENT_LISTENER_INDEX, execution.getEventListenerIndex());
    
    AtomicOperation eventCompletedOperation = execution.getEventCompletedOperation();
    String eventCompletedOperationText = null;
    if (eventCompletedOperation!=null) {
      eventCompletedOperationText = eventCompletedOperation.toString();
    }
    asyncExecutionInfo.put(KEY_EVENT_COMPLETED_OPERATION, eventCompletedOperationText);
    
    setConfiguration(asyncExecutionInfo);
  }

  public Integer getEventObservableParentLevel(ObservableElementImpl eventSource, ObservableElementImpl observableElement) {
    int parentLevel = 0;
    while (eventSource!=observableElement) {
      parentLevel++;
      eventSource = eventSource.getParent();
    }
    return parentLevel;
  }

  protected void executeVoid(Environment environment) throws Exception {
    Map<?, ?> asyncExecutionInfo = (Map<?, ?>) getConfiguration();

    String transitionSourceName = (String) asyncExecutionInfo.get(KEY_TRANSITION_SOURCE);
    TransitionImpl transition = null;
    if (transitionSourceName!=null) {
      ProcessDefinitionImpl processDefinition = execution.getProcessDefinition();
      ActivityImpl transitionSource = processDefinition.findActivity(transitionSourceName);
      Integer transitionIndex = (Integer) asyncExecutionInfo.get(KEY_TRANSITION_SOURCE_INDEX);
      transition = (TransitionImpl) transitionSource.getOutgoingTransitions().get(transitionIndex);
      execution.setTransition(transition);
    }

    if (asyncExecutionInfo.containsKey(KEY_EVENT_SOURCE_PROCESS_DEFINITION)){
      ProcessDefinitionImpl processDefinition = execution.getProcessDefinition();
      execution.setEventSource(processDefinition);

    } else if (asyncExecutionInfo.containsKey(KEY_EVENT_SOURCE_ACTIVITY)){
      ProcessDefinitionImpl processDefinition = execution.getProcessDefinition();
      String activityName = (String) asyncExecutionInfo.get(KEY_EVENT_SOURCE_ACTIVITY);
      ActivityImpl activity = processDefinition.findActivity(activityName);
      execution.setEventSource(activity);
    
    } else if (asyncExecutionInfo.containsKey(KEY_EVENT_SOURCE_TRANSITION)){
      execution.setEventSource(transition);
    }
    
    ObservableElementImpl observableElement = execution.getEventSource();
    int parentLevel = (Integer) asyncExecutionInfo.get(KEY_EVENT_OBSERVABLE_PARENT_LEVEL);
    for (int i=0; i<parentLevel; parentLevel++) {
      observableElement = observableElement.getParent();
    }
    
    String eventName = (String) asyncExecutionInfo.get(KEY_EVENT_NAME);
    EventImpl event = observableElement.getEvent(eventName);
    execution.setEvent(event);
    
    Integer eventListenerIndex = (Integer) asyncExecutionInfo.get(KEY_EVENT_LISTENER_INDEX);
    execution.setEventListenerIndex(eventListenerIndex);

    String eventCompletedOperationText = (String) asyncExecutionInfo.get(KEY_EVENT_COMPLETED_OPERATION);
    AtomicOperation eventCompletedOperation = AtomicOperation.parseAtomicOperation(eventCompletedOperationText);
    execution.setEventCompletedOperation(eventCompletedOperation);
    
    execution.setState((String) asyncExecutionInfo.get(KEY_STATE));

    execution.performAtomicOperationSync(AtomicOperation.EXECUTE_EVENT_LISTENER);
  }

  @Override
  public String toString() {
    return "ExecuteEventListenerMessage[" + dbid + "]";
  }
}

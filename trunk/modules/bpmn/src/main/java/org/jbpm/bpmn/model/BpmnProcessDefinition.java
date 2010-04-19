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
package org.jbpm.bpmn.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.api.model.Transition;
import org.jbpm.bpmn.common.Resource;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;
import org.jbpm.pvm.internal.model.VariableDefinitionImpl;
import org.jbpm.pvm.internal.task.TaskDefinitionImpl;
import org.w3c.dom.Element;

public class BpmnProcessDefinition extends ProcessDefinitionImpl {

  private static final long serialVersionUID = 1L;

  protected Map<String, TaskDefinitionImpl> taskDefinitions = new HashMap<String, TaskDefinitionImpl>();
  protected List<VariableDefinitionImpl> processVariableDefinitions = new ArrayList<VariableDefinitionImpl>();
  protected Map<String, Element> messages = new HashMap<String, Element>();
  protected Map<String, Element> itemDefinitions = new HashMap<String, Element>();
  protected Map<String, Element> interfaces = new HashMap<String, Element>();
  protected Map<String, Element> operations = new HashMap<String, Element>();
  protected Map<String, Resource> resources = new HashMap<String, Resource>();
  protected Map<String, Transition> sequenceFlow = new HashMap<String, Transition>();
  protected Map<String, Set<String>> sourceToTargetMapping = new HashMap<String, Set<String>>(); // convience mapping: sourceRefId to all targetRefs

  protected ExecutionImpl newProcessInstance() {
    return new ExecutionImpl();
  }

  public TaskDefinitionImpl createTaskDefinition(String name) {
    TaskDefinitionImpl taskDefinition = new TaskDefinitionImpl();
    taskDefinitions.put(name, taskDefinition);
    return taskDefinition;
  }

  public Map<String, TaskDefinitionImpl> getTaskDefinitions() {
    return taskDefinitions;
  }

  public String getType(String typeRef) {
    return itemDefinitions.get(typeRef).getAttribute("strutcureRef");
  }

  public void setVariableDefinition(List<VariableDefinitionImpl> variableDefinitions) {
    this.processVariableDefinitions = variableDefinitions;
  }

  public Resource getResource(String ref) {
    return resources.get(ref);
  }

  public Map<String, Resource> getResources() {
    return resources;
  }

  public Map<String, Element> getInterfaces() {
    return interfaces;
  }

  public void setInterfaces(Map<String, Element> interfaces) {
    this.interfaces = interfaces;
  }

  
  public Map<String, Element> getOperations() {
    return operations;
  }

  
  public void setOperations(Map<String, Element> operations) {
    this.operations = operations;
  }
  
  public Map<String, Element> getMessages() {
    return messages;
  }

  
  public void setMessages(Map<String, Element> messages) {
    this.messages = messages;
  }

  
  public Map<String, Element> getItemDefinitions() {
    return itemDefinitions;
  }

  
  public void setItemDefinitions(Map<String, Element> itemDefinitions) {
    this.itemDefinitions = itemDefinitions;
  }

  public void addSequenceFlow(String transitionId, Transition transition) {
    this.sequenceFlow.put(transitionId, transition);
    
    String source = transition.getSource().getName();
    if (sourceToTargetMapping.get(source) == null) {
      sourceToTargetMapping.put(source, new HashSet<String>());
    }
    sourceToTargetMapping.get(source).add(transition.getDestination().getName());
  }
  
  public Map<String, Set<String>> getSourceToTargetMapping() {
    return sourceToTargetMapping;
  }
  
  public boolean isReachable(String srcActivityId , String dstActivityId) {
    return isReachable(srcActivityId, dstActivityId, new HashSet<String>());
  }
  
  protected boolean isReachable(String srcActivityId , String dstActivityId, Set<String> alreadyVisited) {
    if (srcActivityId.equals(dstActivityId)) {
      return true;
    } else {
      alreadyVisited.add(srcActivityId);
      Set<String> directReachable = sourceToTargetMapping.get(srcActivityId);
      if (directReachable != null) {
        for (String destinationId : sourceToTargetMapping.get(srcActivityId)) {
          if (!alreadyVisited.contains(destinationId)) {
            if (isReachable(destinationId, dstActivityId, alreadyVisited)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

}

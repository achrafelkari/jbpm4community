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
package org.jbpm.pvm.internal.builder;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;

/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionBuilder extends CompositeBuilder {
  
  private static final Log log = Log.getLog(ProcessDefinitionBuilder.class.getName());
  
  protected ProcessDefinitionImpl processDefinition;
  protected List<UnresolvedFlow> unresolvedFlows = new ArrayList<UnresolvedFlow>();

  protected ProcessDefinitionBuilder(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
    this.compositeElement = processDefinition;
  }

  public static ProcessDefinitionBuilder startProcess() {
    return startProcess(null);
  }

  public static ProcessDefinitionBuilder startProcess(String processDefinitionName) {
    return startProcess(processDefinitionName, new ProcessDefinitionImpl());
  }

  public static ProcessDefinitionBuilder startProcess(String processDefinitionName, ProcessDefinitionImpl processDefinition) {
    processDefinition.setName(processDefinitionName);
    return new ProcessDefinitionBuilder(processDefinition);
  }
  
  public ProcessDefinitionImpl endProcess() {
    verifyInitial();
    resolveFlows();
    return processDefinition;
  }

  protected void verifyInitial() {
    if (processDefinition.getInitial()==null) {
      errorNoInitial();
    }
  }

  protected void resolveFlows() {
    for (UnresolvedFlow unresolvedFlow: unresolvedFlows) {
      ActivityImpl destination = (ActivityImpl) processDefinition.findActivity(unresolvedFlow.destinationName);
      if (destination==null) {
        errorUnexistingFlowDestination(unresolvedFlow);
      }
      destination.addIncomingTransition(unresolvedFlow.transition);
    }
  }

  public ProcessDefinitionBuilder key(String key) {
    processDefinition.setKey(key);
    return this;
  }

  public ProcessDefinitionBuilder version(int version) {
    processDefinition.setVersion(version);
    return this;
  }

  public ProcessDefinitionBuilder description(String description) {
    processDefinition.setDescription(description);
    return this;
  }

  protected void addUnresolvedFlow(UnresolvedFlow unresolvedFlow) {
    unresolvedFlows.add(unresolvedFlow);
  }

  protected void setProcessDefinitionInitial(ActivityImpl initial) {
    if (processDefinition.getInitial()!=null) {
      errorMultipleInitials(initial);
    }
    processDefinition.setInitial(initial);
  }

  protected void errorMultipleInitials(ActivityImpl initial) {
    log.error("multiple initial activities: "+processDefinition.getInitial()+" and "+initial);
  }

  protected void errorNoInitial() {
    log.error("no initial activity");
  }

  protected void errorUnexistingFlowDestination(UnresolvedFlow unresolvedFlow) {
    String sourceActivityName = unresolvedFlow.transition.getSource().getName();
    log.error("unexisting transition destination: "+sourceActivityName+"-->"+unresolvedFlow.destinationName);
  }
}

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

import java.lang.reflect.Constructor;

import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.Continuation;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;

/**
 * @author Tom Baeyens
 */
public class ActivityBuilder extends CompositeBuilder {
  
  private static final Class<?>[] ACTIVITYBEHAVIOURBUILDER_PARAMTYPES = new Class<?>[]{ActivityBuilder.class};

  /** the enclosing composite */
  protected CompositeBuilder compositeBuilder;
  protected ActivityImpl activity;
  
  public ActivityBuilder(CompositeBuilder compositeBuilder, String activityName) {
    this.compositeBuilder = compositeBuilder;
    this.activity = compositeBuilder.createActivity();
    this.compositeElement = activity;
    this.activity.setName(activityName);
  }
  
  public ActivityBuilder initial() {
    setProcessDefinitionInitial(activity);
    return this;
  }
  
  protected void setProcessDefinitionInitial(ActivityImpl initial) {
    compositeBuilder.setProcessDefinitionInitial(initial);
  }

  public <T extends ActivityBehaviourBuilder> T startBehaviour(Class<T> activityBehaviourBuilderType) {
    return startBehaviour(null, activityBehaviourBuilderType);
  }

  public <T extends ActivityBehaviourBuilder> T startBehaviour(String activityName, Class<T> activityBehaviourBuilderType) {
    if (activityBehaviourBuilderType==null) {
      throw new RuntimeException("activityBuilderType is null");
    }
    try {
      Constructor<T> constructor = activityBehaviourBuilderType.getConstructor(ACTIVITYBEHAVIOURBUILDER_PARAMTYPES);
      T activityBuilder = constructor.newInstance(new Object[]{this});
      return activityBuilder;
    } catch (Exception e) {
      throw new RuntimeException("couldn't instantiate activity behaviour builder type "+activityBehaviourBuilderType.getName(), e);
    }
  }
  
  public CompositeBuilder endActivity() {
    return compositeBuilder;
  }
  
  public FlowBuilder startFlow(String to) {
    UnresolvedFlow unresolvedFlow = new UnresolvedFlow();
    unresolvedFlow.transition = activity.createOutgoingTransition();
    unresolvedFlow.destinationName = to;
    addUnresolvedFlow(unresolvedFlow);
    return new FlowBuilder(this, unresolvedFlow.transition);
  }
  
  public ProcessDefinitionImpl endProcess() {
    return compositeBuilder.endProcess();
  }
  
  public ActivityBuilder transition(String to) {
    startFlow(to);
    return this;
  }

  public ActivityBuilder transition(String to, String name) {
    startFlow(to).name(name);
    return this;
  }

  public ActivityBuilder asyncExecute() {
    activity.setContinuation(Continuation.ASYNCHRONOUS);
    return this;
  }

  protected void addUnresolvedFlow(UnresolvedFlow unresolvedFlow) {
    compositeBuilder.addUnresolvedFlow(unresolvedFlow);
  }
}

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

import org.jbpm.api.JbpmException;
import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.pvm.internal.model.CompositeElementImpl;
import org.jbpm.pvm.internal.model.EventImpl;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.StringDescriptor;

/**
 * @author Tom Baeyens
 */
public abstract class CompositeBuilder {

  protected CompositeElementImpl compositeElement;
  
  public abstract ProcessDefinitionImpl endProcess();
  protected abstract void addUnresolvedFlow(UnresolvedFlow unresolvedFlow);
  protected abstract void setProcessDefinitionInitial(ActivityImpl initial);

  protected ActivityImpl createActivity() {
    return compositeElement.createActivity();
  }

  public EventImpl createEvent(String eventName) {
    return compositeElement.createEvent(eventName);
  }
  
  public CompositeBuilder variable(String name) {
    return startVariable(name)
      .endVariable();
  }
  
  public CompositeBuilder variable(String name, String type) {
    return startVariable(name)
      .type(type)
      .endVariable();
  }
  
  public TimerBuilder startTimer() {
    return new TimerBuilder(this, null);
  }

  public TimerBuilder startTimer(String eventName) {
    return new TimerBuilder(this, eventName);
  }
  
  public VariableBuilder startVariable(String name) {
    return new VariableBuilder(this)
      .name(name);
  }

  public ActivityBuilder startActivity() {
    return startActivity((String)null);
  }

  public ActivityBuilder startActivity(String activityName) {
    return new ActivityBuilder(this, activityName);
  }

  public ActivityBuilder startActivity(Descriptor activityDescriptor) {
    return startActivity(null, activityDescriptor);
  }

  public ActivityBuilder startActivity(String activityName, Descriptor activityDescriptor) {
    if (activityDescriptor==null) {
      throw new RuntimeException("activityDescriptor is null");
    }
    ActivityBuilder activityBuilder = new ActivityBuilder(this, activityName);
    activityBuilder.activity.setActivityBehaviourDescriptor(activityDescriptor);
    return activityBuilder;
  }

  public ActivityBuilder startActivity(ActivityBehaviour activityBehaviour) {
    return startActivity(null, activityBehaviour);
  }

  public ActivityBuilder startActivity(String activityName, ActivityBehaviour activityBehaviour) {
    if (activityBehaviour==null) {
      throw new RuntimeException("activity is null");
    }
    ActivityBuilder activityBuilder = new ActivityBuilder(this, activityName);
    activityBuilder.activity.setActivityBehaviour(activityBehaviour);
    return activityBuilder;
  }

  public ActivityBuilder startActivity(Class<? extends ActivityBehaviour> activityClass) {
    return startActivity(null, new ObjectDescriptor(activityClass));
  }

  public ActivityBuilder startActivity(String activityName, Class<? extends ActivityBehaviour> activityClass) {
    return startActivity(activityName, new ObjectDescriptor(activityClass));
  }

  public EventBuilder startEvent(String eventName) {
    return new EventBuilder(this, eventName);
  }
  
  public CompositeExceptionHandlerBuilder startExceptionHandler(Class<? extends Throwable> exceptionType) {
    return new CompositeExceptionHandlerBuilder(this, exceptionType);
  }
  
  public CompositeBuilder property(String name, String value) {
    return property(new StringDescriptor(name, value));
  }

  public CompositeBuilder property(Descriptor descriptor) {
    compositeElement.addProperty(descriptor);
    return this;
  }

  public CompositeBuilder endActivity() {
    throw new JbpmException("calling endActivity on a processBuilder is invalid");
  }

  public FlowBuilder startFlow(String to) {
    throw new JbpmException("calling startFlow on a processBuilder is invalid");
  }
}

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

import org.jbpm.api.listener.EventListener;
import org.jbpm.api.model.Event;
import org.jbpm.pvm.internal.model.TransitionImpl;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.descriptor.StringDescriptor;


/**
 * @author Tom Baeyens
 */
public class FlowBuilder extends ObservableBuilder {

  protected ActivityBuilder activityBuilder;
  protected TransitionImpl transition;
  
  public FlowBuilder(ActivityBuilder activityBuilder, TransitionImpl transition) {
    super(transition, Event.TAKE);
    this.activityBuilder = activityBuilder;
    this.transition = transition;
  }
  
  public FlowBuilder name(String name) {
    transition.setName(name);
    return this;
  }
  
  public FlowBuilder expr(String expression) {
    // transition.setExpression(expression);
    return this;
  }
  
  public FlowBuilder listener(EventListener eventListener) {
    addListener(eventListener);
    return this;
  }

  public FlowBuilder listener(EventListener eventListener, boolean propagation) {
    addListener(eventListener, propagation);
    return this;
  }

  public FlowBuilder listener(Descriptor descriptor) {
    addListener(descriptor);
    return this;
  }

  public FlowBuilder listener(Descriptor descriptor, boolean propagation) {
    addListener(descriptor, propagation);
    return this;
  }

  public FlowBuilder property(String name, String value) {
    StringDescriptor descriptor = new StringDescriptor(name, value);
    transition.addProperty(descriptor);
    return this;
  }

  public ActivityBuilder endFlow() {
    return activityBuilder;
  }
}

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

import java.util.Date;

import org.jbpm.api.listener.EventListener;
import org.jbpm.pvm.internal.model.TimerDefinitionImpl;
import org.jbpm.pvm.internal.wire.Descriptor;

/**
 * @author Tom Baeyens
 */
public class TimerBuilder extends ObservableBuilder {
  
  protected CompositeBuilder compositeBuilder;
  protected TimerDefinitionImpl timerDefinition;

  public TimerBuilder(CompositeBuilder compositeBuilder, String eventName) {
    super(compositeBuilder.compositeElement, eventName);
    this.compositeBuilder = compositeBuilder;
    
    timerDefinition = compositeBuilder.compositeElement.createTimerDefinition();
  }
  
  public TimerBuilder dueDate(String dueDateCalendarExpression) {
    timerDefinition.setDueDateDescription(dueDateCalendarExpression);
    return this;
  }
  
  public TimerBuilder dueDate(Date dueDate) {
    timerDefinition.setDueDate(dueDate);
    return this;
  }
  
  public TimerBuilder repeat(String repeatCalendarExpression) {
    timerDefinition.setRepeat(repeatCalendarExpression);
    return this;
  }
  
  public TimerBuilder retries(int retries) {
    timerDefinition.setRetries(retries);
    return this;
  }

  public TimerBuilder signal(String signalName) {
    timerDefinition.setSignalName(signalName);
    return this;
  }
  
  public TimerBuilder decision() {
    timerDefinition.setExclusive(true);
    return this;
  }
  
  public TimerBuilder listener(EventListener eventListener) {
    addListener(eventListener);
    return this;
  }

  public TimerBuilder listener(EventListener eventListener, boolean propagation) {
    addListener(eventListener, propagation);
    return this;
  }

  public TimerBuilder listener(Descriptor descriptor) {
    addListener(descriptor);
    return this;
  }

  public TimerBuilder listener(Descriptor descriptor, boolean propagation) {
    addListener(descriptor, propagation);
    return this;
  }

  public CompositeBuilder endTimer() {
    return compositeBuilder;
  }
}

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
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.descriptor.StringDescriptor;


/**
 * @author Tom Baeyens
 */
public class EventBuilder extends ObservableBuilder {

  protected CompositeBuilder compositeBuilder;

  public EventBuilder(CompositeBuilder compositeBuilder, String eventName) {
    super(compositeBuilder.compositeElement, eventName);
    this.compositeBuilder = compositeBuilder;
  }
  
  public EventBuilder listener(EventListener eventListener) {
    addListener(eventListener);
    return this;
  }

  public EventBuilder listener(EventListener eventListener, boolean propagation) {
    addListener(eventListener, propagation);
    return this;
  }

  public EventBuilder listener(Descriptor descriptor) {
    addListener(descriptor);
    return this;
  }

  public EventBuilder listener(Descriptor descriptor, boolean propagation) {
    addListener(descriptor, propagation);
    return this;
  }

  public EventBuilder property(String name, String value) {
    StringDescriptor descriptor = new StringDescriptor(name, value);
    getEvent().addProperty(descriptor);
    return this;
  }
  
  public EventExceptionHandlerBuilder startExceptionHandler(Class<? extends Throwable> exceptionType) {
    return new EventExceptionHandlerBuilder(this, exceptionType);
  }
  
  public CompositeBuilder endEvent() {
    return compositeBuilder;
  }
}

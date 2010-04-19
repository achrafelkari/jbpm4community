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
import org.jbpm.pvm.internal.model.EventImpl;
import org.jbpm.pvm.internal.model.EventListenerReference;
import org.jbpm.pvm.internal.model.ObservableElementImpl;
import org.jbpm.pvm.internal.wire.Descriptor;


/**
 * @author Tom Baeyens
 */
public class ObservableBuilder {

  protected ObservableElementImpl observableElement;
  protected String eventName;
  protected EventImpl event = null;
  
  public ObservableBuilder(ObservableElementImpl observableElement, String eventName) {
    this.observableElement = observableElement;
    this.eventName = eventName;
  }

  protected void addListener(EventListener eventListener) {
    getEvent().createEventListenerReference(eventListener);
  }

  protected void addListener(EventListener eventListener, boolean propagation) {
    EventListenerReference eventListenerReference = getEvent().createEventListenerReference(eventListener);
    eventListenerReference.setPropagationEnabled(propagation);
  }

  protected void addListener(Descriptor descriptor) {
    getEvent().createEventListenerReference(descriptor);
  }

  protected void addListener(Descriptor descriptor, boolean propagation) {
    EventListenerReference eventListenerReference = getEvent().createEventListenerReference(descriptor);
    eventListenerReference.setPropagationEnabled(propagation);
  }

  protected EventImpl getEvent() {
    if (event==null) {
      this.event = observableElement.createEvent(eventName);
    }
    return event;
  }
}

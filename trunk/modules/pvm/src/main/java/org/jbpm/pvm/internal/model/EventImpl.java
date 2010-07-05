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
package org.jbpm.pvm.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.api.listener.EventListener;
import org.jbpm.api.model.Event;
import org.jbpm.pvm.internal.wire.Descriptor;

/**
 * @author Tom Baeyens
 */
public class EventImpl extends ProcessElementImpl implements Event {

  private static final long serialVersionUID = 1L;

  protected String name;
  protected ObservableElementImpl observableElement;
  protected List<EventListenerReference> listenerReferences;
  protected Continuation continuation = Continuation.SYNCHRONOUS;

  public String toString() {
    return (name != null ? "event(" + name + ")" : "event");
  }

  // listener references //////////////////////////////////////////////////////
  
  public EventListenerReference createEventListenerReference() {
    if (listenerReferences==null) {
      listenerReferences = new ArrayList<EventListenerReference>();
    }
    EventListenerReference activityReference = new EventListenerReference();
    activityReference.setProcessDefinition(processDefinition);
    listenerReferences.add(activityReference);
    return activityReference;
  }

  public EventListenerReference createEventListenerReference(Descriptor descriptor) {
    EventListenerReference eventListenerReference = createEventListenerReference();
    eventListenerReference.setEventListenerDescriptor(descriptor);
    return eventListenerReference;
  }
  
  public EventListenerReference createEventListenerReference(EventListener eventListener) {
    EventListenerReference eventListenerReference = createEventListenerReference();
    eventListenerReference.setEventListener(eventListener);
    return eventListenerReference;
  }
  
  public boolean isAsync() {
    return ! (continuation==Continuation.SYNCHRONOUS);
  }

  // getters and setters //////////////////////////////////////////////////////

  public long getDbid() {
    return dbid;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public List<EventListenerReference> getListenerReferences() {
    return listenerReferences;
  }
  public void setListenerReferences(List<EventListenerReference> eventListenerReferences) {
    this.listenerReferences = eventListenerReferences;
  }
  public Continuation getContinuation() {
    return continuation;
  }
  public void setContinuation(Continuation continuation) {
    this.continuation = continuation;
  }
  public ObservableElementImpl getObservableElement() {
    return observableElement;
  }
  public void setObservableElement(ObservableElementImpl observableElement) {
    this.observableElement = observableElement;
  }
}
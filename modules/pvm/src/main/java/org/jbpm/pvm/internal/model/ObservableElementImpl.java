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

import java.util.HashMap;
import java.util.Map;

import org.jbpm.api.model.Event;


/** observable and visible process elements.
 * 
 * @author Tom Baeyens
 */
public class ObservableElementImpl extends ProcessElementImpl implements ObservableElement {

  private static final long serialVersionUID = 1L;
  
  protected String name;
  protected String description;
  protected Map<String, EventImpl> events;
  
  // default parent is null ///////////////////////////////////////////////////

  public ObservableElementImpl getParent() {
    return null;
  }
  
  // events ///////////////////////////////////////////////////////////////////
  
  public EventImpl getEvent(String eventName) {
    return (events != null ? events.get(eventName) : null);
  }

  public boolean hasEvent(String eventName) {
    return (getEvent(eventName)!=null);
  }

  /** event factory method that also establishes the bidirectional relation. */
  public EventImpl createEvent(String eventName) {
    EventImpl event = new EventImpl();
    event.setObservableElement(this);
    event.setName(eventName);
    event.setProcessDefinition(processDefinition);
    return addEvent(event);
  }

  /**
   * add an event to this processDefinition element.
   * @throws NullPointerException if event is null.
   */
  public EventImpl addEvent(EventImpl event) {
    if (events == null) {
      events = new HashMap<String, EventImpl>();
    }
    events.put(event.getName(), event);
    return event;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public Map<String, ? extends Event> getEvents() {
    return events;
  }
}

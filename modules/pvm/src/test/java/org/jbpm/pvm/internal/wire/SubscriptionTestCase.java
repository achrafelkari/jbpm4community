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
package org.jbpm.pvm.internal.wire;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.pvm.internal.util.Listener;
import org.jbpm.test.BaseJbpmTestCase;

/**
 * @author Tom Baeyens
 */
public abstract class SubscriptionTestCase extends BaseJbpmTestCase {

  public static class Event {
    Object source;
    String eventName;
    Object info;
    public Event(Object source, String eventName, Object info) {
      this.source = source;
      this.eventName = eventName;
      this.info = info;
    }
    public String toString() {
      return "("+source+"|"+eventName+"|"+info+")";
    }
  }
  
  static List<Event> events = null;
  
  public void setUp() throws Exception {
    super.setUp();
    events = new ArrayList<Event>();
  }

  public void tearDown() throws Exception {
    events = null;
    super.tearDown();
  }

  public static class Recorder implements Listener {
    public void event(Object source, String eventName, Object info) {
      events.add(new Event(source, eventName, info));
    }
  }
}

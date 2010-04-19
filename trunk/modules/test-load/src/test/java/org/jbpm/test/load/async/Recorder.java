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
package org.jbpm.test.load.async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 
 * @author Tom Baeyens
 */
public class Recorder {

  public Map<String, List<String>> executionEvents = null;
  
  public synchronized void record(String executionId, String event) {
    if (executionEvents==null) {
      executionEvents = new HashMap<String, List<String>>();
    }
    List<String> events = executionEvents.get(executionId);
    if (events == null) {
      events = new ArrayList<String>();
      executionEvents.put(executionId, events);
    }
    events.add(event); 
  }
  
  public void reset() {
    executionEvents = null;
  }
}

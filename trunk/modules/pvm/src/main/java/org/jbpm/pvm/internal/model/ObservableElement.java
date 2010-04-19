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

import java.util.Set;


/** a super class for process elements that have events.
 * 
 * @author Tom Baeyens
 */
public interface ObservableElement {
  
  /** the meaningless database primary key */
  long getDbid();

  /** the short display name given to this element. */
  String getName();

  /** the process definition to which this process element belongs */
  OpenProcessDefinition getProcessDefinition();

  /** the property value for the given key or null if no such 
   * configuration key is present.
   * 
   * This is extra static configuration information that can be 
   * associated to a process element.  Process languages can use this 
   * to store configuration properties for extensions in it.  That way, 
   * these extensions can be added without changing the process model 
   * or the database schema. Examples of extensions that may use these 
   * configuration properties are simulation, predictive scheduling, 
   * graphical information,... */
  Object getProperty(String key);
  
  /** the set of available property keys or an empty set in case 
   * there are no keys. */
  Set<String> getPropertyKeys();

  /** indicates if the given event is defined no this element.
   * This method only looks in this observable element and it will not 
   * search the parent hierarchy for the given event. */
  boolean hasEvent(String eventName);
  
  /** the enclosing activity or the process definition */
  ObservableElement getParent();
}

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
package org.jbpm.api.cmd;

/** exposes configured process engine scope objects and 
 * transaction scope objects {@link Command to command implementations}.
 * 
 * @author Tom Baeyens
 */
public interface Environment {

  /**
   * searches a named object in all the contexts in the default search order. 
   * @return the object if it exists in the environment, <code>null</code> if there is no object with the given name in the environment.
   */
  public abstract Object get(String name);

  /** searches an object based on type.  The search doesn take superclasses of the context elements 
   * into account.
   * @return the first object of the given type or null in case no such element was found.  
   */
  public abstract <T> T get(Class<T> type);

}
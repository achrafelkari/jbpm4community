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
package org.jbpm.pvm.internal.id;

import org.jbpm.api.Execution;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;


/**
 * @author Tom Baeyens
 */
public abstract class IdComposer {

  private static IdComposer defaultIdComposer = new MemoryIdComposer();

  public static IdComposer getIdComposer() {
    IdComposer idComposer = EnvironmentImpl.getFromCurrent(IdComposer.class, false);
    if (idComposer!=null) {
      return idComposer;
    }
    return getDefaultIdComposer();
  }
  
  public static IdComposer getDefaultIdComposer() {
    return defaultIdComposer;
  }

  /** generates a unique id for the given execution. */
  public abstract String createId(ProcessDefinition processDefinition, Execution parent, ExecutionImpl execution);
}

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
package org.jbpm.pvm.internal.env;

import java.util.HashSet;
import java.util.Set;

import org.jbpm.api.JbpmException;
import org.jbpm.pvm.internal.model.ExecutionImpl;

public class ExecutionContext implements Context {
  
  protected ExecutionImpl execution;

  public String getName() {
    return CONTEXTNAME_EXECUTION;
  }

  public ExecutionContext(ExecutionImpl execution) {
    this.execution = execution;
  }

  public Object get(String key) {
    if (CONTEXTNAME_EXECUTION.equals(key)) {
      return execution;
    }
    return execution.getVariable(key);
  }

  public boolean has(String key) {
    if (CONTEXTNAME_EXECUTION.equals(key)) {
      return true;
    }
    return execution.hasVariable(key);
  }

  public Set<String> keys() {
    Set<String> keys = new HashSet<String>(execution.getVariableKeys());
    keys.add(CONTEXTNAME_EXECUTION);
    return keys;
  }

  public Object set(String key, Object value) {
    if (CONTEXTNAME_EXECUTION.equals(key)) {
      throw new JbpmException("can't set execution");
    }
    execution.setVariable(key, value);
    return null;
  }

  public <T> T get(Class<T> type) {
    if (type.isAssignableFrom(ExecutionImpl.class)) {
      return type.cast(execution);
    }
    return null;
  }

  public ExecutionImpl getExecution() {
    return execution;
  }
}

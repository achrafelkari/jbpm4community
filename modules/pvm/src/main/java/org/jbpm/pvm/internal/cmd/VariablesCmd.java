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
package org.jbpm.pvm.internal.cmd;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.client.ClientExecution;
import org.jbpm.pvm.internal.session.DbSession;

/**
 * @author Tom Baeyens
 */
public abstract class VariablesCmd<T> extends AbstractCommand<T> {

  private static final long serialVersionUID = 1L;

  protected Map<String, ?> variables;
  private Map<String, Object> internalMap;

  public void addVariable(String key, Object variable) {
    if (internalMap == null) {
      if (variables != null) {
        throw new JbpmException("variables were set externally");
      }
      variables = internalMap = new HashMap<String, Object>();
    }
    internalMap.put(key, variable);
  }

  public Map<String, ?> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, ?> variables) {
    this.variables = variables;
  }

  protected ClientExecution getExecution(Environment environment, String executionId) {
    DbSession dbSession = environment.get(DbSession.class);
    ClientExecution execution = dbSession.findExecutionById(executionId);
    if (execution == null) {
      throw new JbpmException("execution " + executionId + " doesn't exist");
    }
    return execution;
  }

}

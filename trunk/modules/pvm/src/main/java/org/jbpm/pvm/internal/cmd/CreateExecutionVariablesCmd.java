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

import java.util.Map.Entry;

import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.model.ExecutionImpl;

/**
 * @author Alejandro Guizar
 */
public class CreateExecutionVariablesCmd extends VariablesCmd<Void> {

  protected String executionId;
  protected boolean historyEnabled;

  private static final long serialVersionUID = 1L;

  public CreateExecutionVariablesCmd(String executionId, boolean historyEnabled) {
    this.executionId = executionId;
    this.historyEnabled = historyEnabled;
  }

  public Void execute(Environment environment) throws Exception {
    ExecutionImpl execution = (ExecutionImpl) getExecution(environment, executionId);
    for (Entry<String, ?> entry : variables.entrySet()) {
      execution.createVariable(entry.getKey(), entry.getValue(), null, historyEnabled);
    }
    return null;
  }

}

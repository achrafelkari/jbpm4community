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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.env.EnvironmentImpl;

/**
 * @author Tom Baeyens
 * @author Huisheng Xu
 */
public class VariableOutDefinitionSet implements Serializable {

  private static final long serialVersionUID = 1L;

  protected List<VariableOutDefinitionImpl> variableOutDefinitions;

  public void processOutVariables(ExecutionImpl outerExecution, ScopeInstanceImpl innerScopeInstance) {
    // loop over all variable definitions
    if (hasVariableOutDefinitions()) {
      for (VariableOutDefinitionImpl variableOutDefinition: variableOutDefinitions) {
        String variableName = variableOutDefinition.getName();
        if (variableName!=null) {
          // TODO update evaluateExpression so that scopeInstance can be passed in directly
          String expression = variableOutDefinition.getExpression();
          String language = variableOutDefinition.getLanguage();

          Object value = Expression.create(expression, language).evaluateInScope(innerScopeInstance);
          outerExecution.setVariable(variableName, value);
        }
      }
    }
  }

  public boolean hasVariableOutDefinitions() {
    return ( (variableOutDefinitions!=null)
             && (!variableOutDefinitions.isEmpty())
           );
  }

  public List<VariableOutDefinitionImpl> getVariableOutDefinitions() {
    if (variableOutDefinitions==null) {
      return Collections.emptyList();
    }
    return variableOutDefinitions;
  }

  public VariableOutDefinitionImpl createVariableOutDefinition() {
    VariableOutDefinitionImpl variableOutDefinition = new VariableOutDefinitionImpl();
    if (variableOutDefinitions==null) {
      variableOutDefinitions = new ArrayList<VariableOutDefinitionImpl>();
    }
    variableOutDefinitions.add(variableOutDefinition);
    return variableOutDefinition;
  }
}

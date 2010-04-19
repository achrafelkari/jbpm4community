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
package org.jbpm.jpdl.internal.activity;

import java.io.Serializable;

import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.pvm.internal.el.Expression;

/**
 * @author Tom Baeyens
 */
public class RulesFact implements Serializable {

  private static final long serialVersionUID = 1L;
  
  protected String variableName;
  protected Expression expression;
  protected String language;
  
  public String getVariableName() {
    return variableName;
  }
  
  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }
  
  public Expression getExpression() {
    return expression;
  }
  
  public void setExpression(Expression expression) {
    this.expression = expression;
  }
  
  public String getLanguage() {
    return language;
  }
  
  public void setLanguage(String language) {
    this.language = language;
  }

  public Object getObject(ActivityExecution execution) {
    if (variableName!=null) {
      return execution.getVariable(variableName);

    } else if (expression!=null) {
      return expression.evaluate(execution);
    }

    return null;
  }
}

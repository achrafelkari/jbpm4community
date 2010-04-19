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
package org.jbpm.bpmn.model;

import org.jbpm.api.JbpmException;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.pvm.internal.model.Condition;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.model.ExpressionCondition;

/**
 * Specialized {@link Condition} implementation for BPMN 2.0 sequence flow.
 * 
 * The difference with the JPDL {@link ExpressionCondition} is that expressions are always
 * resolved against the given execution, instead of the executionContext in the environment.
 * 
 * In some BPMN 2.0 processes, the regular resolving of expressions failed because of 
 * execution juggling inside activities (see eg. Inclusive Gateway test cases), hence this impl.
 * 
 * @author Joram Barrez
 */
public class SequenceflowCondition implements Condition {
  
  private static final long serialVersionUID = 1L;

  protected String expression;
  
  protected String language;
  
  public boolean evaluate(OpenExecution execution) {
    ExecutionImpl executionImpl = (ExecutionImpl) execution;
    Object result = executionImpl.resolveExpression(expression, language);
    if (result instanceof Boolean) {
      return ((Boolean) result).booleanValue();
    } else {
      throw new JbpmException("Expression '" + expression + "' did not resolve to a boolean value");
    }
  }
  
  public String getExpression() {
    return expression;
  }
  
  public void setExpression(String expression) {
    this.expression = expression;
  }

  public String getLanguage() {
    return language;
  }
  
  public void setLanguage(String language) {
    this.language = language;
  }

}

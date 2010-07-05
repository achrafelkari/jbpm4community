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
package org.jbpm.pvm.internal.el;

import javax.el.ELContext;
import javax.el.ValueExpression;

import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.api.task.Task;
import org.jbpm.pvm.internal.model.ScopeInstanceImpl;


/**
 * @author Tom Baeyens
 * @author Huisheng Xu
 */
public class UelValueExpression extends Expression {

  private static final long serialVersionUID = 1L;

  protected ValueExpression valueExpression;

  public UelValueExpression(ValueExpression valueExpression) {
    this.valueExpression = valueExpression;
    if (valueExpression==null) {
      throw new JbpmException("no value expression configured");
    }
  }

  public Object evaluateInScope(ScopeInstanceImpl scopeInstance) {
    ELContext elContext = getElContext(scopeInstance);
    return valueExpression.getValue(elContext);
  }

  public void setValue(Task task, Object value) {
    setValue((ScopeInstanceImpl)task, value);
  }

  public void setValue(Execution execution, Object value) {
    setValue((ScopeInstanceImpl)execution, value);
  }

  public void setValue(ScopeInstanceImpl scopeInstance, Object value) {
    ELContext elContext = getElContext(scopeInstance);
    valueExpression.setValue(elContext, value);
  }

  public String getExpressionString() {
    return valueExpression.getExpressionString();
  }

  public boolean isLiteralText() {
    return false;
  }

}

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

import java.io.Serializable;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;

import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.api.task.Task;
import org.jbpm.pvm.internal.model.ScopeInstanceImpl;

/** handles all expression resolving
 *
 * @author Tom Baeyens
 * @author Huisheng Xu
 */
public abstract class Expression implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final String LANGUAGE_UEL = "uel";
  public static final String LANGUAGE_UEL_METHOD = LANGUAGE_UEL + "-method";
  public static final String LANGUAGE_UEL_VALUE = LANGUAGE_UEL + "-value";

  private static ExpressionFactory expressionFactory;
  private static ELContext elContext;

  public static Expression create(String expressionText) {
    return create(expressionText, null);
  }

  public static Expression create(String expressionText, String language) {
    // if there is an expr specified
    if (expressionText==null) {
      throw new JbpmException("expressionText is null");
    }

    if (language==null || language.startsWith(LANGUAGE_UEL)) {
      if (expressionText.indexOf('{')==-1) {
        return new StaticTextExpression(expressionText);
      }
      initExpressionObjects();

      // by default, expr is interpreted as a value expression
      if (language==null || LANGUAGE_UEL_VALUE.equals(language)) {
        try {
          ValueExpression valueExpression = expressionFactory
              .createValueExpression(elContext, expressionText, Object.class);

          return new UelValueExpression(valueExpression);

        // if the expr is not a valid value expr...
        } catch (ELException e) {
          // ... and the expr-type was not specified
          if (language==null) {
            // then try to parse it as a method expression
            language = LANGUAGE_UEL_METHOD;
          }
        }
      }

      if (LANGUAGE_UEL_METHOD.equals(language)) {
        MethodExpression methodExpression = expressionFactory
            .createMethodExpression(elContext, expressionText, null, new Class<?>[]{});
        return new UelMethodExpression(methodExpression);
      }

    }
    return new ScriptExpression(expressionText, language);
  }

  protected static synchronized void initExpressionObjects() {
    if (elContext==null) {
      JbpmElFactory jbpmElFactory = JbpmElFactory.getJbpmElFactory();
      expressionFactory = jbpmElFactory.createExpressionFactory();
      elContext = jbpmElFactory.createElContext();
    }
  }

  // runtime evaluation ///////////////////////////////////////////////////////

  public Object evaluate() {
    return evaluateInScope(null);
  }

  public Object evaluate(Execution execution) {
    return evaluateInScope((ScopeInstanceImpl)execution);
  }

  public Object evaluate(Task task) {
    return evaluateInScope((ScopeInstanceImpl)task);
  }

  public abstract Object evaluateInScope(ScopeInstanceImpl scopeInstance);

  protected ELContext getElContext(ScopeInstanceImpl scopeInstance) {
    if (scopeInstance == null) {
      return JbpmElFactory.getJbpmElFactory().createElContext();
    }

    ELContext elContext = (ELContext) scopeInstance.getElContext();
    if (elContext == null) {
      elContext = JbpmElFactory.getJbpmElFactory().createElContext(scopeInstance);
      scopeInstance.setElContext(elContext);
    }
    return elContext;
  }

  public abstract String getExpressionString();

  public abstract boolean isLiteralText();
}

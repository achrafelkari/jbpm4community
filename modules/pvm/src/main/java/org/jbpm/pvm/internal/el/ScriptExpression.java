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

import org.jbpm.pvm.internal.model.ScopeInstanceImpl;
import org.jbpm.pvm.internal.script.ScriptManager;


/**
 * This is script expression, it will use ScriptManager to evaluate the expr by specified language.
 *
 * @author Tom Baeyens
 * @author Huisheng Xu
 */
public class ScriptExpression extends Expression {

  private static final long serialVersionUID = 1L;

  protected String expressionText;
  protected String language;

  public ScriptExpression(String expressionText, String language) {
    this.expressionText = expressionText;
    this.language = language;
  }

  public Object evaluateInScope(ScopeInstanceImpl scopeInstance) {
    ScriptManager scriptManager = ScriptManager.getScriptManager();
    return scriptManager.evaluateExpression(expressionText, language);
  }

  public String getExpressionString() {
    return expressionText;
  }

  public boolean isLiteralText() {
    return false;
  }
}

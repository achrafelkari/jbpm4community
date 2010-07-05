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

import org.jbpm.api.model.OpenExecution;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.el.Expression;

/**
 * @author Tom Baeyens
 * @author Huisheng Xu
 */
public class ScriptActivity extends JpdlAutomaticActivity {

  private static final long serialVersionUID = 1L;

  protected String script;
  protected String language;
  protected String variableName;

  public void perform(OpenExecution execution) {
    Object returnValue = Expression.create(script, language).evaluate(execution);

    if (variableName != null) {
      execution.setVariable(variableName, returnValue);
    }
  }

  public void setScript(String script) {
    this.script = script;
  }
  public void setLanguage(String language) {
    this.language = language;
  }
  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }
}

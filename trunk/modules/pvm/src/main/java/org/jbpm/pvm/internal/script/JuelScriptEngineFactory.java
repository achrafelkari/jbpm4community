/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jbpm.pvm.internal.script;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngine;
import java.util.List;
import java.util.Collections;

/**
 * https://jira.jboss.org/jira/browse/JBPM-2120
 * 
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class JuelScriptEngineFactory implements ScriptEngineFactory {

  private static List<String> names = Collections.singletonList("juel");
  private static List<String> extensions = names;
  private static List<String> mimeTypes = Collections.emptyList();

  public String getEngineName() {
    return "juel";
  }

  public String getEngineVersion() {
    return de.odysseus.el.ExpressionFactoryImpl.class.getPackage().getImplementationVersion();
  }

  public List<String> getExtensions() {
    return extensions;
  }

  public String getLanguageName() {
    return "JSP 2.1 EL";
  }

  public String getLanguageVersion() {
    return "2.1";
  }

  public String getMethodCallSyntax(String obj, String m, String... args) {
    throw new UnsupportedOperationException("getMethodCallSyntax");
  }

  public List<String> getMimeTypes() {
    return mimeTypes;
  }

  public List<String> getNames() {
    return names;
  }

  public String getOutputStatement(String toDisplay) {
    StringBuilder statement = new StringBuilder();
    statement.append("out:print(\"");

    for (int i = 0, len = toDisplay.length(); i < len; ++i) {
      char ch = toDisplay.charAt(i);
      switch (ch) {
      case '"':
        statement.append("\\\"");
        break;
      case '\\':
        statement.append("\\\\");
        break;
      default:
        statement.append(ch);
      }
    }

    return statement.append("\")").toString();
  }

  public String getParameter(String key) {
    if (key.equals("javax.script.name")) {
      return getLanguageName();
    }
    if (key.equals("javax.script.engine")) {
      return getEngineName();
    }
    if (key.equals("javax.script.engine_version")) {
      return getEngineVersion();
    }
    if (key.equals("javax.script.language")) {
      return getLanguageName();
    }
    if (key.equals("javax.script.language_version")) {
      return getLanguageVersion();
    }
    if (key.equals("THREADING")) {
      return "MULTITHREADED";
    }

    return null;
  }

  public String getProgram(String... statements) {
    StringBuilder program = new StringBuilder();

    if (statements.length != 0) {
      for (int i = 0; i < statements.length; ++i) {
        program.append("${");
        program.append(statements[i]);
        program.append("} ");
      }
    }
    return program.toString();
  }

  public ScriptEngine getScriptEngine() {
    return new JuelScriptEngine(this);
  }
}

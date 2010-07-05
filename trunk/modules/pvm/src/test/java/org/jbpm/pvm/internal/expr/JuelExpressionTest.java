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
package org.jbpm.pvm.internal.expr;

import org.jbpm.pvm.activities.WaitState;
import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.env.EnvironmentFactory;
import org.jbpm.pvm.internal.env.ExecutionContext;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.processengine.ProcessEngineImpl;
import org.jbpm.pvm.internal.script.ScriptManager;
import org.jbpm.test.BaseJbpmTestCase;

/**
 * @author Tom Baeyens
 */
public class JuelExpressionTest extends BaseJbpmTestCase {
  
  public void testJuelExpression() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
        "<jbpm-configuration>" +
        "  <process-engine-context>" +
        "    <script-manager default-expression-language='juel'" +
        "                    default-script-language='juel'>" +
        "      <script-language name='juel' factory='com.sun.script.juel.JuelScriptEngineFactory' />" +
        "    </script-manager>" +
        "  </process-engine-context>" +
        "</jbpm-configuration>"
    );

    ScriptManager scriptManager = environmentFactory.get(ScriptManager.class);
    
    ExecutionImpl execution = (ExecutionImpl) ProcessDefinitionBuilder
    .startProcess()
      .startActivity("initial", new WaitState())
        .initial()
      .endActivity()
    .endProcess()
    .startProcessInstance();
    
    execution.setVariable("pv", "hello");
    
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    environment.setContext(new ExecutionContext(execution));
    try {
      assertEquals("hello", scriptManager.evaluateExpression("#{pv}", null));
    } finally {
      environment.close();
    }
  }
}

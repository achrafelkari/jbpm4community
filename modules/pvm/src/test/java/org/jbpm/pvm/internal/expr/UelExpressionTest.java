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

import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.el.UelValueExpression;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class UelExpressionTest extends JbpmTestCase {
  
  public void testUelExpression() {
    ExecutionImpl execution = (ExecutionImpl) ProcessDefinitionBuilder
    .startProcess()
      .startActivity("initial", new WaitState())
        .initial()
      .endActivity()
    .endProcess()
    .startProcessInstance();
    
    Expression expression = Expression.create("#{pv}", Expression.LANGUAGE_UEL_VALUE);
    UelValueExpression uve = ((UelValueExpression) expression);
    
    execution.setVariable("pv", null);       
    
    assertEquals(null, uve.evaluate(execution));
  }  
}

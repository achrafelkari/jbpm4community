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

import org.jbpm.api.JbpmException;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.model.Activity;
import org.jbpm.api.model.Transition;
import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.model.ExecutionImpl;

/**
 * @author Tom Baeyens
 */
public class DecisionExpressionActivity extends JpdlActivity {

  private static final long serialVersionUID = 1L;

  protected Expression expression;

  public void execute(ActivityExecution execution) {
    execute((ExecutionImpl) execution); 
  }
  
  public void execute(ExecutionImpl execution) {
    Activity activity = execution.getActivity();
    String transitionName = null;

    Object result = expression.evaluate(execution);
    if ( (result!=null)
         && (! (result instanceof String))
       ) {
      throw new JbpmException("expression '"+expression+"' in decision '"+activity.getName()+"' returned "+result.getClass().getName()+" instead of a transitionName (String): "+result);
    }
    transitionName = (String) result;
    
    Transition transition = activity.getOutgoingTransition(transitionName);
    if (transition==null) {
      throw new JbpmException("expression '"+expression+"' in decision '"+activity.getName()+"' returned unexisting outgoing transition name: "+transitionName);
    }
    
    execution.historyDecision(transitionName);

    execution.take(transition);
  }

  public void setExpression(Expression expression) {
    this.expression = expression;
  }
}

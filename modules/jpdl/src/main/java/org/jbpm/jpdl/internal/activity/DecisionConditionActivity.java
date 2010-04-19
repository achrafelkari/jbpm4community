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

import java.util.List;

import org.jbpm.api.JbpmException;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.model.Activity;
import org.jbpm.api.model.Transition;
import org.jbpm.pvm.internal.model.Condition;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.model.TransitionImpl;


/**
 * @author Tom Baeyens
 */
public class DecisionConditionActivity extends JpdlActivity {

  private static final long serialVersionUID = 1L;

  public void execute(ActivityExecution execution) throws Exception {
    execute((ExecutionImpl) execution); 
  }
  
  public void execute(ExecutionImpl execution) throws Exception {
    Transition transition = findTransitionUsingConditions(execution);
    if (transition==null) {
      throw new JbpmException("no outgoing transition condition evaluated to true for decision "+execution.getActivity());
    }
    if (transition.getName()!=null) {
      execution.historyDecision(transition.getName());
    }
    execution.take(transition);
  }

  public static Transition findTransitionUsingConditions(ExecutionImpl execution) {
    Activity activity = execution.getActivity();
    List<TransitionImpl> outgoingTransitions = (List) activity.getOutgoingTransitions();
    for (TransitionImpl transition : outgoingTransitions) {
      Condition condition = transition.getCondition();
      if  ( (condition==null)
            || (condition.evaluate(execution))
          ) {
        return transition;
      }
    }
    return null;
  }
}

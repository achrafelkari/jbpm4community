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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.api.Execution;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.model.Activity;
import org.jbpm.api.model.Transition;
import org.jbpm.pvm.internal.model.Condition;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.model.TransitionImpl;


/**
 * @author Tom Baeyens
 */
public class ForkActivity extends JpdlActivity {

  private static final long serialVersionUID = 1L;
  
  public void execute(ActivityExecution execution) {
    execute((ExecutionImpl)execution);
  }

  public void execute(ExecutionImpl execution) {
    Activity activity = execution.getActivity();

    // evaluate the conditions and find the transitions that should be forked
    List<Transition> forkingTransitions = new ArrayList<Transition>();
    List<TransitionImpl> outgoingTransitions = (List) activity.getOutgoingTransitions();
    for (TransitionImpl transition: outgoingTransitions) {
      Condition condition = transition.getCondition();
      if  ( (condition==null)
            || (condition.evaluate(execution))
          ) {
        forkingTransitions.add(transition);
      }
    }

    // if no outgoing transitions should be forked, 
    if (forkingTransitions.size()==0) {
      // end this execution
      execution.end();

    // if there is exactly 1 transition to be taken, just use the incoming execution
    } else if (forkingTransitions.size()==1) {
      execution.take(forkingTransitions.get(0));
      
    // if there are more transitions
    } else {
      ExecutionImpl concurrentRoot = null;
      if (Execution.STATE_ACTIVE_ROOT.equals(execution.getState())) {
        concurrentRoot = execution;
        execution.setState(Execution.STATE_INACTIVE_CONCURRENT_ROOT);
        execution.setActivity(null);
      } else if (Execution.STATE_ACTIVE_CONCURRENT.equals(execution.getState())) {
        concurrentRoot = execution.getParent();
        execution.end();
      }

      Map<Transition, ExecutionImpl> childExecutionsMap = new HashMap<Transition, ExecutionImpl>();
      for (Transition transition : forkingTransitions) {
        // launch a concurrent path of execution
        String childExecutionName = transition.getName();
        ExecutionImpl concurrentExecution = concurrentRoot.createExecution(childExecutionName);
        concurrentExecution.setActivity(activity);
        concurrentExecution.setState(Execution.STATE_ACTIVE_CONCURRENT);
        childExecutionsMap.put(transition, concurrentExecution);
      }
      
      for (Transition transition : childExecutionsMap.keySet()) {
        childExecutionsMap.get(transition).take(transition);

        if (concurrentRoot.isEnded()) {
          break;
        }
      }
    }
  }
}

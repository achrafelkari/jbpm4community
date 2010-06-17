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
import java.util.Map.Entry;

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

    // evaluate the conditions and select the forking transitions
    List<Transition> forkingTransitions = new ArrayList<Transition>();
    for (Transition transition: activity.getOutgoingTransitions()) {
      Condition condition = ((TransitionImpl) transition).getCondition();
      if (condition==null || condition.evaluate(execution)) {
        forkingTransitions.add(transition);
      }
    }

    switch (forkingTransitions.size()) {
    case 0:
      // if no outgoing transitions should be forked, end this execution
      execution.end();
      break;
    case 1:
      // if there is exactly one transition to be taken, just use the incoming execution
      execution.take(forkingTransitions.get(0));
      break;
    default:
      // if there are more transitions, perform full fork
      ExecutionImpl concurrentRoot;
      if (Execution.STATE_ACTIVE_ROOT.equals(execution.getState())) {
        concurrentRoot = execution;
        execution.setState(Execution.STATE_INACTIVE_CONCURRENT_ROOT);
        execution.setActivity(null);
      }
      else if (Execution.STATE_ACTIVE_CONCURRENT.equals(execution.getState())) {
        concurrentRoot = execution.getParent();
        execution.end();
      }
      else {
        throw new AssertionError(execution.getState());
      }

      Map<Transition, ExecutionImpl> concurrentExecutions = new HashMap<Transition, ExecutionImpl>();
      for (Transition transition : forkingTransitions) {
        ExecutionImpl concurrentExecution = concurrentRoot.createExecution(transition.getName());
        concurrentExecution.setActivity(activity);
        concurrentExecution.setState(Execution.STATE_ACTIVE_CONCURRENT);
        concurrentExecutions.put(transition, concurrentExecution);
      }

      for (Entry<Transition, ExecutionImpl> entry : concurrentExecutions.entrySet()) {
        entry.getValue().take(entry.getKey());
        if (concurrentRoot.isEnded()) break;
      }
    }
  }
}

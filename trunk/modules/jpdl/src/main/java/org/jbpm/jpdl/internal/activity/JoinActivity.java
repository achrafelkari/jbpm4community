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
import java.util.Collection;
import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.model.Activity;
import org.jbpm.api.model.Transition;
import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;

/**
 * @author Tom Baeyens
 */
public class JoinActivity extends JpdlActivity {

  private static final long serialVersionUID = 1L;
  
  private LockMode lockMode = LockMode.UPGRADE;
  private Expression multiplicity;

  public void execute(ActivityExecution execution) {
    execute((ExecutionImpl)execution);
  }

  public void execute(ExecutionImpl execution) {
    ActivityImpl activity = execution.getActivity();
    
    // if this is a single, non concurrent root
    if (Execution.STATE_ACTIVE_ROOT.equals(execution.getState())) {
      // just pass through
      Transition transition = activity.getDefaultOutgoingTransition();
      if (transition==null) {
        throw new JbpmException("join must have an outgoing transition");
      }
      execution.take(transition);
      
    } else if (Execution.STATE_ACTIVE_CONCURRENT.equals(execution.getState())) {
      
      // force version increment in the parent execution
      Session session = EnvironmentImpl.getFromCurrent(Session.class);
      session.lock(execution.getParent(), lockMode);

      execution.setState(Execution.STATE_INACTIVE_JOIN);
      execution.waitForSignal();

      ExecutionImpl concurrentRoot = execution.getParent();
      List<ExecutionImpl> joinedExecutions = getJoinedExecutions(concurrentRoot, activity);
      
      if (isComplete(execution, joinedExecutions)) {
        endExecutions(joinedExecutions);
        // if multiplicity was used
        if (multiplicity != null) {
          // collect concurrent executions still active
          List<ExecutionImpl> danglingExecutions = new ArrayList<ExecutionImpl>();
          for (ExecutionImpl concurrentExecution : concurrentRoot.getExecutions()) {
            if (Execution.STATE_ACTIVE_CONCURRENT.equals(concurrentExecution.getState())) {
              danglingExecutions.add(concurrentExecution);
            }
          }
          // end dangling executions
          endExecutions(danglingExecutions);
        }
        ExecutionImpl outgoingExecution = null;
        if (concurrentRoot.getExecutions().isEmpty()) {
          outgoingExecution = concurrentRoot;
          outgoingExecution.setState(Execution.STATE_ACTIVE_ROOT);
        } else {
          outgoingExecution = concurrentRoot.createExecution();
          outgoingExecution.setState(Execution.STATE_ACTIVE_CONCURRENT);
        }

        execution.setActivity(activity, outgoingExecution);
        Transition transition = activity.getDefaultOutgoingTransition();
        if (transition==null) {
          throw new JbpmException("join must have an outgoing transition");
        }
        outgoingExecution.take(transition);
      }
      
    } else {
      throw new JbpmException("invalid execution state");
    }
  }

  protected boolean isComplete(ExecutionImpl execution, List<ExecutionImpl> joinedExecutions) {
    int executionsToJoin;
    if (multiplicity != null) {
      executionsToJoin = evaluateMultiplicity(execution);
    }
    else {
      executionsToJoin = execution.getActivity().getIncomingTransitions().size();
    }
    return joinedExecutions.size() == executionsToJoin;
  }

  protected List<ExecutionImpl> getJoinedExecutions(ExecutionImpl concurrentRoot, Activity activity) {
    List<ExecutionImpl> joinedExecutions = new ArrayList<ExecutionImpl>();
    Collection<ExecutionImpl> concurrentExecutions = concurrentRoot.getExecutions();
    for (ExecutionImpl concurrentExecution: concurrentExecutions) {
      if ( (Execution.STATE_INACTIVE_JOIN.equals(concurrentExecution.getState()))
           && (concurrentExecution.getActivity()==activity)
         ) {
        joinedExecutions.add(concurrentExecution);
      }
    }
    return joinedExecutions;
  }

  protected void endExecutions(List<ExecutionImpl> executions) {
    for (ExecutionImpl execution: executions) {
      execution.end();
    }
  }

  private int evaluateMultiplicity(ExecutionImpl execution) {
    if (multiplicity != null) {
      Object value = multiplicity.evaluate(execution);
      if (value instanceof Number) {
        Number number = (Number) value;
        return number.intValue();
      }
      if (value instanceof String) {
        return Integer.parseInt((String) value);
      }
    }
    return -1;
  }

  public void setLockMode(LockMode lockMode) {
    this.lockMode = lockMode;
  }  
  public void setMultiplicity(Expression multiplicity) {
    this.multiplicity = multiplicity;
  }
}

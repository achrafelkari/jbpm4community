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
import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.model.Activity;
import org.jbpm.api.model.Transition;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;


/**
 * @author Tom Baeyens
 */
public class JoinActivity extends JpdlActivity {

  private static final long serialVersionUID = 1L;
  
  int multiplicity = -1;
  LockMode lockMode = LockMode.UPGRADE;

  public void execute(ActivityExecution execution) {
    execute((ExecutionImpl)execution);
  }

  public void execute(ExecutionImpl execution) {
    Activity activity = execution.getActivity();
    
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
      
      if (isComplete(joinedExecutions, activity)) {
        endJoinedExecutions(joinedExecutions);

        ExecutionImpl outgoingExecution = null;
        if (concurrentRoot.getExecutions().size()==0) {
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
  
  protected boolean isComplete(List<ExecutionImpl> joinedExecutions, Activity activity) {
    int nbrOfExecutionsToJoin = multiplicity;
    if (multiplicity==-1) {
      nbrOfExecutionsToJoin = activity.getIncomingTransitions().size();
    }
    return joinedExecutions.size()==nbrOfExecutionsToJoin;
  }

  protected List<ExecutionImpl> getJoinedExecutions(ExecutionImpl concurrentRoot, Activity activity) {
    List<ExecutionImpl> joinedExecutions = new ArrayList<ExecutionImpl>();
    List concurrentExecutions = (List)concurrentRoot.getExecutions();
    for (ExecutionImpl concurrentExecution: (List<ExecutionImpl>)concurrentExecutions) {
      if ( (Execution.STATE_INACTIVE_JOIN.equals(concurrentExecution.getState()))
           && (concurrentExecution.getActivity()==activity)
         ) {
        joinedExecutions.add(concurrentExecution);
      }
    }
    return joinedExecutions;
  }

  protected void endJoinedExecutions(List<ExecutionImpl> joinedExecutions) {
    for (ExecutionImpl joinedExecution: joinedExecutions) {
      joinedExecution.end();
    }
  }

  public void setMultiplicity(int multiplicity) {
    this.multiplicity = multiplicity;
  }
  public void setLockMode(LockMode lockMode) {
    this.lockMode = lockMode;
  }
}

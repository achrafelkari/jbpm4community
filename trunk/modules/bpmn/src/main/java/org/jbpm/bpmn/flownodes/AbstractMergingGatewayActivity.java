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
package org.jbpm.bpmn.flownodes;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.model.Activity;
import org.jbpm.api.model.Transition;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;

/**
 * Superclass for gateway activities that wait on multiple incoming executions before merging
 * them together. 
 * 
 * The {@link InclusiveGatewayActivity} and {@link ParallelGatewayActivity} are examples of such
 * gateways which have merge behaviour. 
 * 
 * @author Joram Barrez
 */
public abstract class AbstractMergingGatewayActivity extends AbstractGatewayActivity {
  
  private static final long serialVersionUID = 1L;

  private static final Log LOG = Log.getLog(AbstractMergingGatewayActivity.class.getName());
  
  protected LockMode lockMode = LockMode.UPGRADE;
  
  public void execute(ActivityExecution execution) {
    execute((ExecutionImpl) execution);
  }
  
  /**
   * Executing the actvity logic is common for all gateway types that have merge/split behaviour.
   * 
   * For all incoming sequence flow, the gateway will handle the executions. When all sequence
   * flow have arrived at the gateway, the fork logic is executed (a gateway can have both
   * merging and splitting behaviour).
   */
  public void execute(ExecutionImpl execution) { 
    int nrOfIncoming = execution.getActivity().getIncomingTransitions().size();
    
    if (nrOfIncoming == 1) { // no join behaviour needed, save some time and do a fork immediately
      
      if (LOG.isDebugEnabled()) {
        LOG.debug("Only one incoming sequence flow found. Executing fork logic.");
      }
      fork(execution);
      
    } else {
      
      if (LOG.isDebugEnabled()) {
        LOG.debug("Multiple incoming sequence flow found. Handling incoming execution.");
      }
      boolean allExecutionsArrived = handleIncomingExecution(execution);
      
      if (LOG.isDebugEnabled()) {
        LOG.debug("All executions arrived at the gateway: " + allExecutionsArrived);
      }
      
      // After executing the join functionality, it could be that all executions have arrived 
      // at the gateway. In that case, the gateway can be left.
      if (allExecutionsArrived) {
        ExecutionImpl outgoingExecution = join(execution);
        fork(outgoingExecution);
      }
      
    }
  }
  
  /**
   * Joins the incoming executions.
   * Returns true if all executions have reached the gateway.
   */
  protected boolean handleIncomingExecution(ExecutionImpl execution) {
    if (Execution.STATE_ACTIVE_CONCURRENT.equals(execution.getState())) {
     
      // force version increment in the parent execution
      Session session = EnvironmentImpl.getFromCurrent(Session.class);
      session.lock(execution.getParent(), lockMode);

      execution.setState(Execution.STATE_INACTIVE_JOIN);
      
    }
    
    execution.waitForSignal();
    return isComplete(execution);
  }
  
  /**
   * Joins all the incoming executions currently waiting at the gateway.
   * 
   * @return An execution that can be used to leave the gateway (one outgoing sequence flow)
   * or to create child executions on (fork behaviour when multiple outgoing sequence flow).
   */
  protected ExecutionImpl join(ExecutionImpl execution) {
    Activity activity = execution.getActivity();
    ExecutionImpl concurrentRoot = execution.getParent();
    
    if (concurrentRoot == null) {
      return execution;
    }
    
    List<ExecutionImpl> joinedExecutions = getJoinedExecutions(concurrentRoot, activity);
    endJoinedExecutions(joinedExecutions);
      
    ExecutionImpl outgoingExecution = null;
    if (concurrentRoot.getExecutions().size() == 0) {
      outgoingExecution = concurrentRoot;
      outgoingExecution.setState(Execution.STATE_ACTIVE_ROOT);
    } else {
      outgoingExecution = concurrentRoot.createExecution();
      outgoingExecution.setState(Execution.STATE_ACTIVE_CONCURRENT);
    }
      
    outgoingExecution.setActivity(activity);
    return outgoingExecution;
  }
  
  /**
   * @return All executions currently waiting at the gateway.
   */
  protected List<ExecutionImpl> getJoinedExecutions(ExecutionImpl concurrentRoot, Activity activity) {
    List<ExecutionImpl> joinedExecutions = new ArrayList<ExecutionImpl>();
    List<ExecutionImpl> concurrentExecutions = (List<ExecutionImpl>)concurrentRoot.getExecutions();
    for (ExecutionImpl concurrentExecution: (List<ExecutionImpl>)concurrentExecutions) {
      if ( (Execution.STATE_INACTIVE_JOIN.equals(concurrentExecution.getState()))
           && (concurrentExecution.getActivity()==activity)
         ) {
        joinedExecutions.add(concurrentExecution);
      }
    }
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Found " + joinedExecutions.size() + " executions currently waiting at the gateway");
    }
    
    return joinedExecutions;
  }

  /**
   * Ends all executions in the given list.
   */
  protected void endJoinedExecutions(List<ExecutionImpl> joinedExecutions) {
    for (ExecutionImpl joinedExecution: joinedExecutions) {
      joinedExecution.end();
    }
  }
  
  /*
   * Fork (or 'split') behaviour is dependent on the actual gateway type and cannot be 
   * generalized.
   */
  protected abstract void fork(ExecutionImpl execution);
  
  /*
   * Checking if all incoming sequence flow have arrived at the gateway is gateway type dependent. 
   * Eg for the parallel gateway, all incoming sequence flow need to arrive at the gateway, while
   * for the inclusive gateway it depends on the remaining executions in the process instance.
   */
  protected abstract boolean isComplete(ExecutionImpl executionImpl);

}

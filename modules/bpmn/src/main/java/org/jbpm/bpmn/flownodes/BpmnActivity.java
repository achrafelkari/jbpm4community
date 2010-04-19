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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.model.Activity;
import org.jbpm.api.model.Transition;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.Condition;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.model.TransitionImpl;

/**
 * Basic activity for BPMN activities (tasks, gateways)
 * 
 * @author bernd.ruecker@camunda.com
 * @author Ronald van Kuijk (kukeltje)
 * @author Joram Barrez
 */
public abstract class BpmnActivity implements ActivityBehaviour {

  private static final Log LOG = Log.getLog(BpmnActivity.class.getName());

  private static final long serialVersionUID = 1L;

  protected static final boolean CONDITIONS_CHECKED = true;
  protected static final boolean CONDITIONS_IGNORED = !CONDITIONS_CHECKED;
  
  protected String default_;

  protected List<ActivityResource> activityResources = new ArrayList<ActivityResource>();
  
  public void execute(ActivityExecution execution) throws Exception {
    execute((ExecutionImpl) execution);
  }
  
  public abstract void execute(ExecutionImpl executionImpl);

  /**
   * In BPMN multiple outgoing sequence flows behave like a fork.
   * Code initially based on the JPDL fork logic.
   */
  protected void proceed(ExecutionImpl execution, List<Transition> transitions) {
	if (LOG.isDebugEnabled()) {		
		LOG.debug("Proceeding from execution in " + execution.getActivityName());
	}

    Activity activity = execution.getActivity();

    // if no outgoing transitions should be forked,
    if (transitions.size() == 0) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("No outgoing transitions found. Ending the execution");
      }
      execution.end();
    }

    // if there is exactly 1 transition to be taken, just use the incoming execution
    else if (transitions.size() == 1) {
      execution.take(transitions.get(0));

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
      for (Transition transition : transitions) {
        ExecutionImpl concurrentExecution = concurrentRoot.createExecution(transition.getName());
        concurrentExecution.setActivity(activity);
        concurrentExecution.setState(Execution.STATE_ACTIVE_CONCURRENT);
        childExecutionsMap.put(transition, concurrentExecution);
      }
      
      // For a correct functionality, the child executions must all exist before the actual transitions are taken.
      // So we start following the outgoing transitions after child execution creation.
      for (Transition transition : childExecutionsMap.keySet()) {
        childExecutionsMap.get(transition).take(transition);

        if (concurrentRoot.isEnded()) {
          break;
        }
      }
    }
  }

  /**
   * Returns the list of outgoing sequence flow for this activity.
   * If the boolean 'checkConditions' is true, conditions on the sequence flow will be evaluated.
   */
  protected List<Transition> findOutgoingSequenceFlow(ExecutionImpl execution, boolean checkConditions) {
    ActivityImpl activity = execution.getActivity();
    // evaluate the conditions and find the transitions that should be forked
    List<Transition> forkingTransitions = new ArrayList<Transition>();
    List<TransitionImpl> outgoingTransitions = (List) activity.getOutgoingTransitions();
    
    for (TransitionImpl transition : outgoingTransitions) {
      Condition condition = transition.getCondition();
      if ( ( (condition == null) || (!checkConditions) || (condition.evaluate(execution)) ) 
           && (activity.getDefaultOutgoingTransition() != transition) ) {
        forkingTransitions.add(transition);
      }
    }
    
    if (LOG.isDebugEnabled()) {
    	LOG.debug(forkingTransitions.size() + " out of " + outgoingTransitions.size() + " selected for " + activity.getName());
    }
    
    // If no outgoing sequence flow found, check if there is a default sequence flow
    if (forkingTransitions.isEmpty() && isDefaultEnabled()) {
      Transition defaultSeqFlow = execution.getActivity().getDefaultOutgoingTransition();
      if (defaultSeqFlow != null) {
        forkingTransitions.add(defaultSeqFlow);
        if (LOG.isDebugEnabled()) {
          LOG.debug("Selected default sequence flow for " + execution.getActivityName());
        }
      } else {
        throw new JbpmException("No sequence flow condition evaluated to true for " + 
                execution.getActivityName() + " and no default sequenceFlow was speficied");
      }
    } else if (forkingTransitions.isEmpty()){
      throw new JbpmException("No outgoing sequence flow found for " + execution.getActivityName());
    }
    
    return forkingTransitions;
  }

  public void addActivityResource(ActivityResource activityResource) {
    this.activityResources.add(activityResource);
  }
  
  /**
   * Most of the BPMN activities allow to specify a default outgoing sequence
   * flow. Subclasses must override this method when the default definition does
   * not make sense (eg. ParallelGateway)
   */
  public boolean isDefaultEnabled() {
    return true; 
  }
  
  public String getDefault() {
    return default_;
  }

  public void setDefault(String default_) {
    this.default_ = default_;
  }

}
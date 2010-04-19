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

import org.jbpm.api.Execution;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.model.Activity;
import org.jbpm.api.model.Transition;
import org.jbpm.pvm.internal.model.Condition;
import org.jbpm.pvm.internal.model.ExecutionImpl;

/**
 * The abstract task is a base class for all BPMN 2.0 tasks
 * 
 * @author bernd.ruecker@camunda.com
 */
public abstract class AbstractTask extends BpmnActivity {

    private static final long serialVersionUID = 1L;

//    public abstract void innerExecute(ActivityExecution execution) throws Exception;
//
//    @Override
//    public void execute(ActivityExecution execution) throws Exception {
//	innerExecute(execution);
//	if (execution.());
//	fork((ExecutionImpl) execution);
//    }
//
//    public void fork(ExecutionImpl execution) {
//	Activity activity = execution.getActivity();
//
//	// evaluate the conditions and find the transitions that should be
//	// forked
//	List<Transition> forkingTransitions = new ArrayList<Transition>();
//	List<Transition> outgoingTransitions = activity.getOutgoingTransitions();
//	for (Transition transition : outgoingTransitions) {
//	    Condition condition = transition.getCondition();
//	    if ((condition == null) || (condition.evaluate(execution))) {
//		forkingTransitions.add(transition);
//	    }
//	}
//
//	// if no outgoing transitions should be forked,
//	if (forkingTransitions.size() == 0) {
//	    // end this execution
//	    execution.end();
//
//	    // if there is exactly 1 transition to be taken, just use the
//	    // incoming execution
//	}
//	else if (forkingTransitions.size() == 1) {
//	    execution.take(forkingTransitions.get(0));
//
//	    // if there are more transitions
//	}
//	else {
//	    ExecutionImpl concurrentRoot = null;
//	    if (Execution.STATE_ACTIVE_ROOT.equals(execution.getState())) {
//		concurrentRoot = execution;
//		execution.setState(Execution.STATE_INACTIVE_CONCURRENT_ROOT);
//		execution.setActivity(null);
//	    }
//	    else if (Execution.STATE_ACTIVE_CONCURRENT.equals(execution.getState())) {
//		concurrentRoot = execution.getParent();
//	    }
//
//	    for (Transition transition : forkingTransitions) {
//		// launch a concurrent path of execution
//		String childExecutionName = transition.getName();
//		ExecutionImpl concurrentExecution = concurrentRoot.createExecution(childExecutionName);
//		concurrentExecution.setActivity(activity);
//		concurrentExecution.setState(Execution.STATE_ACTIVE_CONCURRENT);
//		concurrentExecution.take(transition);
//
//		if (concurrentRoot.isEnded()) {
//		    break;
//		}
//	    }
//	}
//    }
}

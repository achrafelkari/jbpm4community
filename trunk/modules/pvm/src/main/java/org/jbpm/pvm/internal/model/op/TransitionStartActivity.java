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
package org.jbpm.pvm.internal.model.op;

import org.jbpm.api.JbpmException;
import org.jbpm.api.model.Event;
import org.jbpm.pvm.internal.job.MessageImpl;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.model.TransitionImpl;

/**
 * @author Tom Baeyens
 */
public class TransitionStartActivity extends AtomicOperation {
  
  private static final long serialVersionUID = 1L;

  public boolean isAsync(ExecutionImpl execution) {
    return false;
  }

  public void perform(ExecutionImpl execution) {
    TransitionImpl transition = execution.getTransition();
    ActivityImpl source = transition.getSource();
    ActivityImpl destination = transition.getDestination();
    
    ActivityImpl activity = execution.getActivity();
    if (activity==null) {
      // find outer most activity to start
      activity = destination;
      while ( (activity.getParentActivity()!=null)
              && (!activity.getParentActivity().contains(source))
            ) {
        activity = activity.getParentActivity();
      }
      
    } else if (activity==destination){
      activity = null;

    } else {
      ActivityImpl parent = activity;
      activity = destination;
      while ( (activity!=null)
              && (activity.getParent()!=parent) 
            ) {
        activity = activity.getParentActivity();
      }
      if (activity==null) {
        throw new JbpmException("implementation bug: couldn't find parent "+parent+" around destination "+destination);
      }
    }

    if (activity==null) {
      execution.setTransition(null);
      execution.performAtomicOperation(AtomicOperation.EXECUTE_ACTIVITY);
   
    } else {
      execution.setActivity(activity);

      ExecutionImpl propagatingExecution = execution;
      if (activity.isLocalScope()) {
        propagatingExecution = execution.createScope(activity);
      }

      propagatingExecution.fire(Event.START, activity, AtomicOperation.TRANSITION_START_ACTIVITY);
    }
  }
  
  public MessageImpl createAsyncMessage(ExecutionImpl execution) {
    throw new UnsupportedOperationException("please implement me");
  }

  public String toString() {
    return "TransitionStartActivity";
  }
}

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

import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.model.Activity;
import org.jbpm.api.model.Transition;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;

/**
 * @author Tom Baeyens
 */
public class EndActivity extends JpdlActivity {

  private static final long serialVersionUID = 1L;

  protected boolean endProcessInstance = true;
  protected String state = null;

  public void execute(ActivityExecution execution) {
    execute((ExecutionImpl)execution);
  }

  public void execute(ExecutionImpl execution) {
    Activity activity = execution.getActivity();
    List<? extends Transition> outgoingTransitions = activity.getOutgoingTransitions();
    ActivityImpl parentActivity = (ActivityImpl) activity.getParentActivity();

    if ( (parentActivity!=null)
         && ("group".equals(parentActivity.getType()))
       ) {
      // if the end activity itself has an outgoing transition
      // (such end activities should be drawn on the border of the group)
      if ( (outgoingTransitions!=null)
              && (outgoingTransitions.size()==1)
          ) {
         Transition outgoingTransition = outgoingTransitions.get(0);
         // taking the transition that goes over the group boundaries will
         // destroy the scope automatically (see atomic operation TakeTransition)
         execution.take(outgoingTransition);

      } else {
        execution.setActivity(parentActivity);
        execution.signal();
      }

    } else {
      ExecutionImpl executionToEnd = null;
      if (endProcessInstance) {
        executionToEnd = execution.getProcessInstance();
        executionToEnd.setActivity(execution.getActivity());
      } else {
        executionToEnd = execution;
      }

      if (state==null) {
        executionToEnd.end();
      } else {
        executionToEnd.end(state);
      }
    }
  }

  public void setEndProcessInstance(boolean endProcessInstance) {
    this.endProcessInstance = endProcessInstance;
  }
  public void setState(String state) {
    this.state = state;
  }
}

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
import java.util.Map;

import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.model.Activity;
import org.jbpm.api.model.Transition;
import org.jbpm.pvm.internal.model.ExecutionImpl;

/**
 * @author Tom Baeyens
 */
public class GroupActivity extends JpdlExternalActivity {

  private static final long serialVersionUID = 1L;

  public void execute(ActivityExecution execution) {
    execute((ExecutionImpl)execution);
  }
  
  public void execute(ExecutionImpl execution) {
    // find the start activity
    Activity activity = execution.getActivity();
    List<Activity> startActivities = findStartActivities(activity);
    if (startActivities.size()==1) {
      execution.execute(startActivities.get(0));
    } else {
      
      ExecutionImpl concurrentRoot = null;
      if (Execution.STATE_ACTIVE_ROOT.equals(execution.getState())) {
        concurrentRoot = execution;
      } else if (Execution.STATE_ACTIVE_CONCURRENT.equals(execution.getState())) {
        concurrentRoot = execution.getParent();
        
      } else {
        throw new JbpmException("illegal state");
      }
      
      for (Activity startActivity: startActivities) {
        ExecutionImpl concurrentExecution = concurrentRoot.createExecution();
        concurrentExecution.setState(Execution.STATE_ACTIVE_CONCURRENT);
        concurrentExecution.execute(startActivity);
      }
    }
  }

  private List<Activity> findStartActivities(Activity activity) {
    List<Activity> startActivities = new ArrayList<Activity>();
    List<? extends Activity> nestedActivities = activity.getActivities();
    for (Activity nestedActivity : nestedActivities) {
      if ( (nestedActivity.getIncomingTransitions()==null)
           || (nestedActivity.getIncomingTransitions().isEmpty())
         ) {
        startActivities.add(nestedActivity);
      }
    }
    return startActivities;
  }

  public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
    signal((ExecutionImpl)execution, signalName, parameters);
  }

  public void signal(ExecutionImpl execution, String signalName, Map<String, ?> parameters) throws Exception {
    Transition transition = null;
    Activity activity = execution.getActivity();
    List<? extends Transition> outgoingTransitions = activity.getOutgoingTransitions();
    
    int nbrOfOutgoingTransitions  = (outgoingTransitions!=null ? outgoingTransitions.size() : 0);
    if ( (signalName==null)
         && (nbrOfOutgoingTransitions==1)
       ) {
      transition = outgoingTransitions.get(0);
    } else {
      transition = activity.getOutgoingTransition(signalName);
    }
    
    execution.take(transition);
  }
}

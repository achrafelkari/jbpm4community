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
import java.util.Map;

import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.model.Activity;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;

/**
 * 
 * @author Joram Barrez
 */
public class SubProcessActivity extends BpmnExternalActivity {

  private static final long serialVersionUID = 1L;
  
  private static final Log LOG = Log.getLog(SubProcessActivity.class.getName());

  public void execute(ExecutionImpl execution) {
    List<Activity> startActivities = findStartActivities(execution);
    
    if (!startActivities.isEmpty()) {
      ExecutionImpl parent = execution.createScope(execution.getActivity());
 
      for (Activity startActivity: startActivities) {
        parent.setState(Execution.STATE_INACTIVE_CONCURRENT_ROOT);
        ExecutionImpl concurrentExecution = parent.createExecution();
        concurrentExecution.setState(Execution.STATE_ACTIVE_CONCURRENT);
        concurrentExecution.execute(startActivity);
      }
      
    } else {
      throw new JbpmException("Could not find start activity for the sub-process " + execution.getActivityName());
    }
  }
  
  /**
   * The start activities of a sub-process are either none start events or
   * activities without incoming sequence flow.
   */
  protected List<Activity> findStartActivities(ExecutionImpl execution) {
    List<Activity> startActivities = new ArrayList<Activity>();
    
    for (Activity nestedActivity : execution.getActivity().getActivities()) {
      if ( (nestedActivity.getIncomingTransitions()==null)
              || (nestedActivity.getIncomingTransitions().isEmpty())) {
        startActivities.add(nestedActivity);
      }
    }
    
    return startActivities;
  }
  
  public void signal(ActivityExecution execution, String signalName, Map<String, ? > parameters) throws Exception { 
    ActivityImpl activity = (ActivityImpl) execution.getActivity();
    
    ExecutionImpl scopedExecution = (ExecutionImpl) execution.getParent();
    
    // If there are still active paths in the sub-process
    if ( (scopedExecution.getExecutions() != null) 
            && (scopedExecution.getExecutions().size() > 1) ) { // not > 0 -> current execution is still a child
      
      if (LOG.isDebugEnabled()) {
        LOG.debug("Scoped execution " + scopedExecution.getId() + " has active child executions." +
                "Ending current execution, but scoped execution is not yet continued");
      }
      execution.end();
    
    // If no other paths are active in the sub-process
    } else {
      
      ExecutionImpl parent = scopedExecution.destroyScope(activity); // child execution will be ended automatically when parent ends
      if (LOG.isDebugEnabled()) {
        LOG.debug("Scoped execution " + scopedExecution.getId() + " has no active child executions." +
                "Destroying scope and proceeding from parent execution " + parent.getId());
      }
      proceed(parent, findOutgoingSequenceFlow(parent, CONDITIONS_CHECKED));
      
    }
  }

}

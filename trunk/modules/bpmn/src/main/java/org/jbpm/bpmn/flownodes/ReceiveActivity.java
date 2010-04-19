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

import org.jbpm.api.JbpmException;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.model.Transition;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;

/**
 * Java version of the Receive activity.
 * 
 * @author Joram Barrez
 */
public class ReceiveActivity extends BpmnExternalActivity {

  private static final long serialVersionUID = 1L;
  
  private static final Log LOG = Log.getLog(ReceiveActivity.class.getName());
  
  public void execute(ActivityExecution execution) {
    execute((ExecutionImpl)execution);
  }
  
  public void execute(ExecutionImpl execution) {
    execution.historyActivityStart();
    execution.waitForSignal();
  }

  public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) {
    signal((ExecutionImpl) execution, signalName, parameters);
  }
  
  public void signal(ExecutionImpl execution, String signalName, Map<String, ?> parameters) {
    ActivityImpl activity = execution.getActivity();
    
    if (parameters!=null) {
      execution.setVariables(parameters);
    }
    
    List<Transition> outgoingTransitions = new ArrayList<Transition>();
    if (signalName != null) {
      Transition transition = activity.findOutgoingTransition(signalName);
      if (transition == null) {
        throw new JbpmException("Cannot find an outgoing transition for " + activity.getName() + 
                " named " + signalName);
      }
      outgoingTransitions.add(transition);
      execution.fire(signalName, activity);
    } else {
      outgoingTransitions.addAll(findOutgoingSequenceFlow(execution, CONDITIONS_CHECKED));
    }
    
    proceed(execution, outgoingTransitions);
  }
}

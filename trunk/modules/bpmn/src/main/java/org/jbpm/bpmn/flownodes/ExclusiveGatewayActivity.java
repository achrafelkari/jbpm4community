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

import java.util.List;

import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.model.Transition;
import org.jbpm.bpmn.parser.BindingsParser;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.model.ExecutionImpl;

/**
 * @author Tom Baeyens
 * @author Ronald van Kuijk (kukeltje)
 */
public class ExclusiveGatewayActivity extends DatabasedGatewayActivity {

  private static final long serialVersionUID = 1L;

  private static final Log log = Log.getLog(BindingsParser.class.getName());

  public void execute(ActivityExecution execution) {
    execute((ExecutionImpl) execution);
  }

  /*
   * Converging/diverging behaviour for the exclusive gateway.
   * 
   * Note that no special handling is needed for the converging behaviour.
   */
  public void execute(ExecutionImpl execution) {

    List<Transition> transitions = findOutgoingSequenceFlow(execution, CONDITIONS_CHECKED);
    int numTransitions = transitions.size();

    if (numTransitions > 1) {
      transitions = transitions.subList(0, 1);
      if (log.isInfoEnabled()) {
	      log.info("More than one outgoing sequenceFlow conditions evaluated to true for " 
	    		  + execution.getActivity() + ", taking the first one ("
	              + transitions.get(0).getName() + ")");
      }
    }
    
    // We are now sure we have only one transition as result
    Transition resultingTransition = transitions.get(0);
    if (resultingTransition.getName() != null) {
    	execution.historyDecision(resultingTransition.getName());
    }
    
    proceed(execution, transitions);

  }

}

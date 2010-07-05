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

import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.job.MessageImpl;
import org.jbpm.pvm.internal.model.Continuation;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl.Propagation;
import org.jbpm.pvm.internal.util.Clock;

public class ExecuteActivity extends AtomicOperation {
  
  private static final long serialVersionUID = 1L;
  
  private static final Log log = Log.getLog(ExecuteActivity.class.getName());
  
  public boolean isAsync(ExecutionImpl execution) {
    return execution.getActivity().isAsync();
  }

  public void perform(ExecutionImpl execution) {
    ActivityImpl activity = execution.getActivity();
    
    if (log.isDebugEnabled()) {
      if (execution.getName()!=null) {
        log.debug(execution.toString()+" executes "+activity);
      } else {
        log.debug("executing "+activity);
      }
    }
    
    ActivityBehaviour activityBehaviour = activity.getActivityBehaviour();
    
    try {
      execution.setPropagation(Propagation.UNSPECIFIED);
      execution.setHistoryActivityStart(Clock.getTime());

      activityBehaviour.execute(execution);
      
    } catch (Exception e) {
      execution.handleException(activity, null, null, e, execution.toString()+" couldn't execute "+activityBehaviour+" for activity "+activity);
    }
    
    if (execution.getPropagation()==Propagation.UNSPECIFIED) {
      execution.proceed();
    }
  }

  public String toString() {
    return "ExecuteActivity";
  }

  public MessageImpl createAsyncMessage(ExecutionImpl execution) {
    ExecuteActivityMessage executeActivityMessage = new ExecuteActivityMessage(execution);
    if (execution.getActivity().getContinuation()==Continuation.EXCLUSIVE) {
      executeActivityMessage.setExclusive(true);
    }
    return executeActivityMessage;
  }
}

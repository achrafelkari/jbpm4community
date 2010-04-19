/**
 * Copyright (C) 2007  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.jbpm.pvm.internal.wire.descriptor;

import org.jbpm.api.JbpmException;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.jobexecutor.JobExecutor;
import org.jbpm.pvm.internal.util.Listener;
import org.jbpm.pvm.internal.wire.WireContext;

/**
 * @author Pascal Verdage
 * 
 * Descriptor for the JobExecutor.
 * If it is automatically started, a listener is created to stop it when
 * the environementFactory is closed
 * @see org.jbpm.pvm.internal.env.EnvironmentFactory
 * @see org.jbpm.pvm.internal.wire.binding.JobExecutorBinding
 */
public class JobExecutorDescriptor extends ObjectDescriptor {
  private static final long serialVersionUID = 1L;
  private static final Log log = Log.getLog(JobExecutorDescriptor.class.getName());

  
  private boolean autoStart = false;

  public JobExecutorDescriptor() {
    super(JobExecutor.class.getName());
  }

  public Object construct(WireContext wireContext) {
    JobExecutor jobExecutor = (JobExecutor) super.construct(wireContext);
    if (autoStart) {
      wireContext.addListener(new JobExecutorStopper(jobExecutor));
    }
    return jobExecutor;
  }

  public void setAutoStart(boolean autoStart) {
    this.autoStart = autoStart;
  }
  
  public static class JobExecutorStopper implements Listener {
    JobExecutor jobExecutor;
    public JobExecutorStopper(JobExecutor jobExecutor) {
      this.jobExecutor = jobExecutor;
    }
    public void event(Object source, String eventName, Object info) {
      if (WireContext.EVENT_CLOSE.equals(eventName)) {
        log.trace("stopping jobExecutor");
        // wait to prevent from calling stop before the run method (activation)
        // has been called (after a system context switching)
        //TODO: do not wait
        try {
          Thread.sleep(200);
        } catch (InterruptedException e) {
          e.printStackTrace();
          throw new JbpmException("exception while stopping JobExecutor");
        }
        jobExecutor.stop(true);
      }
    }
  }
}

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
package org.jbpm.pvm.internal.jobexecutor;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.api.job.Timer;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.job.TimerImpl;
import org.jbpm.pvm.internal.session.TimerSession;
import org.jbpm.pvm.internal.util.CollectionUtil;

/**
 * Timers created with this service are committed at the end of the transaction,
 * so their execution will be late if the delay is shorter than the transaction.
 * In that case, they will be executed at the end of the transaction.
 *
 * @author Tom Baeyens, Pascal Verdage
 * @author Huisheng Xu
 */
public class JobExecutorTimerSession implements TimerSession {

  private static final Log log = Log.getLog(TimerSession.class.getName());

  /* injected */
  Session session;

  /* injected. */
  JobAdditionNotifier jobAdditionNotifier;

  public void schedule(Timer timer) {
    if (timer == null) throw new JbpmException("null timer scheduled");
    TimerImpl timerImpl = (TimerImpl) timer;
    timerImpl.validate();

    log.debug("scheduling " + timer);
    session.save(timer);

    if (jobAdditionNotifier != null) {
      jobAdditionNotifier.registerNotification();
    }
  }

  public void cancel(Timer timer) {
    log.debug("canceling " + timer);
    if (timer != null) {
      session.delete(timer);
    } else {
      throw new JbpmException("timer is null");
    }
  }

  public List<Timer> findTimersByExecution(Execution execution) {
    List<?> timers = session.createCriteria(TimerImpl.class)
      .add(Restrictions.eq("execution", execution))
      .list();
    return CollectionUtil.checkList(timers, Timer.class);
  }
}

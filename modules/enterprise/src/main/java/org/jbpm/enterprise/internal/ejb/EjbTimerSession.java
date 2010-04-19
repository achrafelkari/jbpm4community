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
package org.jbpm.enterprise.internal.ejb;

import java.util.List;

import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.api.job.Timer;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.job.TimerImpl;
import org.jbpm.pvm.internal.session.DbSession;
import org.jbpm.pvm.internal.session.TimerSession;

/**
 * Timer session based on the EJB 2.1 timer service.
 * @author Tom Baeyens
 * @author Alejandro Guizar
 */
public class EjbTimerSession implements TimerSession {

  // injected
  private LocalTimerHome timerHome;

  private static final Log log = Log.getLog(EjbTimerSession.class.getName());

  public void schedule(Timer timer) {
    log.debug("scheduling "+timer);

    // flush timer to database
    DbSession dbSession = EnvironmentImpl.getCurrent().get(DbSession.class);
    dbSession.save(timer);
    dbSession.flush();

    // retrieve timer as entity bean, contact timer service
    try {
      LocalTimer timerBean = timerHome.findByPrimaryKey(((TimerImpl)timer).getDbid());
      timerBean.schedule();
    }
    catch (FinderException e) {
      throw new JbpmException("could not find bean for timer: " + timer);
    }
  }

  public void cancel(Timer timer) {
    try {
      LocalTimer timerBean = timerHome.findByPrimaryKey(((TimerImpl)timer).getDbid());
      log.debug("canceling " + timer);
      /*
       * EJB 2.1 section 22.4.4 If an entity bean is removed, the container must remove
       * the timers for that bean
       */
      timerBean.remove();
    }
    catch (FinderException e) {
      log.error("could not find bean for timer " + timer, e);
    }
    catch (RemoveException e) {
      log.error("could not remove bean for timer " + timer, e);
    }
  }

  public List<Timer> findTimersByExecution(Execution execution) {
    return null;
  }
}

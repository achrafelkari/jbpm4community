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

import java.util.Date;

import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.job.TimerImpl;
import org.jbpm.pvm.internal.session.DbSession;


/**
 * @author Tom Baeyens
 * @author Alejandro Guizar
 */
public class ExecuteTimerCmd implements Command<Date> {

  private final long timerDbid;

  private static final long serialVersionUID = 1L;
  private static final Log log = Log.getLog(ExecuteTimerCmd.class.getName());

  public ExecuteTimerCmd(long timerDbid) {
    this.timerDbid = timerDbid;
  }

  public Date execute(Environment environment) throws Exception {
    DbSession dbSession = environment.get(DbSession.class);
    TimerImpl timerImpl = dbSession.get(TimerImpl.class, timerDbid);
    if (timerImpl == null) {
      log.debug("timer not found: " + timerDbid);
      return null; // i.e. delete this timer
    }
    return timerImpl.execute(environment) ? null : timerImpl.getDueDate();
  }

  public String toString() {
    return ExecuteTimerCmd.class.getSimpleName() + '(' + timerDbid + ')';
  }
}

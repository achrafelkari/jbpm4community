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

import java.rmi.RemoteException;
import java.util.Date;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.job.TimerImpl;

/**
 * @author Tom Baeyens
 */
public abstract class TimerEB implements EntityBean, TimedObject {

  private EntityContext entityContext;
  private LocalCommandExecutor commandExecutor;

  private static final long serialVersionUID = 1L;

  private static final Log log = Log.getLog(TimerEB.class.getName());

  public abstract Long getDbid();

  public abstract void setDbid(Long id);

  public abstract Integer getDbversion();

  public abstract void setDbversion(Integer version);

  public abstract Date getDueDate();

  public abstract void setDueDate(Date dueDate);

  public void schedule() {
    schedule(getDueDate());
  }

  private void schedule(Date dueDate) {
    log.debug("registering timer #" + getDbid() + " due " + TimerImpl.formatDueDate(dueDate));
    TimerService timerService = entityContext.getTimerService();
    timerService.createTimer(dueDate, null);
  }

  public void setEntityContext(EntityContext entityContext) {
    this.entityContext = entityContext;
  }

  public void unsetEntityContext() {
    entityContext = null;
  }

  public void ejbRemove() throws RemoveException, RemoteException {
    commandExecutor = null;
  }

  public void ejbActivate() throws RemoteException {
    try {
      Context context = new InitialContext();
      LocalCommandExecutorHome commandExecutorHome = (LocalCommandExecutorHome) context
          .lookup("java:comp/env/ejb/LocalCommandExecutor");
      context.close();

      commandExecutor = commandExecutorHome.create();
    }
    catch (NamingException e) {
      throw new EJBException("error retrieving command executor home", e);
    }
    catch (CreateException e) {
      throw new EJBException("error creating command executor", e);
    }
  }

  public void ejbPassivate() throws RemoteException {
    commandExecutor = null;
  }

  public void ejbLoad() throws RemoteException {
  }

  public void ejbStore() throws RemoteException {
  }

  public void ejbTimeout(Timer timer) {
    Date nextDueDate = commandExecutor.execute(new ExecuteTimerCmd(getDbid()));
    if (nextDueDate != null) {
      schedule(nextDueDate);
    }
  }
}

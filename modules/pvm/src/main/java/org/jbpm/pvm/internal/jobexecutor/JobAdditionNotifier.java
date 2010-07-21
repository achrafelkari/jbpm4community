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
package org.jbpm.pvm.internal.jobexecutor;

import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.tx.Transaction;

/**
 * @author Huisheng Xu
 */
public class JobAdditionNotifier {

  private static final Log log = Log.getLog(JobAdditionNotifier.class.getName());

  /* injected */
  Transaction transaction;

  /* injected */
  JobExecutor jobExecutor;

  private boolean notificationRegistered;

  public void registerNotification() {
    if (jobExecutor == null) {
      log.debug("cannot find jobExecutor");
      return;
    }

    // a transaction is not required (can be null)
    if (transaction == null) {
      log.debug("cannot find transaction");
      return;
    }

    if (notificationRegistered)
      return;

    // notify the job executor after the transaction is completed
    if (log.isTraceEnabled()) {
      log.trace("registering job addition notifier with " + transaction);
    }
    transaction.registerSynchronization(new JobAddedNotification(jobExecutor));

    notificationRegistered = true;
  }
}

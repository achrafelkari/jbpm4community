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
package org.jbpm.pvm.internal.svc;

import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Command;
import org.jbpm.internal.log.Log;

import org.hibernate.StaleStateException;

/** retries the command execution in case hibernate throws optimistic locking
 * (StaleObjectException) exceptions.
 *
 * @author Tom Baeyens
 * @author Huisheng Xu
 */
public class RetryInterceptor extends Interceptor {

  private static final Log log = Log.getLog(RetryInterceptor.class.getName());

  int retries = 3;
  long delay = 50;
  long delayFactor = 4;

  public <T> T execute(Command<T> command) {

    // TODO JBPM-2196 unify the retry code with the JtaTransactionInterceptor

    int attempt = 1;
    long sleepTime = delay;
    while (attempt<=retries) {
      if (attempt>1) {
        log.trace("retrying...");
      }
      try {

        return next.execute(command);

      } catch (RuntimeException ex) {
        if (!this.isCausedByOptimisticLockingFailure(ex)) {
          throw ex;
        }

        attempt++;
        log.trace("optimistic locking failed: " + ex);
        log.trace("waiting " + sleepTime + " millis");

        try {
          Thread.sleep(sleepTime);
        } catch (InterruptedException e1) {
          log.trace("retry sleeping got interrupted");
        }

        sleepTime *= delayFactor;
      }
    }

    throw new JbpmException("gave up after " + attempt + " attempts");
  }

  /**
   * fix for JBPM-2864.
   * If this exception is caused by StaleStateException, then we should retry.
   */
  protected boolean isCausedByOptimisticLockingFailure(
      Throwable throwable) {
    while (throwable != null) {
      if (throwable instanceof StaleStateException) {
        return true;
      } else {
        throwable = throwable.getCause();
      }
    }

    return false;
  }

  public int getRetries() {
    return retries;
  }
  public void setRetries(int retries) {
    this.retries = retries;
  }
  public long getDelay() {
    return delay;
  }
  public void setDelay(long delay) {
    this.delay = delay;
  }
  public long getDelayFactor() {
    return delayFactor;
  }
  public void setDelayFactor(long delayFactor) {
    this.delayFactor = delayFactor;
  }
}

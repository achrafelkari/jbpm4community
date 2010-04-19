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
package org.jbpm.pvm.internal.tx;

import org.hibernate.Session;
import org.jbpm.internal.log.Log;

/**
 * @author Tom Baeyens
 */
public class HibernateSessionResource implements StandardResource {
  
  private static final Log log = Log.getLog(HibernateSessionResource.class.getName());

  protected Session session;
  protected org.hibernate.Transaction transaction;
  
  public HibernateSessionResource(Session session) {
    this.session = session;
    if (log.isDebugEnabled()) {
      log.debug("----- beginning hibernate tx "+System.identityHashCode(session)+" --------------------------------------------------------");
    }
    
    try {
      this.transaction = session.beginTransaction();
    } catch (RuntimeException e) {
      log.error("hibernate transaction begin failed.  closing hibernate session", e);
      session.close();
      throw e;
    }

    if (log.isTraceEnabled()) log.trace("begun hibernate tx "+System.identityHashCode(transaction)+" on hibernate session "+System.identityHashCode(session));
  }

  public void prepare() {
    if (log.isTraceEnabled()) log.trace("flushing hibernate session "+System.identityHashCode(session));
    session.flush();
  }

  public void commit() {
    if (log.isDebugEnabled()) {
      log.debug("----- committing hibernate tx "+System.identityHashCode(transaction)+" -------------------------------------------------------");
    }
    try {
      transaction.commit();
    } finally {
      closeSession();
    }
  }

  private void closeSession() {
    if (log.isTraceEnabled()) log.trace("closing hibernate session "+System.identityHashCode(session));
    session.close();
  }

  public void rollback() {
    if (log.isDebugEnabled()) {
      log.debug("----- rolling back hibernate tx "+System.identityHashCode(transaction)+" -----------------------------------------------------");
    }
    try {
      transaction.rollback();
    } finally {
      closeSession();
    }
  }
}

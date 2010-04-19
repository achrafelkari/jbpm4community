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
package org.jbpm.pvm.internal.wire.descriptor;

import java.sql.Connection;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.impl.SessionImpl;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.tx.HibernateSessionResource;
import org.jbpm.pvm.internal.tx.StandardTransaction;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireDefinition;
import org.jbpm.pvm.internal.wire.WireException;

/**
 * @author Tom Baeyens
 */
public class HibernateSessionDescriptor extends AbstractDescriptor {
  
  private static final long serialVersionUID = 1L;
  private static final Log log = Log.getLog(HibernateSessionDescriptor.class.getName());
  
  protected String factoryName;
  protected boolean useCurrent = false;
  protected boolean tx = true;
  protected boolean close = true;
  protected String standardTransactionName;
  protected String connectionName;

  public Object construct(WireContext wireContext) {
    EnvironmentImpl environment = EnvironmentImpl.getCurrent();
    if (environment==null) {
      throw new WireException("no environment");
    }

    // get the hibernate-session-factory
    SessionFactory sessionFactory = null;
    if (factoryName!=null) {
      sessionFactory = (SessionFactory) wireContext.get(factoryName);
    } else {
      sessionFactory = environment.get(SessionFactory.class);
    }
    if (sessionFactory==null) {
      throw new WireException("couldn't find hibernate-session-factory "+(factoryName!=null ? "'"+factoryName+"'" : "by type ")+"to open a hibernate-session");
    }

    // open the hibernate-session
    Session session = null;
    if (useCurrent) {
      if (log.isTraceEnabled()) log.trace("getting current hibernate session");
      session = sessionFactory.getCurrentSession();
      
    } else if (connectionName!=null) {
      Connection connection = (Connection) wireContext.get(connectionName);
      if (log.isTraceEnabled()) log.trace("creating hibernate session with connection "+connection);
      session = sessionFactory.openSession(connection);

    } else {
      if (log.isTraceEnabled()) log.trace("creating hibernate session");
      session = sessionFactory.openSession();
    }
    
    StandardTransaction standardTransaction = environment.get(StandardTransaction.class);
    if (standardTransaction!=null) {
      HibernateSessionResource hibernateSessionResource = new HibernateSessionResource(session);
      standardTransaction.enlistResource(hibernateSessionResource);
    }

    return session;
  }
  
  public Class<?> getType(WireDefinition wireDefinition) {
    return SessionImpl.class;
  }

  public void setFactoryName(String factoryName) {
    this.factoryName = factoryName;
  }
  public void setTx(boolean tx) {
    this.tx = tx;
  }
  public void setStandardTransactionName(String standardTransactionName) {
    this.standardTransactionName = standardTransactionName;
  }
  public void setConnectionName(String connectionName) {
    this.connectionName = connectionName;
  }
  public void setUseCurrent(boolean useCurrent) {
    this.useCurrent = useCurrent;
  }
  public void setClose(boolean close) {
    this.close = close;
  }
}

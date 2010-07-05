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

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.jbpm.api.JbpmException;
import org.jbpm.internal.log.Log;

/**
 * @author Tom Baeyens
 * @author Huisheng Xu
 */
public class JtaTransaction extends AbstractTransaction {

  private static Log log = Log.getLog(JtaTransaction.class.getName());

  public static final String JNDINAME_USERTRANSACTION_JBOSS_GLOBAL = "UserTransaction";
  public static final String JNDINAME_TRANSACTIONMANAGER_JBOSS_GLOBAL = "java:/TransactionManager";

  protected String userTransactionJndiName = JNDINAME_USERTRANSACTION_JBOSS_GLOBAL;
  protected String transactionManagerJndiName = JNDINAME_TRANSACTIONMANAGER_JBOSS_GLOBAL;

  public boolean isRollbackOnly() {
    try {
      return lookupJeeUserTransaction().getStatus()==Status.STATUS_MARKED_ROLLBACK;
    } catch (SystemException e) {
      throw new JbpmException("couldn't get status of user transaction: "+e.getMessage(), e);
    }
  }

  public void setRollbackOnly() {
    try {
      lookupJeeUserTransaction().setRollbackOnly();
    } catch (Exception e) {
      throw new JbpmException("couldn't set user transaction to rollback only: "+e.getMessage(), e);
    }
  }

  public void registerSynchronization(Synchronization synchronization) {
    try {
      lookupJeeTransaction().registerSynchronization(synchronization);
    } catch (Exception e) {
      throw new JbpmException("couldn't register synchronization: "+e.getMessage(), e);
    }
  }

  public void begin() {
    try {
      lookupJeeUserTransaction().begin();
    } catch (Exception e) {
      throw new JbpmException("couldn't begin transaction: "+e.getMessage(), e);
    }
  }

  public void rollback() {
    try {
      lookupJeeUserTransaction().rollback();
    } catch (Exception e) {
      throw new JbpmException("couldn't rollback: "+e.getMessage(), e);
    }
  }

  public void commit() {
    try {
      flushDeserializedObjects();

      lookupJeeUserTransaction().commit();
    } catch (Exception e) {
      throw new JbpmException("couldn't commit: "+e.getMessage(), e);
    }
  }

  public javax.transaction.Transaction suspend() {
    try {
      return lookupJeeTransactionManager().suspend();
    } catch (Exception e) {
      throw new JbpmException("couldn't suspend: "+e.getMessage(), e);
    }
  }

  public void resume(javax.transaction.Transaction transaction) {
    try {
      lookupJeeTransactionManager().resume(transaction);
    } catch (Exception e) {
      throw new JbpmException("couldn't resume: "+e.getMessage(), e);
    }
  }

  // lookups //////////////////////////////////////////////////////////////////

  public UserTransaction lookupJeeUserTransaction() {
    return (UserTransaction) lookupFromJndi(userTransactionJndiName);
  }

  public javax.transaction.Transaction lookupJeeTransaction() {
    try {
      TransactionManager transactionManager = lookupJeeTransactionManager();
      return transactionManager.getTransaction();
    } catch (Exception e) {
      throw new JbpmException("couldn't get transaction from transaction manager "+transactionManagerJndiName+": "+e.getMessage(), e);
    }
  }

  public TransactionManager lookupJeeTransactionManager() {
    return (TransactionManager) lookupFromJndi(transactionManagerJndiName);
  }

  public static Object lookupFromJndi(String jndiName) {
    try {
      InitialContext initialContext = new InitialContext();
      return initialContext.lookup(jndiName);
    } catch (Exception e) {
      throw new JbpmException("couldn't lookup '"+jndiName+"' from jndi: "+e.getMessage()+": "+e.getMessage(), e);
    }
  }

  public static int getUserTransactionStatus(UserTransaction userTransaction) {
    int status = -1;
    try {
      status = userTransaction.getStatus();
    } catch (SystemException e) {
      throw new JbpmException("couldn't get transaction status: "+e.getMessage(), e);
    }
    log.trace("jta transaction status: "+JtaStatusHelper.toString(status));
    return status;
  }

  public String getUserTransactionJndiName() {
    return userTransactionJndiName;
  }

  public String getTransactionManagerJndiName() {
    return transactionManagerJndiName;
  }
}

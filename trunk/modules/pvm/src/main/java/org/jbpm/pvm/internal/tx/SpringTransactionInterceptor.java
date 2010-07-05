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

import org.jbpm.api.cmd.Command;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.svc.Interceptor;
import org.jbpm.pvm.internal.svc.Policy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * calls setRollbackOnly on the transaction in the environment in case an
 * exception occurs during execution of the command.
 * 
 * @author Andries Inze
 * @author Joram Barrez
 */
public class SpringTransactionInterceptor extends Interceptor {

  protected int springPropagationBehaviour = TransactionDefinition.PROPAGATION_REQUIRED;
  private String transactionManagerName;

  @SuppressWarnings("unchecked")
  public <T> T execute(Command<T> command) {
    PlatformTransactionManager platformTransactionManager = resolveTransactionManager();
    TransactionTemplate template = new TransactionTemplate(platformTransactionManager);
    template.setPropagationBehavior(springPropagationBehaviour);
    return (T) template.execute(new SpringCommandCallback(next, command));
  }

  /**
   * Resolves the transaction manager from the environment.
   * @param environment
   * @return the transaction manager
   */
  private PlatformTransactionManager resolveTransactionManager() {
    if (transactionManagerName != null && transactionManagerName.length() > 0) {
      return (PlatformTransactionManager) EnvironmentImpl.getFromCurrent(transactionManagerName);
    }

    return EnvironmentImpl.getFromCurrent(PlatformTransactionManager.class);
  }

  public void setPolicy(Policy policy) {
    if (policy==Policy.REQUIRES_NEW) {
      springPropagationBehaviour = TransactionDefinition.PROPAGATION_REQUIRES_NEW;
    } else {
      springPropagationBehaviour = TransactionDefinition.PROPAGATION_REQUIRED;
    }
  }
  
  public void setTransactionManagerName(String transactionManagerName) {
    this.transactionManagerName = transactionManagerName;
  }
}

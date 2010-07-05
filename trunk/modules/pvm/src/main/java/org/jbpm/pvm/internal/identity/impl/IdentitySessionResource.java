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
package org.jbpm.pvm.internal.identity.impl;

import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.Transaction;
import org.picketlink.idm.common.exception.IdentityException;
import org.jbpm.api.JbpmException;
import org.jbpm.pvm.internal.tx.StandardResource;

/**
 * @author Tom Baeyens
 */
public class IdentitySessionResource implements StandardResource {
  
  protected IdentitySession identitySession;
  protected Transaction transaction;
  
  public IdentitySessionResource(IdentitySession identitySession) {
    this.identitySession = identitySession;
    
    begin();
  }

  protected void begin() {
    try {
      transaction = identitySession.beginTransaction();
    } catch (IdentityException e) {
      throw new JbpmException("couldn't begin identity transaction", e);
    }
  }

  public void prepare() {
    try {
      identitySession.save();
    } catch (IdentityException e) {
      throw new JbpmException("couldn't save identity transaction", e);
    }
  }

  public void commit() {
    transaction.commit();
  }

  public void rollback() {
    transaction.rollback();
  }
}

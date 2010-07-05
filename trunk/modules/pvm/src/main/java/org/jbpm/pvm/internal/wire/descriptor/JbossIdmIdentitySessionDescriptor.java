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

import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.IdentitySessionFactory;
import org.picketlink.idm.common.exception.IdentityException;
import org.jbpm.api.JbpmException;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.identity.impl.IdentitySessionResource;
import org.jbpm.pvm.internal.identity.impl.JBossIdmIdentitySessionImpl;
import org.jbpm.pvm.internal.tx.StandardTransaction;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireDefinition;

/**
 * @author Tom Baeyens
 */
public class JbossIdmIdentitySessionDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;

  protected String realmName;

  public Object construct(WireContext wireContext) {
	
    IdentitySessionFactory identitySessionFactory = EnvironmentImpl.getFromCurrent(IdentitySessionFactory.class);
    try {
    	IdentitySession identitySession = identitySessionFactory.createIdentitySession(realmName);
    	
    	EnvironmentImpl environment = EnvironmentImpl.getCurrent();
        StandardTransaction transaction = environment.get(StandardTransaction.class);
        if (transaction != null) {
	        IdentitySessionResource identitySessionResource = new IdentitySessionResource(identitySession);
	        transaction.enlistResource(identitySessionResource);
        }
        
		return new JBossIdmIdentitySessionImpl(identitySession);
	} catch (IdentityException e) {
		throw new JbpmException("couldn't create the identity session for realm [" + realmName + "]");
	}
  }

  public Class< ? > getType(WireDefinition wireDefinition) {
    return org.jbpm.pvm.internal.identity.spi.IdentitySession.class;
  }
  
  public void setRealmName(String realmName) {
    this.realmName = realmName;
  }
}

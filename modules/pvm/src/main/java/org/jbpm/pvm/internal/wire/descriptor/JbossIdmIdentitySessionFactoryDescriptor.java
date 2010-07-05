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

import javax.naming.InitialContext;

import org.picketlink.idm.api.IdentitySessionFactory;
import org.picketlink.idm.impl.configuration.IdentityConfigurationImpl;
import org.jbpm.api.JbpmException;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireDefinition;

/**
 * @author Tom Baeyens
 * @author Jeff Yu
 */
public class JbossIdmIdentitySessionFactoryDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;
  
  protected String resource = "jbpm.identity.cfg.xml";
  protected String jndiName = null;

  public Object construct(WireContext wireContext) {
    if (jndiName!=null) {
      try {
        InitialContext initialContext = new InitialContext();
        IdentitySessionFactory identitySessionFactory = (IdentitySessionFactory) initialContext.lookup(jndiName);
        return identitySessionFactory;
      } catch (Exception e) {
        throw new JbpmException("couldn't get idm session factory from jndi address "+jndiName, e);
      }
    }
    
    try {
      return new IdentityConfigurationImpl().configure(resource).buildIdentitySessionFactory();
    } catch (Exception e) {
      throw new JbpmException("couldn't instanatiate identity session factory: "+e.getMessage(), e);
    }
  }

  public Class< ? > getType(WireDefinition wireDefinition) {
    return IdentitySessionFactory.class;
  }
  
  public void setResource(String resource) {
    this.resource = resource;
  }
  public void setJndiName(String jndiName) {
    this.jndiName = jndiName;
  }
}

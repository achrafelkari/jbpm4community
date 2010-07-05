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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireException;

/**
 * @author Tom Baeyens
 */
public class JndiDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;

  private String jndiName;

  protected JndiDescriptor() {
  }

  public JndiDescriptor(String jndiName) {
    this.jndiName = jndiName;
  }

  public Object construct(WireContext wireContext) {
    try {
      Context initialContext = new InitialContext();
      try {
        return initialContext.lookup(jndiName);
      }
      finally {
        initialContext.close();
      }
    }
    catch (NamingException e) {
      throw new WireException("failed to retrieve named object: " + jndiName, e);
    }
  }
}

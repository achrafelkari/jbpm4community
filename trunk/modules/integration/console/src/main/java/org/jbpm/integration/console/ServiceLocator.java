/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jbpm.integration.console;

import javax.ejb.EJBLocalHome;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
class ServiceLocator
{
  public static EJBLocalHome getEjbLocalHome(String localHomeJndiName)
  {
    EJBLocalHome localHome = null;
    try
    {
      Context ctx = createContext();
      localHome = (EJBLocalHome) ctx.lookup(localHomeJndiName);
    }
    catch (Exception cce)
    {
      throw new RuntimeException("Failed load access EJB: " +localHomeJndiName, cce);
    }

    return localHome;
  }

  public static UserTransaction getUserTransaction()
  {
    UserTransaction tx = null;

    try
    {
      Context ctx = createContext();
      // TODO remove explicit transaction demarcation JBPM-2353
      tx = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
    }
    catch (Exception e)
    {
      throw new RuntimeException("Failed to create UserTransaction");
    }

    return tx;
  }

  private static Context createContext()
      throws NamingException
  {
    InitialContext ctx = new InitialContext();
    return ctx;
  }
}
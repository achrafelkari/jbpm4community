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
package org.jbpm.pvm.internal.util;

public class EqualsUtil {

  private EqualsUtil() {
    // hide default constructor to prevent instantiation
  }

  /**
   * hack to support comparing hibernate proxies against the real objects. since it falls back
   * to ==, clients do not need to override {@link Object#hashCode()}.
   * 
   * @deprecated hack does not work
   * @see <a href="https://jira.jboss.org/jira/browse/JBPM-2489">JBPM-2489</a>   
   */
  @Deprecated
  public static boolean equals(Object thisObject, Object otherObject) {
    if ((thisObject == null) || (otherObject == null))
      return false;

    if (isProxy(otherObject)) {
      return otherObject.equals(thisObject);
    }
    else {
      return otherObject == thisObject;
    }
  }

  private static boolean isInitialized;
  private static boolean isHibernateInClasspath = true;
  private static Class<?> hibernateProxyClass;

  private static boolean isProxy(Object otherObject) {
    boolean isProxy = false;
    if (!isInitialized)
      initializeHibernateProxyClass();

    if (isHibernateInClasspath) {
      return hibernateProxyClass.isAssignableFrom(otherObject.getClass());
    }
    return isProxy;
  }

  private static synchronized void initializeHibernateProxyClass() {
    try {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      hibernateProxyClass = classLoader.loadClass("org.hibernate.proxy.HibernateProxy");
    }
    catch (ClassNotFoundException e) {
      isHibernateInClasspath = false;
    }
    isInitialized = true;
  }
}

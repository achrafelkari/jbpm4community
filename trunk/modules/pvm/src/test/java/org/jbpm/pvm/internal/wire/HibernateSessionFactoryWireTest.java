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
package org.jbpm.pvm.internal.wire;

import org.hibernate.SessionFactory;
import org.jbpm.pvm.internal.wire.WireContext;

/**
 * @author Tom Baeyens
 */
public class HibernateSessionFactoryWireTest extends WireTestCase {

  public void testEmptyHibernateSessionFactory() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <hibernate-session-factory name='sf'>" +
      "    <properties>" +
      "      <property name='hibernate.dialect' value='org.hibernate.dialect.HSQLDialect' />" +
      "    </properties>" +
      "  </hibernate-session-factory>" +
      "</objects>"
    );

    SessionFactory sessionFactory = (SessionFactory) wireContext.get("sf");
    assertNotNull(sessionFactory);
  }

  public void testHibernateSessionFactoryWithSeparateConfiguration() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <hibernate-configuration name='c'>" +
      "    <properties>" +
      "      <property name='hibernate.dialect' value='org.hibernate.dialect.HSQLDialect' />" +
      "    </properties>" +
      "  </hibernate-configuration>" +
      "  <hibernate-session-factory name='sf' configuration='c' />" +
      "</objects>"
    );

    SessionFactory sessionFactory = (SessionFactory) wireContext.get("sf");
    assertNotNull(sessionFactory);
  }
}

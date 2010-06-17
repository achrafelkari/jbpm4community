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
package org.jbpm.pvm.internal.wire.binding;

import java.util.List;
import org.jbpm.test.BaseJbpmTestCase;
import org.jbpm.pvm.internal.tx.JtaTransaction;
import org.jbpm.pvm.internal.xml.Parser;
import org.jbpm.pvm.internal.xml.Bindings;
import org.jbpm.pvm.internal.xml.Problem;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;


/**
 * @author Huisheng Xu
 */
public class TransactionBindingTest extends BaseJbpmTestCase {
  public void testDefaultJndiName() {
    Parser parser = new Parser();
    Bindings bindings = new Bindings();
    parser.setBindings(bindings);
    parser.getBindings().addBinding(new TransactionBinding());

    // jndi for jboss
    String xml = "<transaction type='jta' />";

    ObjectDescriptor objectDescriptor = (ObjectDescriptor) parser
      .createParse()
      .setString(xml)
      .execute()
      .getDocumentObject();

    JtaTransaction jtaTransaction = (JtaTransaction) WireContext.create(objectDescriptor);

    assertEquals("UserTransaction", jtaTransaction.getUserTransactionJndiName());
    assertEquals("java:/TransactionManager", jtaTransaction.getTransactionManagerJndiName());
  }

  public void testCustomJndiName() {
    Parser parser = new Parser();
    Bindings bindings = new Bindings();
    parser.setBindings(bindings);
    parser.getBindings().addBinding(new TransactionBinding());

    // jndi for jotm on tomcat
    String xml = "<transaction type='jta'"
      + " user-transaction='java:comp/UserTransaction'"
      + " transaction-manager='java:comp/UserTransaction' />";

    ObjectDescriptor objectDescriptor = (ObjectDescriptor) parser
      .createParse()
      .setString(xml)
      .execute()
      .getDocumentObject();

    JtaTransaction jtaTransaction = (JtaTransaction) WireContext.create(objectDescriptor);

    assertEquals("java:comp/UserTransaction", jtaTransaction.getUserTransactionJndiName());
    assertEquals("java:comp/UserTransaction", jtaTransaction.getTransactionManagerJndiName());
  }
}

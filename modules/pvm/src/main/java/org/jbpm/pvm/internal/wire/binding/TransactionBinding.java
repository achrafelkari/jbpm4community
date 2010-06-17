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

import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.tx.JtaTransaction;
import org.jbpm.pvm.internal.tx.SpringTransaction;
import org.jbpm.pvm.internal.tx.StandardTransaction;
import org.jbpm.pvm.internal.tx.Transaction;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.StringDescriptor;
import org.jbpm.pvm.internal.wire.operation.FieldOperation;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;
import org.hibernate.cfg.Configuration;
import org.w3c.dom.Element;


/** parses a descriptor for creating a {@link Transaction}.
 *
 * See schema docs for more details.
 *
 * @author Tom Baeyens
 * @author Huisheng Xu
 */
public class TransactionBinding extends WireDescriptorBinding {

  private static Log log = Log.getLog(TransactionBinding.class.getName());

  public TransactionBinding() {
    super("transaction");
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    ObjectDescriptor transactionDescriptor = null;

    String type = "standard";
    if (element.hasAttribute("type")) {
      type = element.getAttribute("type");
    }

    if ("standard".equals(type)) {
      transactionDescriptor = new ObjectDescriptor(StandardTransaction.class);
    } else if ("jta".equals(type)){
      transactionDescriptor = new ObjectDescriptor(JtaTransaction.class);
      
	  this.autoDetectOrParseAttribute(transactionDescriptor, element);
    } else if ("spring".equals(type)){
      transactionDescriptor = new ObjectDescriptor(SpringTransaction.class);
    } else {
      parse.addProblem("unsupported transaction type: "+type, element);
    }

    return transactionDescriptor;
  }

  protected void autoDetectOrParseAttribute(ObjectDescriptor transactionDescriptor, Element element) {
    if (element.hasAttribute("user-transaction")) {
      String userTransactionValue = element.getAttribute("user-transaction");
      
	  if (log.isDebugEnabled()) {
        log.debug("get user-transaction from xml: [" + userTransactionValue + "]");
      }
      
	  this.parseFieldOperation(transactionDescriptor, element,
          "userTransactionJndiName", userTransactionValue);
    } else {
      String userTransactionValue = this.autoDetectHibernateConfiguration("jta.UserTransaction");
      
	  if (userTransactionValue != null) {
        if (log.isDebugEnabled()) {
          log.debug("get user-transaction from hibernate configuration: [" + userTransactionValue + "]");
        }
        
		this.parseFieldOperation(transactionDescriptor, element,
            "userTransactionJndiName", userTransactionValue);
      } else {
        if (log.isDebugEnabled()) {
          log.debug("use default user-transaction: ["
            + JtaTransaction.JNDINAME_USERTRANSACTION_JBOSS_GLOBAL + "]");
        }
      }
    }

    if (element.hasAttribute("transaction-manager")) {
      String transactionManagerValue = element.getAttribute("transaction-manager");
      
	  if (log.isDebugEnabled()) {
        log.debug("get transaction-manager from xml: [" + transactionManagerValue + "]");
      }
      
	  this.parseFieldOperation(transactionDescriptor, element,
          "transactionManagerJndiName", transactionManagerValue);
    } else {
      String transactionManagerValue =
          this.autoDetectHibernateConfiguration("hibernate.transaction.manager_lookup_class");

      if (log.isDebugEnabled()) {
        log.debug("get transaction-manager from hibernate configuration: [" + transactionManagerValue + "]");
      }
      
	  if (transactionManagerValue != null) {
        this.parseFieldOperation(transactionDescriptor, element,
            "transactionManagerJndiName", transactionManagerValue);
      } else {
        if (log.isDebugEnabled()) {
          log.debug("use default transaction-manager: ["
            + JtaTransaction.JNDINAME_TRANSACTIONMANAGER_JBOSS_GLOBAL + "]");
        }
      }
    }
  }

  protected void parseFieldOperation(ObjectDescriptor objectDescriptor,
      Element element, String fieldName, String fieldValue) {
    FieldOperation fieldOperation = new FieldOperation();
    fieldOperation.setFieldName(fieldName);
    fieldOperation.setDescriptor(new StringDescriptor(fieldValue));
    objectDescriptor.addOperation(fieldOperation);
  }

  protected String autoDetectHibernateConfiguration(String propertyName) {
      Configuration cfg = EnvironmentImpl.getFromCurrent(Configuration.class, false);
      if (cfg == null) {
          return null;
      }
      return cfg.getProperty(propertyName);
  }
}

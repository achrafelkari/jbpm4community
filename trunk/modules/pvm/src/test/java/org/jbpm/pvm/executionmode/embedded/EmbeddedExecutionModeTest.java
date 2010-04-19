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
package org.jbpm.pvm.executionmode.embedded;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.jbpm.test.BaseJbpmTestCase;

/**
 * @author Tom Baeyens
 */
public class EmbeddedExecutionModeTest extends BaseJbpmTestCase {

  SessionFactory sessionFactory;
  Session session;
  Transaction transaction;

  public void testLoanApprove() {
    Configuration configuration = new Configuration();
    configuration.configure("org/jbpm/pvm/executionmode/embedded/hibernate.cfg.xml");
    sessionFactory = configuration.buildSessionFactory();

    startTransaction();

    Loan loan = new Loan("john doe", 234.0);
    session.save(loan);
    assertEquals("evaluate", loan.getState());
    
    newTransaction();
    
    loan = (Loan) session.get(Loan.class, loan.getDbid());
    assertEquals("evaluate", loan.getState());
    loan.approve();
    assertEquals("archive", loan.getState());
    
    newTransaction();
    
    loan = (Loan) session.get(Loan.class, loan.getDbid());
    assertEquals("archive", loan.getState());
    loan.archiveComplete();
    assertEquals("end", loan.getState());

    commitTransaction();
  }
  
  public void testLoanReject() {
    Configuration configuration = new Configuration();
    configuration.configure("org/jbpm/pvm/executionmode/embedded/hibernate.cfg.xml");
    sessionFactory = configuration.buildSessionFactory();

    startTransaction();

    Loan loan = new Loan("john doe", 234.0);
    session.save(loan);
    assertEquals("evaluate", loan.getState());
    
    newTransaction();
    
    loan = (Loan) session.get(Loan.class, loan.getDbid());
    assertEquals("evaluate", loan.getState());
    loan.reject();
    assertEquals("end", loan.getState());
    
    newTransaction();
    
    loan = (Loan) session.get(Loan.class, loan.getDbid());
    assertEquals("end", loan.getState());
  }


  void newTransaction() {
    commitTransaction();
    startTransaction();
  }

  void startTransaction() {
    session = sessionFactory.openSession();
    transaction = session.beginTransaction();
  }

  void commitTransaction() {
    transaction.commit();
    session.close();
  }
}

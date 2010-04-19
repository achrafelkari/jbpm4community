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


import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.model.ExecutionImpl;

/**
 * @author Tom Baeyens
 */
public class Loan {

  /** the loan process definition as a static resource */
  private static final ClientProcessDefinition processDefinition = createLoanProcess();
  
  private static ClientProcessDefinition createLoanProcess() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess("loan")
      .startActivity("submit loan request", new AutomaticActivity())
        .initial()
        .transition("evaluate")
      .endActivity()
      .startActivity("evaluate", new WaitState())
        .transition("wire money", "approve")
        .transition("end", "reject")
      .endActivity()
      .startActivity("wire money", new AutomaticActivity())
        .transition("archive")
      .endActivity()
      .startActivity("archive", new WaitState())
        .transition("end")
      .endActivity()
      .startActivity("end", new WaitState())
      .endActivity()
    .endProcess();
    
    return processDefinition;
  }

  /** exposes the process definition to the execution hibernate type */
  private static ClientProcessDefinition getProcessDefinition() {
    return processDefinition;
  }

  long dbid;
  String customer;
  double amount;
  ExecutionImpl execution;
  
  /** constructor for persistence */
  protected Loan() {
  }

  public Loan(String customer, double amount) {
    this.customer = customer;
    this.amount = amount;
    this.execution = (ExecutionImpl) processDefinition.startProcessInstance();
  }

  public void approve() {
    execution.signal("approve");
  }

  public void reject() {
    execution.signal("reject");
  }

  public void archiveComplete() {
    execution.signal();
  }

  public String getState() {
    return execution.getActivityName();
  }
  
  // getters //////////////////////////////////////////////////////////////////

  public long getDbid() {
    return dbid;
  }
  public String getCustomer() {
    return customer;
  }
  public double getAmount() {
    return amount;
  }
}

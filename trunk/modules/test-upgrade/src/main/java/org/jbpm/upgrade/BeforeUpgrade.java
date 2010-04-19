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
package org.jbpm.upgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.api.Configuration;
import org.jbpm.api.Execution;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.ManagementService;
import org.jbpm.api.NewDeployment;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.RepositoryService;
import org.jbpm.api.job.Job;

/**
 * Class to be executed during the upgrade test, before the actual schema
 * upgrade is applied. This class will populate the 'old' DB schema with
 * process, variables, history, ... data, that will be verified by the
 * {@link AfterUpgrade} class.
 * 
 * @author jbarrez
 */
public class BeforeUpgrade {

  private ProcessEngine processEngine;

  private RepositoryService repositoryService;

  private ExecutionService executionService;
  
  private ManagementService managementService;

  private static final String TEST_PROCESS_1 = "testprocess1.jpdl.xml";
  
  private static final String TEST_PROCESS_2 = "testprocess2.jpdl.xml";

  private static final String TEST_PROCESS_3 = "testprocess3.jpdl.xml";
  
  public static void main(String[] args) {
    BeforeUpgrade beforeUpgrade = new BeforeUpgrade();
    beforeUpgrade.generateProcess1Data();
    beforeUpgrade.generateProcess2Data();
    beforeUpgrade.generateProcess3Data();
  }

  public BeforeUpgrade() {
    this.processEngine = new Configuration().buildProcessEngine();
    this.repositoryService = processEngine.getRepositoryService();
    this.executionService = processEngine.getExecutionService();
    this.managementService = processEngine.getManagementService();
  }
  
  
  private void deploy(String processFile) {
    NewDeployment deployment = repositoryService.createDeployment();
    deployment.addResourceFromClasspath(processFile);
    deployment.deploy();
  }

  /**
   * Process 1 is a simple process using fork/join and states
   */
  private void generateProcess1Data() {

    // Deploy test processes
    deploy(TEST_PROCESS_1);

    // Start 5 instances of process1
    List<String> ids = new ArrayList<String>();
    for (int i = 0; i < 5; i++) {
      String procDefKey = TEST_PROCESS_1.replace(".jpdl.xml", "");
      String procInstKey = procDefKey + "-" + i;
      ids.add(executionService.startProcessInstanceByKey(procDefKey, procInstKey).getId());
    }

    // Put these instances in various states

    // First one: only in state 'print documents'
    String execId = executionService.findProcessInstanceById(ids.get(0)).findActiveExecutionIn("send invoice").getId();
    executionService.signalExecutionById(execId);

    execId = executionService.findProcessInstanceById(ids.get(0)).findActiveExecutionIn("load truck").getId();
    executionService.signalExecutionById(execId);
    
    // Second one: put in state 'load truck' and 'send invoice'
    execId = executionService.findProcessInstanceById(ids.get(1)).findActiveExecutionIn("print documents").getId();
    executionService.signalExecutionById(execId);
    
    // third one: finished
    executionService.endProcessInstance(ids.get(2), Execution.STATE_ENDED);
    
  }
  
  /**
   * Process 2 is a process using variables and a decision
   */
  private void generateProcess2Data() {
    
    // Deploy test process
    deploy(TEST_PROCESS_2);
    
    // Start 7 instances, of which 3 will be ended
    List<String> ids = new ArrayList<String>();
    for (int i = 0; i < 7; i++) {
      
      String procDefKey = TEST_PROCESS_2.replace(".jpdl.xml", "");
      String procInstKey = procDefKey + "-" + i;
      
      Map<String, Object> vars = new HashMap<String, Object>();
      vars.put("var", i * 2); // we store as var: 0, 2, 4, 6, 8, 10, 12
      ids.add(executionService.startProcessInstanceByKey(procDefKey, vars, procInstKey).getId());
    }
    
    
  }
  
  /**
   * Process 3 is a basic process containing a human task and a timer
   */
  private void generateProcess3Data() {
    
    // Deploy test process
    deploy(TEST_PROCESS_3);
    
    // Start 2 instances, 1 will have timer fired
    List<String> ids = new ArrayList<String>();
    for (int i = 0; i < 2; i++) {
      
      String procDefKey = TEST_PROCESS_3.replace(".jpdl.xml", "");
      String procInstKey = procDefKey + "-" + i;
      ids.add(executionService.startProcessInstanceByKey(procDefKey, procInstKey).getId());
    }
    
    Job timer = managementService.createJobQuery()
                                 .processInstanceId(ids.get(0)).uniqueResult();
    managementService.executeJob(timer.getId());
    
  }

}

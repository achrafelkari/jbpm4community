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

import org.jbpm.api.Configuration;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.HistoryService;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.RepositoryService;
import org.jbpm.api.TaskService;

import javax.naming.InitialContext;

/**
 * Base class for jbpm integration
 * 
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public abstract class JBPMIntegration {

  protected ProcessEngine processEngine;
  
  protected ExecutionService executionService;
  
  protected TaskService taskService;
  
  protected HistoryService historyService; 
  
  protected RepositoryService repositoryService;

  public JBPMIntegration() {
    initializeProcessEngine();
  }

  protected void initializeProcessEngine() {
    processEngine = ProcessEngineUtil.retrieveProcessEngine();
    
    if (processEngine == null) {
      throw new RuntimeException("Process engine not initialized!");
    }
    
    executionService = processEngine.getExecutionService();
    taskService = processEngine.getTaskService();
    historyService = processEngine.getHistoryService();
    repositoryService = processEngine.getRepositoryService();
  }
  
}

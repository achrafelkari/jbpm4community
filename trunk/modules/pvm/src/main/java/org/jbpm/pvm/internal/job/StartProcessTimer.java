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
package org.jbpm.pvm.internal.job;

import java.text.ParseException;

import org.jbpm.api.ExecutionService;
import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.RepositoryService;
import org.jbpm.api.cmd.Environment;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.cal.CronExpression;
import org.jbpm.pvm.internal.cal.Duration;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.session.RepositorySession;
import org.jbpm.pvm.internal.util.Clock;

/**
 * Job that starts a new process instance of a given process definition 
 * on a fixed duedate/periodic time.
 * 
 * @author Joram Barrez
 */
public class StartProcessTimer extends TimerImpl {

  private static final long serialVersionUID = 1L;
  
  private static final Log LOG = Log.getLog(StartProcessTimer.class.getName());

  // Override execution logic of regular timer
  public Boolean execute(Environment environment) throws Exception {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Periodic start process triggered at " + Clock.getTime());
    }

    // if process definition exists
    if (processDefinitionExists(environment)) {
      startProcessInstance(environment);
      // and a new date is appointed
      if (calculateDueDate(environment)) {
        // do not delete this timer
        return false;
      }
    }

    // delete timer otherwise
    return true;
  }
  
  protected boolean processDefinitionExists(Environment environment) {
    RepositoryService repositoryService = environment.get(RepositoryService.class);
    boolean processExists = !repositoryService.createProcessDefinitionQuery()
                                .processDefinitionName(getProcessDefinitionName()).list().isEmpty();

    if (LOG.isDebugEnabled()) {
      LOG.debug("Process definition with name " + getProcessDefinitionName() + " still exists: " + processExists);
    }
    
    return processExists;
  }

  protected void startProcessInstance(Environment environment) {
    ExecutionService executionService = environment.get(ExecutionService.class);
    if (executionService == null) {
      throw new JbpmException("no " + ExecutionService.class.getName() + " in environment"); 
    }
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Starting a new process instance for process definition with name " + getProcessDefinitionName());
    }
    
    RepositorySession repoSession = environment.get(RepositorySession.class);
    if (repoSession == null) {
      throw new JbpmException("Could not find a" + RepositorySession.class.getName() + " impl in environment");
    }
    
    ProcessDefinition procDef = repoSession.findLatestProcessDefinitionByName(getProcessDefinitionName());
    executionService.startProcessInstanceById(procDef.getId());
  }
  
  protected boolean calculateDueDate(Environment environment) throws ParseException {
      
    if (getIntervalExpression() != null && Duration.isValidExpression(getIntervalExpression())) {
      dueDate = Duration.calculateDueDate(getIntervalExpression());
    } else if (getIntervalExpression() != null &&  CronExpression.isValidExpression(getIntervalExpression())) {
      dueDate = new CronExpression(getIntervalExpression()).getNextValidTimeAfter(Clock.getTime());
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("No next duedate calculated for start process job " +
                "with intervalExpression " + getIntervalExpression());
      }
      return false;
    }
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Next process start duedate: " + dueDate);
    }
    
    return true;
  }
  
  @Override
  public void schedule() {
    if (dueDate == null && getIntervalExpression() != null) {
      try {
        calculateDueDate(EnvironmentImpl.getCurrent());
      } catch (ParseException e) {
        throw new JbpmException("Cannot parse intervalExpression", e);
      }
    } else if (dueDate == null) {
      throw new JbpmException("Cannot schedule start process timer: " +
                              "no duedate or intervalExpression set");
    }
    super.schedule();
  }
  
  @Override
  public void validate() {
    if (getProcessDefinitionName() == null) {
      throw new JbpmException("No process definition name set for start process timer");
    }
    if (dueDate == null && getIntervalExpression() == null) {
      throw new JbpmException("No duedate or intervalExpression found for start process timer");
    }
  }
  
  public String getProcessDefinitionName() {
    return signalName;
  }
  
  public void setProcessDefinitionName(String processDefinitionName) {
    this.signalName = processDefinitionName;
  }

  public String getIntervalExpression() {
    return eventName;
  }

  /**
   * Sets the expression that will define the interval between 
   * two sequential process starts by this job.
   * 
   * Possible expression types are {@link Duration} and {@link CronExpression}.
   */
  public void setIntervalExpression(String intervalExpression) {
    this.eventName = intervalExpression;
  }
  
  @Override
  public String toString() {
    StringBuilder strb = new StringBuilder();
    strb.append("PeriodicProcessStart[");
    if (getProcessDefinitionName() != null) {
      strb.append(getProcessDefinitionName());
    }
    if (dueDate != null) {
      strb.append("| " + dueDate);
    }
    if (getIntervalExpression() != null) {
      strb.append("| " + getIntervalExpression());
    }
    
    strb.append("]");
    return strb.toString();
  }

}

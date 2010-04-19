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
package org.jbpm.pvm.internal.task;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.api.JbpmException;
import org.jbpm.api.model.Activity;
import org.jbpm.pvm.internal.id.IdComposer;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;

/**
 * @author Tom Baeyens
 */
public class LifeCycle extends ProcessDefinitionImpl {
  
  private static final long serialVersionUID = 1L;
  private static LifeCycleParser lifeCycleParser = new LifeCycleParser();
  private static Map<String, ProcessDefinitionImpl> lifeCycleProcesses = new HashMap<String, ProcessDefinitionImpl>();

  public static String initialise(TaskImpl task) {
    ProcessDefinitionImpl lifeCycle = getLifeCycle(task);
    Map<String, Object> lifeCycleVariables = new HashMap<String, Object>();
    lifeCycleVariables.put("task", task);
    ExecutionImpl lifeCycleExecution = (ExecutionImpl) lifeCycle.createProcessInstance();
    lifeCycleExecution.setVariables(lifeCycleVariables);
    lifeCycleExecution.start();
    return lifeCycleExecution.getActivity().getName();
  }

  public static ProcessDefinitionImpl getLifeCycle(TaskImpl task) {
    if (task==null) {
      throw new JbpmException("task is null");
    }
    String resource = task.getLifeCycleResource();
    if (resource==null) {
      throw new JbpmException("task "+task.getClass().getName()+" did'nt return a lifecycle");
    }
    ProcessDefinitionImpl lifeCycleProcess;
    synchronized (lifeCycleProcesses) { 
      lifeCycleProcess = lifeCycleProcesses.get(resource);
      if (lifeCycleProcess==null) {
        lifeCycleProcess = (ProcessDefinitionImpl) lifeCycleParser
            .createParse()
            .setResource(resource)
            .execute()
            .checkErrors("task lifecycle")
            .getDocumentObject();
        lifeCycleProcesses.put(resource, lifeCycleProcess);
      }
    }
    return lifeCycleProcess;
  }

  protected static void fireLifeCycleEvent(String eventName, TaskImpl task) {
    ExecutionImpl lifeCycleExecution = new ExecutionImpl();
    ProcessDefinitionImpl lifeCycleProcess = getLifeCycle(task);
    lifeCycleExecution.setProcessDefinition(lifeCycleProcess);
    String state = task.getState();
    Activity activity = lifeCycleProcess.getActivity(state);
    lifeCycleExecution.setActivity((ActivityImpl) activity);
    lifeCycleExecution.signal(eventName);
    task.setState(lifeCycleExecution.getActivity().getName());
  }

  public IdComposer getIdGenerator() {
    // overrides the default ProcessDefinitionImpl behaviour that will end up 
    // in the execution being persisted
    return null;
  }

  
}

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
package org.jbpm.pvm.internal.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.client.ClientProcessInstance;
import org.jbpm.pvm.internal.task.TaskDefinitionImpl;

/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionImpl extends CompositeElementImpl implements OpenProcessDefinition, ClientProcessDefinition {

  private static final long serialVersionUID = 1L;
  private static final Log log = Log.getLog(ProcessDefinitionImpl.class.getName());
  
  public static final int UNASSIGNED_VERSION = -1;

  /** user provided short reference for the process definition that 
   * has the same scope as the name.  Multiple versions of the same 
   * process can have the same key. The key is used to build the 
   * globally unique execution ids. */
  protected String key;
  
  /** the unique id (e.g. combination of name and versionnumber) for this 
   * process definition. */
  protected String id;

  /** the version number of the process definitions with the same name.*/
  protected int version = UNASSIGNED_VERSION;
  
  /** optional package name similar to the Java package name. */
  protected String packageName;
  
  /** time this process was deployed */
  protected Date deploymentTime;
  
  /** ref to the deployment */
  protected long deploymentDbid;
  
  /** propagated from deployment to process definition during deploy */
  protected boolean isSuspended = false;
  
  /** the activity which is executed when the process starts */
  protected ActivityImpl initial;
  
  protected Map<String, TaskDefinitionImpl> taskDefinitions = new HashMap<String, TaskDefinitionImpl>();
  
  protected String imageResourceName = null;
  
  
  public ProcessDefinitionImpl() {
    this.processDefinition = this;
  }

  // execution factory method /////////////////////////////////////////////////
  
  public ClientProcessInstance createProcessInstance() {
    return createProcessInstance(null, null);
  }
  
  public ClientProcessInstance createProcessInstance(String key) {
    return createProcessInstance(key, null);
  }

  public ClientProcessInstance createProcessInstance(String key, Execution superProcessExecution) {
    if (isSuspended) {
      throw new JbpmException("process definition "+id+" is suspended");
    }
    
    ExecutionImpl processInstance = newProcessInstance();
    log.debug("creating new execution for process '"+name+"'");
    if (superProcessExecution!=null) {
      // establish the bidirectional relation between super process activity instance 
      // and sub process instance 
      ExecutionImpl superProcessExecutionImpl = (ExecutionImpl) superProcessExecution;
      processInstance.setSuperProcessExecution(superProcessExecutionImpl);
      superProcessExecutionImpl.setSubProcessInstance(processInstance);
    }
    processInstance.initializeProcessInstance(this, key);
    return processInstance;
  }
  
  public ClientProcessInstance startProcessInstance() {
    ClientProcessInstance processInstance = createProcessInstance(null, null);
    processInstance.start();
    return processInstance;
  }

  public ClientProcessInstance startProcessInstance(String key) {
    ClientProcessInstance processInstance = createProcessInstance(key, null);
    processInstance.start();
    return processInstance;
  }

  protected ExecutionImpl newProcessInstance() {
    return new ExecutionImpl();
  }

  // task definitions /////////////////////////////////////////////////////////
  
  public void addTaskDefinitionImpl(TaskDefinitionImpl taskDefinition) {
    taskDefinitions.put(taskDefinition.getName(), taskDefinition);
  }
  
  public TaskDefinitionImpl getTaskDefinition(String name) {
    return taskDefinitions.get(name);
  }

  // basic methods ////////////////////////////////////////////////////////////

  public String toString() {
    return (name!=null ? "process("+name+")" : "process");
  }

  // customized getters and setters ///////////////////////////////////////////
  
  public String getDeploymentId() {
    return Long.toString(deploymentDbid);
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public ActivityImpl getInitial() {
    return initial;
  }
  public void setInitial(ActivityImpl initial) {
    this.initial = initial;
  }
  public int getVersion() {
    return version;
  }
  public void setVersion(int version) {
    this.version = version;
  }
  public Date getDeploymentTime() {
    return deploymentTime;
  }
  public void setDeploymentTime(Date deploymentTime) {
    this.deploymentTime = deploymentTime;
  }
  public String getPackageName() {
    return packageName;
  }
  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }
  public String getKey() {
    return key;
  }
  public void setKey(String key) {
    this.key = key;
  }
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public void setDeploymentDbid(long deploymentDbid) {
    this.deploymentDbid = deploymentDbid;
  }
  public String getImageResourceName() {
    return imageResourceName;
  }
  public void setImageResourceName(String imageResourceName) {
    this.imageResourceName = imageResourceName;
  }
  public boolean isSuspended() {
    return isSuspended;
  }
  public void setSuspended(boolean isSuspended) {
    this.isSuspended = isSuspended;
  }
}

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
package org.jbpm.pvm.internal.processengine;

import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jbpm.api.ExecutionService;
import org.jbpm.api.HistoryService;
import org.jbpm.api.IdentityService;
import org.jbpm.api.JbpmException;
import org.jbpm.api.ManagementService;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.RepositoryService;
import org.jbpm.api.TaskService;
import org.jbpm.api.cmd.Command;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.cfg.ConfigurationImpl;
import org.jbpm.pvm.internal.cmd.CheckDbCmd;
import org.jbpm.pvm.internal.cmd.CommandService;
import org.jbpm.pvm.internal.env.Context;
import org.jbpm.pvm.internal.env.EnvironmentFactory;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.env.PvmEnvironment;
import org.jbpm.pvm.internal.env.UserProvidedEnvironmentObject;
import org.jbpm.pvm.internal.jobexecutor.JobExecutor;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireDefinition;

/**
 * an environment factory that also is the process-engine context.
 * 
 * <p>
 * This environment factory will produce environments with 2 contexts: the
 * process-engine context and the block context.
 * </p>
 * 
 * <p>
 * An process-engine context is build from two wire definitions: the
 * process-engine wire definition and the environment wire definition.
 * </p>
 * 
 * <p>
 * The process-engine context itself is build from the process-engine
 * wire definition. So all objects that are created in this context remain
 * cached for the lifetime of this process-engine context object.
 * </p>
 * 
 * <p>
 * This process-engine context is also a environment factory. The produced
 * environments contain 2 contexts: the process-engine context itself and a
 * new environment context, build from the environment wire definition. For each
 * created environment, a new environment context will be created from the same
 * environment wire definition. Objects in the environment context will live for
 * as long as the environment.
 * </p>
 * 
 * @author Tom Baeyens
 */
public class ProcessEngineImpl implements Context, ProcessEngine, EnvironmentFactory, Serializable {

  private static final long serialVersionUID = 1L;
  private static final Log log = Log.getLog(ProcessEngineImpl.class.getName());
  
  public static final String JBPM_LIBRARY_VERSION = "4.4-SNAPSHOT";

  transient protected WireContext processEngineWireContext;
  transient protected WireDefinition transactionWireDefinition;
  
  transient protected ThreadLocal<List<UserProvidedEnvironmentObject>> userProvidedEnvironmentObjectsThreadLocal = new ThreadLocal<List<UserProvidedEnvironmentObject>>();
  transient protected ThreadLocal<String> authenticatedUserIdThreadLocal = new ThreadLocal<String>();
  
  transient protected CommandService userCommandService = null;
  
  public ProcessEngineImpl() {
  }

  public ProcessEngineImpl(ConfigurationImpl configuration) {
    initializeProcessEngine(configuration);
    checkDb(configuration);
  }

  protected void initializeProcessEngine(ConfigurationImpl configuration) {
    configuration.setProducedProcessEngine(this);
    this.processEngineWireContext = configuration.getProcessEngineWireContext();
    this.transactionWireDefinition = configuration.getTransactionWireDefinition();
    
    if (log.isTraceEnabled()) {
      log.trace("created ProcessEngine "+System.identityHashCode(this));
      if ( (processEngineWireContext!=null)
           && (processEngineWireContext.getWireDefinition()!=null)
           && (processEngineWireContext.getWireDefinition().getDescriptorTypes()!=null)
         ) {
        log.trace("  process-engine-context "+System.identityHashCode(processEngineWireContext));
        for (Class<?> descriptorType: processEngineWireContext.getWireDefinition().getDescriptorTypes()) {
          log.trace("    "+descriptorType.getName());
        }
      }
      if ( (transactionWireDefinition!=null)
           && (transactionWireDefinition.getDescriptorTypes()!=null)
         ) {
        log.trace("  transaction-context:");
        for (Class<?> descriptorType: transactionWireDefinition.getDescriptorTypes()) {
          log.trace("    "+descriptorType.getName());
        }
      }
    }

    processEngineWireContext.create();
    userCommandService = (CommandService) processEngineWireContext.get(CommandService.NAME_TX_REQUIRED_COMMAND_SERVICE);

    String jndiName = configuration.getJndiName();
    if (jndiName!=null) {
      try {
        log.debug("publishing jBPM ProcessEngine in jndi at "+jndiName);
        InitialContext initialContext = new InitialContext();
        initialContext.bind(jndiName, this);
      } catch (NamingException e) {
        throw new JbpmException("JNDI binding problem", e);
      }
    }

  }

  protected void checkDb(ConfigurationImpl configuration) {
    if (configuration.isCheckDb()) {
      userCommandService.execute(new CheckDbCmd());
    }
  }

  public ExecutionService getExecutionService() {
    return get(ExecutionService.class);
  }
  public HistoryService getHistoryService() {
    return get(HistoryService.class);
  }
  public ManagementService getManagementService() {
    return get(ManagementService.class);
  }
  public TaskService getTaskService() {
    return get(TaskService.class);
  }
  public IdentityService getIdentityService() {
    return get(IdentityService.class);
  }
  public RepositoryService getRepositoryService() {
    return get(RepositoryService.class);
  }

  public EnvironmentImpl openEnvironment() {
    PvmEnvironment environment = new PvmEnvironment(this);

    if (log.isTraceEnabled()) log.trace("opening " + environment);

    installAuthenticatedUserId(environment);
    installProcessEngineContext(environment);
    installTransactionContext(environment);

    return environment;
  }

  protected void installAuthenticatedUserId(EnvironmentImpl environment) {
    String authenticatedUserId = authenticatedUserIdThreadLocal.get();
    if (authenticatedUserId!=null) {
      environment.setAuthenticatedUserId(authenticatedUserId);
      authenticatedUserIdThreadLocal.set(null);
    }
  }

  protected void installTransactionContext(PvmEnvironment environment) {
    WireContext transactionContext = new WireContext(transactionWireDefinition, Context.CONTEXTNAME_TRANSACTION, true);
    // add the environment block context to the environment
    environment.setContext(transactionContext);

    EnvironmentImpl.pushEnvironment(environment);
    try {
      // finish the creation of the environment wire context
      transactionContext.create();

    } catch (RuntimeException e) {
      EnvironmentImpl.popEnvironment();
      throw e;
    }
  }

  protected void installProcessEngineContext(PvmEnvironment environment) {
    // add the process-engine context
    environment.setContext(processEngineWireContext);
  }

  public void close() {
    JobExecutor jobExecutor = get(JobExecutor.class);
    if (jobExecutor!=null) {
      // stop the job executor and wait till all job executor threads have stopped.
      jobExecutor.stop(true);
    }
    processEngineWireContext.fire(WireContext.EVENT_CLOSE, null);
  }

  // process-engine context delegation methods
  // ///////////////////////////////////

  public Object get(String key) {
    return processEngineWireContext.get(key);
  }

  public <T> T get(Class<T> type) {
    return processEngineWireContext.get(type);
  }

  public String getName() {
    return processEngineWireContext.getName();
  }

  public boolean has(String key) {
    return processEngineWireContext.has(key);
  }

  public Set<String> keys() {
    return processEngineWireContext.keys();
  }

  public Object set(String key, Object value) {
    return processEngineWireContext.set(key, value);
  }
  
  public void addProcessEngineWireDefinition(WireDefinition wireDefinition) {
    processEngineWireContext.getWireDefinition().addWireDefinition(wireDefinition);
  }

  public void addTransactionWireDefinition(WireDefinition wireDefinition) {
    transactionWireDefinition.addWireDefinition(wireDefinition);
  }

  // getters and setters //////////////////////////////////////////////////////

  public void setTransactionWireDefinition(WireDefinition transactionWireDefinition) {
    this.transactionWireDefinition = transactionWireDefinition;
  }
  public WireContext getProcessEngineWireContext() {
    return processEngineWireContext;
  }
  public void setProcessEngineWireContext(WireContext processEngineWireContext) {
    this.processEngineWireContext = processEngineWireContext;
  }
  public WireDefinition getTransactionWireDefinition() {
    return transactionWireDefinition;
  }

  public ProcessEngine setAuthenticatedUserId(String authenticatedUserId) {
    authenticatedUserIdThreadLocal.set(authenticatedUserId);
    return this;
  }

  public ProcessEngine setHibernateSession(Object hibernateSession) {
    addUserProvidedEnvironmentObject(new UserProvidedEnvironmentObject(hibernateSession, null, true));
    return this;
  }

  public ProcessEngine setJdbcConnection(Connection jdbcConnection) {
    addUserProvidedEnvironmentObject(new UserProvidedEnvironmentObject(jdbcConnection, null, true));
    return this;
  }

  protected synchronized void addUserProvidedEnvironmentObject(UserProvidedEnvironmentObject userProvidedEnvironmentObject) {
    List<UserProvidedEnvironmentObject> environmentObjects = userProvidedEnvironmentObjectsThreadLocal.get();
    if (environmentObjects==null) {
      environmentObjects = new ArrayList<UserProvidedEnvironmentObject>();
      userProvidedEnvironmentObjectsThreadLocal.set(environmentObjects);
    }
    environmentObjects.add(userProvidedEnvironmentObject);
  }

  public <T> T execute(Command<T> command) {
    return userCommandService.execute(command);
  }

  // left in for legacy test code
  public static EnvironmentFactory parseXmlString(String jbpmConfigurationXml) {
    return (EnvironmentFactory) new ConfigurationImpl()
        .setXmlString(jbpmConfigurationXml)
        .skipDbCheck()
        .buildProcessEngine();
  }
}

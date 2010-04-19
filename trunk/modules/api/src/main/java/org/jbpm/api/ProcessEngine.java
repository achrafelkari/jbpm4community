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
package org.jbpm.api;

import java.sql.Connection;

import org.jbpm.api.cmd.Command;


/** central starting point for all process engine API
 * interactions.  This is a thread safe object so it can be 
 * kept in a static member field or in JNDI or something
 * similar from which all threads (requests) will fetch the 
 * same ProcessEngine object. 
 * 
 * @author Tom Baeyens
 */
public interface ProcessEngine {

  /** the {@link RepositoryService repository service} that provides access
   * to the process repository. */ 
  RepositoryService getRepositoryService();

  /** the {@link ExecutionService execution service} that provides access
   * to the runtime executions repository. */ 
  ExecutionService getExecutionService();

  /** the {@link HistoryService history service} that provides access
   * to the history executions repository. */ 
  HistoryService getHistoryService();

  /** the {@link TaskService task service} that exposes the 
   * runtime human task lists. */ 
  TaskService getTaskService();

  /** the {@link IdentityService identity service} that exposes the 
   * user and group operations management operations. */ 
  IdentityService getIdentityService();

  /** the {@link ManagementService management service} that exposes the 
   * management operations to operators that have to keep the jBPM system 
   * up and running. */ 
  ManagementService getManagementService();

  /** retrieve and object defined in the process engine by type */
  <T> T get(Class<T> type);

  /** retrieve and object defined in the process engine by name */
  Object get(String name);
  
  /** sets the authenticated user's id for the next invocation of 
   * a service method in the same thread. 
   * This method returns the process engine for convenient method concatenations. */
  ProcessEngine setAuthenticatedUserId(String authenticatedUserId);

  /** sets the hibernate session for the next invocation of 
   * a service method in the same thread. 
   * The hibernateSession parameter is of type Object to 
   * prevent a dependency of the API on hibernate directly.
   * This method returns the process engine for convenient method concatenations. */
  ProcessEngine setHibernateSession(Object hibernateSession);

  /** sets the hibernate session for the next invocation of 
   * a service method in the same thread.
   * This method returns the process engine for convenient method concatenations. */
  ProcessEngine setJdbcConnection(Connection jdbcConnection);

  /** perform a user command.  that allows users to span a transaction over
   * their own updates, as well as the jbpm operations. */
  <T> T execute(Command<T> command);

  /** clean shutdown of the engine. */
  void close();
}

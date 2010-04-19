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
package org.jbpm.pvm.internal.env;

import java.util.Set;

/**
 * a group of named objects in an {@link EnvironmentImpl} that have a similar lifespan.
 * 
 * @author Tom Baeyens
 */
public interface Context {

  /** key of the process-engine-context in the environment */
  String CONTEXTNAME_PROCESS_ENGINE = "process-engine";
  
  /** key of the transaction-context in the environment */
  String CONTEXTNAME_TRANSACTION = "transaction";

  /** key of the execution-context in the environment */
  String CONTEXTNAME_EXECUTION = "execution";

  /** key of the task-context in the environment */
  String CONTEXTNAME_TASK = "task";
  
  /** key of the job-context in the environment */
  String CONTEXTNAME_JOB = "job";
  
  String getName();

  Object get(String key);
  <T> T get(Class<T> type);

  boolean has(String key);
  Object set(String key, Object value);
  Set<String> keys();

}

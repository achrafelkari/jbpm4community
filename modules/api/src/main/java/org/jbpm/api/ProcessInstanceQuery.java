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

import java.util.List;

import org.jbpm.api.history.HistoryProcessInstanceQuery;


/** query for ongoing {@linkplain ProcessInstance process instances}
 * exclusively. refer to {@link HistoryProcessInstanceQuery} for
 * queries that include finished process instances.
 * 
 * @author Tom Baeyens
 */
public interface ProcessInstanceQuery {
  
  /** key property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_KEY = "key";
  
  /** select only process instances for the given process definition */
  ProcessInstanceQuery processDefinitionId(String processDefinitionId);

  /** select only a specific process instances using the process instance id */
  ProcessInstanceQuery processInstanceId(String processInstanceId);
  
  /** select only specific process instances using a business key */
  ProcessInstanceQuery processInstanceKey(String processInstanceKey);

  /** select only suspended process definitions */
  ProcessInstanceQuery suspended();

  /** select only process definitions that are not suspended */
  ProcessInstanceQuery notSuspended();

  /** order selected process instances ascending for certain {@link #PROPERTY_KEY properties} */
  ProcessInstanceQuery orderAsc(String property);

  /** order selected process instances descending for certain {@link #PROPERTY_KEY properties} */
  ProcessInstanceQuery orderDesc(String property);

  /** select a specific page in the result set */
  ProcessInstanceQuery page(int firstResult, int maxResults);

  /** execute the query and obtain the list of {@link ProcessInstance}s */
  List<ProcessInstance> list();

  /** execute the query and obtain the unique {@link ProcessInstance} */
  ProcessInstance uniqueResult();
  
  /** execute a count(*) query and returns number of results */ 
  long count();
}

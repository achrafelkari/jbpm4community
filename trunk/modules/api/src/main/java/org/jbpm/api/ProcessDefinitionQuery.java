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

/** query for {@link ProcessDefinition process definitions}.
 * 
 * @author Tom Baeyens
 */
public interface ProcessDefinitionQuery {

  /** id property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_ID = "idProperty.stringValue";
  /** key property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_KEY = "keyProperty.stringValue";
  /** name property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_NAME = "idProperty.objectName";
  /** version property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_VERSION = "versionProperty.longValue";
  /** timestamp property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_DEPLOYMENT_TIMESTAMP = "deployment.timestamp";

  /** select only the process definition with the given id */
  ProcessDefinitionQuery processDefinitionId(String processDefinitionId);

  /** select only process definitions with the given key */
  ProcessDefinitionQuery processDefinitionKey(String key);

  /** select only process definitions with a name like the 
   * given name (name can include % signs to act as wildcard)) */
  ProcessDefinitionQuery processDefinitionNameLike(String name);

  /** select only process definitions with an exact match for the given name */
  ProcessDefinitionQuery processDefinitionName(String name);

  /** select only process definitions within the given deployment */
  ProcessDefinitionQuery deploymentId(String deploymentId);

  /** select only suspended process definitions */
  ProcessDefinitionQuery suspended();

  /** select only process definitions that are not suspended */
  ProcessDefinitionQuery notSuspended();

  /** order selected process definitions ascending for certain {@link #PROPERTY_ID properties} */
  ProcessDefinitionQuery orderAsc(String property);
  
  /** order selected process definitions descending for certain {@link #PROPERTY_ID properties} */
  ProcessDefinitionQuery orderDesc(String property);

  /** select a specific page in the result set */
  ProcessDefinitionQuery page(int firstResult, int maxResults);
  
  /** execute the query and obtain the list of {@link ProcessDefinition}s */
  List<ProcessDefinition> list();

  /** execute the query and obtain the unique {@link ProcessDefinition} */
  ProcessDefinition uniqueResult();
  
  /** execute a count(*) query and returns number of results */ 
  long count();
}

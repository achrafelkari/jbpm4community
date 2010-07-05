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
package org.jbpm.pvm.internal.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jbpm.api.Deployment;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessDefinitionQuery;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;
import org.jbpm.pvm.internal.repository.DeploymentImpl;
import org.jbpm.pvm.internal.repository.DeploymentProperty;
import org.jbpm.pvm.internal.session.RepositorySession;
import org.jbpm.pvm.internal.util.CollectionUtil;


/** returns partially initialized ProcessDefinitionImpl's that can only be exposed 
 * as {@link ProcessDefinition}.
 * 
 * To get the properly initialized {@link ProcessDefinitionImpl} objects,
 * use {@link RepositorySession#findProcessDefinitionById(String)} or 
 * {@link RepositorySession#findProcessDefinitionByKey(String)}
 * 
 * You can use the query query capabilities in this class to find the id 
 * and then use  {@link RepositorySession#findProcessDefinitionById(String)}
 * to get the properly initialized ProcessDefinitionImpl.
 * 
 * @author Tom Baeyens
 */
public class ProcessDefinitionQueryImpl extends AbstractQuery implements ProcessDefinitionQuery {
  
  private static final long serialVersionUID = 1L;

  protected String id;
  protected String key;
  protected String nameLike;
  protected String name;
  protected Boolean suspended;
  protected String deploymentId;
  
  public Object execute(Session session) {
    Object result = super.execute(session);
    RepositorySession repositorySession = EnvironmentImpl.getFromCurrent(RepositorySession.class);

    if (uniqueResult) {
      if (result == null) return null;
      return getProcessDefinition(repositorySession, result);
    }
    else {
      List<?> propertyMaps = (List<?>) result;
      if (propertyMaps.isEmpty()) return Collections.EMPTY_LIST;

      List<ProcessDefinition> processDefinitions = new ArrayList<ProcessDefinition>();
      for (Object propertyObject: propertyMaps) {
        ProcessDefinitionImpl processDefinition = getProcessDefinition(repositorySession, propertyObject);
        processDefinitions.add(processDefinition);
      }
      return processDefinitions;
    }
  }

  private static ProcessDefinitionImpl getProcessDefinition(RepositorySession repositorySession,
    Object propertyObject) {
    Map<?, ?> propertyMap = (Map<?, ?>) propertyObject;
    String deploymentId = propertyMap.get("deploymentDbid").toString();
    String objectName = (String)propertyMap.get("objectName");
    return (ProcessDefinitionImpl) repositorySession.getObject(deploymentId, objectName);
  }

  public String hql() {
  	StringBuilder hql = new StringBuilder();
    hql.append("select new map( idProperty.objectName as objectName, " +
    		                   "idProperty.deployment.dbid as deploymentDbid ) ");
    hql.append("from ");
    hql.append(DeploymentImpl.class.getName());
    hql.append(" as deployment, ");
    hql.append(DeploymentProperty.class.getName());
    hql.append(" as idProperty, ");
    hql.append(DeploymentProperty.class.getName());
    hql.append(" as keyProperty, ");
    hql.append(DeploymentProperty.class.getName());
    hql.append(" as versionProperty ");

    if (suspended!=null) {
      if (suspended) {
        appendWhereClause("deployment.state = '"+Deployment.STATE_SUSPENDED+"'", hql);
      } else {
        appendWhereClause("deployment.state != '"+Deployment.STATE_SUSPENDED+"'", hql);
      }
    }

    appendWhereClause("idProperty.key = '"+DeploymentImpl.KEY_PROCESS_DEFINITION_ID+"'", hql);
    appendWhereClause("idProperty.deployment = deployment ", hql);

    appendWhereClause("keyProperty.key = '"+DeploymentImpl.KEY_PROCESS_DEFINITION_KEY+"' ", hql);
    appendWhereClause("keyProperty.objectName = idProperty.objectName ", hql);
    appendWhereClause("keyProperty.deployment = deployment ", hql);

    appendWhereClause("versionProperty.key = '"+DeploymentImpl.KEY_PROCESS_DEFINITION_VERSION+"' ", hql);
    appendWhereClause("versionProperty.objectName = idProperty.objectName ", hql);
    appendWhereClause("versionProperty.deployment = deployment ", hql);

    if (id!=null) {
      appendWhereClause("idProperty.stringValue = '"+id+"'", hql);
    }

    if (nameLike!=null) {
      appendWhereClause("idProperty.objectName like '"+nameLike+"' ", hql);
    }

    if (name!=null) {
      appendWhereClause("idProperty.objectName = '"+name+"' ", hql);
    }

    if (key!=null) {
      appendWhereClause("keyProperty.stringValue = '"+key+"' ", hql);
    }
    
    if (deploymentId!=null) {
      appendWhereClause("idProperty.deployment.dbid = "+deploymentId+" ", hql);
    }
    
    appendOrderByClause(hql);

    return hql.toString();
  }
  
  protected void applyParameters(Query query) {
  }
  
  public List<ProcessDefinition> list() {
    return CollectionUtil.checkList(untypedList(), ProcessDefinition.class);
  }
  
  public ProcessDefinition uniqueResult() {
    return (ProcessDefinition) untypedUniqueResult();
  }
  
  @Override
  public long count() {
    return list().size(); // TODO: change this to a real db count, as with the other query types
  }

  public ProcessDefinitionQuery processDefinitionId(String id) {
    this.id = id;
    return this;
  }

  public ProcessDefinitionQuery processDefinitionKey(String key) {
    this.key = key;
    return this;
  }
  
  public ProcessDefinitionQuery suspended() {
    this.suspended = true;
    return this;
  }

  public ProcessDefinitionQuery notSuspended() {
    this.suspended = false;
    return this;
  }

  public ProcessDefinitionQuery processDefinitionNameLike(String name) {
    this.nameLike = name;
    return this;
  }

  public ProcessDefinitionQuery processDefinitionName(String name) {
    this.name = name;
    return this;
  }

  public ProcessDefinitionQuery deploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
    return this;
  } 

  public ProcessDefinitionQuery orderAsc(String property) {
    addOrderByClause(property+" asc");
    return this;
  }

  public ProcessDefinitionQuery orderDesc(String property) {
    addOrderByClause(property+" desc");
    return this;
  }

  public ProcessDefinitionQuery page(int firstResult, int maxResults) {
    this.page = new Page(firstResult, maxResults);
    return this;
  }
}

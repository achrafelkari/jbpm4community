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

import java.util.List;

import org.hibernate.Query;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.ProcessInstanceQuery;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.util.CollectionUtil;

/**
 * @author Tom Baeyens
 */
public class ProcessInstanceQueryImpl extends AbstractQuery implements ProcessInstanceQuery {
  
  private static final long serialVersionUID = 1L;

  protected String processDefinitionId;
  protected String processInstanceId;
  protected String processInstanceKey;
  protected Boolean suspended;

  public ProcessInstance uniqueResult() {
    return (ProcessInstance)untypedUniqueResult();
  }

  public List<ProcessInstance> list() {
    return CollectionUtil.checkList(untypedList(), ProcessInstance.class);
  }

  public String hql() {
  	StringBuilder hql = new StringBuilder();
  	
  	hql.append("select ");
  	if (count) {
  	  hql.append("count(processInstance) ");  	  
  	} else {
  	  hql.append("processInstance ");
  	}
  	
    hql.append("from ");
    hql.append(ExecutionImpl.class.getName());
    hql.append(" as processInstance ");
    
    appendWhereClause("processInstance.parent is null ", hql);

    if (suspended!=null) {
      if (suspended) {
        appendWhereClause("processInstance.state = '"+ExecutionImpl.STATE_SUSPENDED+"' ", hql);
      } else {
        appendWhereClause("processInstance.state != '"+ExecutionImpl.STATE_SUSPENDED+"' ", hql);
      }
    }

    if (processInstanceId != null) {
      appendWhereClause("processInstance.processInstance.id = '" + processInstanceId+"' ", hql);
    }

    if (processDefinitionId != null) {
      appendWhereClause("processInstance.processDefinitionId = '" + processDefinitionId+"' ", hql);
    }
    
    if (processInstanceKey != null) {
      appendWhereClause("processInstance.key = '" + processInstanceKey + "'", hql);
    }

    appendOrderByClause(hql);

    return hql.toString();
  }
  
  protected void applyParameters(Query query) {
  }

  public ProcessInstanceQuery orderAsc(String property) {
    addOrderByClause("processInstance."+property+" asc");
    return this;
  }

  public ProcessInstanceQuery orderDesc(String property) {
    addOrderByClause("processInstance."+property+" desc");
    return this;
  }

  public ProcessInstanceQuery page(int firstResult, int maxResults) {
    this.page = new Page(firstResult, maxResults);
    return this;
  }

  public ProcessInstanceQuery processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public ProcessInstanceQuery processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public ProcessInstanceQuery suspended() {
    this.suspended = true;
    return this;
  }

  public ProcessInstanceQuery processInstanceKey(String processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
    return this;
  }

  public ProcessInstanceQuery notSuspended() {
    this.suspended = false;
    return this;
  }
  
}

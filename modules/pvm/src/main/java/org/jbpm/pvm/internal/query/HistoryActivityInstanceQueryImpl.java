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

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.jbpm.api.JbpmException;
import org.jbpm.api.history.HistoryActivityInstance;
import org.jbpm.api.history.HistoryActivityInstanceQuery;
import org.jbpm.pvm.internal.history.model.HistoryActivityInstanceImpl;
import org.jbpm.pvm.internal.util.CollectionUtil;


/**
 * @author Tom Baeyens
 */
public class HistoryActivityInstanceQueryImpl extends AbstractQuery implements HistoryActivityInstanceQuery {

  private static final long serialVersionUID = 1L;
  
  protected String processDefinitionId;
  protected Long tookLessThen;
  protected Long tookLongerThen;
  protected Date startedBefore;
  protected Date startedAfter;
  protected String executionId;
  protected String processInstanceId;
  protected String activityName;

  public String hql() {
  	StringBuilder hql = new StringBuilder();
  	
  	hql.append("select ");
  	if (count) {
  	  hql.append("count(hai) ");
  	} else {
  	  hql.append("hai ");
  	}
  	
    hql.append("from ");
    hql.append(HistoryActivityInstanceImpl.class.getName());
    hql.append(" as hai ");
    
    if (processDefinitionId!=null) {
      appendWhereClause(" hai.historyProcessInstance.processDefinitionId = '"+processDefinitionId+"' ", hql);
    }

    if (tookLessThen!=null) {
      appendWhereClause(" hai.duration < :tookLessThen ", hql);
    }
    
    if (tookLongerThen!=null) {
      appendWhereClause(" hai.duration > :tookLongerThen ", hql);
    }
    
    if (startedBefore!=null) {
      appendWhereClause(" hai.startTime < :startedBefore ", hql);
    }
    
    if (startedAfter!=null) {
      appendWhereClause(" hai.startTime > :startedAfter ", hql);
    }
    
    if (processInstanceId!=null) {
      appendWhereClause(" hai.historyProcessInstance.processInstanceId = '"+processInstanceId+"'", hql);
    }
    
    if (executionId!=null) {
      appendWhereClause(" hai.executionId = '"+executionId+"'", hql);
    }
    
    if (activityName!=null) {
      appendWhereClause(" hai.activityName = '"+activityName+"'", hql);
    }
    
    appendOrderByClause(hql);

    return hql.toString();
  }

  protected void applyParameters(Query query) {
    if (tookLessThen!=null) {
      query.setLong("tookLessThen", tookLessThen);
    }
    
    if (tookLongerThen!=null) {
      query.setLong("tookLongerThen", tookLongerThen);
    }
    
    if (startedBefore!=null) {
      query.setTimestamp("startedBefore", startedBefore);
    }
    
    if (startedAfter!=null) {
      query.setTimestamp("startedAfter", startedAfter);
    }
  }

  public List<HistoryActivityInstance> list() {
    return CollectionUtil.checkList(untypedList(), HistoryActivityInstance.class);
  }
  
  public HistoryActivityInstance uniqueResult() {
    return (HistoryActivityInstance)untypedUniqueResult();
  }

  public HistoryActivityInstanceQuery activityName(String activityName) {
    this.activityName = activityName;
    return this;
  }

  public HistoryActivityInstanceQuery executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }
  
  public HistoryActivityInstanceQuery processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoryActivityInstanceQuery orderAsc(String property) {
    addOrderByClause("hai."+property+" asc");
    return this;
  }

  public HistoryActivityInstanceQuery orderDesc(String property) {
    addOrderByClause("hai."+property+" desc");
    return this;
  }

  public HistoryActivityInstanceQuery page(int firstResult, int maxResults) {
    this.page = new Page(firstResult, maxResults);
    return this;
  }

  public HistoryActivityInstanceQuery processDefinitionId(String processDefinitionId) {
    if (processDefinitionId==null) {
      throw new JbpmException("processInstanceId is null");
    }
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public HistoryActivityInstanceQuery startedAfter(Date time) {
    this.startedAfter = time;
    return this;
  }

  public HistoryActivityInstanceQuery startedBefore(Date time) {
    this.startedBefore = time;
    return this;
  }

  public HistoryActivityInstanceQuery tookLessThen(long durationInMillis) {
    this.tookLessThen = durationInMillis;
    return this;
  }

  public HistoryActivityInstanceQuery tookLongerThen(long durationInMillis) {
    this.tookLongerThen = durationInMillis;
    return this;
  }
}

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

import org.jbpm.api.history.HistoryTask;
import org.jbpm.api.history.HistoryTaskQuery;
import org.jbpm.pvm.internal.history.model.HistoryTaskImpl;
import org.jbpm.pvm.internal.util.CollectionUtil;

/**
 * @author Tom Baeyens
 */
public class HistoryTaskQueryImpl extends AbstractQuery implements HistoryTaskQuery {

  protected String taskId;
  protected String executionId;
  protected String assignee;
  protected String state;
  protected String outcome;
  protected Long tookLessThen;
  protected Long tookLongerThen;
  protected Date startedBefore;
  protected Date startedAfter;

  public String hql() {
    StringBuilder hql = new StringBuilder();
    
    hql.append("select ");
    if (count) {
      hql.append("count(ht) ");
    } else {
      hql.append("ht ");
    }
    
    hql.append("from ");
    hql.append(HistoryTaskImpl.class.getName());
    hql.append(" as ht ");
    
    if (taskId!=null) {
      appendWhereClause(" ht.dbid = "+taskId+" ", hql);
    }
    
    if (executionId!=null) {
      appendWhereClause(" ht.executionId = '"+executionId+"' ", hql);
    }
    
    if (assignee!=null) {
      appendWhereClause(" ht.assignee = '"+assignee+"' ", hql);
    }

    if (state!=null) {
      appendWhereClause(" ht.state = '"+state+"' ", hql);
    }

    if (outcome!=null) {
      appendWhereClause(" ht.outcome = '"+outcome+"' ", hql);
    }

    if (tookLessThen!=null) {
      appendWhereClause(" ht.duration < :tookLessThen ", hql);
    }
    
    if (tookLongerThen!=null) {
      appendWhereClause(" ht.duration > :tookLongerThen ", hql);
    }

    if (startedBefore!=null) {
      appendWhereClause(" ht.createTime < :startedBefore ", hql);
    }
    
    if (startedAfter!=null) {
      appendWhereClause(" ht.createTime > :startedAfter ", hql);
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

  public List<HistoryTask> list() {
    return CollectionUtil.checkList(untypedList(), HistoryTask.class);
  }
  
  public HistoryTask uniqueResult() {
    return (HistoryTask) untypedUniqueResult();
  }

  public HistoryTaskQuery taskId(String taskId) {
    this.taskId = taskId;
    return this;
  }

  public HistoryTaskQuery executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public HistoryTaskQuery assignee(String assignee) {
    this.assignee = assignee;
    return this;
  }

  public HistoryTaskQuery state(String state) {
    this.state = state;
    return this;
  }

  public HistoryTaskQuery outcome(String outcome) {
    this.outcome = outcome;
    return this;
  }

  public HistoryTaskQuery orderAsc(String property) {
    addOrderByClause("ht."+property+" asc");
    return this;
  }

  public HistoryTaskQuery orderDesc(String property) {
    addOrderByClause("ht."+property+" desc");
    return this;
  }

  public HistoryTaskQuery page(int firstResult, int maxResults) {
    this.page = new Page(firstResult, maxResults);
    return this;
  }

  public HistoryTaskQuery startedAfter(Date time) {
    this.startedAfter = time;
    return this;
  }

  public HistoryTaskQuery startedBefore(Date time) {
    this.startedBefore = time;
    return this;
  }

  public HistoryTaskQuery tookLessThen(long durationInMillis) {
    this.tookLessThen = durationInMillis;
    return this;
  }

  public HistoryTaskQuery tookLongerThen(long durationInMillis) {
    this.tookLongerThen = durationInMillis;
    return this;
  }
}

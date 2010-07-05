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
import org.jbpm.api.history.HistoryDetail;
import org.jbpm.api.history.HistoryDetailQuery;
import org.jbpm.pvm.internal.history.model.HistoryCommentImpl;
import org.jbpm.pvm.internal.history.model.HistoryDetailImpl;
import org.jbpm.pvm.internal.util.CollectionUtil;


/**
 * @author Tom Baeyens
 */
public class HistoryDetailQueryImpl extends AbstractQuery implements HistoryDetailQuery {

  protected Class<?> type = HistoryDetailImpl.class;

  protected Date timeBefore;
  protected Date timeAfter;
  
  protected String processInstanceId;
  protected String activityInstanceId;
  protected String taskId;

  public String hql() {
    StringBuilder hql = new StringBuilder();
    hql.append("select hd ");
    hql.append("from ");
    hql.append(type.getName());
    hql.append(" as hd ");
    
    if (taskId!=null) {
      appendWhereClause(" hd.historyTask.dbid = "+taskId+" ", hql);
    }
    
    if (processInstanceId!=null) {
      appendWhereClause(" hd.historyProcessInstance.processInstanceId = '"+processInstanceId+"' ", hql);
    }
    
    if (activityInstanceId!=null) {
      appendWhereClause(" hd.historyActivityInstance.dbid = "+activityInstanceId+" ", hql);
    }
    
    if (timeBefore!=null) {
      appendWhereClause(" hd.time < :timeBefore ", hql);
    }
    
    if (timeAfter!=null) {
      appendWhereClause(" hd.time > :timeAfter ", hql);
    }
    
    appendOrderByClause(hql);
    
    return hql.toString();
  }

  protected void applyParameters(Query query) {
    if (timeAfter!=null) {
      query.setTime("timeAfter", timeAfter);
    }
    
    if (timeBefore!=null) {
      query.setTime("timeBefore", timeBefore);
    }
  }

  public List<HistoryDetail> list() {
    return CollectionUtil.checkList(untypedList(), HistoryDetail.class);
  }
  
  public HistoryDetail uniqueResult() {
    return (HistoryDetail) untypedUniqueResult();
  }

  public HistoryDetailQuery taskId(String taskId) {
    this.taskId = taskId;
    return this;
  }

  public HistoryDetailQuery orderAsc(String property) {
    addOrderByClause("hd."+property+" asc");
    return this;
  }

  public HistoryDetailQuery orderDesc(String property) {
    addOrderByClause("hd."+property+" desc");
    return this;
  }

  public HistoryDetailQuery page(int firstResult, int maxResults) {
    this.page = new Page(firstResult, maxResults);
    return this;
  }

  public HistoryDetailQuery activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }

  public HistoryDetailQuery processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoryDetailQuery comments() {
    type = HistoryCommentImpl.class;
    return this;
  }

  public HistoryDetailQuery timeAfter(Date timeAfter) {
    this.timeAfter = timeAfter;
    return this;
  }

  public HistoryDetailQuery timeBefore(Date timeBefore) {
    this.timeBefore = timeBefore;
    return this;
  }
}

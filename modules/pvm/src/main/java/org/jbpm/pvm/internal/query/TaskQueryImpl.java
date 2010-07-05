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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import org.jbpm.api.JbpmException;
import org.jbpm.api.TaskQuery;
import org.jbpm.api.identity.Group;
import org.jbpm.api.task.Task;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.identity.spi.IdentitySession;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.task.ParticipationImpl;
import org.jbpm.pvm.internal.task.TaskImpl;
import org.jbpm.pvm.internal.util.CollectionUtil;

/**
 * @author Tom Baeyens
 * @author Heiko Braun <heiko.braun@jboss.com>
 */
public class TaskQueryImpl extends AbstractQuery implements TaskQuery {

  private static final long serialVersionUID = 1L;
  private static final Log log = Log.getLog(TaskQueryImpl.class.getName());

  protected boolean unassigned;
  protected String assignee;
  protected String candidate;
  protected Boolean suspended;
  protected String executionId;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String activityName;

  /* groupIds transports the groupIds from the hql to the applyParameters */
  protected List<String> groupIds; 

  public TaskQuery assignee(String assignee) {
    if (candidate!=null) {
      throw new JbpmException("assignee(...) cannot be combined with candidate(...) in one query");
    }
    this.assignee = assignee;
    return this;
  }

  public TaskQuery candidate(String userId) {
    this.candidate = userId;
    unassigned();
    return this;
  }

  public TaskQuery executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public TaskQuery processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public TaskQuery processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public TaskQuery activityName(String activityName) {
    this.activityName = activityName;
    return this;
  }

  public TaskQuery unassigned() {
    this.assignee = null;
    this.unassigned = true;
    return this;
  }

  public TaskQuery suspended() {
    this.suspended = true;
    return this;
  }

  public TaskQuery notSuspended() {
    this.suspended = false;
    return this;
  }

  public TaskQuery orderAsc(String property) {
    orderByClause = "task." + property + " asc ";
    return this;
  }

  public TaskQuery orderDesc(String property) {
    orderByClause = "task." + property + " desc ";
    return this;
  }

  public TaskQuery page(int firstResult, int maxResults) {
    page = new Page(firstResult, maxResults);
    return this;
  }

  protected void applyParameters(Query query) {
    if (assignee!=null) {
      log.debug("setting parameter assignee: "+assignee);
      query.setString("assignee", assignee);
    }
    
    if (candidate!=null) {
      log.debug("setting parameter candidateUserId: "+candidate);
      query.setString("candidateUserId", candidate);
      
      if (groupIds!=null) {
        log.debug("setting parameter candidateGroupIds: "+groupIds);
        query.setParameterList("candidateGroupIds", groupIds);
      }
    }
  }

  public String hql() {
  	StringBuilder hql = new StringBuilder();
  	hql.append("select ");
    
    // participations
    if (candidate!=null) {
      if (count) {
        hql.append("count(distinct task.id) ");
      } else {
        hql.append("distinct task.id ");
      }
      
      hql.append("from ");
      hql.append(ParticipationImpl.class.getName());
      hql.append(" as participant join participant.task as task ");

      appendWhereClause("participant.type = 'candidate' ", hql);

      IdentitySession identitySession = EnvironmentImpl.getFromCurrent(IdentitySession.class);
      List<Group> groups = identitySession.findGroupsByUser(candidate);
      if (groups.isEmpty()) {
        groupIds = null;
        appendWhereClause("participant.userId = :candidateUserId ", hql);
      }
      else {
        groupIds = new ArrayList<String>();
        for (Group group: groups) {
          groupIds.add(group.getId());
        }  
        appendWhereClause("(participant.userId = :candidateUserId or participant.groupId in (:candidateGroupIds))", hql);
      }
    }
    else {
      if (count) {
        hql.append("count(task) ");
      } else {
        hql.append("task ");
      }
      
      hql.append("from ");
      hql.append(TaskImpl.class.getName());
      hql.append(" as task ");
    }
    
    if (suspended!=null) {
      if (suspended) {
        appendWhereClause("task.state = '"+ExecutionImpl.STATE_SUSPENDED+"' ", hql);
      } else {
        appendWhereClause("task.state != '"+ExecutionImpl.STATE_SUSPENDED+"' ", hql);
      }
    }
    
    if (executionId!=null) {
      appendWhereClause("task.execution.id = '"+executionId+"' ", hql);
    }

    if (processInstanceId!=null) {
      appendWhereClause("task.processInstance.id = '"+processInstanceId+"' ", hql);
    }

    if (activityName!=null) {
      appendWhereClause("task.execution.activityName = '"+activityName+"' ", hql);
    }

    if (processDefinitionId!=null) {
      appendWhereClause("task.processInstance.processDefinitionId = '"+processDefinitionId+"' ", hql);
    }

    if (assignee!=null) {
      appendWhereClause("task.assignee = :assignee ", hql);
    } else if (unassigned) {
      appendWhereClause("task.assignee is null ", hql);
    }

    if (candidate == null && !count)
      appendOrderByClause(hql);

    String hqlQuery = hql.toString();
    log.debug(hqlQuery);
    return hqlQuery;
  }
  
  @Override
  public Object execute(Session session) {
    Object result = super.execute(session);
    if (candidate == null || count) return result;

    if (uniqueResult) {
      if (result == null) return null;
      return session.get(TaskImpl.class, (Serializable) result);
    }
    else {
      List<?> list = (List<?>) result;
      if (list.isEmpty()) return Collections.EMPTY_LIST;

      StringBuilder hql = new StringBuilder();
      hql.append("from ").append(TaskImpl.class.getName()).append(" as task ");
      isWhereAdded = false;
      appendWhereClause("task.id in (:identifiers) ", hql);
      appendOrderByClause(hql);

      return session.createQuery(hql.toString())
        .setParameterList("identifiers", list)
        .list();
    }
  }

  public List<Task> list() {
    return CollectionUtil.checkList(untypedList(), Task.class);
  }

  public Task uniqueResult() {
    return (Task) untypedUniqueResult();
  }
}

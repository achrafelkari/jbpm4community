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

import java.io.ObjectStreamException;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.cmd.CommandService;
import org.jbpm.pvm.internal.env.EnvironmentImpl;

/**
 * @author Tom Baeyens
 */
public abstract class AbstractQuery implements Command<Object> {
  
  private static final long serialVersionUID = 1L;
  
  protected CommandService commandService;
  protected String orderByClause;
  protected Page page;
  protected boolean isWhereAdded;
  protected boolean count;
  protected boolean uniqueResult;
  
  protected abstract void applyParameters(Query query);

  public abstract String hql();

  /* reuse by copy and paste:
   * (return type can't be changed)
  public ConcreteQuery page(int firstResult, int maxResults) {
    this.page = new Page(firstResult, maxResults);
    return this;
  } 
  */

  public List<?> untypedList() {
    if (commandService!=null) {
      return (List<?>) commandService.execute(this);
    }
    Session session = EnvironmentImpl.getFromCurrent(Session.class);
    return (List<?>) execute(session);
  }

  protected Object untypedUniqueResult() {
    uniqueResult = true;

    if (commandService!=null) {
      return commandService.execute(this);
    }
    Session session = EnvironmentImpl.getFromCurrent(Session.class);
    return execute(session); 
  }

  public Object execute(Environment environment) throws Exception {
    Session session = environment.get(Session.class);
    try {
      return execute(session);
    } finally {
      resetQuery(); // reset the query member fields so the query can be reused.
    }
  }

  public Object execute(Session session) {
    Query query = session.createQuery(hql());
    applyParameters(query);
    applyPage(query);
    return uniqueResult ? query.uniqueResult() : query.list();
  }
  
  /**
   * Returns the count of the query.
   * Query types that want to use this count method should
   *   - add the operation signature to their interface
   *   - use the 'count' variable in their hql() method.
   */
  public long count() {
    count = true;
    
    // Page and count cannot be used together, because paging is applied after the query is formed
    if (page != null) {
      throw new JbpmException("page(firstResult, maxResult) and count() cannot be used together");
    }
    
    return (Long) untypedUniqueResult();
  }
  
  /**
   * Resets the query so it can be reused after an invocation.
   */
  private void resetQuery() {
    isWhereAdded = false;
    count = false;
    uniqueResult = false;
  }

  protected void appendWhereClause(String whereClause, StringBuilder hql) {
    if (isWhereAdded) {
      hql.append("  and ");
    } else {
      isWhereAdded = true;
      hql.append("where ");
    }
    hql.append(whereClause);
  }

  protected void appendOrderByClause(StringBuilder hql) {
    if (orderByClause!=null) {
      hql.append("order by ");
      hql.append(orderByClause);
    }
  }

  protected void applyPage(Query query) {
    if (page!=null) {
      query.setFirstResult(page.firstResult);
      query.setMaxResults(page.maxResults);
    }
  }

  protected void addOrderByClause(String clause) {
    if (orderByClause==null) {
      orderByClause = clause;
    } else {
      orderByClause += ", " + clause;
    }
  }
  
  protected Object writeReplace() throws ObjectStreamException {
    this.commandService = null;
    return this;
  }

  public void setCommandService(CommandService commandService) {
    this.commandService = commandService;
  }
}

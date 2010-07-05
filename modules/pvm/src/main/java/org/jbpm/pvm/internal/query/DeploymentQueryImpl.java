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
import org.jbpm.api.Deployment;
import org.jbpm.api.DeploymentQuery;
import org.jbpm.pvm.internal.repository.DeploymentImpl;
import org.jbpm.pvm.internal.util.CollectionUtil;


/**
 * @author Tom Baeyens
 */
public class DeploymentQueryImpl extends AbstractQuery implements DeploymentQuery {
  
  protected String deploymentId = null;
  protected Boolean suspended = null;

  public String hql() {
    StringBuilder hql = new StringBuilder();
    
    hql.append("select ");
    if (count) {
      hql.append("count(d) ");
    } else {
      hql.append("d ");
    }

    hql.append("from ");
    hql.append(DeploymentImpl.class.getName());
    hql.append(" as d ");
    
    if (deploymentId!=null) {
      appendWhereClause("d.dbid = "+deploymentId+" ", hql);
    }

    if (suspended!=null) {
      if (suspended) {
        appendWhereClause(" d.state = '"+Deployment.STATE_SUSPENDED+"' ", hql);
      } else {
        appendWhereClause(" d.state != '"+Deployment.STATE_SUSPENDED+"' ", hql);
      }
    }
    
    appendOrderByClause(hql);
    
    return hql.toString();
  }

  protected void applyParameters(Query query) {
  }

  public DeploymentQuery deploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
    return this;
  }

  public DeploymentQuery notSuspended() {
    suspended = false;
    return this;
  }

  public DeploymentQuery suspended() {
    suspended = true;
    return this;
  }

  public DeploymentQuery orderAsc(String property) {
    addOrderByClause("d."+property+" asc");
    return this;
  }

  public DeploymentQuery orderDesc(String property) {
    addOrderByClause("d."+property+" desc");
    return this;
  }

  public DeploymentQuery page(int firstResult, int maxResults) {
    this.page = new Page(firstResult, maxResults);
    return this;
  }

  public List<Deployment> list() {
    return CollectionUtil.checkList(untypedList(), Deployment.class);
  }

  public Deployment uniqueResult() {
    return (Deployment) untypedUniqueResult();
  }
}

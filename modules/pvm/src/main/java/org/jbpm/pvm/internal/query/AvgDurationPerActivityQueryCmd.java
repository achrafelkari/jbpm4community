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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.history.model.HistoryActivityInstanceImpl;

/**
 * @author Tom Baeyens
 */
public class AvgDurationPerActivityQueryCmd implements Command<Object> {

  private static final long serialVersionUID = 1L;
  
  protected String processDefinitionId;

  public AvgDurationPerActivityQueryCmd(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public Object execute(Environment environment) throws Exception {
    Session session = environment.get(Session.class);
    
    Query query = session.createQuery(
      "select distinct hai.activityName " +
      "from "+HistoryActivityInstanceImpl.class.getName()+" as hai " +
      "where hai.historyProcessInstance.processDefinitionId = :processDefinitionId"
    );
    query.setString("processDefinitionId", processDefinitionId);
    
    Map<String, Long> avgDuration = new HashMap<String, Long>();
    
    List<String> activityNames = query.list();
    for (String activityName: activityNames) {
      query = session.createQuery(
        "select avg(hai.duration) " +
        "from "+HistoryActivityInstanceImpl.class.getName()+" as hai " +
        "where hai.historyProcessInstance.processDefinitionId = :processDefinitionId " +
        "  and hai.activityName = :activityName"
      );
      query.setString("processDefinitionId", processDefinitionId);
      query.setString("activityName", activityName);
      
      Number number = (Number) query.uniqueResult();
      avgDuration.put(activityName, new Long(number.longValue()));
    }
    
    return avgDuration;
  }
}

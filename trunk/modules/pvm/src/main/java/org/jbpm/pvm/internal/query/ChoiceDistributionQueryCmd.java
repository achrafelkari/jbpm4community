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
public class ChoiceDistributionQueryCmd implements Command<Object> {

  private static final long serialVersionUID = 1L;
  
  protected String processDefinitionId;
  protected String activityName;
  
  public ChoiceDistributionQueryCmd(String processDefinitionId, String activityName) {
    this.processDefinitionId = processDefinitionId;
    this.activityName = activityName;
  }

  public Object execute(Environment environment) {
    Session session = environment.get(Session.class);
    
    Query query = session.createQuery(
      "select hai.transitionName, count(hai) " +
      "from "+HistoryActivityInstanceImpl.class.getName()+" as hai " +
      "where hai.historyProcessInstance.processDefinitionId = :processDefinitionId " +
      "  and hai.activityName = :activityName " +
      "group by hai.transitionName "
    );
    query.setString("processDefinitionId", processDefinitionId);
    query.setString("activityName", activityName);

    List<Object[]> transitionCounts = query.list();

    Map<String, Integer> choiceDistributionCounts = new HashMap<String, Integer>();
    
    for (Object[] pair: transitionCounts) {
      String transitionName = (String) pair[0];
      Number number = (Number) pair[1];
      
      choiceDistributionCounts.put(transitionName, new Integer(number.intValue()));
    }
    
    return choiceDistributionCounts;
  }
}

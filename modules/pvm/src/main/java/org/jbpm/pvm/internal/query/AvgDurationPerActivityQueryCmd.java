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

import org.hibernate.Session;

import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.history.model.HistoryActivityInstanceImpl;

/**
 * @author Tom Baeyens
 */
public class AvgDurationPerActivityQueryCmd implements Command<Map<String, Number>> {

  private static final long serialVersionUID = 1L;

  protected String processDefinitionId;

  public AvgDurationPerActivityQueryCmd(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public Map<String, Number> execute(Environment environment) throws Exception {
    List<?> results = environment.get(Session.class)
      .createQuery("select hai.activityName, avg(hai.duration) "
        + "from "
        + HistoryActivityInstanceImpl.class.getName()
        + " as hai "
        + "where hai.historyProcessInstance.processDefinitionId = :processDefinitionId "
        + "group by hai.activityName")
      .setString("processDefinitionId", processDefinitionId)
      .list();

    Map<String, Number> avgDurationPerActivity = new HashMap<String, Number>();
    for (Object result: results) {
      Object[] pair = (Object[]) result;
      avgDurationPerActivity.put((String) pair[0], (Number) pair[1]);
    }

    return avgDurationPerActivity;
  }
}

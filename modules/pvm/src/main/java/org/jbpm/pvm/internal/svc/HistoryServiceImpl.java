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
package org.jbpm.pvm.internal.svc;

import java.util.List;
import java.util.Map;

import org.jbpm.api.HistoryService;
import org.jbpm.api.history.HistoryActivityInstanceQuery;
import org.jbpm.api.history.HistoryComment;
import org.jbpm.api.history.HistoryDetailQuery;
import org.jbpm.api.history.HistoryProcessInstanceQuery;
import org.jbpm.api.history.HistoryTaskQuery;
import org.jbpm.pvm.internal.cmd.CreateHistoryActivityInstanceQueryCmd;
import org.jbpm.pvm.internal.cmd.CreateHistoryDetailQueryCmd;
import org.jbpm.pvm.internal.cmd.CreateHistoryProcessInstanceQueryCmd;
import org.jbpm.pvm.internal.cmd.CreateHistoryTaskQueryCmd;
import org.jbpm.pvm.internal.cmd.GetTaskCommentsCmd;
import org.jbpm.pvm.internal.query.AvgDurationPerActivityQueryCmd;
import org.jbpm.pvm.internal.query.ChoiceDistributionQueryCmd;
import org.jbpm.pvm.internal.query.HistoryActivityInstanceQueryImpl;
import org.jbpm.pvm.internal.query.HistoryDetailQueryImpl;
import org.jbpm.pvm.internal.query.HistoryProcessInstanceQueryImpl;
import org.jbpm.pvm.internal.query.HistoryTaskQueryImpl;


/**
 * @author Tom Baeyens
 */
public class HistoryServiceImpl extends AbstractServiceImpl implements HistoryService {
  
  public Map<String, Long> avgDurationPerActivity(String processDefinitionId) {
    return (Map) commandService.execute(new AvgDurationPerActivityQueryCmd(processDefinitionId));
  }

  public Map<String, Integer> choiceDistribution(String processDefinitionId, String activityName) {
    return (Map) commandService.execute(new ChoiceDistributionQueryCmd(processDefinitionId, activityName));
  }

  public HistoryProcessInstanceQuery createHistoryProcessInstanceQuery() {
    HistoryProcessInstanceQueryImpl query = commandService.execute(new CreateHistoryProcessInstanceQueryCmd());
    query.setCommandService(commandService);
    return query;
  }

  public HistoryActivityInstanceQuery createHistoryActivityInstanceQuery() {
    HistoryActivityInstanceQueryImpl query = commandService.execute(new CreateHistoryActivityInstanceQueryCmd());
    query.setCommandService(commandService);
    return query;
  }

  public HistoryTaskQuery createHistoryTaskQuery() {
    HistoryTaskQueryImpl query = commandService.execute(new CreateHistoryTaskQueryCmd());
    query.setCommandService(commandService);
    return query;
  }

  public HistoryDetailQuery createHistoryDetailQuery() {
    HistoryDetailQueryImpl query = commandService.execute(new CreateHistoryDetailQueryCmd());
    query.setCommandService(commandService);
    return query;
  }
}

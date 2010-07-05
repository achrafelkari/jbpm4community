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
package org.jbpm.api;

import java.util.Map;
import java.util.Set;

import org.jbpm.api.history.HistoryActivityInstanceQuery;
import org.jbpm.api.history.HistoryDetailQuery;
import org.jbpm.api.history.HistoryProcessInstanceQuery;
import org.jbpm.api.history.HistoryTaskQuery;


/** exposes the history information for ongoing and past 
 * process instances.
 * 
 * @author Tom Baeyens
 * @author Maciej Swiderski
 */
public interface HistoryService {

  /** search for process instances in the history */
  HistoryProcessInstanceQuery createHistoryProcessInstanceQuery();

  /** search in history activity instance information */ 
  HistoryActivityInstanceQuery createHistoryActivityInstanceQuery();

  /** search in history task information */ 
  HistoryTaskQuery createHistoryTaskQuery();

  /** search in history details */ 
  HistoryDetailQuery createHistoryDetailQuery();

  /** returns the average duration in milliseconds for each activity in the given process definition */
  Map<String, Number> avgDurationPerActivity(String processDefinitionId);
  
  /** returns for each transitionName, the number of times that transition was taken */
  Map<String, Number> choiceDistribution(String processDefinitionId, String activityName);
  
  /** retrieves a variable */
  Set<String> getVariableNames(String processInstanceId);
  
  /** retrieves a map of variables */
  Object getVariable(String processInstanceId, String variableName);
  
  /** all the variables visible in the given history execution scope */
  Map<String, ?> getVariables(String processInstanceId, Set<String> variableNames);
}

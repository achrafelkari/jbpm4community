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
package org.jbpm.api.history;

import java.util.Date;
import java.util.List;


/** query for {@link HistoryActivityInstance activity occurrences}.
 * 
 * @author Tom Baeyens
 */
public interface HistoryActivityInstanceQuery {
  
  /** starttime property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_STARTTIME = "startTime";
  /** endtime property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_ENDTIME = "endTime";
  /** executionId property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_EXECUTIONID = "executionId";
  /** activityName property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_ACTIVITYNAME = "activityName";
  /** duration property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_DURATION = "duration";

  /** only select activity instances for the given process definition */
  HistoryActivityInstanceQuery processDefinitionId(String processDefinitionId);

  /** only select activity instances for the given execution */
  HistoryActivityInstanceQuery executionId(String executionId);
  
  /** only select activity instances for the given process instance id */
  HistoryActivityInstanceQuery processInstanceId(String processInstanceId);

  /** only select activity instances started after the given time */
  HistoryActivityInstanceQuery startedAfter(Date time);

  /** only select activity instances started before the given time */
  HistoryActivityInstanceQuery startedBefore(Date time);

  /** only select activity instances for the given activity 
   * (this usually used in combination with {@link #processDefinitionId(String)}) */
  HistoryActivityInstanceQuery activityName(String activityName);

  /** only select activity instances that took longer then the given duration in milliseconds */
  HistoryActivityInstanceQuery tookLongerThen(long durationInMillis);

  /** only select activity instances that took less then the given duration in milliseconds */
  HistoryActivityInstanceQuery tookLessThen(long durationInMillis);

  /** order selected activity instances ascending for certain {@link #PROPERTY_STARTTIME properties} */
  HistoryActivityInstanceQuery orderAsc(String property);

  /** order selected process definitions descending for certain {@link #PROPERTY_STARTTIME properties} */
  HistoryActivityInstanceQuery orderDesc(String property);

  /** select a specific page in the result set */
  HistoryActivityInstanceQuery page(int firstResult, int maxResults);

  /** execute the query and obtain the list of {@link HistoryActivityInstance}s */
  List<HistoryActivityInstance> list(); 

  /** execute the query and obtain the unique {@link HistoryActivityInstance} */
  HistoryActivityInstance uniqueResult();
  
  /** execute a count(*) query and returns number of results */ 
  long count();
}

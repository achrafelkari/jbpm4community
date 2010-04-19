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


/** query for history tasks.
 * 
 * @author Tom Baeyens
 */
public interface HistoryTaskQuery {

  /** id property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_ID = "dbid";
  /** starttime property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_CREATETIME = "createTime";
  /** endtime property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_ENDTIME = "endTime";
  /** executionId property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_EXECUTIONID = "executionId";
  /** outcome property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_OUTCOME = "outcome";
  /** outcome property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_ASSIGNEE = "assignee";
  /** state property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_STATE = "state";
  /** duration property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_DURATION = "duration";

  /** only select the history task for the given id */
  HistoryTaskQuery taskId(String taskId);
  
  /** only select history tasks within the given execution */
  HistoryTaskQuery executionId(String executionId);
  
  /** only select history tasks for the given assignee */
  HistoryTaskQuery assignee(String assignee);

  /** only select history tasks in the given state */
  HistoryTaskQuery state(String state);

  /** only select history tasks that have the given outcome */
  HistoryTaskQuery outcome(String outcome);

  /** order selected history tasks ascending for certain {@link #PROPERTY_ID properties} */
  HistoryTaskQuery orderAsc(String property);

  /** order selected process definitions descending for certain {@link #PROPERTY_ID properties} */
  HistoryTaskQuery orderDesc(String property);

  /** select a specific page in the result set */
  HistoryTaskQuery page(int firstResult, int maxResults);

  /** only select history tasks started after the given time */
  HistoryTaskQuery startedAfter(Date time);

  /** only select history tasks started before the given time */
  HistoryTaskQuery startedBefore(Date time);

  /** only select history tasks that took less then the given duration in milliseconds */
  HistoryTaskQuery tookLessThen(long durationInMillis);

  /** only select history tasks that took longer then the given duration in milliseconds */
  HistoryTaskQuery tookLongerThen(long durationInMillis);

  /** execute the query and obtain the list of {@link HistoryTask}s */
  List<HistoryTask> list();
  
  /** execute the query and obtain the unique {@link HistoryTask} */
  HistoryTask uniqueResult();
  
  /** execute a count(*) query and returns number of results */ 
  long count();
}

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


/** query for ongoing and finished
 * {@linkplain HistoryProcessInstance process instances}.
 * 
 * @author Tom Baeyens
 */
public interface HistoryProcessInstanceQuery {
  
  /** starttime property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_STARTTIME = "startTime";
  /** endtime property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_ENDTIME = "endTime";
  /** id property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_ID = "id";
  /** state property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_STATE = "state";
  /** duration property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_DURATION = "duration";
  /** Key propertu to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_KEY = "key";

  /** select only the process instances with the given id */
  HistoryProcessInstanceQuery processInstanceId(String processInstanceId);

  /** select only process instances with the given process definition */
  HistoryProcessInstanceQuery processDefinitionId(String processDefinitionId);
  
  /** select only process instances with the given business key */
  HistoryProcessInstanceQuery processInstanceKey(String processInstanceKey);

  /** select only process instances in the given state */
  HistoryProcessInstanceQuery state(String state);

  /** order selected process instances ascending for certain {@link #PROPERTY_STARTTIME properties} */
  HistoryProcessInstanceQuery orderAsc(String property);

  /** order selected process instances ascending for certain {@link #PROPERTY_STARTTIME properties} */
  HistoryProcessInstanceQuery orderDesc(String property);

  /** select a specific page in the result set */
  HistoryProcessInstanceQuery page(int firstResult, int maxResults);

  /** select only process instances that have ended.
   * cancels bounds previously set by {@link #endedBefore(Date)} or
   * {@link #endedAfter(Date)} */
  HistoryProcessInstanceQuery ended();

  /** select only process instances that have ended before the given date.
   * combine with {@link #endedAfter(Date)} to specify a finite interval
   * @throws IllegalArgumentException if the given threshold is later
   * than the end-after date (if set), thus yielding an empty interval */
  HistoryProcessInstanceQuery endedBefore(Date threshold);

  /** select only process instances that have ended on or after the given date.
   * combine with {@link #endedBefore(Date)} to specify a finite interval
   * @throws IllegalArgumentException if the given threshold is earlier
   * than the end-before date (if set), thus yielding an empty interval */
  HistoryProcessInstanceQuery endedAfter(Date threshold);

  /** execute the query and obtain the list of {@link HistoryProcessInstance}s */
  List<HistoryProcessInstance> list();

  /** execute the query and obtain the unique {@link HistoryProcessInstance} */
  HistoryProcessInstance uniqueResult();
  
  /** execute a count(*) query and returns number of results */ 
  long count();
}

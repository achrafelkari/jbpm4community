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


/** query for task comments, task assignments and so on.
 * 
 * @author Tom Baeyens
 */
public interface HistoryDetailQuery {

  /** userId property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_USERID = "userId";
  /** time property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_TIME = "time";

  /** only select details for the given processInstanceId */
  HistoryDetailQuery processInstanceId(String processInstanceId);

  /** only select details for the given activityInstanceId */
  HistoryDetailQuery activityInstanceId(String activityInstanceId);

  /** only select details for the given taskId */
  HistoryDetailQuery taskId(String taskId);

  /** only select details after the given time */
  HistoryDetailQuery timeAfter(Date time);

  /** only select details before the given time */
  HistoryDetailQuery timeBefore(Date time);

  /** only select details of type comment 
   * (this usually used in combination with {@link #processDefinitionId(String)}) */
  HistoryDetailQuery comments();

  /** order selected process definitions ascending  */
  HistoryDetailQuery orderAsc(String property);
  
  /** order selected process definitions descending  */
  HistoryDetailQuery orderDesc(String property);

  /** select a specific page in the result set */
  HistoryDetailQuery page(int firstResult, int maxResults);

  /** execute the query and obtain the list of {@link HistoryDetail}s */
  List<HistoryDetail> list(); 

  /** execute the query and obtain the unique {@link HistoryDetail} */
  HistoryDetail uniqueResult();

  /** execute a count(*) query and returns number of results */ 
  long count();
}

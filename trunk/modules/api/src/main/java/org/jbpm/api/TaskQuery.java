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

import java.util.List;

import org.jbpm.api.task.Task;


/** query for tasks.
 * 
 * @author Tom Baeyens
 * @author Heiko Braun <heiko.braun@jboss.com> */
public interface TaskQuery {
  
  /** name property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_NAME = "name";
  /** assignee property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_ASSIGNEE = "assignee";
  /** createdate property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_CREATEDATE = "createTime";
  /** duedate property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_DUEDATE = "duedate";
  /** priority property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_PRIORITY = "priority";
  /** progress property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  String PROPERTY_PROGRESS = "progress";

  /** only select tasks for which the given user is the assignee */
  TaskQuery assignee(String userId);
  
  /** only select tasks that are unassigned.
   * These tasks can still potentially have candidates. */
  TaskQuery unassigned();

  /** only query for tasks for which the given user is a candidate.
   * The user could be associated directly as a candidate participant 
   * to the task or the user could be a member of a group that is 
   * associated as a candidate group to the task. */
  TaskQuery candidate(String userId);
  
    /** only select tasks that are associated to the given execution */
  TaskQuery executionId(String executionId);

  /** only select tasks that are associated to the given process instance */
  TaskQuery processInstanceId(String processInstanceId);

  /** only select tasks that are associated to the given process definition */
  TaskQuery processDefinitionId(String processDefinitionId);

  /** only select tasks that are associated to the given activity name.  This 
   * can be used in combination with the {@link #processDefinitionId(String)} */
  TaskQuery activityName(String activityName);

  /** only select suspended tasks */
  TaskQuery suspended();

  /** only select tasks that are not suspended */
  TaskQuery notSuspended();

  /** select a specific page in the result set */
  TaskQuery page(int firstResult, int maxResults);
  
  /** order selected tasks ascending for certain {@link #PROPERTY_NAME properties} */
  TaskQuery orderAsc(String property);
  
  /** order selected tasks descending for certain {@link #PROPERTY_NAME properties} */
  TaskQuery orderDesc(String property);
  
  /** execute a count(*) query and returns number of results */ 
  long count();
  
  /** execute the query and obtain the list of {@link Task}s */
  List<Task> list();

  /** execute the query and obtain the unique {@link Task} */
  Task uniqueResult();
}

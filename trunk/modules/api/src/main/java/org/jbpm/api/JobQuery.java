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

import org.jbpm.api.job.Job;


/** query for jBPM related messages and timers.
 * 
 * <p>Both {@link org.jbpm.api.job.Message messages} and 
 * {@link org.jbpm.api.job.Timer timers} are 
 * {@link org.jbpm.api.job.Job jobs}.</p>
 * 
 * @author Tom Baeyens
 */
public interface JobQuery {

  /** duedate property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  public static final String PROPERTY_DUEDATE = "dueDate";
  /** state property to be used as property in {@link #orderAsc(String)} and {@link #orderDesc(String)} */
  public static final String PROPERTY_STATE = "state";
  
  /** only select messages */
  JobQuery messages();
  
  /** only select timers */
  JobQuery timers();
  
  /** only select jobs related to the given process instance */ 
  JobQuery processInstanceId(String processInstanceId);
  
  /** only select jobs that were rolled back due to an exception */ 
  JobQuery exception(boolean hasException);

  /** order ascending for property {@link #PROPERTY_STATE} 
   * or {@link #PROPERTY_DUEDATE} */
  JobQuery orderAsc(String property);

  /** order descending for property {@link #PROPERTY_STATE} 
   * or {@link #PROPERTY_DUEDATE} */
  JobQuery orderDesc(String property);

  /** only select a specific page */ 
  JobQuery page(int firstResult, int maxResults);

  /** execute the query and get the result list */ 
  List<Job> list();

  /** execute the query and get the unique result */ 
  Job uniqueResult();
  
  /** execute a count(*) query and returns number of results */ 
  long count();
}

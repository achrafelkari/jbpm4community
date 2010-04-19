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


/** operations targeted to system operators that need to keep 
 * the process engine up and running.  This functionality is typically 
 * exposed through a management web console. 
 * 
 * @author Tom Baeyens
 */
public interface ManagementService {

  /** resets the retry count, clears the exception and executes the job. 
   * An exception is thrown out of this method in case the execution 
   * of the job fails.  In case the async command executor is configured 
   * for this service, failing job execution will not result into an 
   * exception coming out of this method. */
  void executeJob(String jobId);

  /** search for jobs */
  JobQuery createJobQuery();
  
  /**
   * Deletes the job with the given id.
   * @return True if the deletion was succesful.
   */
  boolean deleteJob(long jobId);
}

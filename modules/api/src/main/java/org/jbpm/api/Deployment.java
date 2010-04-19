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

/** represents an existing deployment in the repository.
 * 
 * @author Tom Baeyens
 */
public interface Deployment {

  /** normal deployments for which contained process definitions 
   * can be executed. */
  String STATE_ACTIVE = "active";
  
  /** invisible for all operations except delete or resume.  This is 
   * to have a kind of undoable delete operation. */
  String STATE_SUSPENDED = "suspended";

  /** unique identification of this deployment that is used as a reference in the service methods */ 
  String getId();

  /** typically correspond to the file name or url or some other 
   * form of identifying the source archive file for this deployment. */
  String getName();
  
  /** the timestamp can optionally be given and represents the last updated timestamp 
   * of the archive file that is being deployed. 
   * JBoss deployer makes use of this to remember if a file is already deployed or not.*/
  long getTimestamp();

  /** {@link #STATE_ACTIVE} or {@link #STATE_SUSPENDED} */
  String getState();
}

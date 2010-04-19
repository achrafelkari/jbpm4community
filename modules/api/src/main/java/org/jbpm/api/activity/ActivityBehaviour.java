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
package org.jbpm.api.activity;

import java.io.Serializable;

/** implements the runtime behaviour of an activity.
 * 
 * @author Tom Baeyens
 */
public interface ActivityBehaviour extends Serializable {
  
  /** invoked when an execution arrives in an activity.
   * 
   * <p>An ActivityBehaviour can control the propagation 
   * of execution.  ActivityBehaviour's can become external activities when they 
   * invoke {@link ActivityExecution#waitForSignal()}.  That means the 
   * activity will become a wait state.  In that case, {@link ExternalActivityBehaviour} 
   * should be implemented to also handle the external signals. 
   * </p> */
  void execute(ActivityExecution execution) throws Exception;
}

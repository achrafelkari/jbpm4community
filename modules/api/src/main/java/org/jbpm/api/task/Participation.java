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
package org.jbpm.api.task;


/**
 * a link to a participating identity (user or group) and the type of involvement.
 * 
 * @author Tom Baeyens
 * @author Heiko Braun <heiko.braun@jboss.com>
 */
public interface Participation {

  /** alternative owner, but as long as this person is not the owner. 
   * This person is allowed to make comments, but nothing else. */
  String CANDIDATE = "candidate";

  /** the person with ultimate responsibility over a task. */
  String OWNER = "owner";

  /** person that will be using the result of this task.  This person is 
   * allowed to make comments, but nothing else. */
  String CLIENT = "client";

  /** person that is allowed to watch-but-not-touch this task */
  String VIEWER = "viewer";

  /** a person that was assigned to a task, but got replaced because of 
   * absence or another reason. This way, a trace can be left in case 
   * this person returns and wants to take back his tasks that got 
   * reassigned. */
  String REPLACED_ASSIGNEE = "replaced-assignee";

  /** the unique id for this participation that is used as a reference in the service methods */
  String getId();

  /** userId for this participation. 
   * null in case this is a {@link #getGroupId() group participation}. */
  String getUserId();

  /** groupId for this participation. 
   * null in case this is a {@link #getUserId() user participation}. */
  String getGroupId();

  /** see constants for default participations */
  String getType();
}

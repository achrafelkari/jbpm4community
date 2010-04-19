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

import org.jbpm.api.identity.Group;
import org.jbpm.api.identity.User;

/** interface to expose the (configurable) identity component that is 
 * used by jBPM.
 *  
 * @author Tom Baeyens
 */
public interface IdentityService {

  /** create a new user. */
  void createUser(String userId, String givenName, String familyName);
  
  /** create a new user, providing an email address */
  void createUser(String userId, String givenName, String familyName, String businessEmail);

  /** lookup a user 
   * @return the user or null if no such user exists */
  User findUserById(String userId);

  /** get all the users in the system.
   * Returns an empty list if no users exist. */
  List<User> findUsers();

  /** delete the given user.  
   * No effect (no exception) if the user does not exist. 
   * Deletes also the memberships related to the given user, but does not delete the 
   * associated groups. */
  void deleteUser(String userId);

  /** create a group new group 
   * @return the generated id for this group. */
  String createGroup(String groupName);

  /** create a group new group 
   * @return the generated id for this group. */
  String createGroup(String groupName, String groupType);

  /** create a group new group 
   * @return the generated id for this group. */
  String createGroup(String groupName, String groupType, String parentGroupId);

  /** lookup a group.
  * @return the user or null if no such user exists */
  Group findGroupById(String groupId);
  
  /** groups of the given groupType for which the given user is a member.
   * Returns an empty list if no such groups exist. */
  List<Group> findGroupsByUserAndGroupType(String userId, String groupType);
  
  /** all group ids (String) for which this user is a member.
   * Returns an empty list if no such groups exist. */
  List<Group> findGroupsByUser(String userId);
  
  /** all group ids (String) for which this user is a member.
   * Returns an empty list if no such groups exist. */
  List<String> findGroupIdsByUser(String userId);
  
  /** deletes the given group.
   * No effect (no exception) if the group does not exist. 
   * Deleting a group also deletes all the memberships associated to that group, 
   * but not the associated users. */
  void deleteGroup(String groupId);

  /** makes the given user a member of the given group. */
  void createMembership(String userId, String groupId);
  
  /** makes the given user a member of the given group with the given role.
   * Role can be null. */
  void createMembership(String userId, String groupId, String role);

  /** deletes the identified relation between a user and a group. 
   * Role can be null.  If no such membership exists, this method will 
   * not throw an exception and have no effect. */
  void deleteMembership(String userId, String groupId, String role);
}
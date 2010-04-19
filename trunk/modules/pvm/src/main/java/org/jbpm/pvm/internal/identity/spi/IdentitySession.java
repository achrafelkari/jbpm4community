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
package org.jbpm.pvm.internal.identity.spi;

import java.util.List;

import org.jbpm.api.identity.Group;
import org.jbpm.api.identity.User;

/**
 * @author Tom Baeyens
 */
public interface IdentitySession {

  /** create a new user */
  String createUser(String userId, String givenName, String familyName, String businessEmail);
  
  /** lookup a user.
   * @return the user or null if no such user exists */
  User findUserById(String userId);

  /** lookup users with the given identifiers.
   * @return an empty list if no such users exist */
  List<User> findUsersById(String... userIds);

  /** get all the users in the system.
   * @return an empty list if no users exist. */
  List<User> findUsers();

  /** delete the given user.  
   * No effect (no exception) if the user does not exist. */
  void deleteUser(String userId);

  /** create a group new group 
   * @return the generated id for this group. */
  String createGroup(String groupName, String groupType, String parentGroupId);

  /**
   * lookup users with membership in group.
   * @return the users or an empty list if no users exist */
  List<User> findUsersByGroup(String groupId);
  
  /** lookup a group.
  * @return the user or null if no such user exists */
  Group findGroupById(String groupId);
  
  /** groups of the given groupType for which the given user is a member.
   * @return an empty list if no such groups exist. */
  List<Group> findGroupsByUserAndGroupType(String userId, String groupType);
  
  /** all groups for which this user is a member.
   * @return an empty list if no such groups exist. */
  List<Group> findGroupsByUser(String userId);
  
  /** deletes the given group.
   * No effect (no exception) if the group does not exist. */
  void deleteGroup(String groupId);

  /** makes the given user a member of the given group with the given role.
   * Role can be null. */
  void createMembership(String userId, String groupId, String role);

  /** makes the given user a member of the given group with the given role.
   * Role can be null.  If no such membership exists, this method will 
   * not throw an exception and have no effect. */
  void deleteMembership(String userId, String groupId, String role);
}

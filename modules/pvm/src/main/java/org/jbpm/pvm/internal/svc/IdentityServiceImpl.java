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
package org.jbpm.pvm.internal.svc;

import java.util.List;

import org.jbpm.api.IdentityService;
import org.jbpm.api.identity.Group;
import org.jbpm.api.identity.User;
import org.jbpm.pvm.internal.cmd.CommandService;
import org.jbpm.pvm.internal.identity.cmd.CreateGroupCmd;
import org.jbpm.pvm.internal.identity.cmd.CreateMembershipCmd;
import org.jbpm.pvm.internal.identity.cmd.CreateUserCmd;
import org.jbpm.pvm.internal.identity.cmd.DeleteGroupCmd;
import org.jbpm.pvm.internal.identity.cmd.DeleteMembershipCmd;
import org.jbpm.pvm.internal.identity.cmd.DeleteUserCmd;
import org.jbpm.pvm.internal.identity.cmd.FindGroupCmd;
import org.jbpm.pvm.internal.identity.cmd.FindGroupsCmd;
import org.jbpm.pvm.internal.identity.cmd.FindUserCmd;
import org.jbpm.pvm.internal.identity.cmd.FindUsersCmd;


/**
 * @author Tom Baeyens
 */
public class IdentityServiceImpl extends AbstractServiceImpl implements IdentityService {
  
  protected CommandService commandService;

  public void createUser(String userId, String givenName, String familyName) {
    commandService.execute(new CreateUserCmd(userId, givenName, familyName));
  }

  public void createUser(String userId, String givenName, String familyName, String businessEmail) {
    commandService.execute(new CreateUserCmd(userId, givenName, familyName, businessEmail));
  }

  public User findUserById(String userId) {
    return commandService.execute(new FindUserCmd(userId));
  }

  public List<User> findUsers() {
    return commandService.execute(new FindUsersCmd());
  }

  public void deleteUser(String userId) {
    commandService.execute(new DeleteUserCmd(userId));
  }

  public String createGroup(String groupName) {
    return commandService.execute(new CreateGroupCmd(groupName, null, null));
  }

  public String createGroup(String groupName, String groupType) {
    return commandService.execute(new CreateGroupCmd(groupName, groupType, null));
  }

  public String createGroup(String groupName, String groupType, String parentGroupId) {
    return commandService.execute(new CreateGroupCmd(groupName, groupType, parentGroupId));
  }

  public Group findGroupById(String groupId) {
    return commandService.execute(new FindGroupCmd(groupId));
  }

  public List<String> findGroupIdsByUser(String userId) {
    return commandService.execute(new FindGroupIds(userId));
  }

  public List<Group> findGroupsByUser(String userId) {
    return commandService.execute(new FindGroupsCmd(userId));
  }

  public List<Group> findGroupsByUserAndGroupType(String userId, String groupType) {
    return commandService.execute(new FindGroupsCmd(userId, groupType));
  }

  public void deleteGroup(String groupId) {
    commandService.execute(new DeleteGroupCmd(groupId));
  }

  public void createMembership(String userId, String groupId) {
    commandService.execute(new CreateMembershipCmd(userId, groupId, null));
  }

  public void createMembership(String userId, String groupId, String role) {
    commandService.execute(new CreateMembershipCmd(userId, groupId, role));
  }


  public void deleteMembership(String userId, String groupId, String role) {
    commandService.execute(new DeleteMembershipCmd(userId, groupId, role));
  }
}

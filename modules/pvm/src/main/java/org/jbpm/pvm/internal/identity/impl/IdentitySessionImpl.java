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
package org.jbpm.pvm.internal.identity.impl;

import java.util.Arrays;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import org.jbpm.api.JbpmException;
import org.jbpm.api.identity.Group;
import org.jbpm.api.identity.User;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.id.DbidGenerator;
import org.jbpm.pvm.internal.identity.spi.IdentitySession;
import org.jbpm.pvm.internal.util.CollectionUtil;

/**
 * @author Tom Baeyens
 * @author Huisheng Xu
 */
public class IdentitySessionImpl implements IdentitySession {

  protected Session session;

  public String createUser(String userName, String givenName, String familyName,
    String businessEmail) {
    try {
      User user = findUserById(userName);
      if (user != null) {
        throw new JbpmException("Cannot create user, userId: [" + userName + "] has been used");
      }
    } catch(Exception ex) {
      throw new JbpmException("Cannot create user, error while validating", ex);
    }
    UserImpl user = new UserImpl(userName, givenName, familyName);
    user.setBusinessEmail(businessEmail);

    long dbid = EnvironmentImpl.getFromCurrent(DbidGenerator.class).getNextId();
    user.setDbid(dbid);

    session.save(user);

    return user.getId();
  }

  public User findUserById(String userId) {
    return (User) session.createCriteria(UserImpl.class)
      .add(Restrictions.eq("id", userId))
      .uniqueResult();
  }

  public List<User> findUsersById(String... userIds) {
    List<?> users = session.createCriteria(UserImpl.class)
      .add(Restrictions.in("id", userIds))
      .list();
    if (userIds.length != users.size()) {
      throw new JbpmException("not all users were found: " + Arrays.toString(userIds));
    }
    return CollectionUtil.checkList(users, User.class);
  }

  public List<User> findUsers() {
    List<?> users = session.createCriteria(UserImpl.class).list();
    return CollectionUtil.checkList(users, User.class);
  }

  public void deleteUser(String userId) {
    // lookup the user
    User user = findUserById(userId);

    // cascade the deletion to the memberships
    List<?> memberships = session.createCriteria(MembershipImpl.class)
      .add(Restrictions.eq("user", user))
      .list();

    // delete the related memberships
    for (Object membership : memberships) {
      session.delete(membership);
    }

    // delete the user
    session.delete(user);
  }

  public String createGroup(String groupName, String groupType, String parentGroupId) {
    try {
      GroupImpl group = findGroupById(groupName);
      if (group != null) {
        throw new JbpmException("Cannot create group, groupId: [" + groupName + "] has been used");
      }
    } catch(Exception ex) {
      throw new JbpmException("Cannot create group, error while validating", ex);
    }
    GroupImpl group = new GroupImpl();
    String groupId = groupType != null ? groupType + "." + groupName : groupName;
    group.setId(groupId);

    long dbid = EnvironmentImpl.getFromCurrent(DbidGenerator.class).getNextId();
    group.setDbid(dbid);

    group.setName(groupName);
    group.setType(groupType);

    if (parentGroupId != null) {
      GroupImpl parentGroup = findGroupById(parentGroupId);
      group.setParent(parentGroup);
    }

    session.save(group);

    return group.getId();
  }

  public List<User> findUsersByGroup(String groupId) {
    List<?> users = session.createCriteria(MembershipImpl.class)
      .createAlias("group", "g")
      .add(Restrictions.eq("g.id", groupId))
      .setProjection(Projections.property("user"))
      .list();
    return CollectionUtil.checkList(users, User.class);
  }

  public GroupImpl findGroupById(String groupId) {
    return (GroupImpl) session.createCriteria(GroupImpl.class)
      .add(Restrictions.eq("id", groupId))
      .uniqueResult();
  }

  public List<Group> findGroupsByUserAndGroupType(String userId, String groupType) {
    List<?> groups = session.getNamedQuery("findGroupsByUserAndGroupType")
      .setString("userId", userId)
      .setString("groupType", groupType)
      .list();
    return CollectionUtil.checkList(groups, Group.class);
  }

  public List<Group> findGroupsByUser(String userId) {
    List<?> groups = session.getNamedQuery("findGroupsByUser")
      .setParameter("userId", userId)
      .list();
    return CollectionUtil.checkList(groups, Group.class);
  }

  public List<Group> findGroups() {
    List<?> groups = session.createCriteria(GroupImpl.class).list();
    return CollectionUtil.checkList(groups, Group.class);
  }

  public void deleteGroup(String groupId) {
    // look up the group
    GroupImpl group = findGroupById(groupId);

    // cascade the deletion to the memberships
    List<?> memberships = session.createCriteria(MembershipImpl.class)
      .add(Restrictions.eq("group", group))
      .list();

    // delete the related memberships
    for (Object membership : memberships) {
      session.delete(membership);
    }

    // delete the group
    session.delete(group);
  }

  public void createMembership(String userId, String groupId, String role) {
    User user = findUserById(userId);
    if (user == null) {
      throw new JbpmException("user " + userId + " doesn't exist");
    }
    Group group = findGroupById(groupId);
    if (group == null) {
      throw new JbpmException("group " + groupId + " doesn't exist");
    }

    MembershipImpl membership = new MembershipImpl();
    membership.setUser(user);
    membership.setGroup(group);
    membership.setRole(role);

    long dbid = EnvironmentImpl.getFromCurrent(DbidGenerator.class).getNextId();
    membership.setDbid(dbid);

    session.save(membership);
  }

  public void deleteMembership(String userId, String groupId, String role) {
    MembershipImpl membership = (MembershipImpl) session.createCriteria(MembershipImpl.class)
      .createAlias("user", "u")
      .createAlias("group", "g")
      .add(Restrictions.eq("u.id", userId))
      .add(Restrictions.eq("g.id", groupId))
      .uniqueResult();
    session.delete(membership);
  }

  public void setSession(Session session) {
    this.session = session;
  }

}

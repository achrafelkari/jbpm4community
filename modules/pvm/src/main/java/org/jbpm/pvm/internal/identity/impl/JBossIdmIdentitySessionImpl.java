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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.picketlink.idm.api.Attribute;
import org.picketlink.idm.api.AttributesManager;
import org.picketlink.idm.api.IdentitySearchCriteria;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.RoleManager;
import org.picketlink.idm.api.RoleType;
import org.picketlink.idm.common.exception.FeatureNotSupportedException;
import org.picketlink.idm.common.exception.IdentityException;
import org.picketlink.idm.common.p3p.P3PConstants;
import org.picketlink.idm.impl.api.SimpleAttribute;
import org.picketlink.idm.impl.api.model.GroupKey;
import org.picketlink.idm.impl.api.model.SimpleGroup;

import org.jbpm.api.JbpmException;
import org.jbpm.api.identity.Group;
import org.jbpm.api.identity.User;

/**
 * @author Tom Baeyens
 * @author Jeff Yu
 */
public class JBossIdmIdentitySessionImpl implements
  org.jbpm.pvm.internal.identity.spi.IdentitySession {

  protected IdentitySession identitySession;

  private static final String DEFAULT_JBPM_MEMBER_ROLE = "default_jBPM_member_role";

  private static final String DEFAULT_JBPM_GROUP_TYPE = "default_jBPM_Group_Type";

  public JBossIdmIdentitySessionImpl(IdentitySession identitySession) {
    this.identitySession = identitySession;
  }

  public String createUser(String userName, String givenName, String familyName,
    String businessEmail) {
    try {
      org.picketlink.idm.api.User idUser = identitySession.getPersistenceManager()
        .createUser(userName);

      List<Attribute> attrs = new ArrayList<Attribute>();
      if (givenName != null) {
        attrs.add(new SimpleAttribute(P3PConstants.INFO_USER_NAME_GIVEN, givenName));
      }
      if (familyName != null) {
        attrs.add(new SimpleAttribute(P3PConstants.INFO_USER_NAME_FAMILY, familyName));
      }
      if (businessEmail != null) {
        attrs.add(new SimpleAttribute(P3PConstants.INFO_USER_BUSINESS_INFO_ONLINE_EMAIL,
          businessEmail));
      }

      identitySession.getAttributesManager()
        .addAttributes(idUser, attrs.toArray(new Attribute[attrs.size()]));
      return idUser.getId();
    }
    catch (IdentityException e) {
      throw new JbpmException("could not create user: " + userName, e);
    }
  }

  public List<User> findUsers() {
    try {
      Collection<org.picketlink.idm.api.User> idUsers = identitySession.getPersistenceManager()
        .findUser((IdentitySearchCriteria) null);

      List<User> users = new ArrayList<User>();
      for (org.picketlink.idm.api.User idUser : idUsers) {
        users.add(getUserInfo(idUser));
      }
      return users;
    }
    catch (IdentityException e) {
      throw new JbpmException("could not find users", e);
    }
  }

  public User findUserById(String userId) {
    try {
      org.picketlink.idm.api.User idUser = identitySession.getPersistenceManager()
        .findUser(userId);
      if (idUser != null) {
        return getUserInfo(idUser);
      }
      return null;
    }
    catch (IdentityException e) {
      throw new JbpmException("could not find user by id: " + userId, e);
    }
  }

  private User getUserInfo(org.picketlink.idm.api.User idUser) throws IdentityException {
    String name = idUser.getId();
    String givenName = getAttributeString(idUser, P3PConstants.INFO_USER_NAME_GIVEN);
    String familyName = getAttributeString(idUser, P3PConstants.INFO_USER_NAME_FAMILY);
    String businessEmail = getAttributeString(idUser, P3PConstants.INFO_USER_BUSINESS_INFO_ONLINE_EMAIL);

    UserImpl user = new UserImpl(name, givenName, familyName);
    user.setBusinessEmail(businessEmail);
    return user;
  }

  public List<User> findUsersById(String... userIds) {
    List<User> users = new ArrayList<User>();
    for (String userId : userIds) {
      User user = findUserById(userId);
      if (user != null)
        users.add(user);
    }
    return users;
  }

  public List<User> findUsersByGroup(String groupKey) {
    try {
      List<User> users = new ArrayList<User>();
      org.picketlink.idm.api.Group idGroup = findIdmGroupByKey(convertjbpmGroupId2IdmGroupKey(groupKey));
      if (idGroup == null)
        return users;

      Collection<org.picketlink.idm.api.User> idusers = identitySession.getRoleManager()
        .findUsersWithRelatedRole(idGroup, null);
      for (org.picketlink.idm.api.User iduser : idusers) {
        users.add(findUserById(iduser.getId()));
      }
      return users;
    }
    catch (IdentityException e) {
      throw new JbpmException("could not find users by group: " + groupKey, e);
    }
    catch (FeatureNotSupportedException e) {
      throw new JbpmException("could not find users by group: " + groupKey, e);
    }
  }

  public void deleteUser(String userName) {
    try {
      identitySession.getPersistenceManager().removeUser(userName, true);
    }
    catch (IdentityException e) {
      throw new JbpmException("could not delete user: " + userName, e);
    }
  }

  public String createGroup(String groupName, String groupType, String parentGroupKey) {
    try {
      if (groupType == null)
        groupType = DEFAULT_JBPM_GROUP_TYPE;

      SimpleGroup group = (SimpleGroup) identitySession.getPersistenceManager()
        .createGroup(groupName, groupType);
      String groupKey = group.getKey();

      if (parentGroupKey != null) {
        org.picketlink.idm.api.Group parentGroup = findIdmGroupByKey(convertjbpmGroupId2IdmGroupKey(parentGroupKey));
        if (parentGroup == null) {
          throw new JbpmException("parent group does not exist: " + parentGroupKey);
        }
        identitySession.getRelationshipManager().associateGroups(parentGroup, group);
      }
      return convertIdmGroupKey2jbpmGroupId(groupKey);
    }
    catch (IdentityException e) {
      throw new JbpmException("could not create group: " + groupName, e);
    }
  }

  public Group findGroupById(String groupId) {
    try {
      SimpleGroup idGroup = (SimpleGroup) findIdmGroupByKey(convertjbpmGroupId2IdmGroupKey(groupId));
      if (idGroup == null) {
        return null;
      }
      GroupImpl group = new GroupImpl();
      group.setId(convertIdmGroupKey2jbpmGroupId(idGroup.getKey()));
      group.setType(idGroup.getGroupType());
      group.setName(idGroup.getName());

      Collection<org.picketlink.idm.api.Group> idParentGroups = identitySession.getRelationshipManager()
        .findAssociatedGroups(idGroup, null, false, false);

      if (idParentGroups.size() > 0) {
        SimpleGroup idParent = (SimpleGroup) idParentGroups.iterator().next();
        GroupImpl parentGroup = new GroupImpl();
        parentGroup.setId(convertIdmGroupKey2jbpmGroupId(idParent.getKey()));
        parentGroup.setType(idParent.getGroupType());
        parentGroup.setName(idParent.getName());

        group.setParent(parentGroup);
      }
      return group;
    }
    catch (IdentityException e) {
      throw new JbpmException("could not find group by id: " + groupId, e);
    }
  }

  public List<Group> findGroupsByGroupType(String groupType) {
    try {
      Collection<org.picketlink.idm.api.Group> idGroups = identitySession.getPersistenceManager()
        .findGroup(groupType);

      List<Group> groups = new ArrayList<Group>();
      for (org.picketlink.idm.api.Group idGroup : idGroups) {
        SimpleGroup simpleGroup = (SimpleGroup) idGroup;
        groups.add(findGroupById(convertIdmGroupKey2jbpmGroupId(simpleGroup.getKey())));
      }
      return groups;
    }
    catch (IdentityException e) {
      throw new JbpmException("could not find groups by type: " + groupType, e);
    }
  }

  public List<Group> findGroupsByUser(String userId) {
    try {
      Collection<org.picketlink.idm.api.Group> idGroups = identitySession.getRoleManager()
        .findGroupsWithRelatedRole(userId, null);

      List<Group> groups = new ArrayList<Group>();
      for (org.picketlink.idm.api.Group idGroup : idGroups) {
        groups.add(findGroupById(convertIdmGroupKey2jbpmGroupId(idGroup.getKey())));
      }
      return groups;
    }
    catch (IdentityException e) {
      throw new JbpmException("could not find groups by user: " + userId, e);
    }
    catch (FeatureNotSupportedException e) {
      throw new JbpmException("could not find groups by user: " + userId, e);
    }
  }

  public List<Group> findGroupsByUserAndGroupType(String userName, String groupType) {
    try {
      org.picketlink.idm.api.User idUser = identitySession.getPersistenceManager()
        .findUser(userName);

      Collection<org.picketlink.idm.api.Group> idGroups = identitySession.getRoleManager()
        .findGroupsWithRelatedRole(idUser, groupType, null);
      List<Group> groups = new ArrayList<Group>();

      for (org.picketlink.idm.api.Group idGroup : idGroups) {
        SimpleGroup simpleGroup = (SimpleGroup) idGroup;
        groups.add(findGroupById(convertIdmGroupKey2jbpmGroupId(simpleGroup.getKey())));
      }
      return groups;
    }
    catch (IdentityException e) {
      throw new JbpmException("could not find groups by user '" + userName + "' and type: "
        + groupType, e);
    }
    catch (FeatureNotSupportedException e) {
      throw new JbpmException("could not find groups by user '" + userName + "' and type: "
        + groupType, e);
    }
  }

  public void deleteGroup(String groupId) {
    try {
      org.picketlink.idm.api.Group group = findIdmGroupByKey(convertjbpmGroupId2IdmGroupKey(groupId));
      if (group == null)
        return;

      identitySession.getPersistenceManager().removeGroup(group, true);
    }
    catch (IdentityException e) {
      throw new JbpmException("could not delete group " + groupId, e);
    }
  }

  public void createMembership(String userId, String groupId, String role) {
    try {
      org.picketlink.idm.api.Group group = findIdmGroupByKey(convertjbpmGroupId2IdmGroupKey(groupId));
      if (group == null) {
        throw new JbpmException("group not found: " + groupId);
      }

      org.picketlink.idm.api.User idUser = identitySession.getPersistenceManager()
        .findUser(userId);
      if (idUser == null) {
        throw new JbpmException("user not found: " + userId);
      }

      if (role == null)
        role = DEFAULT_JBPM_MEMBER_ROLE;

      RoleManager roleManager = identitySession.getRoleManager();
      RoleType roleType = roleManager.getRoleType(role);
      if (roleType == null) {
        roleType = roleManager.createRoleType(role);
      }
      roleManager.createRole(roleType, idUser, group);
    }
    catch (IdentityException e) {
      throw new JbpmException("could not create membership for user '" + userId + "', group '"
        + groupId + "' and role: " + role, e);
    }
    catch (FeatureNotSupportedException e) {
      throw new JbpmException("could not create membership for user '" + userId + "', group '"
        + groupId + "' and role: " + role, e);
    }
  }

  public void deleteMembership(String userId, String groupId, String role) {
    try {
      RoleManager roleManager = identitySession.getRoleManager();
      RoleType rtype = roleManager.getRoleType(role);
      roleManager.removeRole(rtype.getName(), userId, convertjbpmGroupId2IdmGroupKey(groupId));
    }
    catch (IdentityException e) {
      throw new JbpmException("could not delete membership for user '" + userId + "', group '"
        + groupId + "' and role: " + role, e);
    }
    catch (FeatureNotSupportedException e) {
      throw new JbpmException("could not delete membership for user '" + userId + "', group '"
        + groupId + "' and role: " + role, e);
    }
  }

  protected org.picketlink.idm.api.Group findIdmGroupByKey(String groupKey) {
    try {
      return identitySession.getPersistenceManager().findGroupByKey(groupKey);
    }
    catch (IdentityException e) {
      throw new JbpmException("could not find group by key: " + groupKey, e);
    }
  }

  protected String getAttributeString(org.picketlink.idm.api.User idUser, String attributeName)
    throws IdentityException {
    return getAttributeString(idUser, null, attributeName);
  }

  protected String getAttributeString(org.picketlink.idm.api.Group idGroup, String attributeName)
    throws IdentityException {
    return getAttributeString(null, idGroup, attributeName);
  }

  protected String getAttributeString(org.picketlink.idm.api.User idUser,
    org.picketlink.idm.api.Group idGroup, String attributeName) throws IdentityException {
    AttributesManager attributesManager = identitySession.getAttributesManager();
    Attribute attribute;
    if (idUser != null) {
      attribute = attributesManager.getAttribute(idUser, attributeName);
    }
    else {
      attribute = attributesManager.getAttribute(idGroup, attributeName);
    }
    return attribute != null ? (String) attribute.getValue() : null;
  }

  /**
   * Return jBPM groupId, which is: GroupType.GroupName, from IDM GroupKey
   */
  private String convertIdmGroupKey2jbpmGroupId(String groupKey) {
    GroupKey theGroupKey = new GroupKey(groupKey);
    String type = theGroupKey.getType();
    if (type == null || DEFAULT_JBPM_GROUP_TYPE.equals(type)) {
      return theGroupKey.getName();
    }
    return type + "." + theGroupKey.getName();
  }

  /**
   * Convert the jBPM GroupId to IDM GroupKey.
   */
  private String convertjbpmGroupId2IdmGroupKey(String jbpmGroupId) {
    StringTokenizer st = new StringTokenizer(jbpmGroupId, ".");
    String type = DEFAULT_JBPM_GROUP_TYPE;
    if (st.countTokens() > 1) {
      type = st.nextToken();
    }

    String name = st.nextToken();
    return new GroupKey(name, type).getKey();
  }

  public IdentitySession getIdentitySession() {
    return identitySession;
  }

  public void setIdentitySession(IdentitySession identitySession) {
    this.identitySession = identitySession;
  }
}

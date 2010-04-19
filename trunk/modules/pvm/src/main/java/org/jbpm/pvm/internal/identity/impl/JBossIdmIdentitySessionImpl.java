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

import org.jboss.identity.idm.api.Attribute;
import org.jboss.identity.idm.api.AttributesManager;
import org.jboss.identity.idm.api.IdentitySearchCriteria;
import org.jboss.identity.idm.api.IdentitySession;
import org.jboss.identity.idm.api.RoleType;
import org.jboss.identity.idm.common.exception.FeatureNotSupportedException;
import org.jboss.identity.idm.common.exception.IdentityException;
import org.jboss.identity.idm.common.p3p.P3PConstants;
import org.jboss.identity.idm.impl.api.SimpleAttribute;
import org.jboss.identity.idm.impl.api.model.GroupId;
import org.jbpm.api.JbpmException;
import org.jbpm.api.identity.Group;
import org.jbpm.api.identity.User;

/**
 * @author Tom Baeyens
 * @author Jeff Yu
 */
public class JBossIdmIdentitySessionImpl implements org.jbpm.pvm.internal.identity.spi.IdentitySession {

  protected IdentitySession identitySession;

  public String DEFAULT_JBPM_MEMBER_ROLE = "default_jBPM_member_role";
  
  public String DEFAUL_JBPM_GROUP_TYPE = "default_jBPM_Group_Type";
  
  public JBossIdmIdentitySessionImpl(IdentitySession identitySession) {
    this.identitySession = identitySession;
  }

  public String createUser(String userName, String givenName, String familyName, String businessEmail) {
    try {
    	
      org.jboss.identity.idm.api.User idUser= identitySession.getPersistenceManager().createUser(userName);
      
      List<Attribute> attrs = new ArrayList<Attribute>();
      if (givenName != null) {
    	  attrs.add(new SimpleAttribute(P3PConstants.INFO_USER_NAME_GIVEN, givenName));
      }
      if (familyName != null) {
    	  attrs.add(new SimpleAttribute(P3PConstants.INFO_USER_NAME_FAMILY, familyName));
      }
      if (businessEmail != null) {
    	  attrs.add(new SimpleAttribute(P3PConstants.INFO_USER_BUSINESS_INFO_ONLINE_EMAIL, businessEmail));
      }
            
      identitySession.getAttributesManager().addAttributes(idUser, attrs.toArray(new Attribute[attrs.size()]));
      return idUser.getId();
      
    } catch (IdentityException e) {
      throw new JbpmException("couldn't create user "+userName, e);
    }

  }
  

  public List<User> findUsers() {
    try {
      Collection<org.jboss.identity.idm.api.User> idUsers = 
    	  identitySession.getPersistenceManager().findUser((IdentitySearchCriteria)null);    
      
      List<User> users = new ArrayList<User>();
      for (org.jboss.identity.idm.api.User idUser : idUsers) {
        users.add(getUserInfo(idUser));
      }
      
      return users;
      
    } catch (IdentityException e) {
      throw new JbpmException("couldn't get users from identity component", e);
    }
  }

  public User findUserById(String userId) {
	  try {
		org.jboss.identity.idm.api.User idUser = identitySession.getPersistenceManager().findUser(userId);
		if (idUser != null) {
	    	return getUserInfo(idUser);
		}
		return null;
	} catch (IdentityException e) {
		throw new JbpmException("couldn't get user from id of " + userId, e);
	}
  }

private User getUserInfo(org.jboss.identity.idm.api.User idUser) throws IdentityException {
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
	for (String userId : userIds){
		User user = findUserById(userId);
		if (user != null) {
			users.add(user);
		}
	}
	  
    return users;
  }

  public List<User> findUsersByGroup(String groupId) {
	try {
		List<User> users = new ArrayList<User>();
		org.jboss.identity.idm.api.Group idGroup = findIdmGroupByIdmGroupId(convertjbpmGroupId2IdmGroupId(groupId));
		if (idGroup == null){
			return users;
		}
		Collection<org.jboss.identity.idm.api.User> idusers = 
				identitySession.getRoleManager().findUsersWithRelatedRole(idGroup, null);
		for (org.jboss.identity.idm.api.User iduser : idusers) {
			users.add(findUserById(iduser.getId()));
		}
		
		return users;
	} catch (IdentityException e) {
		throw new JbpmException("couldn't find users by groupid: " + groupId, e);
	} catch (FeatureNotSupportedException e) {
		throw new JbpmException("couldn't find users by groupid: " + groupId, e);
	}
	  
  }

  public void deleteUser(String userName) {
    try {    	
      identitySession.getPersistenceManager().removeUser(userName, true);    
    } catch (IdentityException e) {
      throw new JbpmException("couldn't delete user ["+userName + "]", e);
    }
  }

  public String createGroup(String groupName, String groupType, String parentGroupId) {
    
    try {
      String gtype = groupType;
      if (gtype == null) {
    	  gtype = DEFAUL_JBPM_GROUP_TYPE;
      }
      org.jboss.identity.idm.api.Group group = identitySession.getPersistenceManager().createGroup(groupName, gtype);
      String groupId = group.getId();
      
      if (parentGroupId!=null) {
        org.jboss.identity.idm.api.Group parentGroup = findIdmGroupByIdmGroupId(convertjbpmGroupId2IdmGroupId(parentGroupId));
        if (parentGroup==null) {
          throw new JbpmException("parent group "+parentGroupId+" doesn't exist");
        }
        identitySession.getRelationshipManager().associateGroups(parentGroup, group);
      }
     return convertIdmGroupId2jbpmGroupId(groupId);
     
    } catch (IdentityException e) {
      throw new JbpmException("couldn't create group "+groupName, e);
    }
    
  }
  
  public Group findGroupById(String groupId) {
	try {
		org.jboss.identity.idm.api.Group idGroup = findIdmGroupByIdmGroupId(convertjbpmGroupId2IdmGroupId(groupId));
		if (idGroup == null) {
			return null;
		}
		GroupImpl group = new GroupImpl();
		group.setId(convertIdmGroupId2jbpmGroupId(idGroup.getId()));
		group.setType(idGroup.getGroupType());
		group.setName(idGroup.getName());
		
		Collection<org.jboss.identity.idm.api.Group> idParentGroups = 
			identitySession.getRelationshipManager().findAssociatedGroups(idGroup, null, false, false);
		
		if (idParentGroups.size() > 0) {
			org.jboss.identity.idm.api.Group idParent = idParentGroups.iterator().next();
			GroupImpl parentGroup = new GroupImpl();
			parentGroup.setId(convertIdmGroupId2jbpmGroupId(idParent.getId()));
			parentGroup.setType(idParent.getGroupType());
			parentGroup.setName(idParent.getName());
			
			group.setParent(parentGroup);
		}
		
		return group;
		
	} catch (IdentityException e) {
		throw new JbpmException("couldn't find group by id [" + groupId + "]", e);
	}  
  }

  public List<Group> findGroupsByGroupType(String groupType) {
    try {
     Collection<org.jboss.identity.idm.api.Group> idGroups = identitySession.getPersistenceManager().
     															findGroup(groupType);	
    
      List<Group> groups = new ArrayList<Group>();
      for (org.jboss.identity.idm.api.Group idGroup: idGroups) {
        groups.add(findGroupById(convertIdmGroupId2jbpmGroupId(idGroup.getId())));
      }
      
      return groups;
      
    } catch (IdentityException e) {
      throw new JbpmException("couldn't get groups from identity component, groupType [" + groupType + "]", e);
    }
  }
  
  public List<Group> findGroupsByUser(String userId) {
	  
    try {
		Collection<org.jboss.identity.idm.api.Group> idGroups = identitySession.getRoleManager()
																	.findGroupsWithRelatedRole(userId, null);
		
		List<Group> groups = new ArrayList<Group>();
		for (org.jboss.identity.idm.api.Group idGroup : idGroups) {
			groups.add(findGroupById(convertIdmGroupId2jbpmGroupId(idGroup.getId())));
		}
		return groups;
	} catch (Exception e) {
		throw new JbpmException("Couldn't get Groups by userId [" + userId + "]", e);
		
	}
  }

  public List<Group> findGroupsByUserAndGroupType(String userName, String groupType) {
    try {
      org.jboss.identity.idm.api.User idUser = identitySession.getPersistenceManager().findUser(userName);
      
      Collection<org.jboss.identity.idm.api.Group> idGroups = identitySession.getRoleManager()
      														.findGroupsWithRelatedRole(idUser, groupType, null);
      List<Group> groups = new ArrayList<Group>();
      
      for (org.jboss.identity.idm.api.Group idGroup : idGroups) {
    	  groups.add(findGroupById(convertIdmGroupId2jbpmGroupId(idGroup.getId())));
      }
      return groups;
    } catch (Exception e) {
      throw new JbpmException("couldn't get groups for user "+userName+" and groupType "+groupType, e);
    }
  }

  public void deleteGroup(String groupId) {
    try {
      org.jboss.identity.idm.api.Group group = findIdmGroupByIdmGroupId(convertjbpmGroupId2IdmGroupId(groupId));

      if (group==null) {
        return;
      }

      identitySession.getPersistenceManager().removeGroup(group, true);
    
    } catch (IdentityException e) {
      throw new JbpmException("couldn't delete group "+groupId, e);
    }
  }

  public void createMembership(String userId, String groupId, String role) {
    try {
      org.jboss.identity.idm.api.Group group = findIdmGroupByIdmGroupId(convertjbpmGroupId2IdmGroupId(groupId));
      if (group==null) {
        throw new JbpmException("group "+groupId+" doesn't exist");
      }
  
      org.jboss.identity.idm.api.User idUser = identitySession.getPersistenceManager().findUser(userId);
      if (idUser==null) {
        throw new JbpmException("user "+userId+" doesn't exist");
      }
      
      if (role == null) {
    	  role = DEFAULT_JBPM_MEMBER_ROLE;
      }
	  RoleType roleType = identitySession.getRoleManager().getRoleType(role);
	  System.out.println("The Role Type is: " + roleType);
	  if (roleType == null) {
	    roleType = identitySession.getRoleManager().createRoleType(role);
	  }
      identitySession.getRoleManager().createRole(roleType, idUser, group);
      
    } catch (Exception e) {
      throw new JbpmException("couldn't create membership "+userId+", "+groupId+", "+role, e);
    }
  }
  
  public void deleteMembership(String userId, String groupId, String role) {
	try {
		RoleType rtype = identitySession.getRoleManager().getRoleType(role);
		identitySession.getRoleManager().removeRole(rtype.getName(), userId, convertjbpmGroupId2IdmGroupId(groupId));
	} catch (Exception e) {
		throw new JbpmException("couldn't delete the membership [" + userId + "," + groupId + "," + role + "]", e);
	}  
	
  }

  protected org.jboss.identity.idm.api.Group findIdmGroupByIdmGroupId(String groupId) {
	try {
		return identitySession.getPersistenceManager().findGroupById(groupId);
	} catch (IdentityException e) {
		throw new JbpmException("couldn't find the group by groupId: " + groupId, e);
	}  
  }

  protected String getAttributeString(org.jboss.identity.idm.api.User idUser, String attributeName) throws IdentityException {
    return getAttributeString(idUser, null, attributeName);
  }

  protected String getAttributeString(org.jboss.identity.idm.api.Group idGroup, String attributeName) throws IdentityException {
    return getAttributeString(null, idGroup, attributeName);
  }

  protected String getAttributeString(org.jboss.identity.idm.api.User idUser, org.jboss.identity.idm.api.Group idGroup, String attributeName) throws IdentityException {
    AttributesManager attributesManager = identitySession.getAttributesManager();
    Attribute attribute = null;
    if (idUser !=null) {
      attribute = attributesManager.getAttribute(idUser, attributeName);
    } else {
      attribute = attributesManager.getAttribute(idGroup, attributeName);
    }
    if (attribute!=null) {
      return (String) attribute.getValue();
    }
    return null;
  }
  
  
  /**
   * Return jBPM groupId, which is: GroupType.GroupName, from IDM GroupId
   * 
   * @param groupId
   * @return
   */
  private String convertIdmGroupId2jbpmGroupId(String groupId) {
	GroupId theGroupId = new GroupId(groupId);
	if (this.DEFAUL_JBPM_GROUP_TYPE.equals(theGroupId.getType()) || theGroupId.getType() == null) {
		return theGroupId.getName();
	}
	return theGroupId.getType() + "." + theGroupId.getName();
  }
  
  /**
   * Convert the jBPM GroupId to IDM GroupId.
   * 
   * @param jbpmGroupId
   * @return
   */
  private String convertjbpmGroupId2IdmGroupId(String jbpmGroupId) {
	  StringTokenizer st = new StringTokenizer(jbpmGroupId, ".");
	  String type = DEFAUL_JBPM_GROUP_TYPE;
	  if (st.countTokens() > 1) {
		  type = st.nextToken();
	  }
	  String name = st.nextToken();
	  
	  return new GroupId(name, type).getId();
  }
  
  public IdentitySession getIdentitySession() {
    return identitySession;
  }
  
  public void setIdentitySession(IdentitySession identitySession) {
    this.identitySession = identitySession;
  }


}

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
package org.jbpm.test.identity;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbpm.api.JbpmException;
import org.jbpm.api.identity.Group;
import org.jbpm.api.identity.User;
import org.jbpm.test.JbpmTestCase;

/**
 * @author Tom Baeyens
 * @author Huisheng Xu
 */
public class IdentityTest extends JbpmTestCase {

  public void testSingleUser() throws Exception {
    identityService.createUser("johndoe", "John", "Doe");

    User user = identityService.findUserById("johndoe");

    assertEquals("johndoe", user.getId());
    assertEquals("John", user.getGivenName());
    assertEquals("Doe", user.getFamilyName());

    identityService.deleteUser("johndoe");
  }

  public void testCreateGroup() throws Exception {
    String testGroupId = identityService.createGroup("testGroup", "unit", null);

    Group group = identityService.findGroupById(testGroupId);
    assertEquals("testGroup", group.getName());
    assertEquals("unit", group.getType());

    identityService.deleteGroup(testGroupId);
  }

  public void testFindGroupsByUser() throws Exception {
    String redhatGroupId = identityService.createGroup("redhat", "unit", null);

    identityService.createUser("jeffyu", "Jeff", "Yu");
    identityService.createMembership("jeffyu", redhatGroupId);

    List<Group> groups = identityService.findGroupsByUser("jeffyu");
    Set<String> groupNames = new HashSet<String>();
    for (Group group : groups) {
      groupNames.add(group.getName());
    }

    assertEquals(Collections.singleton("redhat"), groupNames);

    identityService.deleteUser("jeffyu");
    identityService.deleteGroup(redhatGroupId);
  }

  public void testFindGroupByUserAndGroupType() throws Exception {

    identityService.createUser("johndoe", "John", "Doe");
    String redhatGroupId = identityService.createGroup("redhat", "unit", null);
    identityService.createMembership("johndoe", redhatGroupId, "developer");

    List<Group> groups = identityService.findGroupsByUserAndGroupType("johndoe", "unit");
    Set<String> groupNames = new HashSet<String>();
    for (Group group : groups) {
      groupNames.add(group.getName());
    }

    assertEquals(Collections.singleton("redhat"), groupNames);

    identityService.deleteUser("johndoe");
    identityService.deleteGroup(redhatGroupId);
  }

  public void testSingleGroup() throws Exception {
    identityService.createUser("johndoe", "John", "Doe");
    identityService.createUser("joesmoe", "Joe", "Smoe");
    identityService.createUser("jackblack", "Jack", "Black");

    String redhatGroupId = identityService.createGroup("redhat", "unit", null);
    String jbossId = identityService.createGroup("jboss", "unit", redhatGroupId);
    String jbpmId = identityService.createGroup("jbpm", "unit", jbossId);

    identityService.createMembership("johndoe", redhatGroupId, "developer");
    identityService.createMembership("joesmoe", jbpmId, "leader");
    identityService.createMembership("jackblack", jbossId, "manager");

    List<Group> groups = identityService.findGroupsByUserAndGroupType("johndoe", "unit");

    assertEquals(1, groups.size());
    Group group = groups.get(0);
    assertEquals("redhat", group.getName());

    identityService.deleteGroup(jbpmId);
    identityService.deleteGroup(jbossId);
    identityService.deleteGroup(redhatGroupId);

    identityService.deleteUser("johndoe");
    identityService.deleteUser("joesmoe");
    identityService.deleteUser("jackblack");
  }

  public void testSingleUser2() throws Exception {
    identityService.createUser("johndoe", "John", "Doe");

    List<User> users = identityService.findUsers();
    assertNotNull(users);

    User johndoe = null;

    for (User user : users) {
        if ("johndoe".equals(user.getId())) {
            johndoe = user;
        }
    }

    assertNotNull(johndoe);

    assertEquals("johndoe", johndoe.getId());
    assertEquals("John", johndoe.getGivenName());
    assertEquals("Doe", johndoe.getFamilyName());
    assertEquals("John Doe", johndoe.toString());

    identityService.deleteUser("johndoe");

  }

  public void testDuplicatedUser() {
    identityService.createUser("johndoe", "John", "Doe");
    try {
      identityService.createUser("johndoe", "John", "Doe");
      fail("shouldn't allow duplicated user");
    } catch(JbpmException ex) {
      assertEquals("Cannot create user, error while validating", ex.getMessage());
    }
    identityService.deleteUser("johndoe");
  }

  public void testDuplicatedGroup() {
    identityService.createGroup("group");
    try {
    identityService.createGroup("group");
      fail("shouldn't allow duplicated group");
    } catch(JbpmException ex) {
      assertEquals("Cannot create group, error while validating", ex.getMessage());
    }
    identityService.deleteGroup("group");
  }
}

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
package org.jbpm.test.task;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbpm.api.task.Participation;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class TaskParticipationsTest extends JbpmTestCase {

  public void testTaskParticipants() {
    Task task = taskService.newTask();
    task.setName("do laundry");
    String taskId = taskService.saveTask(task);
    
    taskService.addTaskParticipatingUser(taskId, "johndoe", Participation.CANDIDATE);
    taskService.addTaskParticipatingUser(taskId, "joesmoe", Participation.CANDIDATE);
    taskService.addTaskParticipatingUser(taskId, "joesmoe", Participation.OWNER);
    taskService.addTaskParticipatingGroup(taskId, "losers", Participation.CANDIDATE);
    taskService.addTaskParticipatingGroup(taskId, "dummies", Participation.CANDIDATE);
    
    List<Participation> taskParticipations = taskService.getTaskParticipations(taskId);

    Set<String> candidateUserIds = extractParticipatingUserIds(taskParticipations, Participation.CANDIDATE);
    Set<String> ownerUserIds = extractParticipatingUserIds(taskParticipations, Participation.OWNER);
    Set<String> candidateGroupIds = extractParticipatingGroupIds(taskParticipations, Participation.CANDIDATE);

    Set<String> expectedIds  = new HashSet<String>();
    expectedIds.add("johndoe");
    expectedIds.add("joesmoe");

    assertEquals(expectedIds, candidateUserIds);

    expectedIds  = new HashSet<String>();
    expectedIds.add("joesmoe");

    assertEquals(expectedIds, ownerUserIds);
    
    expectedIds  = new HashSet<String>();
    expectedIds.add("losers");
    expectedIds.add("dummies");

    assertEquals(expectedIds, candidateGroupIds);
    
    taskService.removeTaskParticipatingUser(taskId, "joesmoe", Participation.OWNER);
    taskService.removeTaskParticipatingGroup(taskId, "losers", Participation.CANDIDATE);

    taskParticipations = taskService.getTaskParticipations(taskId);

    candidateUserIds = extractParticipatingUserIds(taskParticipations, Participation.CANDIDATE);
    ownerUserIds = extractParticipatingUserIds(taskParticipations, Participation.OWNER);
    candidateGroupIds = extractParticipatingGroupIds(taskParticipations, Participation.CANDIDATE);

    expectedIds  = new HashSet<String>();
    expectedIds.add("johndoe");
    expectedIds.add("joesmoe");

    assertEquals(expectedIds, candidateUserIds);

    expectedIds  = new HashSet<String>();

    assertEquals(expectedIds, ownerUserIds);
    
    expectedIds  = new HashSet<String>();
    expectedIds.add("dummies");

    assertEquals(expectedIds, candidateGroupIds);
    
    taskService.deleteTaskCascade(taskId);
  }

  public Set<String> extractParticipatingUserIds(List<Participation> taskParticipations, String participationType) {
    Set<String> userIds = new HashSet<String>();
    for (Participation participation : taskParticipations) {
      if (participationType.equals(participation.getType())) {
        if (participation.getUserId()!=null) {
          userIds.add(participation.getUserId());
        }
      }
    }
    return userIds;
  }

  public Set<String> extractParticipatingGroupIds(List<Participation> taskParticipations, String participationType) {
    Set<String> groupIds = new HashSet<String>();
    for (Participation participation : taskParticipations) {
      if (participationType.equals(participation.getType())) {
        if (participation.getGroupId()!=null) {
          groupIds.add(participation.getGroupId());
        }
      }
    }
    return groupIds;
  }
}

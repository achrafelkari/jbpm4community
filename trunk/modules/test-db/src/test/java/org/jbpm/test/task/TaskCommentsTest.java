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

import java.util.List;

import org.jbpm.api.history.HistoryComment;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class TaskCommentsTest extends JbpmTestCase {
  
  public void testComments() {
    Task task = taskService.newTask();
    task.setName("clean da house");
    String taskId = taskService.saveTask(task);

    // what a party yesterday
    //   - what! you had a party while i was out ?!
    //      - euh yes.  it was a great party :-)
    // i'll clean up the mess
    HistoryComment comment = taskService.addTaskComment(taskId, "what a party yesterday");
    String whatAPartyId = comment.getId();
    comment = taskService.addReplyComment(whatAPartyId, "what! you had a party while i was out ?!");
    String youHadAPartyId = comment.getId();
    taskService.addReplyComment(youHadAPartyId, "euh yes.  it was a great party :-)");

    taskService.addTaskComment(taskId, "i'll clean up the mess");
    
    List<HistoryComment> taskComments = taskService.getTaskComments(taskId);
    assertEquals("what a party yesterday", taskComments.get(0).getMessage());
    assertEquals("i'll clean up the mess", taskComments.get(1).getMessage());
    
    taskComments = taskComments.get(0).getReplies();
    assertEquals("what! you had a party while i was out ?!", taskComments.get(0).getMessage());
    
    taskComments = taskComments.get(0).getReplies();
    assertEquals("euh yes.  it was a great party :-)", taskComments.get(0).getMessage());
    
    taskService.deleteComment(whatAPartyId);

    taskComments = taskService.getTaskComments(taskId);
    assertEquals("i'll clean up the mess", taskComments.get(0).getMessage());
    
    // the following should delete the remaining comment.  if not, this will show up as a fixme.
    taskService.deleteTaskCascade(taskId);
  }

  public void testGetTaskCommentsWithUnexistingTaskId() {
    List<HistoryComment> comments = taskService.getTaskComments("-1234");
    assertTrue(comments.isEmpty());
  }
  
}

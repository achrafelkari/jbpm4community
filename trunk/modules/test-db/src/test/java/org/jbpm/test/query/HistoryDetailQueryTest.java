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
package org.jbpm.test.query;

import java.util.List;

import org.jbpm.api.ProcessInstance;
import org.jbpm.api.history.HistoryComment;
import org.jbpm.api.history.HistoryDetail;
import org.jbpm.api.history.HistoryDetailQuery;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.assertion.QueryAssertions;


/**
 * @author Tom Baeyens
 */
public class HistoryDetailQueryTest extends JbpmTestCase {

  public void testProcessInstance() {
    deployJpdlXmlString(
      "<process name='HistoryDetails'>" +
      "  <start>" +
      "    <transition to='review' />" +
      "  </start>" +
      "  <task name='review' " +
      "        assignee='johndoe'>" +
      "    <transition to='wait' />" +
      "  </task>" +
      "  <state name='wait'/>" +
      "</process>"
    );
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("HistoryDetails");
    String pid = processInstance.getId();

    List<HistoryDetail> processInstanceHistoryDetails = historyService
        .createHistoryDetailQuery()
        .processInstanceId(pid)
        .list();
    
    // at this moment, there are not process instance details
    // but this test already checks if the query works ok
    assertNotNull(processInstanceHistoryDetails);
    assertEquals(0, processInstanceHistoryDetails.size());
  }

  public void testTaskCommentDetail() {
    deployJpdlXmlString(
      "<process name='TaskCommentDetail'>" +
      "  <start>" +
      "    <transition to='t' />" +
      "  </start>" +
      "  <task name='t' assignee='johndoe'/>" +
      "</process>"
    );

    executionService.startProcessInstanceByKey("TaskCommentDetail");
    
    String taskId = taskService.createTaskQuery().uniqueResult().getId();
    
    taskService.addTaskComment(taskId, "hello");

    List<HistoryDetail> historyDetails = historyService.createHistoryDetailQuery().list();
    assertEquals(historyDetails.toString(), 1, historyDetails.size());
    
    historyDetails = historyService
        .createHistoryDetailQuery()
        .comments()
        .list();
    
    assertEquals(historyDetails.toString(), 1, historyDetails.size());
    
    HistoryComment historyComment = (HistoryComment) historyDetails.get(0);
    assertEquals("hello", historyComment.getMessage());
  }

  public void testTaskCommentRepliesDetail() {
    deployJpdlXmlString(
      "<process name='TaskCommentRepliesDetail'>" +
      "  <start>" +
      "    <transition to='t' />" +
      "  </start>" +
      "  <task name='t' assignee='johndoe'/>" +
      "</process>"
    );

    executionService.startProcessInstanceByKey("TaskCommentRepliesDetail");
    
    String taskId = taskService.createTaskQuery().uniqueResult().getId();
    
    processEngine.setAuthenticatedUserId("johndoe");
    String helloCommentId = taskService.addTaskComment(taskId, "hi, how are you guys?").getId();

    processEngine.setAuthenticatedUserId("joesmoe");
    taskService.addReplyComment(helloCommentId, "i'm doing fine, thanks");

    processEngine.setAuthenticatedUserId("jackblack");
    taskService.addReplyComment(helloCommentId, "i got a hangover");

    List<HistoryDetail> historyDetails = historyService.createHistoryDetailQuery().list();
    assertEquals(historyDetails.toString(), 3, historyDetails.size());
    
    historyDetails = historyService
        .createHistoryDetailQuery()
        .comments()
        .list();
  }
  
  public void testOrderBy() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    
    // add some comments
    taskService.addTaskComment(task.getId(), "aaaaaaa");
    taskService.addTaskComment(task.getId(), "xxxxxxx");
    String commentId = taskService.addTaskComment(task.getId(), "ggggggg").getId();
    taskService.addReplyComment(commentId, "jjjjjj");
    
    List<HistoryDetail> listAsc = 
      historyService.createHistoryDetailQuery().orderAsc(HistoryDetailQuery.PROPERTY_TIME).list();
    
    List<HistoryDetail> listDesc = 
      historyService.createHistoryDetailQuery().orderDesc(HistoryDetailQuery.PROPERTY_TIME).list();

    QueryAssertions.assertOrderIsNatural(HistoryDetail.class, HistoryDetailQuery.PROPERTY_TIME, listAsc, listDesc, 4);
    
    taskService.deleteTaskCascade(task.getId());
  }
  
}

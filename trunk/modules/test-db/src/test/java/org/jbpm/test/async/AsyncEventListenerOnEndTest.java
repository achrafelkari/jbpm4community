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
package org.jbpm.test.async;

import org.jbpm.api.Execution;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.job.Job;
import org.jbpm.api.listener.EventListener;
import org.jbpm.api.listener.EventListenerExecution;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class AsyncEventListenerOnEndTest extends JbpmTestCase {
  
  public static class AsyncEventListener implements EventListener {
    private static final long serialVersionUID = 1L;
    public void notify(EventListenerExecution execution) throws Exception {
    }
  }

  public void testAsyncEventListenerAfterWaitState() {
    deployJpdlXmlString(
      "<process name='AsyncEventListenerOnEnd'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition name='start-to-end' to='end'>" +
      "      <event-listener continue='async' class='"+AsyncEventListener.class.getName()+"' />" +
      "    </transition>" +
      "  </state>" +
      "  <end name='end' />" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("AsyncEventListenerOnEnd");
    
    processInstance = executionService.signalExecutionById(processInstance.getId());
    
    assertEquals(Execution.STATE_ASYNC, processInstance.getState());
    
    Job job = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .uniqueResult();
    
    assertNotNull(job);
    
    managementService.executeJob(job.getId());
    
    assertEquals(Execution.STATE_ENDED, 
      historyService.createHistoryProcessInstanceQuery()
        .processInstanceId(processInstance.getId())
        .uniqueResult()
        .getState() );
  }

  public void testAsyncEventListenerInStartTransaction() {
    deployJpdlXmlString(
      "<process name='AsyncEventListenerOnEnd'>" +
      "  <start name='start'>" +
      "    <transition name='start-to-end' to='end'>" +
      "      <event-listener continue='async' class='"+AsyncEventListener.class.getName()+"' />" +
      "    </transition>" +
      "  </start>" +
      "  <end name='end' />" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("AsyncEventListenerOnEnd");
    assertEquals(Execution.STATE_ASYNC, processInstance.getState());
    
    Job job = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .uniqueResult();
    
    assertNotNull(job);
    
    managementService.executeJob(job.getId());
    
    assertEquals(Execution.STATE_ENDED, 
      historyService.createHistoryProcessInstanceQuery()
        .processInstanceId(processInstance.getId())
        .uniqueResult()
        .getState() );
  }
}

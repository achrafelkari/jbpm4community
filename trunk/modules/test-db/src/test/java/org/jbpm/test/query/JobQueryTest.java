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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jbpm.api.JobQuery;
import org.jbpm.api.job.Job;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.assertion.QueryAssertions;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class JobQueryTest extends JbpmTestCase {
  
  public void testQueryOutstandingTimers() {
   startTestProcessInstances(4);
    
    List<Job> timers = managementService.createJobQuery()
      .timers()
      .list();
    
    assertEquals(4, timers.size());
  }

  public void testQueryBacklogMessages() {
    deployJpdlXmlString(
            "<process name='MessagesQueryTest' >" +
            "  <start>" +
            "    <transition to='t' />" +
            "  </start>" +
            "  <state name='t' continue='async' />" +
            "</process>"
          );

    executionService.startProcessInstanceByKey("MessagesQueryTest");
    executionService.startProcessInstanceByKey("MessagesQueryTest");
    executionService.startProcessInstanceByKey("MessagesQueryTest");
    executionService.startProcessInstanceByKey("MessagesQueryTest");
    
    List<Job> jobs = managementService.createJobQuery()
      .messages()
      .list();
    
    assertEquals(4, jobs.size());
  }
  
  public static class Dog implements Serializable {
    private static final long serialVersionUID = 1L;
    public void bark() {
      throw new RuntimeException("wooof");
    }
  }
  
  public void testErrorMessages() {
    deployJpdlXmlString(
      "<process name='ErrorMsgQueryTest' >" +
      "  <start>" +
      "    <transition to='t' />" +
      "  </start>" +
      "  <java name='t' " +
      "        continue='async' " +
      "        class='"+Dog.class.getName()+"'" +
      "        method='bark'>" +
      "  </java>" +
      "</process>"
    );

    executionService.startProcessInstanceByKey("ErrorMsgQueryTest");
    executionService.startProcessInstanceByKey("ErrorMsgQueryTest");
    executionService.startProcessInstanceByKey("ErrorMsgQueryTest");
    
    
    List<Job> messages = managementService.createJobQuery()
      .messages()
      .list();

    assertEquals(3, messages.size());

    try {
      managementService.executeJob(messages.get(0).getId());
      fail("expected exception");
    } catch (Exception e) {
      // OK
    }

    List<Job> errorJobs = managementService.createJobQuery()
      .exception(true)
      .list();
  
    assertEquals(1, errorJobs.size());
    
    assertTextPresent("wooof", errorJobs.get(0).getException());

    messages = managementService.createJobQuery()
      .messages()
      .exception(false)
      .list();

    assertEquals(2, messages.size()); 
  }
  
  public void testOrderByDueDate() {
    startTestProcessInstances(4);
    List<Job> jobsAsc = managementService.createJobQuery().orderAsc(JobQuery.PROPERTY_DUEDATE).list();
    List<Job> jobsDesc = managementService.createJobQuery().orderDesc(JobQuery.PROPERTY_DUEDATE).list();
    QueryAssertions.assertOrderIsNatural(Job.class, JobQuery.PROPERTY_DUEDATE, jobsAsc, jobsDesc, 4);
  }
  
  public void testCount() {
    List<String> procInstIds = startTestProcessInstances(6);
    
    assertEquals(6, managementService.createJobQuery().count());
    assertEquals(6, managementService.createJobQuery().timers().count());
    
    for (String id : procInstIds) {
      assertEquals(1, managementService.createJobQuery().processInstanceId(id).count()); 
      assertEquals(1, managementService.createJobQuery().processInstanceId(id).timers().count());
    }

  }
  
  private List<String> startTestProcessInstances(int nrOfInstances) {
    deployJpdlXmlString(
            "<process name='TimerQueryTest' >" +
            "  <start>" +
            "    <transition to='t' />" +
            "  </start>" +
            "  <state name='t'>" +
            "    <transition name='timeout' to='t'>" +
            "      <timer duedate='20 hours' />" +
            "    </transition>" +
            "  </state>" +
            "</process>"
          );
    
    List<String> ids = new ArrayList<String>();
    for (int i = 0; i < nrOfInstances; i++) {
      ids.add(executionService.startProcessInstanceByKey("TimerQueryTest").getId());      
    }
    return ids;
  }
  
}

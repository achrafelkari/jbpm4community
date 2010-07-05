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
package org.jbpm.test.custom.cal.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.jbpm.api.ProcessInstance;
import org.jbpm.api.job.Job;
import org.jbpm.pvm.internal.cal.BusinessCalendar;
import org.jbpm.test.JbpmCustomCfgTestCase;


/**
 * @author Tom Baeyens
 */
public class CustomBusinessCalendarImplTest extends JbpmCustomCfgTestCase {

  public void testCustomBusinessCalendar() {
    BusinessCalendar customBusinessCalendar = processEngine.get(BusinessCalendar.class);
    assertNotNull(customBusinessCalendar);
    assertEquals(CustomBusinessCalendar.class, customBusinessCalendar.getClass());
  }

  public void testCustomBusinessCalendarUsage() {
    deployJpdlXmlString(
      "<process name='CustomBusinessCalendar'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition to='b' />" +
      "    <transition name='timeout' to='escalate'>" +
      "      <timer duedate='my next birthday' />" +
      "    </transition>" +
      "  </state>" +
      "  <state name='b' />" +
      "  <end name='escalate' />" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("CustomBusinessCalendar");

    Job job = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .uniqueResult();
    
    Date duedate = job.getDuedate();
    
    GregorianCalendar gregorianCalendar = new GregorianCalendar();
    gregorianCalendar.setTime(duedate);
    assertEquals(Calendar.JULY, gregorianCalendar.get(Calendar.MONTH));
    assertEquals(21, gregorianCalendar.get(Calendar.DAY_OF_MONTH));
  }
}

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
package org.jbpm.test.custom.cal.cfg;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.jbpm.api.ProcessInstance;
import org.jbpm.api.job.Job;
import org.jbpm.pvm.internal.util.Clock;
import org.jbpm.test.JbpmCustomCfgTestCase;


/**
 * @author Tom Baeyens
 */
public class CustomBusinessCalendarCfgTest extends JbpmCustomCfgTestCase {
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    Clock.setExplicitTime(null);
  }

  public void testCustomBusinessCalendarCfg() {
    deployJpdlXmlString(
      "<process name='CustomBusinessCalendarCfg'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition to='b' />" +
      "    <transition name='timeout' to='escalate'>" +
      "      <timer duedate='8 business hours' />" +
      "    </transition>" +
      "  </state>" +
      "  <state name='b' />" +
      "  <end name='escalate' />" +
      "</process>"
    );
    
    GregorianCalendar gregorianCalendar = new GregorianCalendar();
    gregorianCalendar.set(2009, Calendar.JANUARY, 30, 11, 0, 0);
    Date clockDate = gregorianCalendar.getTime();
    Clock.setExplicitTime(clockDate);

    try {
      ProcessInstance processInstance = executionService.startProcessInstanceByKey("CustomBusinessCalendarCfg");

      Job job = managementService.createJobQuery()
        .processInstanceId(processInstance.getId())
        .uniqueResult();
      
      Date duedate = job.getDuedate();
      
      gregorianCalendar = new GregorianCalendar();
      gregorianCalendar.setTime(duedate);
      assertEquals(2009, gregorianCalendar.get(Calendar.YEAR));
      assertEquals(Calendar.NOVEMBER, gregorianCalendar.get(Calendar.MONTH));
      assertEquals(2, gregorianCalendar.get(Calendar.DAY_OF_MONTH));
      assertEquals(10, gregorianCalendar.get(Calendar.HOUR_OF_DAY));
      assertEquals(0, gregorianCalendar.get(Calendar.MINUTE));
      assertEquals(0, gregorianCalendar.get(Calendar.SECOND));

    } finally {
      Clock.setExplicitTime(null);
    }
    
  }
}

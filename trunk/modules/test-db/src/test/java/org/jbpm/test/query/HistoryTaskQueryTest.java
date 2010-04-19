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
/**
 * 
 */
package org.jbpm.test.query;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jbpm.api.history.HistoryTask;
import org.jbpm.api.history.HistoryTaskQuery;
import org.jbpm.pvm.internal.util.Clock;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.assertion.QueryAssertions;


/**
 * @author Joram Barrez
 */
public class HistoryTaskQueryTest extends JbpmTestCase {
  
  protected void tearDown() throws Exception {
    Clock.setExplicitTime(null);
    super.tearDown();
  }
  
  public void testOrderByName() {
    testOrderBy(HistoryTaskQuery.PROPERTY_ASSIGNEE, Arrays.asList("Alex", "Joram", "Tom"));
  }
  
  public void testOrderByStartTime() {
    testOrderByNaturalOrdening(HistoryTaskQuery.PROPERTY_CREATETIME, 3);
  }
  
  public void testOrderByEndTime() {
    testOrderByNaturalOrdening(HistoryTaskQuery.PROPERTY_ENDTIME, 3);
  }
  
  public void testOrderByDuration() {
    testOrderByNaturalOrdening(HistoryTaskQuery.PROPERTY_DURATION, 3);
  }
  
  public void testOrderByexecutionId() {
    testOrderByNaturalOrdening(HistoryTaskQuery.PROPERTY_EXECUTIONID, 3);
  }
  
  public void testOrderByOutcome() {
    testOrderByNaturalOrdening(HistoryTaskQuery.PROPERTY_OUTCOME, 3);
  }
  
  public void testOrderByState() {
    testOrderByNaturalOrdening(HistoryTaskQuery.PROPERTY_STATE, 3);
  }

  private void testOrderBy(String property, List expectedValues) {
    testOrderBy(property, expectedValues, null, false);
  }

  private void testOrderByNaturalOrdening(String property, int expectedNrOfResults) {
    testOrderBy(property, null, expectedNrOfResults, true);
  }
  
  public void testCount() {
    List<String> taskIds = createTestHistoryTasks();
    
    assertEquals(3, historyService.createHistoryTaskQuery().count());
    assertEquals(1, historyService.createHistoryTaskQuery().assignee("Tom").count());
    
    for (String taskid : taskIds) {
      assertEquals(1, historyService.createHistoryTaskQuery().taskId(taskid).count());
    }
    
    assertEquals(0, historyService.createHistoryTaskQuery().startedBefore(new Date(CLOCK_TEST_TIME - 10000L)).count());
    assertEquals(1, 
            historyService.createHistoryTaskQuery().startedAfter(new Date(CLOCK_TEST_TIME - 10000L))
            .taskId(taskIds.get(0)).count());
    assertEquals(0, 
            historyService.createHistoryTaskQuery().startedAfter(new Date(CLOCK_TEST_TIME - 10000L))
            .taskId("-1").count());
  }
  
  @SuppressWarnings("unchecked")
  private void testOrderBy(String property, List expectedValues, 
          Integer expectedNrOfResults, boolean naturalOrderCheck) {
    
    createTestHistoryTasks();
    
    List<HistoryTask> historyTasksAsc = 
      historyService.createHistoryTaskQuery().orderAsc(property).list();
    
    List<HistoryTask> historyTasksDesc = 
      historyService.createHistoryTaskQuery().orderDesc(property).list();

    if (naturalOrderCheck) {
      QueryAssertions.assertOrderIsNatural(HistoryTask.class, property, historyTasksAsc, historyTasksDesc, expectedNrOfResults);      
    } else {
      QueryAssertions.assertOrderOnProperty(HistoryTask.class, property, historyTasksAsc, historyTasksDesc, expectedValues);
    }
    
  }
  
  private static final Long CLOCK_TEST_TIME = 30000L;
  
  private List<String> createTestHistoryTasks() {
    String processXml1 = 
      "<process name='theProcess1'>" +
      "  <start>" +
      "    <transition to='theTask' />" +
      "  </start>" +
      "  <task name='theTask' assignee='Alex'>" +
      "    <transition to='theEnd' />" +
      "  </task>" +
      "  <end name='theEnd' />" +
      "</process>";
    
    Clock.setExplicitTime(new Date(CLOCK_TEST_TIME));
    
    String processXml3 = processXml1.replace("1", "2").replace("Alex", "Tom");
    String processXml2 = processXml1.replace("1", "3").replace("Alex", "Joram");
   
    deployJpdlXmlString(processXml1);
    deployJpdlXmlString(processXml2);
    deployJpdlXmlString(processXml3);
    
    executionService.startProcessInstanceByKey("theProcess1");
    executionService.startProcessInstanceByKey("theProcess2");
    executionService.startProcessInstanceByKey("theProcess3");
    
    List<String> taskIds = new ArrayList<String>();
    taskIds.add(taskService.findPersonalTasks("Alex").get(0).getId());
    taskIds.add(taskService.findPersonalTasks("Joram").get(0).getId());
    taskIds.add(taskService.findPersonalTasks("Tom").get(0).getId());
    
    for (String taskId : taskIds) {
      taskService.completeTask(taskId);
    }
    
    return taskIds;
  }
  
  private Date stringToDate(String dateString) {
    DateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
    try {
      return dateFormat.parse(dateString);
    } catch (ParseException e) {
      throw new RuntimeException("Couldn't convert " + dateString);
    }
  }
  
  
}

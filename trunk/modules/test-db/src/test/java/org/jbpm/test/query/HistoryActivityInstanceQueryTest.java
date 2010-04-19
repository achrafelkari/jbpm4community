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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.history.HistoryActivityInstance;
import org.jbpm.api.history.HistoryActivityInstanceQuery;
import org.jbpm.pvm.internal.util.Clock;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.assertion.QueryAssertions;


/**
 * @author Tom Baeyens
 */
public class HistoryActivityInstanceQueryTest extends JbpmTestCase {
  
  protected void tearDown() throws Exception {
    Clock.setExplicitTime(null);
    super.tearDown();
  }

  public void testSimpleQuery() {
    deployStartAndSignalTestProcesses();
    
    List<HistoryActivityInstance> histActInsts = historyService.createHistoryActivityInstanceQuery()
      .activityName("a")
      .list();
    
    assertEquals(4, histActInsts.size());

    histActInsts = historyService.createHistoryActivityInstanceQuery()
      .activityName("b")
      .list();
    
    assertEquals(3, histActInsts.size());

    histActInsts = historyService.createHistoryActivityInstanceQuery()
      .activityName("c")
      .list();
    
    assertEquals(2, histActInsts.size());
  }
  
  // Test for JBPM-2649
  public void testQueryByProcessDefinitionId() {
    String procDefId = deployTestProcess();
    generateHistoryForTestProcess();

    ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery()
                                                 .deploymentId(procDefId).uniqueResult();

    List<HistoryActivityInstance> hacs = historyService.createHistoryActivityInstanceQuery()
                                                       .processDefinitionId(procDef.getId()).list();
    assertEquals(9, hacs.size()); // 4 x a (ended, abc, ab, a), 3 x b (ended, abc, ab, a), etc

    // Verify counts of historical activities by using its name
    Map<String, Integer> activityCounts = new HashMap<String, Integer>();
    for (HistoryActivityInstance hac : hacs) {
      String name = hac.getActivityName();
      Integer current = activityCounts.get(name);
      activityCounts.put(name, (current == null ? 0 : current) + 1);
    }

    assertEquals(new Integer(4), activityCounts.get("a"));
    assertEquals(new Integer(3), activityCounts.get("b"));
    assertEquals(new Integer(2), activityCounts.get("c"));
  }
  
  // Currently only verifies the size of the result set
  public void testQueryByExecutionId() {
    List<String> ids = deployStartAndSignalTestProcesses();
    
    assertEquals(3, historyService.createHistoryActivityInstanceQuery().executionId(ids.get(0)).list().size());
    assertEquals(3, historyService.createHistoryActivityInstanceQuery().executionId(ids.get(1)).list().size());
    assertEquals(2, historyService.createHistoryActivityInstanceQuery().executionId(ids.get(2)).list().size());
    assertEquals(1, historyService.createHistoryActivityInstanceQuery().executionId(ids.get(3)).list().size());
  }
  
  // Currently only verifies the size of the result set
  public void testQueryByStartedAfter() {
    deployTestProcess();
    
    // start 8 processes (18 activity instances) on simulated time 3000000
    Clock.setExplicitTime(new Date(3000000));
    generateHistoryForTestProcess();
    generateHistoryForTestProcess();

    // start another 4 processes (9 activity instances) on simulated time 3005000
    Clock.setExplicitTime(new Date(3005000));
    generateHistoryForTestProcess();
    
    Date timeStamp = new Date(3003000);
    assertEquals(9, historyService.createHistoryActivityInstanceQuery().startedAfter(timeStamp).list().size());
  }
  
  // Currently only verifies the size of the result set
  public void testQueryByStartedBefore() {
    deployTestProcess();
    
    // start 4 processes (9 activity instances) on simulated time 3000000
    Clock.setExplicitTime(new Date(3000000));
    generateHistoryForTestProcess();

    // start another 8 processes (18 activity instances) on simulated time 3000200
    Clock.setExplicitTime(new Date(3002000));
    generateHistoryForTestProcess();
    generateHistoryForTestProcess();
    
    Date timeStamp = new Date(3001000);
    assertEquals(9, historyService.createHistoryActivityInstanceQuery().startedBefore(timeStamp).list().size());
  }
  
  //Currently only verifies the size of the result set
  public void testQueryByActivityName() {
    deployStartAndSignalTestProcesses();
    assertEquals(4, historyService.createHistoryActivityInstanceQuery().activityName("a").list().size());
    assertEquals(3, historyService.createHistoryActivityInstanceQuery().activityName("b").list().size());
    assertEquals(2, historyService.createHistoryActivityInstanceQuery().activityName("c").list().size());
  }
  
  //Currently only verifies the size of the result set
  public void testQueryByTookLessThen() {
    deployStartAndSignalTestProcesses();
    assertEquals(9, historyService.createHistoryActivityInstanceQuery().tookLessThen(60*60*1000).list().size());
  }
  
  public void testOrderByActivityName() {
    testOrderBy(HistoryActivityInstanceQuery.PROPERTY_ACTIVITYNAME, Arrays.asList("a", "a", "a", "a", "b", "b", "b", "c", "c"));
  }
  
  public void testOrderByStartTime() {
    testOrderByNaturalOrdening(HistoryActivityInstanceQuery.PROPERTY_STARTTIME, 9);
  }
  
  public void testOrderByEndTime() {
    testOrderByNaturalOrdening(HistoryActivityInstanceQuery.PROPERTY_ENDTIME, 9);
  }
  
  public void testOrderByDuration() {
    testOrderByNaturalOrdening(HistoryActivityInstanceQuery.PROPERTY_DURATION, 9);
  }
  
  public void testOrderByExecutionId() {
    testOrderByNaturalOrdening(HistoryActivityInstanceQuery.PROPERTY_EXECUTIONID, 9);
  }
  
  public void testCount() {
    List<String> procInstIds = deployStartAndSignalTestProcesses();
    
    assertEquals(9, historyService.createHistoryActivityInstanceQuery().count());
    assertEquals(4, historyService.createHistoryActivityInstanceQuery().activityName("a").count());
    assertEquals(3, historyService.createHistoryActivityInstanceQuery().activityName("b").count());
    assertEquals(2, historyService.createHistoryActivityInstanceQuery().activityName("c").count());
    assertEquals(0, historyService.createHistoryActivityInstanceQuery().activityName("d").count());
    
    assertEquals(3, historyService.createHistoryActivityInstanceQuery().executionId(procInstIds.get(0)).count());
    assertEquals(3, historyService.createHistoryActivityInstanceQuery().executionId(procInstIds.get(1)).count());
    assertEquals(2, historyService.createHistoryActivityInstanceQuery().executionId(procInstIds.get(2)).count());
    assertEquals(1, historyService.createHistoryActivityInstanceQuery().executionId(procInstIds.get(3)).count());
    
    assertEquals(1, historyService.createHistoryActivityInstanceQuery()
                                  .executionId(procInstIds.get(0)).activityName("a").count());
    assertEquals(1, historyService.createHistoryActivityInstanceQuery()
            .executionId(procInstIds.get(0)).activityName("b").count());
    assertEquals(1, historyService.createHistoryActivityInstanceQuery()
            .executionId(procInstIds.get(0)).activityName("c").count());
  }
  
  /**
   * Generates some history data.
   * 
   * Calling this method will produce (in this order):
   *   - 1 ended process instance (signalled state a,b,c)
   *   - 1 process instance in state c (signalled a,b)
   *   - 1 process instance in state b (signalled a)
   *   - 1 process instance in state a (just started)
   *   
   *   (in this exact order!)
   */
  private List<String> deployStartAndSignalTestProcesses() {
    deployTestProcess();
    return generateHistoryForTestProcess();
  }
  
  /**
   * Returns deployment db id
   */
  private String deployTestProcess() {
    return deployJpdlXmlString(
            "<process name='abc'>" +
            "  <start>" +
            "    <transition to='a' />" +
            "  </start>" +
            "  <state name='a'>" +
            "    <transition to ='b' />" +
            "  </state>" +
            "  <state name='b'>" +
            "    <transition to ='c' />" +
            "  </state>" +
            "  <state name='c'>" +
            "    <transition to ='end' />" +
            "  </state>" +
            "  <end name='end' />" +
            "</process>"
          );
  }

  private List<String> generateHistoryForTestProcess() {
    List<String> ids = new ArrayList<String>();
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("abc");
    executionService.signalExecutionById(processInstance.getId());
    executionService.signalExecutionById(processInstance.getId());
    executionService.signalExecutionById(processInstance.getId());
    ids.add(processInstance.getId());

    processInstance = executionService.startProcessInstanceByKey("abc");
    executionService.signalExecutionById(processInstance.getId());
    executionService.signalExecutionById(processInstance.getId());
    ids.add(processInstance.getId());

    processInstance = executionService.startProcessInstanceByKey("abc");
    executionService.signalExecutionById(processInstance.getId());
    ids.add(processInstance.getId());

    processInstance = executionService.startProcessInstanceByKey("abc");
    ids.add(processInstance.getId());
    
    return ids;
  }
  
  private void testOrderBy(String property, List expectedValues) {
    testOrderBy(property, expectedValues, null, false);
  }

  private void testOrderByNaturalOrdening(String property, int expectedNrOfResults) {
    testOrderBy(property, null, expectedNrOfResults, true);
  }
  
  @SuppressWarnings("unchecked")
  private void testOrderBy(String property, List expectedValues, 
          Integer expectedNrOfResults, boolean naturalOrderCheck) {
    
    deployStartAndSignalTestProcesses();

    List<HistoryActivityInstance> listAsc = 
      historyService.createHistoryActivityInstanceQuery().orderAsc(property).list();
    
    List<HistoryActivityInstance> listDesc = 
      historyService.createHistoryActivityInstanceQuery().orderDesc(property).list();

    if (naturalOrderCheck) {
      QueryAssertions.assertOrderIsNatural(HistoryActivityInstance.class, property, listAsc, listDesc, expectedNrOfResults);      
    } else {
      QueryAssertions.assertOrderOnProperty(HistoryActivityInstance.class, property, listAsc, listDesc, expectedValues);
    }
    
  }
  
  /**
   * Deploys example process to test history by process instance and execution id.
   * Task has a timer which will append name of the task to the execution id causing
   * that query by execution id (given as processInstanceId) will not find that node.
   */
  private String deployTestProcessWithTask() {
    return deployJpdlXmlString(
            "<process name='abcd'>" +
            "  <start>" +
            "    <transition to='a' />" +
            "  </start>" +
            "  <state name='a'>" +
            "    <transition to ='b' />" +
            "  </state>" +
            "  <task assignee='alex' name='b'>" +
            "    <on event='start'>" +
            "       <timer duedate='2 minutes' />" +
            "    </on>" +
            "    <transition to ='c' />" +
            "  </task>" +
            "  <state name='c'>" +
            "    <transition to ='end' />" +
            "  </state>" +
            "  <end name='end' />" +
            "</process>"
          );
  }
  
  private List<String> generateHistoryForTestProcessWithTask() {
    List<String> ids = new ArrayList<String>();
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("abcd");
    executionService.signalExecutionById(processInstance.getId());
    
    taskService.completeTask(taskService.findPersonalTasks("alex").get(0).getId());
    executionService.signalExecutionById(processInstance.getId());
    ids.add(processInstance.getId());

    return ids;
  }
  
  public void testQueryByProcessInstanceId() {
    deployTestProcessWithTask();
    List<String> ids = generateHistoryForTestProcessWithTask();
    List<HistoryActivityInstance> history = historyService.createHistoryActivityInstanceQuery().processInstanceId(ids.get(0)).list();
    
    //first check size of history entries
    assertEquals(3, history.size());
    
    // next check activity names
    assertEquals("a", history.get(0).getActivityName());
    assertEquals("b", history.get(1).getActivityName());
    assertEquals("c", history.get(2).getActivityName());
    
    // last check execution ids to illustrate that for task with timer it is not same as process instance id
    assertEquals(ids.get(0), history.get(0).getExecutionId());
    assertEquals(ids.get(0) + ".b", history.get(1).getExecutionId());
    assertEquals(ids.get(0), history.get(2).getExecutionId());
  }
  
  public void testQueryByExecutionIdMissingTask() {
    deployTestProcessWithTask();
    List<String> ids = generateHistoryForTestProcessWithTask();
    List<HistoryActivityInstance> history = historyService.createHistoryActivityInstanceQuery().executionId(ids.get(0)).list();
    
    // check size of history entries
    assertEquals(2, history.size());
    // next check activity names - missing task node
    assertEquals("a", history.get(0).getActivityName());
    assertEquals("c", history.get(1).getActivityName());
  }
  
}

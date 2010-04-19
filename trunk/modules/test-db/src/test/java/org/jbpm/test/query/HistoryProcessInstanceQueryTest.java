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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.history.HistoryProcessInstance;
import org.jbpm.api.history.HistoryProcessInstanceQuery;
import org.jbpm.pvm.internal.util.Clock;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.assertion.QueryAssertions;


/**
 * @author Joram Barrez
 * @author Alejandro Guizar
 */
public class HistoryProcessInstanceQueryTest extends JbpmTestCase {
  
  public void testQueryByProcessInstanceId() {
    List<String> procInstIds = createTestHistoryProcessInstances(4);
    for (String processInstanceId : procInstIds) {
      assertNotNull(historyService.createHistoryProcessInstanceQuery().processInstanceId(processInstanceId).uniqueResult());
    }
  }
  
  public void testQueryByProcessDefinitionId() {
    createTestHistoryProcessInstances(5);
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().uniqueResult();
    assertEquals(5, historyService.createHistoryProcessInstanceQuery()
            .processDefinitionId(processDefinition.getId()).list().size());  
  }
  
  public void testQueryByBusinessKey() {
    final int nrOfInstances = 3;
    createTestHistoryProcessInstances(nrOfInstances);
    for (int i = 0; i < nrOfInstances; i++) {
      assertNotNull(historyService.createHistoryProcessInstanceQuery().processInstanceKey("theProcess-" + i).uniqueResult());
    }
  }
  
  public void testCount() {
    List<String> procInstIds = createTestHistoryProcessInstances(7);
    
    assertEquals(0, historyService.createHistoryProcessInstanceQuery().processDefinitionId("-1").count());
    
    assertEquals(7, historyService.createHistoryProcessInstanceQuery().count());
    
    for (String id : procInstIds) {
      assertEquals(1, historyService.createHistoryProcessInstanceQuery().processInstanceId(id).count());      
    }
    
  }
  
  public void testOrderByStartTime() {
    testOrderByNaturalOrdening(HistoryProcessInstanceQuery.PROPERTY_STARTTIME, 4);
  }
  
  public void testOrderByEndTime() {
    testOrderByNaturalOrdening(HistoryProcessInstanceQuery.PROPERTY_ENDTIME, 4);
  }
  
  public void testOrderByDuration() {
    testOrderByNaturalOrdening(HistoryProcessInstanceQuery.PROPERTY_DURATION, 4);
  }
  
  public void testQueryEnded() {
    List<String> procInstIds = createTestHistoryProcessInstances(4);
    String endedProcInstId = procInstIds.get(0);
    executionService.endProcessInstance(endedProcInstId, "ended");

    List<HistoryProcessInstance> procInsts = historyService.createHistoryProcessInstanceQuery()
        .ended()
        .list();
    assertEquals(1, procInsts.size());

    HistoryProcessInstance endedProcInst = procInsts.get(0);
    assertEquals(endedProcInstId, endedProcInst.getProcessInstanceId());
  }
  
  public void testQueryEndedBefore() {
    List<String> procInstIds = createTestHistoryProcessInstances(4);
    
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DATE, -1);
    Date yesterday = calendar.getTime();
    Clock.setExplicitTime(yesterday);
    
    String endedYesterdayProcInstId = procInstIds.get(0);
    executionService.endProcessInstance(endedYesterdayProcInstId, "ended");
    
    calendar.add(Calendar.DATE, -1);
    Date twoDaysAgo = calendar.getTime();
    Clock.setExplicitTime(twoDaysAgo);
    
    String endedTwoDaysAgoProcInstId = procInstIds.get(1);
    executionService.endProcessInstance(endedTwoDaysAgoProcInstId, "ended");
    
    List<HistoryProcessInstance> procInsts = historyService.createHistoryProcessInstanceQuery()
        .endedBefore(yesterday)
        .list();
    assertEquals(1, procInsts.size());
    
    HistoryProcessInstance endedTwoDaysAgoProcInst = procInsts.get(0);
    assertEquals(endedTwoDaysAgoProcInstId, endedTwoDaysAgoProcInst.getProcessInstanceId());
    
    Clock.setExplicitTime(null);
  }

  public void testQueryEndedAfter() {
    List<String> procInstIds = createTestHistoryProcessInstances(4);
    
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DATE, -1);
    Date yesterday = calendar.getTime();
    Clock.setExplicitTime(yesterday);
    
    String endedYesterdayProcInstId = procInstIds.get(0);
    executionService.endProcessInstance(endedYesterdayProcInstId, "ended");
    
    calendar.add(Calendar.DATE, -1);
    Date twoDaysAgo = calendar.getTime();
    Clock.setExplicitTime(twoDaysAgo);
    
    String endedTwoDaysAgoProcInstId = procInstIds.get(1);
    executionService.endProcessInstance(endedTwoDaysAgoProcInstId, "ended");
    
    List<HistoryProcessInstance> procInsts = historyService.createHistoryProcessInstanceQuery()
        .endedAfter(yesterday)
        .list();
    assertEquals(1, procInsts.size());
    
    HistoryProcessInstance endedYesterdayProcInst = procInsts.get(0);
    assertEquals(endedYesterdayProcInstId, endedYesterdayProcInst.getProcessInstanceId());
    
    Clock.setExplicitTime(null);
  }

  public void testQueryEndedAfterAndBefore() {
    List<String> procInstIds = createTestHistoryProcessInstances(4);
    
    String endedTodayProcInstId = procInstIds.get(0);
    executionService.endProcessInstance(endedTodayProcInstId, "ended");
    
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DATE, -1);
    Date yesterday = calendar.getTime();
    Clock.setExplicitTime(yesterday);
    
    String endedYesterdayProcInstId = procInstIds.get(1);
    executionService.endProcessInstance(endedYesterdayProcInstId, "ended");
    
    calendar.add(Calendar.DATE, -1);
    Date twoDaysAgo = calendar.getTime();
    Clock.setExplicitTime(twoDaysAgo);
    
    String endedTwoDaysAgoProcInstId = procInstIds.get(2);
    executionService.endProcessInstance(endedTwoDaysAgoProcInstId, "ended");
    
    calendar.add(Calendar.DATE, 3);
    Date tomorrow = calendar.getTime();
    
    List<HistoryProcessInstance> procInsts = historyService.createHistoryProcessInstanceQuery()
        .endedAfter(yesterday)
        .endedBefore(tomorrow)
        .list();
    assertEquals(2, procInsts.size());

    for (HistoryProcessInstance procInst : procInsts) {
      String procInstId = procInst.getProcessInstanceId();
      assert procInstId.equals(endedYesterdayProcInstId) ||
          procInstId.equals(endedTodayProcInstId) : procInstId;
    }

    Clock.setExplicitTime(null);
  }

  /* -------------------------------------------------------------
   * HELPER METHODS
   * ------------------------------------------------------------- */

  // Don't delete because it isn't used. Could be handy in the future!
  private void testOrderBy(String property, List<Object> expectedValues) {
    createTestHistoryProcessInstances(4);
    
    List<HistoryProcessInstance> histProcListAsc = 
        historyService.createHistoryProcessInstanceQuery()
                      .orderAsc(property)
                      .list();
    
    List<HistoryProcessInstance> histProcListDesc = 
      historyService.createHistoryProcessInstanceQuery()
                    .orderDesc(property)
                    .list();

    QueryAssertions.assertOrderOnProperty(HistoryProcessInstance.class, property, histProcListAsc, histProcListDesc, expectedValues);
  }

  private void testOrderByNaturalOrdening(String property, int expectedNrOfResults) {
    createTestHistoryProcessInstances(4);
    
    List<HistoryProcessInstance> histProcListAsc = 
        historyService.createHistoryProcessInstanceQuery()
                      .orderAsc(property)
                      .list();
    
    List<HistoryProcessInstance> histProcListDesc = 
      historyService.createHistoryProcessInstanceQuery()
                    .orderDesc(property)
                    .list();
    
    QueryAssertions.assertOrderIsNatural(HistoryProcessInstance.class, property, histProcListAsc, histProcListDesc, expectedNrOfResults);
  }
  
  private List<String> createTestHistoryProcessInstances(int nrOfInstances) {
    deployJpdlXmlString(
          "<process name='theProcess'>" +
          "  <start>" +
          "    <transition to='wait' />" +
          "  </start>" +
          "  <state name='wait'>" +
          "    <transition to='end' />" +
          "  </state>" +
          "  <end name='end' />" +
          "</process>");
    
    List<String> procInstIds = new ArrayList<String>();
    for (int i = 0; i < nrOfInstances; i++) {
      procInstIds.add(executionService.startProcessInstanceByKey("theProcess", "theProcess-" + i).getId());
    }
    return procInstIds;
  }
}

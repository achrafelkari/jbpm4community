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
package org.jbpm.test.history;

import java.util.List;

import org.jbpm.api.ProcessInstance;
import org.jbpm.api.history.HistoryProcessInstance;
import org.jbpm.api.history.HistoryProcessInstanceQuery;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class ProcessInstanceHistoryTest extends JbpmTestCase {

  public void testProcessInstanceHistory() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='end' />" +
      "  </start>" +
      "  <end name='end' />" +
      "</process>"
    );

    executionService.startProcessInstanceByKey("ICL");
    executionService.startProcessInstanceByKey("ICL");
    executionService.startProcessInstanceByKey("ICL");
    
    List<HistoryProcessInstance> historyProcessInstances = historyService
      .createHistoryProcessInstanceQuery()
      .processDefinitionId("ICL-1")
      .orderAsc(HistoryProcessInstanceQuery.PROPERTY_STARTTIME)
      .list();

    assertEquals(3, historyProcessInstances.size());
    
    for (HistoryProcessInstance historyProcessInstance: historyProcessInstances) {
      assertTrue(historyProcessInstance.getProcessInstanceId().startsWith("ICL"));
      assertEquals(HistoryProcessInstance.STATE_ENDED, historyProcessInstance.getState());
      assertNotNull(historyProcessInstance.getStartTime());
      assertNotNull(historyProcessInstance.getEndTime());
      assertTrue("hpi.duration should be bigger then 0: "+historyProcessInstance.getDuration(), historyProcessInstance.getDuration()>=0);
    }
    
    // also check that the ended process instances have been removed from the 
    // runtime database
    List<ProcessInstance> processInstances = executionService
      .createProcessInstanceQuery()
      .processDefinitionId("ICL-1")
      .list();

    assertEquals(0, processInstances.size());
  }
  
}

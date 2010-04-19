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
package org.jbpm.test.process;

import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessInstanceQuery;
import org.jbpm.api.history.HistoryProcessInstanceQuery;
import org.jbpm.test.JbpmTestCase;

/**
 * @author jorambarrez
 */
public class DeleteProcessInstanceTest extends JbpmTestCase {

  private static final String SIMPLE_PROCESS = 
    "<process name='simpleProcess'>" + 
    "  <start >" +
    "    <transition to='wait' />" +
    "  </start>" +
    "  <state name='wait' >" +
    "    <transition to='theEnd' />" +
    "  </state>" +
    "  <end name='theEnd' />" +
    "</process>";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    deployJpdlXmlString(SIMPLE_PROCESS);
  }

  public void testDeleteExistingProcess() {
    String id = executionService.startProcessInstanceByKey("simpleProcess").getId();
    ProcessInstanceQuery query = executionService.createProcessInstanceQuery().processInstanceId(id);
    assertNotNull(query.uniqueResult());
    
    executionService.deleteProcessInstance(id);
    assertNull(query.uniqueResult());
    
    HistoryProcessInstanceQuery historyQuery = historyService.createHistoryProcessInstanceQuery().processInstanceId(id);
    assertNotNull(historyQuery.uniqueResult());
  }
  
  public void testDeleteCascadeExistingProcess() {
    String id = executionService.startProcessInstanceByKey("simpleProcess").getId();
    ProcessInstanceQuery query = executionService.createProcessInstanceQuery().processInstanceId(id);
    assertNotNull(query.uniqueResult());
    
    executionService.deleteProcessInstanceCascade(id);
    assertNull(query.uniqueResult());
    
    HistoryProcessInstanceQuery historyQuery = historyService.createHistoryProcessInstanceQuery().processInstanceId(id);
    assertNull(historyQuery.uniqueResult());
  }
  
  public void testDeleteUnexistingProcessInstance() {
    try {
      executionService.deleteProcessInstance("-1");
      fail("Expected exception");
    } catch (JbpmException e) {
      // Exception should've been thrown
    }
  }
  
  public void testDeleteCascadeUnexistingProcessInstance() {
    try {
      executionService.deleteProcessInstanceCascade("-1");
      fail("Expected exception");
    } catch (JbpmException e) {
      // Exception should've been thrown
    }
  }

}

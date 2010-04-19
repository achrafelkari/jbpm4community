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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.jbpm.api.Execution;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class AvgDurationTest extends JbpmTestCase {

  public void testAvgDuration() throws Exception {
    String deploymentId = deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='one' />" +
      "  </start>" +
      "  <state name='one'>" +
      "    <transition to='two' />" +
      "  </state>" +
      "  <state name='two'>" +
      "    <transition to='three' />" +
      "  </state>" +
      "  <state name='three'>" +
      "    <transition to='end' />" +
      "  </state>" +
      "  <end name='end' />" +
      "</process>"
    );

    executeProcess();
    executeProcess();
    executeProcess();

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().uniqueResult();
    Map<String, Long> avgDurations = historyService.avgDurationPerActivity(processDefinition.getId());


    Long avgDurationOne = avgDurations.get("one");
    assertNotNull(avgDurationOne);
    assertTrue("expected avg duration bigger then 40, but was "+avgDurationOne, avgDurationOne>40);
    Long avgDurationTwo = avgDurations.get("two");
    assertNotNull(avgDurationTwo);
    assertTrue("expected avg duration bigger then 10, but was "+avgDurationTwo, avgDurationTwo>10);
    Long avgDurationThree = avgDurations.get("three");
    assertNotNull(avgDurationThree);
    assertTrue("expected avg duration bigger then 0, but was "+avgDurationThree, avgDurationThree>=0);

    assertEquals(3, avgDurations.size());
  }

  static DateFormat dateFormat = new SimpleDateFormat("ss,SSS");
  protected void executeProcess() throws InterruptedException {
    Execution execution = executionService.startProcessInstanceByKey("ICL");
    log.info("waiting 50 millis at "+dateFormat.format(new Date()));
    try {
      Thread.sleep(50);
    } catch (Exception e) {
      e.printStackTrace();
    }
    log.info("done waiting 50 millis at "+dateFormat.format(new Date()));
    executionService.signalExecutionById(execution.getId());
    log.info("waiting 20 millis at "+dateFormat.format(new Date()));
    try {
      Thread.sleep(20);
    } catch (Exception e) {
      e.printStackTrace();
    }
    log.info("done waiting 20 millis at "+dateFormat.format(new Date()));
    executionService.signalExecutionById(execution.getId());
    executionService.signalExecutionById(execution.getId());
  }

}

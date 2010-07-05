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
package org.jbpm.test.timer;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;
import org.jbpm.api.job.Job;
import org.jbpm.api.listener.EventListener;
import org.jbpm.api.listener.EventListenerExecution;
import org.jbpm.test.JbpmTestCase;

/**
 * @author Tom Baeyens
 * @author Ronald Van Kuijk
 * @author Joram Barrez
 * @author Huisheng Xu
 */
public class TimerTest extends JbpmTestCase {

  private static final String TEST_PROCESS_CUSTOM =
    "<process name='Insurance claim' key='ICL'>" +
    "  <start>" +
    "    <transition to='a' />" +
    "  </start>" +
    "  <custom continue='async' name='a' class='" + MyCustomWait.class.getName() + "'>" +
    "    <transition to='b' />" +
    "    <transition name='timeout' to='escalate'>" +
    "      <event-listener class='" + MyCustomWait.class.getName() + "'/>" +
    "      <timer duedate='2 minutes' />" +
    "    </transition>" +
    "  </custom>" +
    "  <state name='b' />" +
    "  <end name='escalate' />" +
    "</process>";

  public void testTimerTimeout() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition to='b' />" +
      "    <transition name='timeout' to='escalate'>" +
      "      <timer duedate='2 minutes' />" +
      "    </transition>" +
      "  </state>" +
      "  <state name='b' />" +
      "  <end name='escalate' />" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ICL", "82436");

    Job job = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .uniqueResult();

    managementService.executeJob(job.getId());
    assertProcessInstanceEnded(processInstance);
  }

  /**
   * Test case for https://jira.jboss.org/jira/browse/JBPM-2517
   *
   * In this issue, it is stated that the calculations for the timer
   * will overflow if larger than 4 weeks due to integer limitations.
   */
  public void testTimerInFuture() {
    deployJpdlXmlString(
      "<process name='theProcess'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition to='b' />" +
      "    <transition name='timeout' to='escalate'>" +
      "      <timer duedate='10 years' />" +
      "    </transition>" +
      "  </state>" +
      "  <state name='b' />" +
      "  <end name='escalate' />" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("theProcess");

    Calendar now = Calendar.getInstance();
    int currentYear = now.get(Calendar.YEAR);

    Job job = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .uniqueResult();

    Calendar jobDate = Calendar.getInstance();
    jobDate.setTime(job.getDuedate());

    assertEquals(currentYear + 10, jobDate.get(Calendar.YEAR));
  }

  public void testTimerELDate() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition to='b' />" +
      "    <transition name='timeout' to='escalate'>" +
      "      <timer duedate='#{proc_var}' />" +
      "    </transition>" +
      "  </state>" +
      "  <state name='b' />" +
      "  <end name='escalate' />" +
      "</process>"
    );

    Map<String, Object> proc_vars = new HashMap<String, Object>();
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, 6);
    proc_vars.put("proc_var", cal.getTime());
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ICL", proc_vars, "82436");

    Job job = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .uniqueResult();

    Calendar jobDate = Calendar.getInstance();
    jobDate.setTime(job.getDuedate());

    assertEquals(cal.get(Calendar.DAY_OF_MONTH), jobDate.get(Calendar.DAY_OF_MONTH));

    managementService.executeJob(job.getId());
    assertProcessInstanceEnded(processInstance);
  }

  public void testTimerELCalendar() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition to='b' />" +
      "    <transition name='timeout' to='escalate'>" +
      "      <timer duedate='#{proc_var}' />" +
      "    </transition>" +
      "  </state>" +
      "  <state name='b' />" +
      "  <end name='escalate' />" +
      "</process>"
    );

    Map<String, Object> proc_vars = new HashMap<String, Object>();
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, 6);
    proc_vars.put("proc_var", cal);
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ICL", proc_vars, "82436");

    Job job = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .uniqueResult();

    Calendar jobDate = Calendar.getInstance();
    jobDate.setTime(job.getDuedate());

    assertEquals(cal.get(Calendar.DAY_OF_MONTH), jobDate.get(Calendar.DAY_OF_MONTH));

    managementService.executeJob(job.getId());
    assertProcessInstanceEnded(processInstance);
  }

  public void testTimerELString() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition to='b' />" +
      "    <transition name='timeout' to='escalate'>" +
      "      <timer duedate='#{proc_var}' />" +
      "    </transition>" +
      "  </state>" +
      "  <state name='b' />" +
      "  <end name='escalate' />" +
      "</process>"
    );

    Map<String, Object> proc_vars = new HashMap<String, Object>();
    proc_vars.put("proc_var", "2 minutes");

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ICL", proc_vars, "82436");

    Job job = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .uniqueResult();

    Date jobDate = job.getDuedate();
    assertTrue("should less than 2 minutes", jobDate.getTime() - new Date().getTime() <= 2 * 60 * 1000);

    managementService.executeJob(job.getId());
    assertProcessInstanceEnded(processInstance);
  }

  public void testTimerELCalendarAdd() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition to='b' />" +
      "    <transition name='timeout' to='escalate'>" +
      "      <timer duedate='#{proc_var} + 5 days' />" +
      "    </transition>" +
      "  </state>" +
      "  <state name='b' />" +
      "  <end name='escalate' />" +
      "</process>"
    );

    Map<String, Object> proc_vars = new HashMap<String, Object>();
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, 6);
    proc_vars.put("proc_var", cal);
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ICL", proc_vars, "82436");

    Job job = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .uniqueResult();

    Calendar jobDate = Calendar.getInstance();
    jobDate.setTime(job.getDuedate());

    cal.add(Calendar.DAY_OF_MONTH, 5);
    assertEquals(cal.get(Calendar.DAY_OF_MONTH), jobDate.get(Calendar.DAY_OF_MONTH));

    managementService.executeJob(job.getId());
    assertProcessInstanceEnded(processInstance);
  }

  public void testTimerELCalendarSubtract() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition to='b' />" +
      "    <transition name='timeout' to='escalate'>" +
      "      <timer duedate='#{proc_var} - 5 days' />" +
      "    </transition>" +
      "  </state>" +
      "  <state name='b' />" +
      "  <end name='escalate' />" +
      "</process>"
    );

    Map<String, Object> proc_vars = new HashMap<String, Object>();
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, 6);

    proc_vars.put("proc_var", cal);
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ICL", proc_vars, "82436");

    Job job = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .uniqueResult();

    Calendar jobDate = Calendar.getInstance();
    jobDate.setTime(job.getDuedate());

    // 6 days from now minus 5 days is tomorrow so subtract 5 from the original added 6.
    cal.add(Calendar.DAY_OF_MONTH, -5);
    assertEquals(cal.get(Calendar.DAY_OF_MONTH) , jobDate.get(Calendar.DAY_OF_MONTH));

    managementService.executeJob(job.getId());
    assertProcessInstanceEnded(processInstance);
  }

  public void testTimerELCalendarAddBusiness() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition to='b' />" +
      "    <transition name='timeout' to='escalate'>" +
      "      <timer duedate='#{proc_var} + 5 business days' />" +
      "    </transition>" +
      "  </state>" +
      "  <state name='b' />" +
      "  <end name='escalate' />" +
      "</process>"
    );

    Map<String, Object> proc_vars = new HashMap<String, Object>();
    Calendar cal = Calendar.getInstance();
    cal.set(2010, 01, 12, 12, 00, 00); // 12 feb 2010 noon
    proc_vars.put("proc_var", cal);
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ICL", proc_vars, "82436");

    Job job = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .uniqueResult();

    Calendar jobDate = Calendar.getInstance();
    jobDate.setTime(job.getDuedate());

    // 12 feb is friday, 5 businessdays further is friday 19th
    assertEquals(19 , jobDate.get(Calendar.DAY_OF_MONTH));

    managementService.executeJob(job.getId());
    assertProcessInstanceEnded(processInstance);
  }

  public void testTimerELFail() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition to='b' />" +
      "    <transition name='timeout' to='escalate'>" +
      "      <timer duedate='#{proc_var}' />" +
      "    </transition>" +
      "  </state>" +
      "  <state name='b' />" +
      "  <end name='escalate' />" +
      "</process>"
    );

    Map<String, Object> proc_vars = new HashMap<String, Object>();
    proc_vars.put("proc_var", new Long(0));
    try {
      executionService.startProcessInstanceByKey("ICL", proc_vars, "82436");
      fail("Should not happen, exception expected");
    } catch (Exception e) {}
  }

  public void testTimerELSubtractBusinessFail() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition to='b' />" +
      "    <transition name='timeout' to='escalate'>" +
      "      <timer duedate='#{proc_var} - 6 business days' />" +
      "    </transition>" +
      "  </state>" +
      "  <state name='b' />" +
      "  <end name='escalate' />" +
      "</process>"
    );

    Map<String, Object> proc_vars = new HashMap<String, Object>();
    Calendar cal = Calendar.getInstance();
    cal.set(2010, 01, 12, 12, 00, 00); // 12 feb 2010 noon
    proc_vars.put("proc_var", cal);
    try {
      executionService.startProcessInstanceByKey("ICL", proc_vars, "82436");
      fail("Should not happen, exception expected");
    } catch (Exception e) {}
  }

  public void testTimerELSubtractPastFail() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition to='b' />" +
      "    <transition name='timeout' to='escalate'>" +
      "      <timer duedate='#{proc_var} - 3 days' />" +
      "    </transition>" +
      "  </state>" +
      "  <state name='b' />" +
      "  <end name='escalate' />" +
      "</process>"
    );

    Map<String, Object> proc_vars = new HashMap<String, Object>();
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, 2);
    proc_vars.put("proc_var", cal);
    try {
      executionService.startProcessInstanceByKey("ICL", proc_vars, "82436");
      fail("Should not happen, exception expected");
    } catch (Exception e) {}
  }

  public void testTimerTimeoutCustom() {
    deployJpdlXmlString(TEST_PROCESS_CUSTOM);

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ICL");
    assertEquals(Execution.STATE_INACTIVE_SCOPE, processInstance.getState());
    assertNotActivityActive(processInstance.getId(), "a");

    // execute the async state so it becomes active
    Job async = managementService.createJobQuery().messages().processInstanceId(processInstance.getId()).uniqueResult();
    managementService.executeJob(async.getId());
    assertActivityActive(processInstance.getId(), "a");

    try {
      executionService.signalExecutionById(processInstance.getId());
      fail("Should not happen, exception expected");
    } catch (JbpmException e) {
      // Should happen....Signalling an inactive-scope exexcution is not
      // allowed. If the timer is removed, this test should fail since the
      // state of the execution is async then and signalling that is allowed.
    }

    assertProcessInstanceActive(processInstance);
    executionService.signalExecutionById(processInstance.getExecution("a").getId());
    assertActivityActive(processInstance.getId(), "b");
  }

  public void testTimerSignalCustom() {
    deployJpdlXmlString(TEST_PROCESS_CUSTOM);
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ICL");

    Job async = managementService.createJobQuery().messages().processInstanceId(processInstance.getId()).uniqueResult();
    managementService.executeJob(async.getId());

    int beforeTimer = MyCustomWait.nrOfTimesCalled;
    Job timer = managementService.createJobQuery().timers().processInstanceId(processInstance.getId()).uniqueResult();
    managementService.executeJob(timer.getId());
    int afterTimer = MyCustomWait.nrOfTimesCalled;
    assertEquals(beforeTimer + 1, afterTimer);

    assertProcessInstanceEnded(processInstance);
  }

  public void testTimerRepeat() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <on event='timeout'>" +
      "     <timer duedate='20 minutes' repeat='10 seconds' />" +
      "     <event-listener class='" + MyCustomWait.class.getName() + "' />" +
      "   </on>" +
      "    <transition to='b' />" +
      "  </state>" +
      "  <state name='b' >" +
      "    <transition to='escalate' />" +
      "  </state>" +
      "  <end name='escalate' />" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ICL", "82436");

    Job job = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .uniqueResult();

    managementService.executeJob(job.getId());


    job = managementService.createJobQuery()
    .processInstanceId(processInstance.getId())
    .uniqueResult();

    managementService.executeJob(job.getId());

    processInstance = executionService.findProcessInstanceById(processInstance.getId());
    String processInstanceId = processInstance.findActiveExecutionIn("a").getId();

    processInstance = executionService.signalExecutionById(processInstanceId);

    processInstance = executionService.signalExecutionById(processInstance.getId());

    assertProcessInstanceEnded(processInstance);
  }

  public void testTimerELRepeat() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <on event='timeout'>" +
      "     <timer duedate='20 minutes' repeat='#{repeat}' />" +
      "     <event-listener class='" + MyCustomWait.class.getName() + "' />" +
      "   </on>" +
      "    <transition to='b' />" +
      "  </state>" +
      "  <state name='b' >" +
      "    <transition to='escalate' />" +
      "  </state>" +
      "  <end name='escalate' />" +
      "</process>"
    );

    Map<String, String> variables = Collections.singletonMap("repeat", "20 seconds");
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ICL", variables, "82436");

    Job job = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .uniqueResult();

    managementService.executeJob(job.getId());


    job = managementService.createJobQuery()
    .processInstanceId(processInstance.getId())
    .uniqueResult();

    managementService.executeJob(job.getId());

    processInstance = executionService.findProcessInstanceById(processInstance.getId());
    String processInstanceId = processInstance.findActiveExecutionIn("a").getId();

    processInstance = executionService.signalExecutionById(processInstanceId);

    processInstance = executionService.signalExecutionById(processInstance.getId());

    assertProcessInstanceEnded(processInstance);
  }

  public static class MyCustomWait implements ExternalActivityBehaviour, EventListener {

    private static final long serialVersionUID = 1L;

    static int nrOfTimesCalled;

    public void execute(ActivityExecution execution) throws Exception {
      execution.waitForSignal();
    }

    public void signal(ActivityExecution execution, String signalName, Map<String, ? > parameters) throws Exception {
      execution.take(signalName);
    }

    public void notify(EventListenerExecution execution) throws Exception {
      nrOfTimesCalled++;
    }
  }

}
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
package org.jbpm.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.jms.Session;

import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import org.jbpm.api.Configuration;
import org.jbpm.api.Execution;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.HistoryService;
import org.jbpm.api.IdentityService;
import org.jbpm.api.JbpmException;
import org.jbpm.api.ManagementService;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.RepositoryService;
import org.jbpm.api.TaskService;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.task.Task;
import org.jbpm.test.assertion.CollectionAssertions;

/** base class for persistent jBPM tests.
 *
 * This class exposes a lot of extra convenience methods for testing
 * process executions.
 *
 * The ProcessEngine services will be initialized and available as
 * member fields.
 *
 * This test assumes that each test will clean the DB itself and that
 * no data is in the DB tables when the test finishes.
 *
 * During tearDown, a check will be done if all the DB tables are
 * empty.  If not, that is logged with a F I X M E and the DB tables
 * are cleaned.
 *
 * @author Tom Baeyens
 * @author Heiko Braun
 */
public abstract class JbpmTestCase extends BaseJbpmTestCase {

  protected ProcessEngine processEngine;

  protected RepositoryService repositoryService;
  protected ExecutionService executionService;
  protected ManagementService managementService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected IdentityService identityService;

  /** registered deployments.  registered deployments will be deleted automatically
   * in the tearDown. This is a convenience function as each test is expected to clean up the DB. */
  protected List<String> registeredDeployments;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    processEngine = buildProcessEngine();

    repositoryService = processEngine.getRepositoryService();
    executionService = processEngine.getExecutionService();
    historyService = processEngine.getHistoryService();
    managementService = processEngine.getManagementService();
    taskService = processEngine.getTaskService();
    identityService = processEngine.getIdentityService();
  }

  protected ProcessEngine buildProcessEngine() {
    return Configuration.getProcessEngine();
  }

  @Override
  protected void tearDown() throws Exception {
    deleteRegisteredDeployments();
    String errorMsg = verifyDbClean();

    super.tearDown();

    if (errorMsg!=null) {
      throw new JbpmException(errorMsg);
    }
  }

  public static void jmsCreateQueue(String connectionFactoryJndiName, String queueJndiName) {
    JmsExtensions.createQueue(connectionFactoryJndiName, queueJndiName);
  }

  public static Object jmsConsumeMessageFromQueue(String connectionFactoryJndiName, String queueJndiName) {
    return jmsConsumeMessageFromQueue(connectionFactoryJndiName, queueJndiName, 1000, true, Session.AUTO_ACKNOWLEDGE);
  }

  public static Object jmsConsumeMessageFromQueue(String connectionFactoryJndiName, String queueJndiName, long timeout, boolean transacted, int acknowledgeMode) {
    return JmsExtensions.consumeMessageFromQueue(connectionFactoryJndiName, queueJndiName, timeout, transacted, acknowledgeMode);
  }

  public static Object jmsConsumeMessageFromQueueXA(String connectionFactoryJndiName, String queueJndiName, long timeout) {
    return JmsExtensions.consumeMessageFromQueueXA(connectionFactoryJndiName, queueJndiName, timeout);
  }

  public static void jmsAssertQueueEmpty(String connectionFactoryJndiName, String queueJndiName, long timeout, boolean transacted, int acknowledgeMode) {
    JmsExtensions.jmsAssertQueueEmpty(connectionFactoryJndiName, queueJndiName, timeout, transacted, acknowledgeMode);
  }

  public static void jmsAssertQueueEmptyXA(String connectionFactoryJndiName, String queueJndiName, long timeout) {
    JmsExtensions.jmsAssertQueueEmptyXA(connectionFactoryJndiName, queueJndiName, timeout);
  }

  public static void jmsRemoveQueue(String connectionFactoryJndiName, String queueJndiName) {
    JmsExtensions.removeQueue(connectionFactoryJndiName, queueJndiName);
  }

  public static void jmsCreateTopic(String connectionFactoryJndiName, String topicJndiName) {
    JmsExtensions.createTopic(connectionFactoryJndiName, topicJndiName);
  }

  public static JmsTopicListener jmsStartTopicListener(String connectionFactoryJndiName, String topicJndiName, boolean transacted, int acknowledgeMode) {
    return new JmsNonXATopicListener(connectionFactoryJndiName, topicJndiName, transacted, acknowledgeMode);
  }

  public static JmsTopicListener jmsStartTopicListenerXA(String connectionFactoryJndiName, String topicJndiName) {
    return new JmsXATopicListener(connectionFactoryJndiName, topicJndiName);
  }

  public static void jmsRemoveTopic(String connectionFactoryJndiName, String topicJndiName) {
    JmsExtensions.removeTopic(connectionFactoryJndiName, topicJndiName);
  }

  protected String verifyDbClean() {
    String errorMsg = null;
    String recordsLeftMsg = Db.verifyClean(processEngine);
    if ( (recordsLeftMsg!=null)
         && (recordsLeftMsg.length()>0)
       ) {
      errorMsg = "database was not clean after test: "+recordsLeftMsg;
    }
    return errorMsg;
  }

  protected void deleteRegisteredDeployments() {
    if (registeredDeployments != null) {
      for (String deploymentId : registeredDeployments) {
        repositoryService.deleteDeploymentCascade(deploymentId);
      }
    }
  }

  // deployment helper methods ////////////////////////////////////////////////

  public String deployFromClasspath(String resourceName) {
    String deploymentDbid =
        repositoryService.createDeployment()
            .addResourceFromClasspath(resourceName)
            .deploy();

    registerDeployment(deploymentDbid);

    return deploymentDbid;
  }

  /** deploys the process, keeps a reference to the deployment and
   * automatically deletes the deployment in the tearDown */
  public String deployJpdlXmlString(String jpdlXmlString) {
    String deploymentDbid =
        repositoryService.createDeployment()
            .addResourceFromString("xmlstring.jpdl.xml", jpdlXmlString)
            .deploy();

    registerDeployment(deploymentDbid);

    return deploymentDbid;
  }

  public String deployBpmn2XmlString(String bpmn2XmlString) {
    String deploymentDbid =
      repositoryService.createDeployment()
          .addResourceFromString("xmlstring.bpmn.xml", bpmn2XmlString)
          .deploy();

    registerDeployment(deploymentDbid);
    return deploymentDbid;
  }

  /** registered deployments will be deleted in the tearDown */
  protected void registerDeployment(String deploymentId) {
    if (registeredDeployments == null) registeredDeployments = new ArrayList<String>();
    registeredDeployments.add(deploymentId);
  }

  // task helper methods //////////////////////////////////////////////////////

  public static void assertContainsTask(List<Task> taskList, String taskName) {
    if (getTask(taskList, taskName)==null) {
      fail("tasklist doesn't contain task '"+taskName+"': "+taskList);
    }
  }

  public static void assertContainsTask(List<Task> taskList, String taskName, String assignee) {
    if (getTask(taskList, taskName, assignee)==null) {
      fail("tasklist doesn't contain task '"+taskName+"' for assignee '"+assignee+"': "+taskList);
    }
  }

  public static Task getTask(List<Task> taskList, String taskName) {
    for (Task task : taskList) {
      if (taskName.equals(task.getName())) {
        return task;
      }
    }
    return null;
  }

  public static Task getTask(List<Task> taskList, String taskName, String assignee) {
    for (Task task : taskList) {
      if (taskName.equals(task.getName())) {
        if (assignee==null) {
          if (task.getAssignee()==null) {
            return task;
          }
        } else {
          if (assignee.equals(task.getAssignee())) {
            return task;
          }
        }
      }
    }
    return null;
  }

  public void assertNoOpenTasks(String processInstanceId) {
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    assertTrue("There were still open tasks found for the process instance with id " +
               processInstanceId + ". Current tasks are: " +
               listAllOpenTasks(processInstanceId), tasks.isEmpty());
  }

  protected String listAllOpenTasks(String processInstanceId) {
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    StringBuilder result = new StringBuilder();
    for (Task task : tasks) {
      result.append("'" + task.getName() + "', ");
    }

    if (result.length() > 2) {
      result.setLength(result.length() - 2); // remove the last ', '
    }

    return result.toString();
  }

  // execution helper methods //////////////////////////////////////////

  public void assertExecutionEnded(String processInstanceId) {
    assertNull("Error: an active process instance with id " + processInstanceId + " was found",
            executionService.findProcessInstanceById(processInstanceId));
  }

  public void assertProcessInstanceEnded(String processInstanceId) {
    assertExecutionEnded(processInstanceId);
  }

  public void assertProcessInstanceEnded(ProcessInstance processInstance) {
    assertExecutionEnded(processInstance.getId());
  }

  public void assertProcessInstanceActive(ProcessInstance processInstance) {
      assertProcessInstanceActive(processInstance.getId());
  }

  public void assertProcessInstanceActive(String processInstanceId) {
    assertNotNull("Error: an active process instance with id " + processInstanceId + " was not found",
            executionService.findProcessInstanceById(processInstanceId));
  }

  public void assertActivityActive(String executionId, String activityName) {
    assertTrue("The execution with id '" + executionId +
               "' is not active in the activity '" + activityName + "'." +
               "Current activitites are: " + listAllActiveActivites(executionId),
               executionService.findExecutionById(executionId).isActive(activityName));
  }

  public void assertNotActivityActive(String executionId, String activityName) {
    Execution execution = executionService.findExecutionById(executionId);
    assertFalse(execution.isActive(activityName));
  }

  public void assertActivitiesActive(String executionId, String ... activityNames) {
    CollectionAssertions.assertContainsSameElements(
            executionService.findExecutionById(executionId).findActiveActivityNames(), activityNames);
  }

  /** Checks if the given execution is active in one (or more) of the given activities */
  public void assertExecutionInOneOrMoreActivitiesActive(String executionId, String ... activityNames) {

    boolean inOneActivityActive = false;
    Execution execution = executionService.findExecutionById(executionId);

    for (String activityName : activityNames) {
      if (execution.isActive(activityName)) {
        inOneActivityActive = true;
      }
    }

    assertTrue("The execution with id '" + executionId +
            "' is not active in one of these activities: " + activityNames +
            "Current activitites are: " + listAllActiveActivites(executionId),
            inOneActivityActive);
  }

  protected String listAllActiveActivites(String executionId) {
    Execution execution = executionService.findExecutionById(executionId);
    Set<String> activeActivities = execution.findActiveActivityNames();
    StringBuilder result = new StringBuilder();
    for (String activeActivity : activeActivities) {
      result.append("'" + activeActivity + "', ");
    }

    if (result.length() > 2) {
      result.setLength(result.length() - 2); // remove the last ', '
    }

    return result.toString();
  }

  public void waitTillNoMoreMessages() {
    final long timeout = 60 * 1000;
    final long checkInterval = 1000;

    // install a timer that will interrupt if it takes too long
    // if that happens, it will lead to an interrupted exception and the test
    // will fail
    TimerTask interruptTask = new TimerTask() {

      Thread testThread = Thread.currentThread();

      public void run() {
        log.debug("test " + getName() + " took too long. going to interrupt..." + testThread);
        testThread.interrupt();
      }
    };
    Timer timer = new Timer();
    timer.schedule(interruptTask, timeout);

    try {
      for (int jobCount; (jobCount = getJobCount()) > 0;) {
        log.debug("waiting " + checkInterval + " ms for " + jobCount + " jobs to execute");
        Thread.sleep(checkInterval);
      }
    } catch (InterruptedException e) {
      fail("test execution exceeded treshold of " + timeout + " milliseconds");
    } finally {
      timer.cancel();
    }
  }

  protected int getJobCount() {
    return processEngine.execute(new Command<Integer>() {
      private static final long serialVersionUID = 1L;

      public Integer execute(Environment environment) {
        org.hibernate.Session session = environment.get(org.hibernate.Session.class);
        return (Integer) session.createCriteria("org.jbpm.pvm.internal.job.JobImpl")
          .add(Restrictions.gt("retries", 0))
          .setProjection(Projections.rowCount())
          .uniqueResult();
      }
    });
  }
}

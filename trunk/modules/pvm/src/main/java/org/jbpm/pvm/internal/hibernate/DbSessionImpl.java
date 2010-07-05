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
package org.jbpm.pvm.internal.hibernate;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;

import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.api.history.HistoryComment;
import org.jbpm.api.task.Task;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.client.ClientExecution;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.history.model.HistoryCommentImpl;
import org.jbpm.pvm.internal.history.model.HistoryProcessInstanceImpl;
import org.jbpm.pvm.internal.id.DbidGenerator;
import org.jbpm.pvm.internal.job.JobImpl;
import org.jbpm.pvm.internal.job.StartProcessTimer;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.query.DeploymentQueryImpl;
import org.jbpm.pvm.internal.query.HistoryActivityInstanceQueryImpl;
import org.jbpm.pvm.internal.query.HistoryDetailQueryImpl;
import org.jbpm.pvm.internal.query.HistoryProcessInstanceQueryImpl;
import org.jbpm.pvm.internal.query.JobQueryImpl;
import org.jbpm.pvm.internal.query.ProcessInstanceQueryImpl;
import org.jbpm.pvm.internal.query.TaskQueryImpl;
import org.jbpm.pvm.internal.session.DbSession;
import org.jbpm.pvm.internal.task.TaskImpl;
import org.jbpm.pvm.internal.util.Clock;
import org.jbpm.pvm.internal.util.CollectionUtil;

/**
 * @author Tom Baeyens
 */
public class DbSessionImpl implements DbSession {

  private static Log log = Log.getLog(DbSessionImpl.class.getName());

  protected Session session;

  public void close() {
    session.close();
  }

  public <T> T get(Class<T> entityClass, Object primaryKey) {
    return entityClass.cast(session.get(entityClass, (Serializable) primaryKey));
  }

  public void flush() {
    session.flush();
  }

  public void forceVersionUpdate(Object entity) {
    session.lock(entity, LockMode.FORCE);
  }

  public void lockPessimistically(Object entity) {
    session.lock(entity, LockMode.UPGRADE);
  }

  public void save(Object entity) {
    session.save(entity);
  }

  public void update(Object entity) {
    session.update(entity);
  }

  public void merge(Object entity) {
    session.merge(entity);
  }

  public void delete(Object entity) {
    session.delete(entity);
  }

  public Session getSession() {
    return session;
  }

  public void setSession(Session session) {
    this.session = session;
  }

  public void deleteProcessDefinitionHistory(String processDefinitionId) {
    List<?> historyProcessInstances = session.createCriteria(HistoryProcessInstanceImpl.class)
      .add(Restrictions.eq("processDefinitionId", processDefinitionId))
      .list();

    for (Object hpi : historyProcessInstances) {
      session.delete(hpi);
    }
  }

  public boolean isHistoryEnabled() {
    ClassMetadata historyHibernateMetadata = session.getSessionFactory()
      .getClassMetadata(HistoryProcessInstanceImpl.class);
    return historyHibernateMetadata != null;
  }

  // process execution queries ////////////////////////////////////////////////

  public ClientExecution findExecutionById(String executionId) {
    // query definition can be found at the bottom of resource jbpm.execution.hbm.xml
    return (ClientExecution) session.getNamedQuery("findExecutionById")
      .setString("id", executionId)
      .setMaxResults(1)
      .uniqueResult();
  }

  public ClientExecution findProcessInstanceById(String processInstanceId) {
    // query definition can be found at the bottom of resource jbpm.execution.hbm.xml
    return (ClientExecution) session.getNamedQuery("findProcessInstanceById")
      .setString("processInstanceId", processInstanceId)
      .setMaxResults(1)
      .uniqueResult();
  }

  public ClientExecution findProcessInstanceByIdIgnoreSuspended(String processInstanceId) {
    // query definition can be found at the bottom of resource jbpm.execution.hbm.xml
    return (ClientExecution) session.getNamedQuery("findProcessInstanceByIdIgnoreSuspended")
      .setString("processInstanceId", processInstanceId)
      .setMaxResults(1)
      .uniqueResult();
  }

  public List<String> findProcessInstanceIds(String processDefinitionId) {
    // query definition can be found at the bottom of resource jbpm.execution.hbm.xml
    List<?> processInstanceIds = session.getNamedQuery("findProcessInstanceIds")
      .setString("processDefinitionId", processDefinitionId)
      .list();
    return CollectionUtil.checkList(processInstanceIds, String.class);
  }

  public void deleteProcessInstance(String processInstanceId) {
    deleteProcessInstance(processInstanceId, true);
  }

  public void deleteProcessInstance(String processInstanceId, boolean deleteHistory) {
    if (processInstanceId == null) {
      throw new JbpmException("processInstanceId is null");
    }

    // if history should be deleted
    if (deleteHistory && (isHistoryEnabled())) {
      // try to get the history
      HistoryProcessInstanceImpl historyProcessInstance = findHistoryProcessInstanceById(processInstanceId);

      // if there is a history process instance in the db
      if (historyProcessInstance != null) {
        if (log.isDebugEnabled()) {
          log.debug("deleting history process instance " + processInstanceId);
        }
        session.delete(historyProcessInstance);
      }
    }

    ExecutionImpl processInstance = (ExecutionImpl) findProcessInstanceByIdIgnoreSuspended(processInstanceId);
    if (processInstance != null) {
      deleteSubProcesses(processInstance, deleteHistory);

      // delete remaining tasks for this process instance
      List<TaskImpl> tasks = findTasks(processInstanceId);
      for (TaskImpl task : tasks) {
        session.delete(task);
      }

      // delete remaining jobs for this process instance
      JobImpl currentJob = EnvironmentImpl.getFromCurrent(JobImpl.class, false);
      List<JobImpl> jobs = findJobs(processInstanceId);
      for (JobImpl job : jobs) {
        if (job != currentJob) {
          session.delete(job);
        }
      }

      if (log.isDebugEnabled()) {
        log.debug("Deleting process instance " + processInstanceId);
      }
      session.delete(processInstance);
    }
    else {
      throw new JbpmException("Can't delete processInstance " + processInstanceId
        + ": no processInstance found for the given id");
    }
  }

  private void deleteSubProcesses(ExecutionImpl execution, boolean deleteHistory) {
    ExecutionImpl subProcessInstance = execution.getSubProcessInstance();
    if (subProcessInstance != null) {
      subProcessInstance.setSuperProcessExecution(null);
      execution.setSubProcessInstance(null);
      deleteProcessInstance(subProcessInstance.getId(), deleteHistory);
    }
    Collection<ExecutionImpl> childExecutions = execution.getExecutions();
    if (childExecutions != null) {
      for (ExecutionImpl childExecution : childExecutions) {
        deleteSubProcesses(childExecution, deleteHistory);
      }
    }
  }

  public HistoryProcessInstanceImpl findHistoryProcessInstanceById(String processInstanceId) {
    return (HistoryProcessInstanceImpl) session.createCriteria(HistoryProcessInstanceImpl.class)
      .add(Restrictions.eq("processInstanceId", processInstanceId))
      .uniqueResult();
  }

  List<TaskImpl> findTasks(String processInstanceId) {
    List<?> tasks = session.createCriteria(TaskImpl.class)
      .createAlias("processInstance", "pi")
      .add(Restrictions.eq("pi.id", processInstanceId))
      .list();
    return CollectionUtil.checkList(tasks, TaskImpl.class);
  }

  List<JobImpl> findJobs(String processInstanceId) {
    List<?> jobs = session.createCriteria(JobImpl.class)
      .createAlias("processInstance", "pi")
      .add(Restrictions.eq("pi.id", processInstanceId))
      .list();
    return CollectionUtil.checkList(jobs, JobImpl.class);
  }

  public void cascadeExecutionSuspend(ExecutionImpl execution) {
    // cascade suspend to jobs
    List<?> jobs = session.createCriteria(JobImpl.class)
      .add(Restrictions.eq("execution", execution))
      .add(Restrictions.ne("state", JobImpl.STATE_SUSPENDED))
      .list();
    for (JobImpl job : CollectionUtil.checkList(jobs, JobImpl.class)) {
      job.suspend();
    }

    // cascade suspend to tasks
    List<?> tasks = session.createCriteria(TaskImpl.class)
      .add(Restrictions.eq("execution", execution))
      .add(Restrictions.ne("state", Task.STATE_SUSPENDED))
      .list();
    for (TaskImpl task : CollectionUtil.checkList(tasks, TaskImpl.class)) {
      task.suspend();
    }
  }

  public void cascadeExecutionResume(ExecutionImpl execution) {
    // cascade resume to jobs
    List<?> jobs = session.createCriteria(JobImpl.class)
      .add(Restrictions.eq("execution", execution))
      .add(Restrictions.eq("state", JobImpl.STATE_SUSPENDED))
      .list();
    for (JobImpl job : CollectionUtil.checkList(jobs, JobImpl.class)) {
      job.resume();
    }

    // cascade resume to tasks
    List<?> tasks = session.createCriteria(TaskImpl.class)
      .add(Restrictions.eq("execution", execution))
      .add(Restrictions.eq("state", Task.STATE_SUSPENDED))
      .list();
    for (TaskImpl task : CollectionUtil.checkList(tasks, TaskImpl.class)) {
      task.resume();
    }
  }

  public TaskImpl createTask() {
    TaskImpl task = newTask();
    task.setCreateTime(Clock.getTime());
    return task;
  }

  protected TaskImpl newTask() {
    TaskImpl task = new TaskImpl();
    long dbid = EnvironmentImpl.getFromCurrent(DbidGenerator.class).getNextId();
    task.setDbid(dbid);
    task.setNew(true);
    return task;
  }

  public TaskImpl findTaskByDbid(long taskDbid) {
    return (TaskImpl) session.get(TaskImpl.class, taskDbid);
  }

  public TaskImpl findTaskByExecution(Execution execution) {
    return (TaskImpl) session.createCriteria(TaskImpl.class)
      .add(Restrictions.eq("execution", execution))
      .uniqueResult();
  }

  public JobImpl findFirstAcquirableJob() {
    return (JobImpl) session.getNamedQuery("findFirstAcquirableJob")
      .setTimestamp("now", Clock.getTime())
      .setMaxResults(1)
      .uniqueResult();
  }

  public List<JobImpl> findExclusiveJobs(Execution processInstance) {
    List<?> exclusiveJobs = session.getNamedQuery("findExclusiveJobs")
      .setTimestamp("now", Clock.getTime())
      .setEntity("processInstance", processInstance)
      .list();
    return CollectionUtil.checkList(exclusiveJobs, JobImpl.class);
  }

  public JobImpl findFirstDueJob() {
    return (JobImpl) session.getNamedQuery("findFirstDueJob")
      .setMaxResults(1)
      .uniqueResult();
  }

  public List<StartProcessTimer> findStartProcessTimers(String processDefinitionName) {
    List<?> timers = session.createCriteria(StartProcessTimer.class)
      .add(Restrictions.eq("signalName", processDefinitionName))
      .list();
    return CollectionUtil.checkList(timers, StartProcessTimer.class);
  }

  public ProcessInstanceQueryImpl createProcessInstanceQuery() {
    return new ProcessInstanceQueryImpl();
  }

  public TaskQueryImpl createTaskQuery() {
    return new TaskQueryImpl();
  }

  public HistoryProcessInstanceQueryImpl createHistoryProcessInstanceQuery() {
    return new HistoryProcessInstanceQueryImpl();
  }

  public HistoryActivityInstanceQueryImpl createHistoryActivityInstanceQuery() {
    return new HistoryActivityInstanceQueryImpl();
  }

  public HistoryDetailQueryImpl createHistoryDetailQuery() {
    return new HistoryDetailQueryImpl();
  }

  public JobQueryImpl createJobQuery() {
    return new JobQueryImpl();
  }

  public DeploymentQueryImpl createDeploymentQuery() {
    return new DeploymentQueryImpl();
  }

  public List<HistoryComment> findCommentsByTaskId(String taskId) {
    List<?> comments = session.createCriteria(HistoryCommentImpl.class)
      .createAlias("historyTask", "task")
      .add(Restrictions.eq("task.dbid", Long.parseLong(taskId)))
      .addOrder(Order.asc("historyTaskIndex"))
      .list();
    return CollectionUtil.checkList(comments, HistoryComment.class);
  }
}

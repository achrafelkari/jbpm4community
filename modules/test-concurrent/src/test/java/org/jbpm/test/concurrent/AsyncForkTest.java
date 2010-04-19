package org.jbpm.test.concurrent;

import java.util.List;

import org.hibernate.StaleStateException;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.job.Job;

/**
 * Concurrency test case: when using an async fork with differen outgoing
 * transitions, potentially conflicts can occur (eg when the outgoing paths
 * come together in the Join activity at the same time). 
 * 
 * @author jbarrez
 */
public class AsyncForkTest extends ConcurrentJbpmTestCase {
  
  /**
   * Test case using an async fork with 2 outgoing transactions.
   * In jBPM3, a StaleStateException was thrown when the different paths
   * came together in the Join at the same time.
   * 
   * However, the jBPM4 Join activity is designed with concurrency in mind,
   * and the StaleStateException should not occur when this scenario happens.
   */
  public void testAsyncForkNoOptimisticLockingFailure()  {
    deployJpdlXmlString(
            "<process name='asyncFork'>" +
            "  <start>" +
            "    <transition to='theFork' />" +
            "  </start>" +
            "  <fork name='theFork'>" +
            "    <on event='end' continue='async' />" +
            "    <transition to='pathA' />" +
            "    <transition to='pathB' />" +
            "  </fork>" +
            "  <custom name='pathA' class='org.jbpm.test.concurrent.PassThroughActivity' >" +
            "    <transition to='theJoin' />" +
            "  </custom>" + 
            "  <custom name='pathB' class='org.jbpm.test.concurrent.PassThroughActivity' >" +
            "    <transition to='theJoin' />" +
            "  </custom>" + 
            // Can't test with default lock-mode (upgrade). SELECT ... FOR UPGRADE 
            // will block transactions at database level with no decent approach 
            // to check if the thread is blocking. So we use the default lockmode,
            // which is the standard Hibernate optimistic locking.
            //
            // Note: not using lockmode upgrade can cause the Join logic to
            // work with incorrect data: ie it could be that an incoming 
            // transition is not seen as the 'last' one, due to a concurrent 
            // read of data. This siutation is avoided in the test by executing
            // the Join activity logic of the last transition only after the other one.
            "  <join name='theJoin' lockmode='none'>" + 
            "    <transition to='end' />" +
            "  </join>" + 
            "  <end name='end' />" +
            "</process>"
          );
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("asyncFork");
    final List<Job> jobs = managementService.createJobQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(2, jobs.size()); 
    
    SynchronizableCommandExecutor executor1 = startThreadAndSyncAfterExecution(jobs.get(0));
    SynchronizableCommandExecutor executor2 = startThreadAndSyncAfterExecution(jobs.get(1));
    
    try {
      executor1.join();
      executor2.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    
    if (executor1.getException() instanceof StaleStateException
            || executor2.getException() instanceof StaleStateException) {
      fail("A StaleStaeException was thrown, altough this shouldn't happen");
    }
    
    assertProcessInstanceEnded(processInstance);
  }
  
  private SynchronizableCommandExecutor startThreadAndSyncAfterExecution(Job job) {
    SynchronizableCommandExecutor executor = new SynchronizableCommandExecutor(environmentFactory, job);
    executor.synchroniseAfterExecution();
    executor.start();
    executor.waitUntilExecutionFinished(false);
    return executor;
  }

}

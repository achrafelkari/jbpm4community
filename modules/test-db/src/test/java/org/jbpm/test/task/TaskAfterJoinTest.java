
package org.jbpm.test.task;

import java.util.HashSet;
import java.util.Set;

import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmTestCase;

/**
 * JBPM-2836.
 *
 * @author Huisheng Xu
 */
public class TaskAfterJoinTest extends JbpmTestCase {
  protected static final String PROCESS_XML = ""
    + "<process name='TaskAfterJoinTest' xmlns='http://jbpm.org/4.3/jpdl'>"
    + "   <start g='16,60,48,48'>"
    + "      <transition to='fork'/>"
    + "   </start>"
    + "   <fork g='96,60,48,48' name='fork'>"
    + "      <transition g='120,41:' to='state1'/>"
    + "      <transition to='state2' g='120,126:'/>"
    + "   </fork>"
    + "   <state g='176,16,149,52' name='state1'>"
    + "      <transition g='379,40:' to='join'/>"
    + "   </state>"
    + "   <state g='176,100,149,52' name='state2'>"
    + "      <transition to='join' g='382,125:'/>"
    + "   </state>"
    + "   <join g='357,60,48,48' name='join'>"
    + "      <transition to='task1'/>"
    + "   </join>"
    + "   <end g='561,60,48,48' name='end'/>"
    + "   <task candidate-groups='sales-dept' g='437,58,92,52' name='task1'>"
    + "      <transition to='end'/>"
    + "   </task>"
    + "</process>";

  public void testTask() {
    deployJpdlXmlString(PROCESS_XML);

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("TaskAfterJoinTest");
    String pid = processInstance.getId();

    Set<String> expectedActivityNames = new HashSet<String>();
    expectedActivityNames.add("state1");
    expectedActivityNames.add("state2");

    assertEquals(expectedActivityNames, processInstance.findActiveActivityNames());

    assertNotNull(processInstance.findActiveExecutionIn("state1"));
    assertNotNull(processInstance.findActiveExecutionIn("state2"));

    String state1Id = processInstance.findActiveExecutionIn("state1").getId();
    processInstance = executionService.signalExecutionById(state1Id);

    expectedActivityNames.remove("state1");
    assertEquals(expectedActivityNames, processInstance.findActiveActivityNames());

    assertNotNull(processInstance.findActiveExecutionIn("state2"));

    String state2Id = processInstance.findActiveExecutionIn("state2").getId();
    // HERE - it raises NullPointerException
    processInstance = executionService.signalExecutionById(state2Id);

    expectedActivityNames.remove("state2");
    expectedActivityNames.add("task1");
    assertEquals(expectedActivityNames, processInstance.findActiveActivityNames());

    assertNotNull(processInstance.findActiveExecutionIn("task1"));

    String task1Id = processInstance.findActiveExecutionIn("task1").getId();
    processInstance = executionService.signalExecutionById(task1Id);

    assertNull("execution "+pid+" should not exist", executionService.findExecutionById(pid));
  }
}

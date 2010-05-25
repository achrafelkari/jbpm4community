package org.jbpm.test.task;
 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
 
import org.jbpm.api.Configuration;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.TaskService;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.api.task.Assignable;
import org.jbpm.api.task.AssignmentHandler;
import org.jbpm.api.task.Participation;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;
 
public class AssignmentHandlerTest extends JbpmTestCase {
 
    /** Deployment id. */
    String deploymentId;
 
    /**
     * Set up.
     * @throws Exception exception
     */
    protected void setUp() throws Exception {
        super.setUp();
 
        // XML definition
        StringBuilder jpdl = new StringBuilder();
        jpdl.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        jpdl.append("<process key=\"testProcess\" name=\"Test Process\" xmlns=\"http://jbpm.org/4.3/jpdl\">");
        jpdl.append("  <start g=\"67,236,48,48\" name=\"start1\">");
        jpdl.append("    <transition g=\"-43,-18\" name=\"to fork1\" to=\"fork1\"/>");
        jpdl.append("  </start>");
        jpdl.append("  <task g=\"255,144,92,52\" name=\"task1\" candidate-groups=\"firstGroup\">");
        jpdl.append("    <assignment-handler class=\"org.jbpm.test.task.AssignmentHandlerTest$AutoAssignment\"/>");
        jpdl.append("    <transition g=\"-41,-18\" name=\"to join1\" to=\"join1\"/>");
        jpdl.append("  </task>");
        jpdl.append("  <task g=\"258,334,92,52\" name=\"task2\" candidate-groups=\"secondGroup\">");
        jpdl.append("    <assignment-handler class=\"org.jbpm.test.task.AssignmentHandlerTest$AutoAssignment\"/>");
        jpdl.append("    <transition g=\"-41,-18\" name=\"to join1\" to=\"join1\"/>");
        jpdl.append("  </task>");
        jpdl.append("  <task g=\"515,228,92,52\" name=\"task3\" candidate-groups=\"thirdGroup\">");
        jpdl.append("    <assignment-handler class=\"org.jbpm.test.task.AssignmentHandlerTest$AutoAssignment\"/>");
        jpdl.append("    <transition g=\"-42,-18\" name=\"to end1\" to=\"end1\"/>");
        jpdl.append("  </task>");
        jpdl.append("  <end g=\"676,232,48,48\" name=\"end1\"/>");
        jpdl.append("  <fork g=\"172,236,48,48\" name=\"fork1\">");
        jpdl.append("    <transition g=\"-44,-18\" name=\"to task1\" to=\"task1\"/>");
        jpdl.append("    <transition g=\"-44,-18\" name=\"to task2\" to=\"task2\"/>");
        jpdl.append("  </fork>");
        jpdl.append("  <join g=\"385,233,48,48\" name=\"join1\">");
        jpdl.append("    <transition g=\"-44,-18\" name=\"to task3\" to=\"task3\"/>");
        jpdl.append("  </join>");
        jpdl.append("</process>");
 
        // Deploys the process
        deploymentId =
                repositoryService.createDeployment().addResourceFromString("testProcess.jpdl.xml", jpdl.toString())
                        .deploy();
    }
 
    /**
     * Tear down.
     * @throws Exception exception
     */
    protected void tearDown() throws Exception {
        repositoryService.deleteDeploymentCascade(deploymentId);
        super.tearDown();
    }
 
    /**
     * Tests the process.
     */
    public void testProcess() {
 
        // Starts a new process instance and gets the instance id
        ProcessInstance processInstance = executionService.startProcessInstanceByKey("testProcess");
        String pid = processInstance.getId();
 
        // Gets the tasks auto-assigned for the first user and completes the task
        List < Task > taskList = taskService.findPersonalTasks("firstUser");
        assertEquals(1, taskList.size());
        Task task = taskList.get(0);
        taskService.completeTask(task.getId());
 
        // Gets the tasks auto-assigned for the second user and completes the task
        taskList = taskService.findPersonalTasks("secondUser");
        assertEquals(1, taskList.size());
        task = taskList.get(0);
        taskService.completeTask(task.getId());
 
        // Gets the tasks auto-assigned for the second user and completes the task
        taskList = taskService.findPersonalTasks("thirdUser");
        assertEquals(1, taskList.size());
        task = taskList.get(0);
        taskService.completeTask(task.getId());
 
        // Tries to load the instance and checks if it was finished
        processInstance = executionService.findProcessInstanceById(pid);
        assertNull(processInstance);
    }
 
    /**
     * Auto assignment class.
     */
    public static class AutoAssignment implements AssignmentHandler {
 
        /** Serial version ID. */
        private static final long serialVersionUID = 9063679883107908899L;
 
        /**
         * Auto-claim the task to the default user.
         * @param assignable assignable object
         * @param execution execution object
         * @throws Exception exception
         */
        public void assign(Assignable assignable, OpenExecution execution) throws Exception {
 
            // Default users (group --> user mapping)
            Map < String, String > defaultUsers = new HashMap < String, String >();
            defaultUsers.put("firstGroup", "firstUser");
            defaultUsers.put("secondGroup", "secondUser");
            defaultUsers.put("thirdGroup", "thirdUser");
 
            // Engine and task service
            ProcessEngine processEngine = new Configuration().buildProcessEngine();
            TaskService taskService = processEngine.getTaskService();
 
            // Loads the active activities
            Set < String > activities = execution.findActiveActivityNames();
 
            // Iterates the activities
            for (String activity : activities) {
 
                // Loads the tasks according to the process instance and activity name
                List < Task > tasks =
                        taskService.createTaskQuery().activityName(activity).processInstanceId(
                            execution.getProcessInstance().getId()).list();
 
                // Iterates the tasks
                for (Task task : tasks) {
 
                    // Compares the task name to the activity name
                    // If the task name matches the activity name, loads the candidate-group and assigns the default user
                    if (task.getName().equals(activity)) {
 
                        // Loads the tasks candidate groups (in our process we have just one group)
                        List < Participation > groups = taskService.getTaskParticipations(task.getId());
 
                        // If the groups collection is not empty, loads the default user and sets in the task
                        if (!groups.isEmpty()) {
                            assignable.setAssignee(defaultUsers.get(groups.get(0).getGroupId()));
                        }
                    }
                }
            }
        }
    }
}

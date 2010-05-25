
package org.jbpm.jpdl.internal.model;

import junit.framework.TestCase;
import org.jbpm.pvm.internal.task.TaskDefinitionImpl;

/**
 * @author Huisheng Xu
 */
public class JpdlProcessDefinitionTest extends TestCase {
    public void testCreateTaskDefinition() {
        JpdlProcessDefinition jpdlProcessDefinition = new JpdlProcessDefinition();
        jpdlProcessDefinition.createTaskDefinition("do something");
        assertEquals("do something", jpdlProcessDefinition.getTaskDefinition("do something").getName());
        assertEquals(1, jpdlProcessDefinition.getTaskDefinitions().size());
    }

    public void testAddTaskDefinition() {
        JpdlProcessDefinition jpdlProcessDefinition = new JpdlProcessDefinition();
        TaskDefinitionImpl taskDefinition = new TaskDefinitionImpl();
        taskDefinition.setName("do something");
        jpdlProcessDefinition.addTaskDefinitionImpl(taskDefinition);
        assertEquals("do something", jpdlProcessDefinition.getTaskDefinition("do something").getName());
        assertEquals(1, jpdlProcessDefinition.getTaskDefinitions().size());
    }
}

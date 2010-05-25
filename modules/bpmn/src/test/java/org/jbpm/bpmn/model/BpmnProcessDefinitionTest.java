
package org.jbpm.bpmn.model;

import junit.framework.TestCase;
import org.jbpm.pvm.internal.task.TaskDefinitionImpl;

/**
 * @author Huisheng Xu
 */
public class BpmnProcessDefinitionTest extends TestCase {
    public void testCreateTaskDefinition() {
        BpmnProcessDefinition bpmnProcessDefinition = new BpmnProcessDefinition();
        bpmnProcessDefinition.createTaskDefinition("do something");
        assertEquals("do something", bpmnProcessDefinition.getTaskDefinition("do something").getName());
        assertEquals(1, bpmnProcessDefinition.getTaskDefinitions().size());
    }

    public void testAddTaskDefinition() {
        BpmnProcessDefinition bpmnProcessDefinition = new BpmnProcessDefinition();
        TaskDefinitionImpl taskDefinition = new TaskDefinitionImpl();
        taskDefinition.setName("do something");
        bpmnProcessDefinition.addTaskDefinitionImpl(taskDefinition);
        assertEquals("do something", bpmnProcessDefinition.getTaskDefinition("do something").getName());
        assertEquals(1, bpmnProcessDefinition.getTaskDefinitions().size());
    }
}


package org.jbpm.pvm.internal.task;

import junit.framework.TestCase;
import org.jbpm.api.task.Task;
import org.jbpm.pvm.internal.task.TaskImpl;

public class TaskImplTest extends TestCase {
    public void testSuspend() {
        TaskImpl task = new TaskImpl();
        assertEquals(Task.STATE_OPEN, task.getState());

        task.suspend();
        assertEquals(Task.STATE_SUSPENDED, task.getState());
        assertFalse(task.isCompleted());

        task.resume();
        assertEquals(Task.STATE_OPEN, task.getState());
    }

    public void testState() {
        TaskImpl task = new TaskImpl();
        assertEquals(Task.STATE_OPEN, task.getState());

        task.setState(Task.STATE_COMPLETED);

        assertEquals(Task.STATE_COMPLETED, task.getState());
        assertTrue(task.isCompleted());
    }

}

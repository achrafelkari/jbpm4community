package org.jbpm.bpmn.flownodes;

import org.jbpm.pvm.internal.model.ExecutionImpl;

/**
 * Manual activities in BPMN are ignored by the Process Engine.
 * So this is just a no-op, doing nothing.
 * 
 * @author Bernd Ruecker (bernd.ruecker@camunda.com)
 */
public class ManualTaskActivity extends BpmnActivity {

    private static final long serialVersionUID = 1L;

    public void execute(ExecutionImpl executionImpl) {
    }
}

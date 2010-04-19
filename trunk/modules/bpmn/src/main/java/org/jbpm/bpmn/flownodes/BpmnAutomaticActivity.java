package org.jbpm.bpmn.flownodes;

import org.jbpm.api.listener.EventListener;
import org.jbpm.api.listener.EventListenerExecution;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.pvm.internal.model.ExecutionImpl;


public abstract class BpmnAutomaticActivity extends BpmnActivity implements EventListener {

  private static final long serialVersionUID = 1L;
  
  public void execute(ExecutionImpl executionImpl) {
    perform(executionImpl);
    executionImpl.historyAutomatic();
  }
    
  public void notify(EventListenerExecution execution) throws Exception {
    perform(execution);
  }    
    
  abstract void perform(OpenExecution execution);
  
}

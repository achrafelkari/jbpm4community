package org.jbpm.jpdl.internal.activity;

import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.listener.EventListener;
import org.jbpm.api.listener.EventListenerExecution;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.pvm.internal.model.ExecutionImpl;


public abstract class JpdlAutomaticActivity extends JpdlActivity implements EventListener {

  private static final long serialVersionUID = 1L;

  public void execute(ActivityExecution execution) throws Exception {
    perform(execution);
    ((ExecutionImpl)execution).historyAutomatic();
  }
    
  public void notify(EventListenerExecution execution) throws Exception {
    perform(execution);
  }    
    
  abstract void perform(OpenExecution execution) throws Exception;
}

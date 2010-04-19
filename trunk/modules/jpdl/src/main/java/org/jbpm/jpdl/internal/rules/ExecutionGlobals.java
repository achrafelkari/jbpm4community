package org.jbpm.jpdl.internal.rules;

import org.drools.runtime.Globals;
import org.jbpm.api.Execution;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.model.ExecutionImpl;

public class ExecutionGlobals implements Globals {
  
  private static final Log log = Log.getLog(ExecutionGlobals.class.getName());

  ExecutionImpl execution;
  Outcome outcome = new Outcome();
  
  public ExecutionGlobals(Execution execution) {
    this.execution = (ExecutionImpl) execution;
  }

  public Object get(String variableName) {
    if ("execution".equals(variableName)) {
      log.info("returning execution");
      return execution;
    }
    if ("outcome".equals(variableName)) {
      log.info("returning outcome");
      return outcome;
    }
    Object variableValue = execution.getVariable(variableName);
    log.info("returning variable "+variableName+": "+variableValue);
    return variableValue;
  }

  public void set(String variableName, Object value) {
    throw new UnsupportedOperationException();
  }

  public void setDelegate(Globals globals) {
    throw new UnsupportedOperationException();
  }
  public Outcome getOutcome() {
    return outcome;
  }
}

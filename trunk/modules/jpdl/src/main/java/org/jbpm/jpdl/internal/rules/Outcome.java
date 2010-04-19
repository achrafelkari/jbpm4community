package org.jbpm.jpdl.internal.rules;

import org.jbpm.internal.log.Log;

public class Outcome {

  private static final Log log = Log.getLog(Outcome.class.getName());

  String outcome = null;
  
  public void set(String outcome) {
    log.info("outcome is being set to "+outcome);
    this.outcome = outcome;
  }
  
  public boolean isDefined() {
    return (outcome!=null);
  }
  
  public String get() {
    log.info("outcome "+outcome+" is being fetched");
    return outcome;
  }
}

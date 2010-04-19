package org.jbpm.pvm.internal.type.variable;

import org.jbpm.pvm.internal.type.Variable;


/**
 * uses the cache in variable instance to store any object
 * without persisting it.
 */
public class UnpersistableVariable extends Variable {
  
  private static final long serialVersionUID = 1L;
  
  Object value;

  protected Object getObject() {
    return value;
  }

  public boolean isStorable(Object value) {
    return true;
  }

  protected void setObject(Object value) {
    this.value = value;
  }
}

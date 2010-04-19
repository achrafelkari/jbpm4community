package org.jbpm.pvm.internal.type.variable;

import org.jbpm.pvm.internal.type.Variable;


public class NullVariable extends Variable {

  private static final long serialVersionUID = 1L;

  protected Object getObject() {
    return null;
  }
  
  public boolean isStorable(Object value) {
    return (value==null);
  }
  
  protected void setObject(Object value) {
  }
}

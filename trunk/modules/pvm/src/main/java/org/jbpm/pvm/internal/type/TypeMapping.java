package org.jbpm.pvm.internal.type;

import java.io.Serializable;


public class TypeMapping implements Serializable {

  Matcher matcher;
  Type type;

  private static final long serialVersionUID = 1L;
  
  public boolean matches(String name, Object value) {
    return matcher.matches(name, value);
  }
  
  public String toString() {
    return "("+matcher+"-->"+type+")";
  }
  
  public void setMatcher(Matcher matcher) {
    this.matcher = matcher;
  }
  public Type getType() {
    return type;
  }
  public void setType(Type type) {
    this.type = type;
  }
  public Matcher getMatcher() {
    return matcher;
  }
}

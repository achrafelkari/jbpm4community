package org.jbpm.pvm.internal.type;

import java.io.Serializable;


public interface Matcher extends Serializable {

  boolean matches(String name, Object value);
}

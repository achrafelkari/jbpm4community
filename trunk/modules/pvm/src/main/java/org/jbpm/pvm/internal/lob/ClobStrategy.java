package org.jbpm.pvm.internal.lob;

public interface ClobStrategy {

  void set(char[] text, Lob lob);
  char[] get(Lob lob);
}

package org.jbpm.pvm.internal.lob;

public interface BlobStrategy {
  
  void set(byte[] bytes, Lob lob);
  byte[] get(Lob lob);
}

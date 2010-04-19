package org.jbpm.pvm.internal.lob;

public class BlobStrategyBinary implements BlobStrategy {

  public byte[] get(Lob lob) {
    return lob.bytes;
  }

  public void set(byte[] bytes, Lob lob) {
    lob.bytes = bytes;
  }
}

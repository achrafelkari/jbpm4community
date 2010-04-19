package org.jbpm.pvm.internal.lob;

public class ClobStrategyText implements ClobStrategy {

  public char[] get(Lob lob) {
    return lob.extractChars();
  }

  public void set(char[] chars, Lob lob) {
    String text = new String(chars);
    lob.text = text;
  }

}

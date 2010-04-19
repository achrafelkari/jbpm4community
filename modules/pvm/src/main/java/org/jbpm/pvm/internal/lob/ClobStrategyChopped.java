package org.jbpm.pvm.internal.lob;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class ClobStrategyChopped implements ClobStrategy {
  
  int chopSize = 1024;
  
  public char[] get(Lob lob) {
    return glue(lob.charChops);
  }

  public void set(char[] chars, Lob lob) {
    lob.charChops = chop(chars);
  }

  public List<CharChop> chop(char[] chars) {
    List<CharChop> charChops = null;
    if ( (chars!=null)
         && (chars.length>0) ){
      charChops = new ArrayList<CharChop>();
      int index = 0;
      while ( (chars.length-index) > chopSize ) {
        String chop = new String(chars, index, chopSize);
        charChops.add(new CharChop(chop));
        index+=chopSize;
      }
      // add remainder chop
      String chop = new String(chars, index, chars.length-index);
      charChops.add(new CharChop(chop));
    }
    return charChops;
  }

  public char[] glue(List<CharChop> charChops) {
    if (charChops!=null) {
      StringWriter writer = new StringWriter();
      
      for (CharChop charChop: charChops) {
        writer.write(charChop.getText());
      }
      
      return writer.toString().toCharArray();
    }

    return null;
  }
}
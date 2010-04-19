package org.jbpm.pvm.internal.lob;

import java.sql.SQLException;

import org.hibernate.Hibernate;
import org.jbpm.api.JbpmException;

public class ClobStrategyClob implements ClobStrategy {

  public void set(char[] chars, Lob lob) {
    if (chars!=null) {
      lob.clob = Hibernate.createClob(new String(chars));
    }
  }

  public char[] get(Lob lob) {
    java.sql.Clob sqlClob = lob.clob;
    if (sqlClob!=null) {
      try {
        int length = (int) sqlClob.length();
        String text = sqlClob.getSubString(1, length);
        return text.toCharArray();
      } catch (SQLException e) {
        throw new JbpmException("couldn't extract chars out of clob", e);
      }
    } 
    return null;
  }
}

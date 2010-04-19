package org.jbpm.pvm.internal.lob;

import java.sql.SQLException;

import org.hibernate.Hibernate;
import org.jbpm.api.JbpmException;

public class BlobStrategyBlob implements BlobStrategy {
  
  public void set(byte[] bytes, Lob lob) {
    if (bytes!=null) {
      lob.cachedBytes = bytes;
      lob.blob = Hibernate.createBlob(bytes);
    }
  }

  public byte[] get(Lob lob) {
    if (lob.cachedBytes!=null) {
      return lob.cachedBytes;
    }
    
    java.sql.Blob sqlBlob = lob.blob;
    if (sqlBlob!=null) {
      try {
        return sqlBlob.getBytes(1, (int) sqlBlob.length());
      } catch (SQLException e) {
        throw new JbpmException("couldn't extract bytes out of blob", e);
      }
    } 
    return null;
  }
}

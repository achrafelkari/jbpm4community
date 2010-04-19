package org.jbpm.pvm.internal.id;

import org.hibernate.Session;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;

/**
 * @author Tom Baeyens
 */
public class AcquireDbidBlockCmd implements Command<Long> {

  private static final long serialVersionUID = 1L;
  
  long blocksize;
  
  public AcquireDbidBlockCmd(long blocksize) {
    this.blocksize = blocksize;
  }

  public Long execute(Environment environment) throws Exception {
    Session session = environment.get(Session.class);
    
    PropertyImpl property = (PropertyImpl) session.createQuery(
        "select property " +
        "from "+PropertyImpl.class.getName()+" as property " +
        "where property.key = '"+PropertyImpl.NEXT_DBID_KEY+"'"
    ).uniqueResult();
    
    String nextIdText = property.getValue();
    Long nextId = new Long(nextIdText);
    
    property.setValue(Long.toString(nextId.longValue()+blocksize));
    
    session.update(property);
    session.flush();

    return nextId;
  }
}
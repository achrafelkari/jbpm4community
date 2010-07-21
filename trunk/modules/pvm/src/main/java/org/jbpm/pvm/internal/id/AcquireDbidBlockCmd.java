package org.jbpm.pvm.internal.id;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

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

    PropertyImpl property = (PropertyImpl) session.createCriteria(PropertyImpl.class)
      .add(Restrictions.eq("key", PropertyImpl.NEXT_DBID_KEY))
      .uniqueResult();

    long nextId = Long.parseLong(property.getValue());
    property.setValue(Long.toString(nextId + blocksize));

    session.flush();

    return nextId;
  }
}
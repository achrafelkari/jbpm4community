package org.jbpm.pvm.internal.wire.operation;

import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.tx.StandardResource;
import org.jbpm.pvm.internal.tx.StandardTransaction;
import org.jbpm.pvm.internal.tx.Transaction;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireException;


/**
 * enlists this {@link StandardResource} with the current {@link Transaction}.
 *
 * <p>This {@link Operation} specifies that the object on which this operation is applied
 * should be added as a {@link StandardResource} to the specified {@link Transaction}.
 * </p>
 * 
 * <p>property transactionName refers to the objectName of the {@link Transaction}
 * and it may not be null.
 * </p>
 *
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 */
public class EnlistOperation implements Operation {

  private static final long serialVersionUID = 1L;
  private static Log log = Log.getLog(EnlistOperation.class.getName());

  String transactionName = null;

  /**
   * @throws WireException if this operation is applied on an object which is not a resource
   * or if the specified transaction cannot be found.
   */
  public void apply(Object target, WireContext wireContext) {
    if (! (target instanceof StandardResource)) {
      throw new WireException("operation enlist can only be applied on objects that implement "+StandardResource.class.getName()+": "+target+(target!=null ? " ("+target.getClass().getName()+")" : ""));
    }

    Object object = null;
    if (transactionName!=null) {
      object = wireContext.get(transactionName);
    } else {
      object = wireContext.get(Transaction.class);
    }

    if ( (object==null)
         || (! (object instanceof StandardTransaction))
       ) {
      throw new WireException("couldn't find "+StandardTransaction.class.getName()+" "+(transactionName!=null ? "'"+transactionName+"'" : "by type")+" to enlist resource "+target);
    }

    StandardTransaction standardTransaction = (StandardTransaction) object;

    log.trace("enlisting resource "+target+" with transaction");
    standardTransaction.enlistResource((StandardResource)target);
  }

  /**
   * Gets the name of the transaction to which the object should be added.
   */
  public String getTransactionName() {
    return transactionName;
  }

  /**
   * Sets the name of the transaction to which the object should be added.
   * @param transactionName
   */
  public void setTransactionName(String transactionName) {
    this.transactionName = transactionName;
  }
}

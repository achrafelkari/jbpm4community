package org.jbpm.pvm.internal.wire.descriptor;

import java.io.Serializable;

import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.operation.InvokeOperation;
import org.jbpm.pvm.internal.wire.operation.SubscribeOperation;

/**
 * <p>This class specifies an argument to be supplied to a method.</p>
 *
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 *
 * @see ObjectDescriptor
 * @see InvokeOperation
 * @see SubscribeOperation
 */
public class ArgDescriptor implements Serializable{

  private static final long serialVersionUID = 1L;
  
  long dbid;
  int dbversion;
  String typeName;
  Descriptor descriptor;

  public ArgDescriptor() {
  }

  /**
   * Gets the Descriptor used to construct the value given to the argument.
   */
  public Descriptor getDescriptor() {
    return descriptor;
  }

  /**
   * Sets the Descriptor used to construct the value given to the argument.
   * @param descriptor
   */
  public void setDescriptor(Descriptor descriptor) {
    this.descriptor = descriptor;
  }

  /**
   * Gets the name of the type of this argument if it is defined.
   * If the type name is not defined, the type of the Descriptor is used.
   */
  public String getTypeName() {
    return typeName;
  }

  /**
   * Sets the name of the type of this argument.
   * @param typeName
   */
  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }
}

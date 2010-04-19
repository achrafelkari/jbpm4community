package org.jbpm.pvm.internal.wire.operation;

import java.io.Serializable;

import org.jbpm.pvm.internal.wire.WireContext;

/**
 * any field update or method invocation after the construction of an object.
 */
public interface Operation extends Serializable {

  /**
   * Apply this operation to the specified object, defined in the specified {@link WireContext}.
   * @param target object on which the operation should be performed.
   * @param wireContext context in which the operation is applied.
   */
  void apply(Object target, WireContext wireContext);
}

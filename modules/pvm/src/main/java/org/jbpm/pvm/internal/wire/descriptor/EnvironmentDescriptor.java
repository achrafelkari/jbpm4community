package org.jbpm.pvm.internal.wire.descriptor;

import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.WireContext;

/** parses a descriptor for obtaining a reference to the environment
 * 
 * See schema docs for more details.
 *
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 *
 * @see Descriptor
 */
public class EnvironmentDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;

  public Object construct(WireContext wireContext) {
    return EnvironmentImpl.getCurrent();
  }
}

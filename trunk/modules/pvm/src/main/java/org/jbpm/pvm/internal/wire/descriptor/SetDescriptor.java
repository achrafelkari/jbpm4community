package org.jbpm.pvm.internal.wire.descriptor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbpm.pvm.internal.wire.Descriptor;

/**
*
* <p>This {@link Descriptor} creates a {@link Set}.</p>
*
* <p>If no specific implementation for the {@link Set} is specified, a {@link HashSet} will be used.
*
* <p>Entries can be added during the set initialization.
* The list of entries is specified with {@link #setValueDescriptors(List)}.</p>
*
* @author Tom Baeyens
* @author Guillaume Porcher (documentation)
*
* @see Descriptor
*/
public class SetDescriptor extends CollectionDescriptor {

  private static final long serialVersionUID = 1L;

  public SetDescriptor() {
    super(HashSet.class.getName());
  }
}

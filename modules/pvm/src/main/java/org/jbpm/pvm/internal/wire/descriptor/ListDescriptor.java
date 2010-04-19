package org.jbpm.pvm.internal.wire.descriptor;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.pvm.internal.wire.Descriptor;

/**
 *
 * <p>This {@link Descriptor} creates a {@link List}.</p>
 *
 * <p>If no specific implementation for the {@link List} is specified, an {@link ArrayList} will be used.
 *
 * <p>Entries can be added during the list initialization.
 * The list of entries is specified with {@link #setValueDescriptors(List)}.</p>
 *
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 *
 * @see Descriptor
 */
public class ListDescriptor extends CollectionDescriptor {

  private static final long serialVersionUID = 1L;

  public ListDescriptor() {
    super(ArrayList.class.getName());
  }
}

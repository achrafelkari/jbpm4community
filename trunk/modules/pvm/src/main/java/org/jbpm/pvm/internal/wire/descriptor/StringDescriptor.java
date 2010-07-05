package org.jbpm.pvm.internal.wire.descriptor;

import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.WireContext;

/**
 *
 * <p>This {@link Descriptor} creates a String.</p>
 *
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 *
 * @see Descriptor
 */
public class StringDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;

  String text;

  public StringDescriptor() {
  }

  public StringDescriptor(String value) {
    setValue(value);
  }

  public StringDescriptor(String name, String value) {
    setName(name);
    setValue(value);
  }

  public Object construct(WireContext factory) {
    return text;
  }

  public void setValue(String value) {
    this.text = value;
  }
}


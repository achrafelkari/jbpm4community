package org.jbpm.pvm.internal.wire.descriptor;

import org.jbpm.api.JbpmException;
import org.jbpm.pvm.internal.util.ReflectUtil;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.WireContext;

/**
 * <p>This {@link Descriptor} specifies a reference to an object.
 * The object referenced should be declared somewhere else in the wireContext.</p>
 *
 * <p>The constructed object is the referenced object.</p>
 *
 * <p>The {@link #init} field can be used to force initialization of the referenced object.</p>
 *
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 */
public class ReferenceDescriptor extends AbstractDescriptor implements Descriptor {

  private static final long serialVersionUID = 1L;

  String text = null;
  String type = null;

  // TODO add a refExpression that is evaluated with el
  // the base referenced descriptor always should have delayedInitialization = false;

  public ReferenceDescriptor() {
  }

  public ReferenceDescriptor(String objectName) {
    setValue(objectName);
  }

  public Object construct(WireContext wireContext) {
    if (text!=null) {
      return wireContext.get(text, isDelayedInitializationAllowed());
    } else if (type!=null) {
      try {
        Class<?> clazz = ReflectUtil.classForName(type);
        return wireContext.get(clazz);
      } catch (Exception e) {
        throw new JbpmException("couldn't load "+type, e);
      }
    }
    return null;
  }

  public boolean isDelayedInitializationAllowed() {
    return (init == INIT_EAGER || init == INIT_LAZY);
  }
  public void setValue(String objectName) {
    this.text = objectName;
  }
  public void setType(String type) {
    this.type = type;
  }
}

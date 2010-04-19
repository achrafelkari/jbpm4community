package org.jbpm.pvm.internal.wire.operation;

import java.lang.reflect.Method;

import org.jbpm.pvm.internal.util.ReflectUtil;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireException;

/**
 * injects another object with a setter method.
 *
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 *
 */
public class PropertyOperation extends AbstractOperation {

  private static final long serialVersionUID = 1L;

  String setterName = null;
  /* the method will be searched by reflection on the runtime value */
  Descriptor descriptor = null;

  public void apply(Object target, WireContext wireContext) {
    // create the value to assign to the property
    Object value = wireContext.create(descriptor, true);
    Method method = null;
    Class<?> clazz = target.getClass();
    Object[] args = new Object[]{value};
    method = ReflectUtil.findMethod(clazz, setterName, null, args);
    if (method==null) {
      throw new WireException("couldn't find property setter "+setterName+" for value "+value);
    }
    ReflectUtil.invoke(method, target, args);
  }

  /**
   * Sets the name of the property that should be updated by this operation.
   * If propertyName is <code>foo</code>, the method used to set the value will be <code>setFoo</code>.
   * @param propertyName
   */
  public void setPropertyName(String propertyName) {
    this.setterName = "set"+propertyName.substring(0,1).toUpperCase()+propertyName.substring(1);
  }

  /**
   * Gets the name of the setter method used to inject the property value.
   * @return name of the setter method used to inject the property value.
   */
  public String getSetterName() {
    return setterName;
  }

  /**
   * Sets the name of the setter method to use to inject the property value.
   * @param setterName
   */
  public void setSetterName(String setterName) {
    this.setterName = setterName;
  }

  /**
   * Gets the descriptor used to create the field's value.
   */
  public Descriptor getDescriptor() {
    return descriptor;
  }

  /**
   * Sets the descriptor used to create the field's value
   * @param valueDescriptor
   */
  public void setDescriptor(Descriptor valueDescriptor) {
    this.descriptor = valueDescriptor;
  }
}

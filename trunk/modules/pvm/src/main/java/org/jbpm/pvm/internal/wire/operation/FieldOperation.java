package org.jbpm.pvm.internal.wire.operation;

import java.lang.reflect.Field;

import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.util.ReflectUtil;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireException;


/**
 * injects another object into a field.
 *
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 *
 */
public class FieldOperation extends AbstractOperation {

  private static final long serialVersionUID = 1L;

  String fieldName = null;
  Descriptor descriptor = null;

  transient Field field = null;

  public FieldOperation() {
  }

  public void apply(Object target, WireContext wireContext) {
    if (target!=null) {

      // Get field
      synchronized(this) {
        if (field==null) {
          Class<?> clazz = target.getClass();
          field = ReflectUtil.findField(clazz, fieldName);
        }
      }

      // Create value
      Object value = wireContext.create(descriptor, true);
      // Set the field value
      try {
        ReflectUtil.set(field, target, value);
      } catch (Exception e) {
        throw new WireException("couldn't set "+fieldName+" to "+value, e);
      }
    }
  }

  /**
   * Gets the name of the field that should be updated by this operation.
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * Sets the name of the field that should be updated by this operation.
   * @param fieldName
   */
  public synchronized void setFieldName(String fieldName) {
    this.fieldName = fieldName;
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
  public synchronized void setDescriptor(Descriptor valueDescriptor) {
    this.descriptor = valueDescriptor;
  }

  private static Log log = Log.getLog(FieldOperation.class.getName());
}

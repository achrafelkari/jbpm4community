package org.jbpm.pvm.internal.wire.descriptor;

import org.jbpm.pvm.internal.util.ReflectUtil;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireException;

/** loads the class with the specified class name using the WireContext class loader.
 *
 * @see WireContext#getClassLoader()
 *
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 */
public class ClassDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;

  String className;

  /** loads the class from the class loader of the specified WireContext.
   * @throws WireException if the class could not be loaded.
   */
  public Object construct(WireContext wireContext) {
    try {
      return ReflectUtil.classForName(className);
    } catch (ClassNotFoundException e) {
      throw new WireException("could not find class: "+className, e);
    }
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public void setClass(Class<?> clazz) {
    className = clazz != null ? clazz.getName() : null;
  }
}

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

  String text;

  /** loads the class from the class loader of the specified WireContext.
   * @throws WireException if the class could not be loaded.
   */
  public Object construct(WireContext wireContext) {
    try {
      return ReflectUtil.classForName(text);
    } catch (Exception e) {
      Throwable cause = (e.getCause()!=null ? e.getCause() : e);
      throw new WireException("couldn't load class '"+text+"': "+cause.getMessage(), cause);
    }
  }

  public void setClassName(String className) {
    this.text = className;
  }

  public void setClass(Class<?> clazz) {
    if (clazz==null) {
      text = null;
    } else {
      this.text = clazz.getName();
    }
  }
}

package org.jbpm.pvm.internal.wire.operation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jbpm.pvm.internal.util.ReflectUtil;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireException;
import org.jbpm.pvm.internal.wire.descriptor.ArgDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;

/**
 * invokes a method.
 *
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 * @see ArgDescriptor
 * @see ObjectDescriptor
 */

public class InvokeOperation extends AbstractOperation {

  private static final long serialVersionUID = 1L;

  /** name of the method to invoke. */
  String methodName = null;
  /** list of descriptors for creating arguments supplied to the method */
  List<ArgDescriptor> argDescriptors = null;

  public void apply(Object target, WireContext wireContext) {
    try {
      Object[] args = ObjectDescriptor.getArgs(wireContext, argDescriptors);
      Class<?> clazz = target.getClass();
      Method method = ReflectUtil.findMethod(clazz, methodName, argDescriptors, args);
      if (method==null) {
        throw new WireException("method "+ReflectUtil.getSignature(methodName, argDescriptors, args)+" unavailable");
      }
      ReflectUtil.invoke(method, target, args);
    } catch (WireException e) {
      throw e;
    } catch (Exception e) {
      throw new WireException("couldn't invoke method "+methodName+": "+e.getMessage(), e);
    }
  }

  /**
   * Adds a descriptor to the list of arguments descriptors.
   */
  public void addArgDescriptor(ArgDescriptor argDescriptor) {
    if (argDescriptors==null) {
      argDescriptors = new ArrayList<ArgDescriptor>();
    }
    argDescriptors.add(argDescriptor);
  }

  /**
   * Gets the name of the method to invoke.
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * Sets the name of the method to invoke.
   * @param methodName the name of the method to invoke.
   */
  public synchronized void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  /**
   * Gets the list of descriptor to create arguments supplied to the method .
   */
  public List<ArgDescriptor> getArgDescriptors() {
    return argDescriptors;
  }

  /**
   * Sets the list of descriptor to create arguments supplied to the method .
   */
  public void setArgDescriptors(List<ArgDescriptor> argDescriptors) {
    this.argDescriptors = argDescriptors;
  }
}

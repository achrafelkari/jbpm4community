package org.jbpm.pvm.internal.wire.operation;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.util.Listener;
import org.jbpm.pvm.internal.util.Observable;
import org.jbpm.pvm.internal.util.ReflectUtil;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireException;
import org.jbpm.pvm.internal.wire.descriptor.ArgDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;


/**
 * Wrapper for the subscribe operation.
 * This class will be used to call a specified method on reception of an event.
 * This class is used so that a non {@link Listener} class can subscribe to an {@link Observable} object.
 *
 * @see SubscribeOperation
 *
 * @author Tom Baeyens
 * @author Guillaume Porcher (Documentation)
 */
public class MethodInvokerListener implements Listener, Serializable {

  private static final long serialVersionUID = 1L;
  private static Log log = Log.getLog(MethodInvokerListener.class.getName());

  String methodName;
  List<ArgDescriptor> argDescriptors = null;
  WireContext wireContext;
  Object target;

  transient Method method = null;

  /**
   * Creates a new Wrapper.
   * When an event is received, the arguments <code>args</code> are created from the list <code>argDescriptors</code>, and <code>target.methodName(args)</code> is called.
   * @param methodName name of the method to call when an event is received.
   * @param argDescriptors list of descriptors of arguments given to the method.
   * @param wireContext context to use to create the arguments
   * @param target object on which the method will be called.
   */
  public MethodInvokerListener(String methodName, List<ArgDescriptor> argDescriptors, WireContext wireContext, Object target) {
    this.methodName = methodName;
    this.argDescriptors = argDescriptors;
    this.wireContext = wireContext;
    this.target = target;
  }

  public void event(Object source, String eventName, Object info) {
    log.debug("invoking "+methodName+" on "+target+" for event "+eventName);
    try {
      Object[] args = ObjectDescriptor.getArgs(wireContext, argDescriptors);
      Class<?> clazz = target.getClass();
      Method method = ReflectUtil.findMethod(clazz, methodName, argDescriptors, args);
      if(method == null) {
        throw new WireException("method "+ReflectUtil.getSignature(methodName, argDescriptors, args)+" unavailable for "+target);
      }
      ReflectUtil.invoke(method, target, args);
    } catch (WireException e) {
      throw e;
    } catch (Exception e) {
      throw new WireException("couldn't invoke listener method "+methodName+": "+e.getMessage(), e);
    }
  }
}

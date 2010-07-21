package org.jbpm.pvm.internal.wire.descriptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jbpm.api.JbpmException;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.util.ReflectUtil;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.JbpmClassNotFoundException;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireDefinition;
import org.jbpm.pvm.internal.wire.WireException;
import org.jbpm.pvm.internal.wire.operation.FieldOperation;
import org.jbpm.pvm.internal.wire.operation.Operation;
import org.jbpm.pvm.internal.wire.operation.PropertyOperation;

/**
 * <p>This {@link Descriptor} creates and initializes an object.
 * Objects can be instantiated from a constructor or from a method invocation.</p>
 *
 * <p>The way to create an object is specified one of these methods (see <a href='#createTime'>creating objects</a>):
 * <ul>
 * <li>className ({@link #setClassName(String)})</li>
 * <li>factoryObjectName ({@link #setFactoryObjectName(String)})</li>
 * <li>factoryDescriptor ({@link #setFactoryDescriptor(Descriptor)})</li>
 * </ul>
 * Only one of these methods can be used.
 * </p>
 *
 * <h3 id='createTime'>Creating objects</h3>
 * <h4>Creating object from a constructor</h4>
 *
 * <p>This method is used when <code>{@link #getClassName()}!=null && {@link #getMethodName()}==null</code>.</p>
 *
 * <p>The {@link #construct(WireContext)} method creates a new object
 * from a constructor matching the given arguments
 * (specified with {@link #setArgDescriptors(List)}).</p>
 *
 *
 * <h4>Creating an object from a method invocation</h4>
 *
 * <p>The name of the method to call is specified by the method attribute.</p>
 * <ul>
 * <li>If the method is <i>static</i>, the related class is {@link #getClassName()}.</li>
 * <li>If the method is an object method, the object to which the method will be applied is defined by:
 *  <ul>
 *    <li>If <code>{@link #getFactoryObjectName()}!=null</code>: the object with the name factoryObjectName will be fetched from the context.</li>
 *    <li>if <code>{@link #getFactoryDescriptor()}!=null</code>: the object will be created from the factory descriptor.</li>
 *  </ul>
 * </li>
 * </ul>
 * <p>The object returned by {@link #construct(WireContext)} is the object returned by the method invocation.</p>
 *
 *
 * <h3>Initializing Objects</h3>
 * <h4>Auto Wiring</h4>
 * <p>If the auto wiring is enabled for the object (<code>{@link #isAutoWireEnabled()}==true</code>),
 * the WireContext will try to look for objects with the same name as the fields in the class.
 * If it finds an object with that name, and if it is assignable to the field's type, it is automatically injected,
 * without the need for explicit {@link FieldOperation} that specifies the injection in the wiring xml.</p>
 * <p>If the auto wiring is enabled and the WireContext finds an object with the name of a field, but not assignable to this field,
 * a warning message is generated.</p>
 *
 * <p>Auto-wiring is disabled by default.</p>
 *
 * <h4>Operations</h4>
 * <p>Field injection or property injection are done after the auto-wiring. For more information, see {@link Operation}.</p>
 *
 * <p>If a field was injected by auto-wiring, its value can be overridden by specifying
 *  a {@link FieldOperation} or {@link PropertyOperation} operation.</p>
 *
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 *
 */
public class ObjectDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;
  private static Log log = Log.getLog(ObjectDescriptor.class.getName());

  protected String className;

  /** specifies the object reference on which the method will be invoked.
   * Either className, objectName or a descriptor has to be specified.
   *
   * TODO check if this member can be replaced by a RefDescriptor in the factoryDescriptor member.
   *
   * */
  String factoryObjectName;

  protected Expression expression;

  /** specifies the object on which to invoke the method.
   * Either className, objectName or a descriptor has to be specified. */
  protected Descriptor factoryDescriptor;

  protected String methodName;

  /** map to db as a component */
  protected List<ArgDescriptor> argDescriptors;
  /** list of operations to perform during initialization. */
  protected List<Operation> operations;

  /** True if autowiring is enabled.  */
  protected boolean isAutoWireEnabled;

  public ObjectDescriptor() {
  }

  public ObjectDescriptor(String className) {
    this.className = className;
  }

  public ObjectDescriptor(Class<?> clazz) {
    this.className = clazz.getName();
  }

  /**
   * This method constructs a new Object from the ObjectDefinition.
   * This object will be created from a class constructor or from a method invocation.
   * @throws WireException one of the following exception occurred:
   * <ul><li>if the object cannot be created (unable to load the specified class or no matching constructor found)</li>
   * <li>if the invocation of the specified method failed</li>
   * </ul>
   * @see ObjectDescriptor
   */
  public Object construct(WireContext wireContext) {
    Object object = null;
    Class<?> clazz = null;

    if (className!=null) {
      try {
        clazz = ReflectUtil.classForName(className);
      } catch (Exception e) {
        throw new JbpmClassNotFoundException("couldn't load class "+className, e);
      }

      if (methodName==null) {
        // plain instantiation
        try {
          Object[] args = getArgs(wireContext, argDescriptors);
          Constructor<?> constructor = ReflectUtil.findConstructor(clazz, argDescriptors, args);
          if (constructor==null) {
            throw new WireException("couldn't find constructor "+clazz.getName()+" with args "+Arrays.toString(args));
          }
          object = constructor.newInstance(args);
        } catch (WireException e) {
          throw e;
        } catch (Exception e) {
          throw new WireException("couldn't create object '"+(name!=null ? name : className)+"': "+e.getMessage(), e);
        }
      }

    } else if (factoryObjectName!=null) {
      // referenced factory object
      object = wireContext.get(factoryObjectName);
      if (object==null) {
        throw new WireException("can't invoke method '"+methodName+"' on null, resulted from fetching object '"+factoryObjectName+"' from this wiring environment");
      }

    } else if (factoryDescriptor!=null) {
      // factory object descriptor
      object = wireContext.create(factoryDescriptor, false);
      if (object==null) {
        throw new WireException("created factory object is null, can't invoke method '"+methodName+"' on it");
      }
    } else if (expression!=null) {
      object = expression.evaluateInScope(wireContext.getScopeInstance());
    }

    if (methodName!=null) {
      try {
        object = invokeMethod(methodName, argDescriptors, wireContext, object, clazz);

      } catch (WireException e) {
        throw e;
      } catch (Exception e) {
        throw new WireException("couldn't invoke factory method "+methodName+": "+e.getMessage(), e);
      }
    }

    return object;
  }

  public static Object invokeMethod(String methodName, List<ArgDescriptor> argDescriptors, WireContext wireContext, Object object, Class< ? > clazz) throws Exception {
    // method invocation on object or static method invocation in case object is null
    if (object!=null) {
      clazz = object.getClass();
    }
    Object[] args = ObjectDescriptor.getArgs(wireContext, argDescriptors);
    Method method = ReflectUtil.findMethod(clazz, methodName, argDescriptors, args);
    if (method==null) {
      // throw exception but first, generate decent message
      throw new WireException("method "+ReflectUtil.getSignature(methodName, argDescriptors, args)+" is not available on "+
          (object!=null ? "object "+object+" ("+clazz.getName()+")" : "class "+clazz.getName()));
    }
    if (object == null && (!Modifier.isStatic(method.getModifiers()))) {
      // A non static method is invoked on a null object
      throw new WireException("method "+ clazz.getName() + "." + ReflectUtil.getSignature(methodName, argDescriptors, args)+" is not static. It cannot be called on a null object.");
    }
    object = ReflectUtil.invoke(method, object, args);
    return object;
  }

  /**
   * Initializes the specified object.
   * If auto-wiring was set to <code>true</code>, auto-wiring is performed (see {@link #autoWire(Object, WireContext)}). Fields and properties injections are then performed.
   *
   */
  public void initialize(Object object, WireContext wireContext) {
    try {
      // specified operations takes precedence over auto-wiring.
      // e.g. in case there is a collision between
      // a field or property injection and an autowired value,
      // the field or property injections should win.
      // That is why autowiring is done first
      if (isAutoWireEnabled) {
        autoWire(object, wireContext);
      }
      if (operations!=null) {
        for(Operation operation: operations) {
          operation.apply(object, wireContext);
        }
      }
    } catch (Exception e) {
      throw new WireException("couldn't initialize object '"+(name!=null ? name : className)+"': "+e.getMessage(), e);
    }
  }

  public Class<?> getType(WireDefinition wireDefinition) {
    if (className!=null) {
      try {
        return ReflectUtil.classForName(className);
      } catch (Exception e) {
        throw new WireException("couldn't load class '"+className+"'", e);
      }
    }

    Descriptor descriptor = null;
    if (factoryDescriptor!=null) {
      descriptor = factoryDescriptor;
    } else if (factoryObjectName!=null) {
      descriptor = wireDefinition.getDescriptor(factoryObjectName);
    }

    if (descriptor!=null) {
      Class<?> factoryClass = descriptor.getType(wireDefinition);
      if (factoryClass!=null) {
        Method method = ReflectUtil.findMethod(factoryClass, methodName, argDescriptors, null);
        if (method!=null) {
          return method.getReturnType();
        }
      }
    }

    return null;
  }

  /**
   * Auto wire object present in the context and the specified object's fields.
   * @param object object on which auto-wiring is performed.
   * @param wireContext context in which the wiring objects are searched.
   */
  protected void autoWire(Object object, WireContext wireContext) {
    Class<?> clazz = object.getClass();
    while (clazz!=null) {
      Field[] declaredFields = clazz.getDeclaredFields();
      if (declaredFields!=null) {
        for (Field field: declaredFields) {
          if (! Modifier.isStatic(field.getModifiers())) {
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();

            Object autoWireValue = null;
            if ("environment".equals(fieldName)) {
              autoWireValue = EnvironmentImpl.getCurrent();

            } else if ( ("context".equals(fieldName))
                || ("wireContext".equals(fieldName))
            ) {
              autoWireValue = wireContext;

            } else if (wireContext.has(fieldName)) {
              autoWireValue = wireContext.get(fieldName);

            } else {
              autoWireValue = wireContext.get(fieldType);
            }
            // if auto wire value has not been found in current context,
            // search in environment
            if (autoWireValue == null) {
              EnvironmentImpl currentEnvironment = EnvironmentImpl.getCurrent();
              if (currentEnvironment != null) {
                autoWireValue = currentEnvironment.get(fieldName);
                if (autoWireValue == null) {
                  autoWireValue = currentEnvironment.get(fieldType);
                }
              }
            }

            if (autoWireValue!=null) {
              try {
                if (log.isTraceEnabled()) log.trace("auto wiring field "+fieldName+" in "+name);
                ReflectUtil.set(field, object, autoWireValue);
              } catch (JbpmException e) {
                if(e.getCause() instanceof IllegalArgumentException) {
                  log.info("WARNING: couldn't auto wire "+fieldName+" (of type "+fieldType.getName()+") " +
                      "with value "+autoWireValue + " (of type "+autoWireValue.getClass().getName()+")");
                } else {
                  log.info("WARNING: couldn't auto wire "+fieldName+" with value "+autoWireValue);
                }
              }
            }
          }
        }
      }
      clazz = clazz.getSuperclass();
    }
  }

  /**
   * Creates a list of arguments (objects) from a list of argument descriptors.
   * @param wireContext context used to create objects.
   * @param argDescriptors list of argument descriptors.
   * @return a list of object created from the descriptors.
   * @throws Exception
   */
  public static Object[] getArgs(WireContext wireContext, List<ArgDescriptor> argDescriptors) throws Exception {
    Object[] args = null;
    if (argDescriptors!=null) {
      args = new Object[argDescriptors.size()];
      for(int i=0; i<argDescriptors.size(); i++) {
        ArgDescriptor argDescriptor = argDescriptors.get(i);
        Object arg;
        try {
          arg = wireContext.create(argDescriptor.getDescriptor(), true);
          args[i] = arg;
        } catch (RuntimeException e) {
          // i have made sure that the runtime exception is caught everywhere the getArgs method
          // is used so that a better descriptive exception can be rethrown
          throw new Exception("couldn't create argument "+i+": "+e.getMessage(), e);
        }
      }
    }
    return args;
  }

  /**
   * Adds a argument descriptor to the list of arguments descriptor to used when invoking the specified method.
   * @param argDescriptor argument descriptor to add.
   */
  public void addArgDescriptor(ArgDescriptor argDescriptor) {
    if (argDescriptors==null) {
      argDescriptors = new ArrayList<ArgDescriptor>();
    }
    argDescriptors.add(argDescriptor);
  }

  /**
   * Adds an operation to perform during initialization.
   * @param operation operation to add.
   */
  public void addOperation(Operation operation) {
    if (operations==null) {
      operations = new ArrayList<Operation>();
    }
    operations.add(operation);
  }

  /** convenience method to add a type based field injection */
  public void addTypedInjection(String fieldName, Class<?> type) {
    addInjection(fieldName, new EnvDescriptor(type));
  }

  /** add a field injection based on a descriptor */
  public void addInjection(String fieldName, Descriptor descriptor) {
    FieldOperation injectionOperation = new FieldOperation();
    injectionOperation.setFieldName(fieldName);
    injectionOperation.setDescriptor(descriptor);
    addOperation(injectionOperation);
  }

  /** add a property injection based on a descriptor */
  public void addPropertyInjection(String propertyName,
      Descriptor valueDescriptor) {
    PropertyOperation operation = new PropertyOperation();
    operation.setPropertyName(propertyName);
    operation.setDescriptor(valueDescriptor);
    addOperation(operation);
  }


  // getters and setters //////////////////////////////////////////////////////

  public String getClassName() {
    return className;
  }
  public void setClassName(String className) {
    this.className = className;
  }
  public List<ArgDescriptor> getArgDescriptors() {
    return argDescriptors;
  }
  public void setArgDescriptors(List<ArgDescriptor> argDescriptors) {
    this.argDescriptors = argDescriptors;
  }
  public List<Operation> getOperations() {
    return operations;
  }
  public void setOperations(List<Operation> operations) {
    this.operations = operations;
  }
  public Descriptor getFactoryDescriptor() {
    return factoryDescriptor;
  }
  public void setFactoryDescriptor(Descriptor factoryDescriptor) {
    this.factoryDescriptor = factoryDescriptor;
  }
  public String getFactoryObjectName() {
    return factoryObjectName;
  }
  public void setFactoryObjectName(String factoryObjectName) {
    this.factoryObjectName = factoryObjectName;
  }
  public String getMethodName() {
    return methodName;
  }
  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }
  public boolean isAutoWireEnabled() {
    return isAutoWireEnabled;
  }
  public void setAutoWireEnabled(boolean isAutoWireEnabled) {
    this.isAutoWireEnabled = isAutoWireEnabled;
  }
  public Expression getExpression() {
    return expression;
  }
  public void setExpression(Expression expr) {
    this.expression = expr;
  }
}

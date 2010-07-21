package org.jbpm.pvm.internal.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.api.JbpmException;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;
import org.jbpm.pvm.internal.model.ScopeInstanceImpl;
import org.jbpm.pvm.internal.repository.DeploymentClassLoader;
import org.jbpm.pvm.internal.repository.RepositoryCache;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.descriptor.ArgDescriptor;

public class ReflectUtil {

  private ReflectUtil() {
    // hide default constructor to prevent instantiation
  }

  private static final Log log = Log.getLog(ReflectUtil.class.getName());

  /**
   * Maps wrapper <code>Class</code>es to their corresponding primitive types.
   */
  private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap = createWrapperPrimitiveMap();

  private static Map<Class<?>, Class<?>> createWrapperPrimitiveMap() {
    Map<Class<?>, Class<?>> map = new HashMap<Class<?>, Class<?>>();
    map.put(Boolean.class, boolean.class);
    map.put(Byte.class, byte.class);
    map.put(Character.class, char.class);
    map.put(Short.class, short.class);
    map.put(Integer.class, int.class);
    map.put(Long.class, long.class);
    map.put(Double.class, double.class);
    map.put(Float.class, float.class);
    return map;
  }

  /** searches for the field in the given class and in its super classes */
  public static Field findField(Class<?> clazz, String fieldName) {
    return findField(clazz, fieldName, clazz);
  }

  private static Field findField(Class<?> clazz, String fieldName, Class<?> original) {
    Field field = null;

    try {
      field = clazz.getDeclaredField(fieldName);
      if (log.isTraceEnabled()) log.trace("found field "+fieldName+" in "+clazz.getName());
    } catch (NoSuchFieldException e) {
      if (clazz.getSuperclass()!=null) {
        return findField(clazz.getSuperclass(), fieldName, original);
      } else {
        throw new JbpmException("couldn't find field '"+original.getName()+"."+fieldName+"'", e);
      }
    }

    return field;
  }

  /** searches for the method in the given class and in its super classes */
  public static Method getMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
    return getMethod(clazz, methodName, parameterTypes, clazz);
  }

  private static Method getMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Class<?> original) {
    Method method = null;

    try {
      method = clazz.getDeclaredMethod(methodName, parameterTypes);

      if (log.isTraceEnabled()) log.trace("found method "+clazz.getName()+"."+methodName+"("+Arrays.toString(parameterTypes)+")");

    } catch (NoSuchMethodException e) {
      if (clazz.getSuperclass()!=null) {
        return getMethod(clazz.getSuperclass(), methodName, parameterTypes, original);
      } else {
        throw new JbpmException("couldn't find method '"+original.getName()+"."+methodName+"("+getParameterTypesText(parameterTypes)+")'", e);
      }
    }

    return method;
  }

  private static String getParameterTypesText(Class<?>[] parameterTypes) {
    if (parameterTypes==null) return "";
    StringBuilder parameterTypesText = new StringBuilder();
    for (int i=0; i<parameterTypes.length; i++) {
      Class<?> parameterType = parameterTypes[i];
      parameterTypesText.append(parameterType.getName());
      if (i!=parameterTypes.length-1) {
        parameterTypesText.append(", ");
      }
    }
    return parameterTypesText.toString();
  }

  public static <T> T newInstance(Class<T> clazz) {
    if (clazz==null) {
      throw new IllegalArgumentException("cannot create new instance without class");
    }
    try {
      return newInstance(clazz.getConstructor());
    }
    catch (NoSuchMethodException e) {
      throw new IllegalArgumentException("cannot instantiate class without default constructor");
    }
  }

  public static <T> T newInstance(Constructor<T> constructor, Object... args) {
    if (constructor==null) {
      throw new IllegalArgumentException("cannot create new instance without constructor");
    }

    Class<T> clazz = constructor.getDeclaringClass();
    if (log.isTraceEnabled()) log.trace("creating new instance for "+clazz+" with args "+Arrays.toString(args));
    if (!constructor.isAccessible()) {
      if (log.isTraceEnabled()) log.trace("making constructor accessible");
      constructor.setAccessible(true);
    }
    try {
      return constructor.newInstance(args);
    }
    catch (InstantiationException e) {
      throw new JbpmException("failed to instantiate " + clazz, e);
    }
    catch (IllegalAccessException e) {
      throw new JbpmException(ReflectUtil.class + " has no access to " + constructor, e);
    }
    catch (InvocationTargetException e) {
      throw new JbpmException(constructor + " threw exception", e.getCause());
    }
  }

  public static Object get(Field field, Object object) {
    if (field==null) {
      throw new NullPointerException("field is null");
    }
    try {
      Object value = field.get(object);
      if (log.isTraceEnabled()) log.trace("got value '"+value+"' from field '"+field.getName()+"'");
      return value;
    } catch (Exception e) {
      throw new JbpmException("couldn't get '"+field.getName()+"'", e);
    }
  }

  public static void set(Field field, Object object, Object value) {
    if (field==null) {
      throw new NullPointerException("field is null");
    }
    try {
      if (log.isTraceEnabled()) log.trace("setting field '"+field.getName()+"' to value '"+value+"'");
      if (!field.isAccessible()) {
        if (log.isTraceEnabled()) log.trace("making field accessible");
        field.setAccessible(true);
      }
      field.set(object, value);
    } catch (Exception e) {
      throw new JbpmException("couldn't set '"+field.getName()+"' to '"+value+"'", e);
    }
  }

  public static Object invoke(Method method, Object target, Object[] args) {
    if (method==null) {
      throw new JbpmException("method is null");
    }
    try {
      if (log.isTraceEnabled()) log.trace("invoking '"+method.getName()+"' on '"+target+"' with "+Arrays.toString(args));
      if (!method.isAccessible()) {
        log.trace("making method accessible");
        method.setAccessible(true);
      }
      return method.invoke(target, args);
    } catch (InvocationTargetException e) {
      Throwable targetException = e.getTargetException();
      throw new JbpmException("couldn't invoke '"+method.getName()+"' with "+Arrays.toString(args)+" on "+target+": "+targetException.getMessage(), targetException);
    } catch (Exception e) {
      throw new JbpmException("couldn't invoke '"+method.getName()+"' with "+Arrays.toString(args)+" on "+target+": "+e.getMessage(), e);
    }
  }

  public static Method findMethod(Class<?> clazz, String methodName, List<ArgDescriptor> argDescriptors, Object[] args) {
    if (log.isTraceEnabled()) log.trace("searching for method "+methodName+" in "+clazz.getName());
    Method[] candidates = clazz.getDeclaredMethods();
    for (int i=0; i<candidates.length; i++) {
      Method candidate = candidates[i];
      if ( (candidate.getName().equals(methodName))
           && (isArgumentMatch(candidate.getParameterTypes(), argDescriptors, args))
         ) {

        if (log.isTraceEnabled()) {
          if (log.isTraceEnabled()) log.trace("found matching method "+clazz.getName()+"."+methodName);
        }

        return candidate;
      }
    }
    if (clazz.getSuperclass()!=null) {
      return findMethod(clazz.getSuperclass(), methodName, argDescriptors, args);
    }
    return null;
  }

  public static Constructor<?> findConstructor(Class<?> clazz, List<ArgDescriptor> argDescriptors, Object[] args) {
    Constructor<?>[] constructors = clazz.getDeclaredConstructors();
    for (int i=0; i<constructors.length; i++) {
      if (isArgumentMatch(constructors[i].getParameterTypes(), argDescriptors, args)) {
        return constructors[i];
      }
    }
    return null;
  }

  public static boolean isArgumentMatch(Class<?>[] parameterTypes, List<ArgDescriptor> argDescriptors, Object[] args) {
    int nbrOfArgs = args!=null ? args.length : 0;
    int nbrOfParameterTypes = parameterTypes!=null ? parameterTypes.length : 0;

    if (nbrOfArgs!=nbrOfParameterTypes) {
      return false;
    }

    if (nbrOfArgs==0) {
      return true;
    }

    for (int i=0; i<parameterTypes.length; i++) {
      Class<?> parameterType = parameterTypes[i];
      String argTypeName;
      if (argDescriptors == null || (argTypeName = argDescriptors.get(i).getTypeName()) == null) {
        Object arg = args[i];
        if (!isAssignable(parameterType, arg)) {
          return false;
        }
      }
      else if (!parameterType.getName().equals(argTypeName)) {
        return false;
      }
    }
    return true;
  }

  /**
   * <p>
   * Checks if the given <code>value</code> can be assigned to a variable of the specified
   * <code>type</code>.
   * </p>
   * <p>
   * Unlike the {@link Class#isAssignableFrom(Class)} method, this method takes into
   * account widenings of primitive types and <code>null</code>s.
   * </p>
   * <p>
   * Primitive widenings allow an int to be assigned to a long, float or double. This method
   * returns the correct result for these cases.
   * </p>
   * <p>
   * <code>null</code> may be assigned to any reference type. This method will return
   * <code>true</code> if <code>null</code> is passed in and the specified <code>type</code> is
   * a reference type.
   * </p>
   * <p>
   * Specifically, this method tests whether the class of the given <code>value</code> parameter
   * can be converted to the type represented by the specified <code>Class</code> via an
   * identity, widening primitive or widening reference conversion. See the
   * <a href="http://java.sun.com/docs/books/jls/">Java Language Specification</a>,
   * sections 5.1.1, 5.1.2 and 5.1.4 for details.
   * </p>
   * @param type the Class to try to assign into
   * @param value the object to check, may be <code>null</code>
   * @return <code>true</code> if assignment is possible
   * @see <a
   *      href="http://commons.apache.org/lang/api-release/org/apache/commons/lang/ClassUtils.html#isAssignable(java.lang.Class,%20java.lang.Class)"
   *      >ClassUtils.isAssignable()</a>
   */
  private static boolean isAssignable(Class<?> type, Object value) {
    // check for null value
    if (value == null) {
      // null is assignable to reference types
      return !type.isPrimitive();
    }

    if (type.isPrimitive()) {
      // unboxing
      Class<?> valueType = wrapperToPrimitive(value.getClass());
      if (null == valueType) {
        return false;
      }
      if (type == valueType) {
        return true;
      }
      // widening primitive conversion
      if (int.class == valueType) {
        return long.class == type || float.class == type || double.class == type;
      }
      if (long.class == valueType) {
        return float.class == type || double.class == type;
      }
      if (boolean.class == valueType) {
        return false;
      }
      if (double.class == valueType) {
        return false;
      }
      if (float.class == valueType) {
        return double.class == type;
      }
      if (char.class == valueType) {
        return int.class == type || long.class == type || float.class == type
          || double.class == type;
      }
      if (short.class == valueType) {
        return int.class == type || long.class == type || float.class == type
          || double.class == type;
      }
      if (byte.class == valueType) {
        return short.class == type || int.class == type || long.class == type
          || float.class == type || double.class == type;
      }
      // should never get here
      return false;
    }

    return type.isInstance(value);
  }

  /**
   * <p>
   * Converts the specified wrapper class to its corresponding primitive class.
   * </p>
   * <p>
   * If the passed in class is a wrapper class for a primitive type, this primitive type will be
   * returned (e.g. <code>Integer.TYPE</code> for <code>Integer.class</code>). For other
   * classes, or if the parameter is <code>null</code>, the return value is <code>null</code>.
   * </p>
   * @param cls the class to convert, may be <code>null</code>
   * @return the corresponding primitive type if <code>cls</code> is a wrapper class,
   *         <code>null</code> otherwise
   * @see <a
   *      href="http://commons.apache.org/lang/api-release/org/apache/commons/lang/ClassUtils.html#wrapperToPrimitive(java.lang.Class)"
   *      >ClassUtils.wrapperToPrimitive</a>
   */
  private static Class<?> wrapperToPrimitive(Class<?> cls) {
    return wrapperPrimitiveMap.get(cls);
  }

  public static String getSignature(String methodName, List<ArgDescriptor> argDescriptors, Object[] args) {
    String signature = methodName+"(";
    if (args!=null) {
      for (int i=0; i<args.length; i++) {
        String argType = null;
        if (argDescriptors!=null) {
          ArgDescriptor argDescriptor = argDescriptors.get(i);
          if ( (argDescriptor!=null)
               && (argDescriptor.getTypeName()!=null)
             ) {
            argType = argDescriptor.getTypeName();
          }
        }
        if ( (argType==null)
             && (args[i]!=null)
           ) {
          argType = args[i].getClass().getName();
        }
        signature += argType;
        if (i<(args.length-1)) {
          signature += ", ";
        }
      }
    }
    signature+=")";
    return signature;
  }

  public static ClassLoader installDeploymentClassLoader(ProcessDefinitionImpl processDefinition) {
    String deploymentId = processDefinition.getDeploymentId();
    if (deploymentId==null) {
      return null;
    }

    Thread currentThread = Thread.currentThread();
    ClassLoader original = currentThread.getContextClassLoader();

    RepositoryCache repositoryCache = EnvironmentImpl.getFromCurrent(RepositoryCache.class);
    DeploymentClassLoader deploymentClassLoader = repositoryCache.getDeploymentClassLoader(deploymentId, original);
    if (deploymentClassLoader==null) {
      deploymentClassLoader = new DeploymentClassLoader(original, deploymentId);
      repositoryCache.setDeploymentClassLoader(deploymentId, original, deploymentClassLoader);
    }

    currentThread.setContextClassLoader(deploymentClassLoader);

    return original;
  }

  public static void uninstallDeploymentClassLoader(ClassLoader original) {
    if (original!=null) {
      Thread.currentThread().setContextClassLoader(original);
    }
  }

  public static Object instantiateUserCode(Descriptor descriptor, ProcessDefinitionImpl processDefinition, ScopeInstanceImpl scopeInstance) {
    ClassLoader classLoader = ReflectUtil.installDeploymentClassLoader(processDefinition);
    try {
      return WireContext.create(descriptor, scopeInstance);
    } finally {
      ReflectUtil.uninstallDeploymentClassLoader(classLoader);
    }
  }

  /**
   * Perform resolution of a class name.
   * <p/>
   * Same as {@link #classForName(String, Class)} except that here we delegate to
   * {@link Class#forName(String)} if the context classloader lookup is unsuccessful.
   *
   * @param name The class name
   * @return The class reference.
   * @throws ClassNotFoundException From {@link Class#forName(String)}.
   */
  public static Class<?> classForName(String name) throws ClassNotFoundException {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    if (contextClassLoader != null) {
      try {
        return Class.forName(name, true, contextClassLoader);
      }
      catch (ClassNotFoundException e) {
        // keep going to load through the loader of the current class
      }
    }
    return Class.forName(name);
  }
}

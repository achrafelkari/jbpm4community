package org.jbpm.pvm.internal.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

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

public abstract class ReflectUtil {

  private static Log log = Log.getLog(ReflectUtil.class.getName());
  
  /** searches for the field in the given class and in its super classes */
  public static Field findField(Class<?> clazz, String fieldName) {
    return findField(clazz, fieldName, clazz);
  }

  private static Field findField(Class<?> clazz, String fieldName, Class<?> original) {
    Field field = null;

    try {
      field = clazz.getDeclaredField(fieldName);
      if (log.isTraceEnabled()) log.trace("found field "+fieldName+" in "+clazz.getName());
    } catch (SecurityException e) {
      throw new JbpmException("wasn't allowed to get field '"+clazz.getName()+"."+fieldName+"'", e);
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

    } catch (SecurityException e) {
      throw new JbpmException("wasn't allowed to get method '"+clazz.getName()+"."+methodName+"("+getParameterTypesText(parameterTypes)+")'", e);
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
    return newInstance(clazz, null, null);
  }
  public static <T> T newInstance(Constructor<T> constructor) {
    return newInstance(null, constructor, null);
  }
  public static <T> T newInstance(Constructor<T> constructor, Object[] args) {
    return newInstance(null, constructor, args);
  }
  
  private static <T> T newInstance(Class<T> clazz, Constructor<T> constructor, Object[] args) {
    if ( (clazz==null)
         && (constructor==null)
       ) {
      throw new IllegalArgumentException("can't create new instance without clazz or constructor");
    }

    try {
      if (log.isTraceEnabled()) log.trace("creating new instance for class '"+clazz.getName()+"' with args "+Arrays.toString(args));
      if (constructor==null) {
        if (log.isTraceEnabled()) log.trace("getting default constructor");
        constructor = clazz.getConstructor((Class[])null);
      }
      if (!constructor.isAccessible()) {
        if (log.isTraceEnabled()) log.trace("making constructor accessible");
        constructor.setAccessible(true);
      }
      return constructor.newInstance(args);

    } catch (Throwable t) {
      throw new JbpmException("couldn't construct new '"+clazz.getName()+"' with args "+Arrays.toString(args), t);
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
    int nbrOfArgs = 0;
    if (args!=null) nbrOfArgs = args.length;
    
    int nbrOfParameterTypes = 0;
    if (parameterTypes!=null) nbrOfParameterTypes = parameterTypes.length;
    
    if ( (nbrOfArgs==0)
         && (nbrOfParameterTypes==0)
       ) {
      return true;
    }
    
    if (nbrOfArgs!=nbrOfParameterTypes) {
      return false;
    }

    for (int i=0; (i<parameterTypes.length); i++) {
      Class<?> parameterType = parameterTypes[i];
      String argTypeName = (argDescriptors!=null ? argDescriptors.get(i).getTypeName() : null);
      if (argTypeName!=null) {
         if (! argTypeName.equals(parameterType.getName())) {
           return false;
         }
      } else if ( (args[i]!=null)
                  && (! parameterType.isAssignableFrom(args[i].getClass()))
                ) {
        return false;
      }
    }
    return true;
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
  
  public static String getUnqualifiedClassName(Class<?> clazz) {
    if (clazz==null) {
      return null;
    }
    return getUnqualifiedClassName(clazz.getSimpleName());
  }

  public static String getUnqualifiedClassName(String className) {
    if (className==null) {
      return null;
    }
    int dotIndex = className.lastIndexOf('.');
    if (dotIndex!=-1) {
      className = className.substring(dotIndex+1);
    }
    return className;
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
}

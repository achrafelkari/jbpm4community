/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jbpm.pvm.internal.script;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.jbpm.pvm.internal.env.ExecutionContext;
import org.jbpm.pvm.internal.util.ReflectUtil;

import de.odysseus.el.util.SimpleResolver;

class JuelScriptEngine extends AbstractScriptEngine
    implements Compilable
{
  private ScriptEngineFactory factory;
  ExpressionFactory exprFactory;

  public JuelScriptEngine(ScriptEngineFactory factory)
  {
    this.factory = factory;
    this.exprFactory = new de.odysseus.el.ExpressionFactoryImpl();
  }

  public CompiledScript compile(String script)
      throws ScriptException
  {
    ValueExpression expr = parse(script, this.context);

    return new JuelCompiledScript(this, expr);
  }

  public CompiledScript compile(Reader reader)
      throws ScriptException
  {
    return compile(readFully(reader));
  }

  public Object eval(String script, ScriptContext ctx)
      throws ScriptException
  {
    ValueExpression expr = parse(script, ctx);

    return evalExpr(expr, ctx);
  }

  public Object eval(Reader reader, ScriptContext ctx)
      throws ScriptException
  {
    return eval(readFully(reader), ctx);
  }

  public ScriptEngineFactory getFactory()
  {
    synchronized (this)
    {
      if (this.factory == null)
      {
        this.factory = new JuelScriptEngineFactory();
      }

    }

    return this.factory;
  }

  public Bindings createBindings()
  {
    return new SimpleBindings();
  }

  private ELContext toELContext(final ScriptContext ctx)
  {
    Object tmp = ctx.getAttribute("elcontext");

    if (tmp instanceof ELContext)
    {
      return ((ELContext)tmp);
    }

    ctx.setAttribute("context", ctx, 100);

    ctx.setAttribute("out:print", getPrintMethod(), 100);

    SecurityManager manager = System.getSecurityManager();

    if (manager == null)
    {
      ctx.setAttribute("lang:import", getImportMethod(), 100);
    }

    ELContext elContext = new ELContext()
    {
      ELResolver resolver = makeResolver();
      VariableMapper varMapper = new ScriptContextVariableMapper(ctx);
      FunctionMapper funcMapper = new ScriptContextFunctionMapper(ctx);

      public ELResolver getELResolver()
      {
        return this.resolver;
      }

      public VariableMapper getVariableMapper()
      {
        return this.varMapper;
      }

      public FunctionMapper getFunctionMapper()
      {
        return this.funcMapper;
      }

    };

    ctx.setAttribute("elcontext", elContext, 100);

    return elContext;
  }

  ELResolver makeResolver()
  {
    CompositeELResolver chain = new CompositeELResolver();

    chain.add(new ArrayELResolver());

    chain.add(new ListELResolver());

    chain.add(new MapELResolver());

    chain.add(new ResourceBundleELResolver());

    chain.add(new BeanELResolver());

    return new SimpleResolver(chain);
  }

  private ValueExpression parse(String script, ScriptContext ctx)
      throws ScriptException
  {
    try
    {
      return this.exprFactory.createValueExpression(
          toELContext(ctx), script, Object.class);
    }
    catch (ELException elexp)
    {
      throw new ScriptException(elexp);
    }
  }

  private Object evalExpr(ValueExpression expr, ScriptContext ctx)
      throws ScriptException
  {
    try
    {
      return expr.getValue(toELContext(ctx));
    }
    catch (ELException elexp)
    {
      throw new ScriptException(elexp);
    }
  }

  private String readFully(Reader reader)
      throws ScriptException
  {
    int numChars;
    char[] arr = new char[8192];

    StringBuilder text = new StringBuilder();
    try
    {
      while ((numChars = reader.read(arr, 0, arr.length)) > 0)
      {
        text.append(arr, 0, numChars);
      }

    }
    catch (IOException exp)
    {
      throw new ScriptException(exp);
    }

    return text.toString();
  }

  private static Method getPrintMethod()
  {
    Class<?> myClass;
    try
    {
      myClass = JuelScriptEngine.class;

      Method method = myClass.getMethod(
          "print", new Class[] { Object.class });

      return method;
    }
    catch (Exception exp)
    {
    }

    return null;
  }

  public static void print(Object obj)
  {
    System.out.print(obj);
  }

  private static Method getImportMethod()
  {
    Class<?> myClass;
    try
    {
      myClass = JuelScriptEngine.class;

      Method method = myClass.getMethod(
          "importFunctions",
          new Class[] { ScriptContext.class,
              String.class,
              Object.class });

      return method;
    }
    catch (Exception exp)
    {
    }

    return null;
  }

  public static void importFunctions(ScriptContext ctx, String namespace, Object obj)
  {
    Class<?> clazz = null;

    if (obj instanceof Class<?>)
    {
      clazz = (Class<?>)obj;
    } else {
      if (obj instanceof String)
      {
        try
        {
          clazz = ReflectUtil.classForName((String) obj);
        }
        catch (ClassNotFoundException cnfe)
        {
          throw new ELException(cnfe);
        }

      }

      throw new ELException("Class or class name is missing");
    }

    Method[] methods = clazz.getMethods();

    for (Method m : methods)
    {
      int mod = m.getModifiers();

      if ((Modifier.isStatic(mod)) &&
          (Modifier.isPublic(mod)))
      {
        String name = namespace + ":" + m.getName();

        ctx.setAttribute(name, m,
            100);
      }
    }
  }

  private class JuelCompiledScript extends CompiledScript
  {
    private ValueExpression expr;
    private ScriptEngine engine;

    JuelCompiledScript(ScriptEngine engine, ValueExpression expr)
    {
      this.engine = engine;
      this.expr = expr;
    }

    public ScriptEngine getEngine()
    {
      return engine;
    }

    public Object eval(ScriptContext ctx)
        throws ScriptException
    {
      return engine.eval(this.expr.getExpressionString(), ctx);
    }
  }

  private class ScriptContextFunctionMapper extends FunctionMapper
  {
    private ScriptContext ctx;

    ScriptContextFunctionMapper(ScriptContext ctx)
    {
      this.ctx = ctx;
    }

    private String getFullName(String prefix, String localName)
    {
      return prefix + ":" + localName;
    }

    public Method resolveFunction(String prefix, String localName)
    {
      String fullName = getFullName(prefix, localName);

      int scope = this.ctx.getAttributesScope(fullName);

      if (scope != -1)
      {
        Object tmp = this.ctx.getAttribute(fullName);

        return ((tmp instanceof Method) ? (Method)tmp : null);
      }

      return null;
    }
  }

  private class ScriptContextVariableMapper extends VariableMapper
  {
    private ScriptContext ctx;

    ScriptContextVariableMapper(ScriptContext ctx)
    {
      this.ctx = ctx;
    }

    public ValueExpression resolveVariable(String variable)
    {
      int scope = this.ctx.getAttributesScope(variable);

      if (scope != -1)
      {
        Object value = this.ctx.getAttribute(variable, scope);

        if (value instanceof ValueExpression)
        {
          return ((ValueExpression)value);
        }

        return exprFactory.createValueExpression(
            value, Object.class);
      } else {
        // to support null value for existing variables
        Bindings b = this.ctx.getBindings(ScriptContext.ENGINE_SCOPE);
        ExecutionContext execContext = (ExecutionContext) ((EnvironmentBindings) b).environment.getContext("execution");
        // if variable name exist then set value expression as null
        // since it was not discovered by attribute scope method
        if (execContext.getExecution().getVariables().containsKey(variable)) {
          return exprFactory.createValueExpression(null, Object.class);
        }
      }
      return null;
    }


    public ValueExpression setVariable(String variable, ValueExpression value)
    {
      ValueExpression oldValue = resolveVariable(variable);

      this.ctx.setAttribute(variable, value, 100);

      return oldValue;
    }
  }
}
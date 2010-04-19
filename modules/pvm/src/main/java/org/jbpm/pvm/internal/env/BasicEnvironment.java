/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jbpm.pvm.internal.env;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.api.JbpmException;
import org.jbpm.internal.log.Log;


/**
 * @author Tom Baeyens
 */
public class BasicEnvironment extends EnvironmentImpl {

  private static final long serialVersionUID = 1L;
  
  private static final Log log = Log.getLog(BasicEnvironment.class.getName()); 

  protected String authenticatedUserId;
  protected Map<String, Context> contexts;
  protected ArrayList<String> defaultSearchOrderList;
  protected String[] defaultSearchOrder;
  protected Throwable exception;

  protected transient ClassLoader classLoader;

  public BasicEnvironment() {
    contexts = new HashMap<String, Context>();
    defaultSearchOrderList = new ArrayList<String>();
    defaultSearchOrder = null;
  }

  // context methods ////////////////////////////////////////////////////////////

  public Context getContext(String contextName) {
    return contexts.get(contextName);
  }

  public void setContext(Context context) {
    String key = context.getName();
    if (contexts.put(key, context)==null) {
      defaultSearchOrderList.add(key);
    }
    defaultSearchOrder = null;
  }

  public Context removeContext(Context context) {
    return removeContext(context.getName());
  }
  
  public Context removeContext(String contextName) {
    Context removedContext = contexts.remove(contextName);
    if (removedContext!=null) {
      defaultSearchOrderList.remove(contextName);
      defaultSearchOrder = null;
    }
    return removedContext;
  }

  public Context getEnvironmentFactoryContext() {
    return getContext(Context.CONTEXTNAME_PROCESS_ENGINE);
  }

  public Context getEnvironmentContext() {
    return getContext(Context.CONTEXTNAME_TRANSACTION);
  }

  // authenticatedUserId //////////////////////////////////////////////////////
  
  public String getAuthenticatedUserId() {
    return authenticatedUserId;
  }
  
  public void setAuthenticatedUserId(String authenticatedUserId) {
    this.authenticatedUserId = authenticatedUserId;
  }

  // classloader methods //////////////////////////////////////////////////////

  public ClassLoader getClassLoader() {
    return classLoader;
  }
  
  public void setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }
  
  // search methods ///////////////////////////////////////////////////////////

  public Object get(String name) {
    return get(name, null);
  }
  
  public Object get(String name, String[] searchOrder) {
    if (searchOrder==null) {
      searchOrder = getDefaultSearchOrder();
    }
    for (int i=0; i<searchOrder.length; i++){
      Context context = contexts.get(searchOrder[i]);
      if (context.has(name)) {
        return context.get(name);
      }
    }
    return null;
  }

  public <T> T get(Class<T> type) {
    return get(type, null);
  }

  public <T> T get(Class<T> type, String[] searchOrder) {
    if (searchOrder==null) {
      searchOrder = getDefaultSearchOrder();
    }
    for (int i=0; i<searchOrder.length; i++){
      Context context = contexts.get(searchOrder[i]);
      T o = context.get(type);
      if (o!=null) {
        return o;
      }
    }
    return null;
  }
  
  // close ////////////////////////////////////////////////////////////////////

  public void close() {
    log.trace("closing "+this);

    EnvironmentImpl popped = EnvironmentImpl.popEnvironment();
    if (this!=popped) {
      throw new JbpmException("environment nesting problem");
    }
  }
  
  // private methods //////////////////////////////////////////////////////////

  protected String[] getDefaultSearchOrder() {
    if (defaultSearchOrder==null) {
      int size = defaultSearchOrderList.size();
      defaultSearchOrder = (String[]) new String[size];
      for (int i=0; i<size; i++) {
        defaultSearchOrder[i] = defaultSearchOrderList.get(size-1-i);
      }
    }
    return defaultSearchOrder;
  }
}

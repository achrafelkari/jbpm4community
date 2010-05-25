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

import java.util.LinkedHashMap;
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
  protected Map<String, Context> contexts = new LinkedHashMap<String, Context>();
  protected Throwable exception;

  protected transient ClassLoader classLoader;

  // context methods ////////////////////////////////////////////////////////////

  public Context getContext(String contextName) {
    return contexts.get(contextName);
  }

  public void setContext(Context context) {
    contexts.put(context.getName(), context);
  }

  public Context removeContext(Context context) {
    return removeContext(context.getName());
  }
  
  public Context removeContext(String contextName) {
    return contexts.remove(contextName);
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
    return get(name, searchOrder, true);
  }

  public Object get(String name, boolean nullIfNotFound) {
    return get(name, null, nullIfNotFound);
  }

  public Object get(String name, String[] searchOrder, boolean nullIfNotFound) {
    if (searchOrder == null) {
	  searchOrder = getDefaultSearchOrder();
	}
	for (String contextName : searchOrder) {
	  Context context = contexts.get(contextName);
	  if (context.has(name)) {
		return context.get(name);
	  }
	}
    if (nullIfNotFound) {
      return null;
    } else {
      throw new JbpmException("Null value found for " + name + " but null is not allowed");
    }
  }

  public <T> T get(Class<T> type) {
    return get(type, (String[]) null);
  }

  public <T> T get(Class<T> type, String[] searchOrder) {
    if (searchOrder==null) {
      searchOrder = getDefaultSearchOrder();
    }
    for (String contextName : searchOrder) {
      Context context = contexts.get(contextName);
      T object = context.get(type);
      if (object != null) return object;
    }
    return null;
  }
  
  /**
   * searches an object based on type in the default search order.
   * if this environment contains the given context, the search skips
   * contexts registered after it.
   */
  public <T> T get(Class<T> type, Context requester) {
    String[] searchOrder = getDefaultSearchOrder();
    int searchPosition = 0;
    for (int i = 0; i < searchOrder.length; i++) {
      if (contexts.get(searchOrder[i]) == requester) {
        searchPosition = i + 1;
        break;
      }
    }
    for (int i = searchPosition; i < searchOrder.length; i++) {
      Context context = contexts.get(searchOrder[i]);
      T object = context.get(type);
      if (object != null) return object;
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
    int size = contexts.size();
    String[] defaultSearchOrder = new String[size];

    int index = size;
    for (String contextName : contexts.keySet()) {
      defaultSearchOrder[--index] = contextName;
    }

    return defaultSearchOrder;
  }
}

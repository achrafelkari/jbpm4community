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
package org.jbpm.pvm.internal.script;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;

import org.jbpm.pvm.internal.env.EnvironmentImpl;

/**
 * @author Tom Baeyens
 * @author Huisheng Xu
 */
public class EnvironmentBindings implements Bindings {

  protected EnvironmentImpl environment;

  public EnvironmentBindings(String[] readContextNames, String writeContextName) {
    environment = EnvironmentImpl.getCurrent();
  }

  public Object get(Object key) {
    return environment.get((String) key);
  }

  public boolean containsKey(Object key) {
    return get(key) != null;
  }

  public Object put(String key, Object value) {
    // return named object, if any, to comply with Map contract
    // ignore put otherwise
    return environment.get(key);
  }

  public void putAll(Map<? extends String, ? extends Object> toMerge) {
    // ignore puts
  }

  public Object remove(Object key) {
    return null;
  }

  public void clear() {
  }

  public boolean containsValue(Object value) {
    return false;
  }

  public Set<Entry<String, Object>> entrySet() {
    return Collections.EMPTY_SET;
  }

  public boolean isEmpty() {
    return true;
  }

  public Set<String> keySet() {
    return Collections.EMPTY_SET;
  }

  public int size() {
    return 0;
  }

  public Collection<Object> values() {
    return Collections.EMPTY_SET;
  }
}

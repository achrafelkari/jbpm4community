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
package org.jbpm.pvm.internal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Alejandro Guizar
 */
public class CollectionUtil {

  /** Indicates whether collection elements should be actually checked. */
  private static final boolean DEBUG = false;

  private CollectionUtil() {
    // hide default constructor to prevent instantiation
  }

  /**
   * Ensures that all elements of the given collection can be cast to a desired type.
   * 
   * @param collection the collection to check
   * @param type the desired type
   * @return a collection of the desired type
   * @throws ClassCastException if an element cannot be cast to the desired type
   */
  @SuppressWarnings("unchecked")
  public static <E> Collection<E> checkCollection(Collection<?> collection, Class<E> type) {
    if (DEBUG) {
      Collection<E> copy = new ArrayList<E>(collection.size());
      for (Object element : collection) {
        copy.add(type.cast(element));
      }
      return copy;
    }
    return (Collection<E>) collection;
  }

  /**
   * Ensures that all elements of the given list can be cast to a desired type.
   * 
   * @param list the list to check
   * @param type the desired type
   * @return a list of the desired type
   * @throws ClassCastException if an element cannot be cast to the desired type
   */
  @SuppressWarnings("unchecked")
  public static <E> List<E> checkList(List<?> list, Class<E> type) {
    if (DEBUG) {
      List<E> copy = new ArrayList<E>(list.size());
      for (Object element : list) {
        copy.add(type.cast(element));
      }
      return copy;
    }
    return (List<E>) list;
  }

  /**
   * Ensures that all elements of the given set can be cast to a desired type.
   * 
   * @param list the set to check
   * @param type the desired type
   * @return a set of the desired type
   * @throws ClassCastException if an element cannot be cast to the desired type
   */
  @SuppressWarnings("unchecked")
  public static <E> Set<E> checkSet(Set<?> set, Class<E> type) {
    if (DEBUG) {
      Set<E> copy = new HashSet<E>();
      for (Object element : set) {
        copy.add(type.cast(element));
      }
      return copy;
    }
    return (Set<E>) set;
  }

  @SuppressWarnings("unchecked")
  public static <K, V> Map<K, V> checkMap(Map<?, ?> map, Class<K> keyType, Class<V> valueType) {
    if (DEBUG) {
      Map<K, V> copy = new HashMap<K, V>();
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        copy.put(keyType.cast(entry.getKey()), valueType.cast(entry.getValue()));
      }
      return copy;
    }
    return (Map<K, V>) map;
  }
}

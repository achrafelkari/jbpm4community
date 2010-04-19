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
/**
 * 
 */
package org.jbpm.test.assertion;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.Assert;

/**
 * Utility class with assertions for collections.
 * 
 * @author Joram Barrez
 */
public class CollectionAssertions {
  
  // No need to instantiate
  private CollectionAssertions() {}
  
  /**
   * Compares the elements of the two given collections.
   * The order of elements is not checked.
   */
  public static <T>  void assertContainsSameElements(Collection<T> collection1, Collection<T> collection2) {
    
    Assert.assertTrue("One of the given collections is null, while the other collection is not null",
                    (collection1 == null && collection2 == null) 
                    || (collection1 != null && collection2 != null) );
    
    if (collection1 != null && collection2 != null) {
      
      Assert.assertEquals("Collection 1 does not have the same number of elements as collection 2. " +
              debugCollections(collection1, collection2), collection1.size(), collection2.size());
      
      Iterator<T> it = collection1.iterator();
      while (it.hasNext()) {
        T t = it.next();
        Assert.assertTrue("Collection 1 contains element" + t + ", which does not exist in collection 2 ",
                collection2.contains(t));
      }
      
    }
    
  }
  
  /**
   * Compares the elements of the two given collections.
   * The order of elements is not checked.
   */
  public static <T>  void assertContainsSameElements(Collection<T> collection1, T ... elements) {
    assertContainsSameElements(collection1, Arrays.asList(elements));
  }
  
  @SuppressWarnings("unchecked")
  private static String debugCollections(Collection ... collections) {
    StringBuilder strb = new StringBuilder();
    for (int i = 0; i < collections.length; i++) {
      strb.append("Collection " + (i+1) + ": " + debugCollection(collections[i]) + ". "); 
    }
    return strb.toString();
  }
  
  @SuppressWarnings("unchecked")
  private static String debugCollection(Collection collection) {
    StringBuilder strb = new StringBuilder();
    Iterator it = collection.iterator();
    while (it.hasNext()) {
      strb.append("'" + it.next() + "', ");
    }
    if (strb.length() > 2) {
      strb.delete(strb.length() - 2, strb.length());
    }
    return strb.toString();
  }

}

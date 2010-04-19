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

import java.lang.reflect.Method;
import java.util.List;

import junit.framework.Assert;

/**
 * Utility class with assertions for query test.
 * 
 * @author Joram Barrez
 */
public class QueryAssertions {

  // No need to instantiate
  private QueryAssertions() {
  }

  /**
   * Assertion to be used to check if the result of a certain XXXQuery produces
   * the correct result.
   * 
   * Example usage:
   * 
   * List<Task> taskListAsc = taskService.createTaskQuery().orderAsc(property).list(); List<Task>
   * taskListDesc = taskService.createTaskQuery().orderDesc(property).list();
   * QueryAssertions.assertOrderOnProperty(Task.class, property, taskListAsc, taskListDesc, Arrays.asList("a", "b", "c"));
   * 
   * The assertion will do the following assertions:
   * 
   * @param clazz
   *          Due to generics mumbo-jumbo, it's required to provide the class
   *          of the generic type T. If only we could do T.getClass() ...
   * @param <T>
   *          The return type of the XXXQuery
   * @param property
   *          The property on which the query was ordered
   * @param listOrderedAsc
   *          The list resulting from executing the query with an 'order by asc'
   * @param listOrderedDesc
   *          The list resulting from executing the query with an 'order by
   *          desc'
   * @param expectedValuesAsc
   *          The hard coded list of expected elements, in ascending order. The
   *          assertion for the descending case will be done by reversing this
   *          list
   */
  public static <T> void assertOrderOnProperty(Class<T> clazz, String property, 
          List<T> listOrderedAsc, List<T> listOrderedDesc, List<Object> expectedValuesAsc) {
    
    Assert.assertNotNull(listOrderedAsc);
    Assert.assertEquals(expectedValuesAsc.size(), expectedValuesAsc.size());

    Assert.assertNotNull(listOrderedDesc);
    Assert.assertEquals(expectedValuesAsc.size(), listOrderedDesc.size());

    for (int i = 0; i < expectedValuesAsc.size(); i++) {

      Object realValueAsc = null;
      Object realValueDesc = null;

      try {

        realValueAsc = invokeGetter(property, clazz, listOrderedAsc.get(i));
        realValueDesc = invokeGetter(property, clazz, listOrderedDesc.get(i));

      } catch (Exception e) {
        Assert.fail(e.getMessage());
      }

      Assert.assertEquals("Incorrect order by ASC for property " + property, 
              expectedValuesAsc.get(i), realValueAsc);
      Assert.assertEquals("Incorrect order by DESC for property " + property, 
              expectedValuesAsc.get((listOrderedDesc.size() -1) - i), realValueDesc);

    }
  }
  
  /**
   * Does the same as the assertOrderOnProperty assertion, but instead of
   * checking if the resulting list have the correct elements now it is checked
   * if the elements are naturally ordered ascending and descending.
   * 
   * IMPORTANT: the type T must be implementing the {@link Comparable} interface!
   */
  @SuppressWarnings("unchecked")
  public static <T> void assertOrderIsNatural(Class<T> clazz, String property, List<T> listOrderedAsc, List<T> listOrderedDesc, int nrOfExpectedElements) {

    Assert.assertEquals(nrOfExpectedElements, listOrderedAsc.size());
    Assert.assertEquals(nrOfExpectedElements, listOrderedDesc.size());

    if (nrOfExpectedElements > 1) {

      for (int i = 1; i < listOrderedAsc.size(); i++) {

        // ascending check
        Comparable c1 = (Comparable) invokeGetter(property, clazz, listOrderedAsc.get(i - 1));
        Comparable c2 = (Comparable) invokeGetter(property, clazz, listOrderedAsc.get(i));
        if (c1 != null && c2 != null) {
          Assert.assertTrue("The ascending list does not have a natural order",c1.compareTo(c2) <= 0); // c1 <= c2 when ascending
        }

        // ascending check
        Comparable c3 = (Comparable) invokeGetter(property, clazz, listOrderedDesc.get(i - 1));
        Comparable c4 = (Comparable) invokeGetter(property, clazz, listOrderedDesc.get(i));
        if (c3 != null && c4 != null) {
          Assert.assertTrue("The descending list does not have a natural order", c3.compareTo(c4) >= 0); // c3 >= c4 when ascending
        }

      }
      
    }
    
  }
  
  private static <T> Object invokeGetter(String property, Class<T> clazz, T obj) {
    try {
      Method getter = clazz.getMethod("get" + property.substring(0, 1).toUpperCase() + property.substring(1));
      return getter.invoke(obj);
    } catch (Exception e) {
      throw new RuntimeException("Couldn't invoke getter for property " + property);
    }
  }
  

}

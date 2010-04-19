/**
 * Copyright (C) 2007  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.jbpm.pvm.internal.wire;

import java.util.HashSet;
import java.util.Set;

import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.env.EnvironmentFactory;
import org.jbpm.pvm.internal.processengine.ProcessEngineImpl;
import org.jbpm.pvm.internal.wire.WireContext;

/**
 * @author Guillaume Porcher
 */
public class ContextTest extends WireTestCase {

  public void testLazyCreation() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <string name='o' value='test'/>" +
      "</objects>"
    );

    assertFalse(wireContext.hasCached("o"));
    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals(String.class, o.getClass());
    assertEquals("test", o);
  }
  
  public void testEagerCreation() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <string name='o' value='test' init='eager'/>" +
      "</objects>"
    );

    assertTrue(wireContext.hasCached("o"));
  }

  public void testObjectRemoved() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <string name='o' value='test' init='eager'/>" +
      "  <string name='s' value='foo' init='eager'/>" +
      "</objects>"
    );

    assertTrue(wireContext.hasCached("o"));
    assertTrue(wireContext.hasCached("s"));
    wireContext.remove("o");
    assertFalse(wireContext.hasCached("o"));
    assertTrue(wireContext.hasCached("s"));
    
    Object o = wireContext.get("o");
    assertNotNull(o);
    assertEquals(String.class, o.getClass());
    
    assertTrue(wireContext.hasCached("o"));
    assertTrue(wireContext.hasCached("s"));
    
    assertEquals("test", o);
  }
  
  public void testCacheCleared() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <string name='o' value='test' init='eager'/>" +
      "  <string name='s' value='foo' init='eager'/>" +
      "</objects>"
    );

    assertTrue(wireContext.hasCached("o"));
    assertTrue(wireContext.hasCached("s"));
    wireContext.clear();
    assertFalse(wireContext.hasCached("o"));
    assertFalse(wireContext.hasCached("s"));
    
    Object o = wireContext.get("o");
    assertNotNull(o);
    assertEquals(String.class, o.getClass());
    
    assertTrue(wireContext.hasCached("o"));
    assertFalse(wireContext.hasCached("s"));
    
    assertEquals("test", o);
  }
  
  
  public void testEmptyCacheCleared() {
    WireContext wireContext = createWireContext(
      "<objects/>"
    );

    wireContext.clear();
  }
  
  public void testRemoveObjectNotInCache(){
    WireContext wireContext = createWireContext(
        "<objects/>"
      );

      wireContext.remove("o"); 
  }
  
  public void testKeys(){
    WireContext wireContext = createWireContext(
        "<objects>" +
        "  <string name='o' value='test' init='eager'/>" +
        "  <string name='s' value='foo'/>" +
        "  <string name='t' value='bar' init='eager'/>" +
        "</objects>"
    );
    
    assertTrue(wireContext.has("o"));
    assertTrue(wireContext.has("s"));
    assertTrue(wireContext.has("t"));
    assertTrue(wireContext.hasCached("o"));
    assertFalse(wireContext.hasCached("s"));
    assertTrue(wireContext.hasCached("t"));
    
    Set<String> expectedKeys = new HashSet<String>();
    expectedKeys.add("o");
    expectedKeys.add("s");
    expectedKeys.add("t");
    
    assertEquals(expectedKeys, wireContext.keys());
  }
  
  public void testHasOnEmptyContext(){
    WireContext wireContext = createWireContext(
        "<objects/>"
    );
    
    assertFalse(wireContext.has("foo"));
  }
  
  public void testHasOnEmptyEnv(){
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
        "<environment-scopes/>"
      );

      EnvironmentImpl environment = environmentFactory.openEnvironment();

      try {
        Object foo = environment.get("foo");
        assertNull(foo);
      } finally {
        environment.close();
      }
      environmentFactory.close();
  }
}

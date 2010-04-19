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
package org.jbpm.pvm.internal.wire;

import org.jbpm.pvm.internal.wire.WireContext;


/**
 * @author Tom Baeyens
 */
public class TypeLookupTest extends WireTestCase {
  
  public static class A {
  }

  public void testTypeLookup() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object class='"+A.class.getName()+"' />" +
      "</objects>"
    );
    
    A a = wireContext.get(A.class);
    assertNotNull(a);
  }

  public static class B extends A {
  }

  public void testTypeLookupWithInheritence() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object class='"+B.class.getName()+"' />" +
      "</objects>"
    );
    
    A a = wireContext.get(A.class);
    assertNotNull(a);
    assertEquals(B.class, a.getClass());
  }

  public static class Text {
    String text;
  }

  public void testTypeLookupOrder() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object class='"+Text.class.getName()+"'>" +
      "    <field name='text'><string value='a'/></field>" +
      "  </object>" +
      "  <object class='"+Text.class.getName()+"'>" +
      "    <field name='text'><string value='b'/></field>" +
      "  </object>" +
      "</objects>"
    );
    
    assertEquals("a", wireContext.get(Text.class).text);

    wireContext = createWireContext(
      "<objects>" +
      "  <object class='"+Text.class.getName()+"'>" +
      "    <field name='text'><string value='b'/></field>" +
      "  </object>" +
      "  <object class='"+Text.class.getName()+"'>" +
      "    <field name='text'><string value='a'/></field>" +
      "  </object>" +
      "</objects>"
    );
    
    assertEquals("b", wireContext.get(Text.class).text);
  }
}

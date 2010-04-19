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
import org.jbpm.pvm.internal.wire.WireException;

/**
 * @author Tom Baeyens
 */
public class DependencyTest extends WireTestCase {
  
  public static class A {
    public A(){}
    public A(B b) {this.b = b;}
    B b;
  }
  
  public static class B {
    public B(){}
    public B(A a) {this.a = a;}
    A a;
  }

  public void testResolvableBidirectionalDependency() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='a' class='"+A.class.getName()+"'>" +
      "    <field name='b'>" +
      "      <ref object='b' />" +
      "    </field>" +
      "  </object>" +
      "  <object name='b' class='"+B.class.getName()+"'>" +
      "    <field name='a'>" +
      "      <ref object='a' />" +
      "    </field>" +
      "  </object>" +
      "</objects>"
    );

    A a = (A) wireContext.get("a");
    B b = (B) wireContext.get("b");

    assertSame(b, a.b);
    assertSame(a, b.a);
  }

  public void testUnresolvableBidirectionalDependency() {
    WireContext wireContext = createWireContext(
        "<objects>" +
        "  <object name='a' class='"+A.class.getName()+"'>" +
        "    <constructor>" +
        "      <arg>" +
        "        <ref object='b' />" +
        "      </arg>" +
        "    </constructor>" +
        "  </object>" +
        "  <object name='b' class='"+B.class.getName()+"'>" +
        "    <constructor>" +
        "      <arg>" +
        "        <ref object='a' />" +
        "      </arg>" +
        "    </constructor>" +
        "  </object>" +
        "</objects>"
      );

    try {
      wireContext.get("a");
      fail("expected exception");
    } catch (WireException e) {
      assertTextPresent("circular dependency", e.getMessage());
    }
  }
}

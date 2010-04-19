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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.pvm.internal.xml.Problem;

/**
 * @author Tom Baeyens
 * @author Guillaume Porcher
 */
public class RefWireTest extends WireTestCase {

  public void testSimpleAlias() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+Object.class.getName()+"' />" +
      "  <ref name='r' object='o' />" +
      "</objects>"
    );

    Object r = wireContext.get("r");
    assertNotNull(r);

    Object o = wireContext.get("o");
    assertNotNull(o);

    assertSame(o, r);
  }

  public void testMissingObjectName() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <ref name='r'/>" +
      "</objects>"
    );
    assertNotNull(problems);
    assertEquals(1, problems.size());
    assertTextPresent("ref must have object attribute: ", problems.get(0).getMsg());
  }

  public void testReferenceInList() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+Object.class.getName()+"' />" +
      "  <list name='l'>" +
      "    <ref object='o' />" +
      "  </list>" +
      "</objects>"
    );

    List l = (List) wireContext.get("l");
    assertNotNull(l);

    Object o = wireContext.get("o");
    assertNotNull(o);

    assertSame(o, l.get(0));
  }

  public void testReferenceInMap() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+Object.class.getName()+"' />" +
      "  <map name='m'>" +
      "    <entry>" +
      "      <key>" +
      "        <string value='k' />" +
      "      </key>" +
      "      <value>" +
      "        <ref object='o' />" +
      "      </value>" +
      "    </entry>" +
      "  </map>" +
      "</objects>"
    );

    Map m = (Map) wireContext.get("m");
    assertNotNull(m);

    Object o = wireContext.get("o");
    assertNotNull(o);

    assertSame(o, m.get("k"));
  }

  public void testReferenceInSet() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+Object.class.getName()+"' />" +
      "  <set name='s'>" +
      "    <ref object='o' />" +
      "  </set>" +
      "</objects>"
    );

    Set s = (Set) wireContext.get("s");
    assertNotNull(s);

    Object o = wireContext.get("o");
    assertNotNull(o);

    assertSame(o, s.iterator().next());
  }

  public static class X {
    String constructorArgValue;
    String fieldValue;
    String propertyValue;
    String invokeFirstArg;
    String invokeSecondArg;
    public X() {
    }
    public X(String constructorArgValue) {
      this.constructorArgValue = constructorArgValue;
    }
    public String getPropertyValue() {
      return propertyValue;
    }
    public void setP(String propertyValue) {
      this.propertyValue = propertyValue;
    }
    public void invoke(String invokeFirstArg, String invokeSecondArg) {
      this.invokeFirstArg = invokeFirstArg;
      this.invokeSecondArg = invokeSecondArg;
    }
  }

  public void testConstructorArgument() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='x' class='"+X.class.getName()+"'>" +
      "    <constructor>" +
      "      <arg><ref object='AMS' /></arg>" +
      "    </constructor>" +
      "  </object>" +
      "  <string name='AMS' value='xxx' />" +
      "</objects>"
    );

    X x = (X) wireContext.get("x");
    assertEquals("xxx", x.constructorArgValue);
  }

  public void testFieldArgument() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='x' class='"+X.class.getName()+"'>" +
      "    <field name='fieldValue'><ref object='AMS' /></field>" +
      "  </object>" +
      "  <string name='AMS' value='xxx' />" +
      "</objects>"
    );

    X x = (X) wireContext.get("x");
    assertEquals("xxx", x.fieldValue);
  }

  public void testPropertyArgument() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='x' class='"+X.class.getName()+"'>" +
      "    <property name='p'><ref object='AMS' /></property>" +
      "  </object>" +
      "  <string name='AMS' value='xxx' />" +
      "</objects>"
    );

    X x = (X) wireContext.get("x");
    assertEquals("xxx", x.propertyValue);
  }

  public void testInvokeArgument() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='x' class='"+X.class.getName()+"'>" +
      "    <invoke method='invoke'>" +
      "      <arg><ref object='AMS' /></arg>" +
      "      <arg><ref object='HAM' /></arg>" +
      "    </invoke>" +
      "  </object>" +
      "  <string name='AMS' value='xxx' />" +
      "  <string name='HAM' value='hamburger' />" +
      "</objects>"
    );

    X x = (X) wireContext.get("x");
    assertEquals("xxx", x.invokeFirstArg);
    assertEquals("hamburger", x.invokeSecondArg);
  }
}

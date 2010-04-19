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

import org.jbpm.pvm.internal.xml.Problem;

/**
 * @author Tom Baeyens
 * @author Guillaume Porcher
 */
public class AutoWireTest extends WireTestCase {

  public static class Shape {
  }
  public static class Square extends Shape {
  }

  public static class PieceOfCake {

    // auto-wire means that when an object like this is createTime,
    // that the WireScope will try to look for objects with
    // the same name as the fields in the class.  If it finds
    // an object with that name, and if it is assignable to the
    // field's type, it is automatically injected, without the
    // need for explicit <field /> tag that specifies the injection
    // in the wiring xml.

    String color;
    long size;
    Shape shape;

    public String getColor() {
      return color;
    }
    public Shape getShape() {
      return shape;
    }
    public long getSize() {
      return size;
    }
  }

  public void testAutoWireDefaultOff() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='pieceOfCake' class='"+PieceOfCake.class.getName()+"' />" +
      "  <string name='color' value='green' />" +
      "  <long name='size' value='23' />" +
      "  <object name='shape' class='"+Square.class.getName()+"' />" +
      "</objects>"
    );

    PieceOfCake pieceOfCake = (PieceOfCake) wireContext.get("pieceOfCake");
    assertNull("green", pieceOfCake.getColor());
    assertNull(pieceOfCake.getShape());
    assertEquals(0L, pieceOfCake.getSize());
  }

  public void testAutoWire() {
    WireContext wireContext = createWireContext(
        "<objects>" +
        // auto-wire values can be one of enabled, on, yes, true
        "  <object auto-wire='enabled' name='pieceOfCake' class='"+PieceOfCake.class.getName()+"' />" +
        "  <string name='color' value='green' />" +
        "  <long name='size' value='23' />" +
        "  <object name='shape' class='"+Square.class.getName()+"' />" +
        "</objects>"
      );

    PieceOfCake pieceOfCake = (PieceOfCake) wireContext.get("pieceOfCake");
    assertEquals("green", pieceOfCake.getColor());
    assertEquals(Square.class, pieceOfCake.getShape().getClass());
    assertEquals(23L, pieceOfCake.getSize());
  }

  public void testAutoWireWrongType() {
    WireContext wireContext = createWireContext(
        "<objects>" +
        // auto-wire values can be one of enabled, on, true
        "  <object auto-wire='enabled' name='pieceOfCake' class='"+PieceOfCake.class.getName()+"' />" +
        "  <long name='color' value='23' />" +
        "  <string name='size' value='green' />" +
        "  <object name='shape' class='"+Square.class.getName()+"' />" +
        "</objects>"
      );

    PieceOfCake pieceOfCake = (PieceOfCake) wireContext.get("pieceOfCake");
    assertNull(pieceOfCake.getColor());
    assertEquals(Square.class, pieceOfCake.getShape().getClass());
    assertEquals(0L, pieceOfCake.getSize());
  }

  public void testAutoWireBadValue() {
    List<Problem> problems = parseProblems(
        "<objects>" +
        // auto-wire values can be one of enabled, on, true
        "  <object auto-wire='bad-value' name='pieceOfCake' class='"+PieceOfCake.class.getName()+"' />" +
        "</objects>"
      );

    assertNotNull(problems);
    assertEquals(problems.toString(), 1, problems.size());
    assertTextPresent("attribute <object auto-wire=\"bad-value\" value not in {true, enabled, on, false, disabled, off}", problems.get(0).getMsg());
  }
}

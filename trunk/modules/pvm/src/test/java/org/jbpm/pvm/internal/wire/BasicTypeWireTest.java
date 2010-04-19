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
 *
 */
public class BasicTypeWireTest extends WireTestCase {

  public void testBooleanTrue() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <true name='a' />" +
      "</objects>"
    );

    assertEquals(Boolean.TRUE, wireContext.get("a"));
  }

  public void testBooleanFalse() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <false name='a' />" +
      "</objects>"
    );

    assertEquals(Boolean.FALSE, wireContext.get("a"));
  }

  public void testCharacter() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <char name='sizeof chruches' value='L' />" +
      "</objects>"
    );

    assertEquals(new Character('L'), wireContext.get("sizeof chruches"));
  }

  public void testCharacterWithoutValue() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <char name='buzz' />" +
      "</objects>"
    );

    assertEquals(problems.toString(), 1, problems.size());
    assertTextPresent("'value' is required in element 'char'", problems.get(0).toString());
  }

  public void testCharacterInvalidValue() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <char name='buzz' value='ooops' />" +
      "</objects>"
    );

    assertEquals(problems.toString(), 1, problems.size());
    assertTextPresent("char has invalid formatted value", problems.get(0).toString());
    assertTextPresent("length of value must be 1", problems.get(0).toString());
  }

  public void testDouble() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <double name='lenght of surgery cut' value='12.3' />" +
      "</objects>"
    );

    assertEquals(new Double(12.3), wireContext.get("lenght of surgery cut"));
  }

  public void testDoubleWithoutValue() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <double name='buzz' />" +
      "</objects>"
    );

    assertEquals(problems.toString(), 1, problems.size());
    assertTextPresent("'value' is required in element 'double'", problems.get(0).toString());
  }

  public void testDoubleInvalidValue() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <double name='buzz' value='ooops' />" +
      "</objects>"
    );

    assertEquals(problems.toString(), 1, problems.size());
    assertTextPresent("'ooops' cannot be parsed to a double", problems.get(0).toString());
  }

  public void testFloat() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <float name='lenght of surgery cut' value='12.3' />" +
      "</objects>"
    );

    assertEquals(new Float(12.3), wireContext.get("lenght of surgery cut"));
  }

  public void testFloatWithoutValue() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <float name='buzz' />" +
      "</objects>"
    );

    assertEquals(problems.toString(), 1, problems.size());
    assertTextPresent("'value' is required in element 'float'", problems.get(0).toString());
  }

  public void testFloatInvalidValue() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <float name='buzz' value='ooops' />" +
      "</objects>"
    );

    assertEquals(problems.toString(), 1, problems.size());
    assertTextPresent("'ooops' cannot be parsed to a float", problems.get(0).toString());
  }

  public void testInteger() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <int name='lenght of surgery cut' value='12' />" +
      "</objects>"
    );

    assertEquals(new Integer(12), wireContext.get("lenght of surgery cut"));
  }

  public void testIntegerWithoutValue() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <int name='buzz' />" +
      "</objects>"
    );

    assertEquals(problems.toString(), 1, problems.size());
    assertTextPresent("'value' is required in element 'int'", problems.get(0).toString());
  }

  public void testIntegerInvalidValue() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <int name='buzz' value='ooops' />" +
      "</objects>"
    );

    assertEquals(problems.toString(), 1, problems.size());
    assertTextPresent("'ooops' cannot be parsed to an int", problems.get(0).toString());
  }

  public void testShort() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <short name='lenght of surgery cut' value='12' />" +
      "</objects>"
    );

    assertEquals(new Short((short) 12), wireContext.get("lenght of surgery cut"));
  }

  public void testShortWithoutValue() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <short name='buzz' />" +
      "</objects>"
    );

    assertEquals(problems.toString(), 1, problems.size());
    assertTextPresent("'value' is required in element 'short'", problems.get(0).toString());
  }

  public void testShortInvalidValue() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <short name='buzz' value='ooops' />" +
      "</objects>"
    );

    assertEquals(problems.toString(), 1, problems.size());
    assertTextPresent("'ooops' cannot be parsed to a short", problems.get(0).toString());
  }

  public void testLong() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <long name='lenght of surgery cut' value='12' />" +
      "</objects>"
    );

    assertEquals(new Long((long) 12), wireContext.get("lenght of surgery cut"));
  }

  public void testLongWithoutValue() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <long name='buzz' />" +
      "</objects>"
    );

    assertEquals(problems.toString(), 1, problems.size());
    assertTextPresent("'value' is required in element 'long'", problems.get(0).toString());
  }

  public void testLongInvalidValue() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <long name='buzz' value='ooops' />" +
      "</objects>"
    );

    assertEquals(problems.toString(), 1, problems.size());
    assertTextPresent("'ooops' cannot be parsed to a long", problems.get(0).toString());
  }

  public void testByte() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <byte name='lenght of surgery cut' value='12' />" +
      "</objects>"
    );

    assertEquals(new Byte((byte) 12), wireContext.get("lenght of surgery cut"));
  }

  public void testByteWithoutValue() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <byte name='buzz' />" +
      "</objects>"
    );

    assertEquals(problems.toString(), 1, problems.size());
    assertTextPresent("'value' is required in element 'byte'", problems.get(0).toString());
  }

  public void testByteInvalidValue() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <byte name='buzz' value='ooops' />" +
      "</objects>"
    );

    assertEquals(problems.toString(), 1, problems.size());
    assertTextPresent("'ooops' cannot be parsed to a byte", problems.get(0).toString());
  }

  public void testNamedNull() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <null name='n' />" +
      "</objects>"
    );

    assertNull(wireContext.get("n"));
  }
}

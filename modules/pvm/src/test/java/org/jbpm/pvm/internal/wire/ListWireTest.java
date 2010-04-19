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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jbpm.pvm.internal.xml.Problem;

/**
 * @author Tom Baeyens
 * @author Guillaume Porcher
 */
public class ListWireTest extends WireTestCase {

  public void testMixedElementTypes() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <list name='l'>" +
      "    <true />" +
      "    <string value='v' />" +
      "    <null />" +
      "  </list>" +
      "</objects>"
    );

    List<Object> values = (List<Object>) wireContext.get("l");
    // make a copy so that we can compare 2 ArrayLists
    values = new ArrayList<Object>(values);

    List<Object> expected = new ArrayList<Object>(3);
    expected.add(Boolean.TRUE);
    expected.add("v");
    expected.add(null);

    assertEquals(expected, values);
  }

  public void testCustomListType() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <list name='l' class='java.util.LinkedList' />" +
      "</objects>"
    );
    Object l = wireContext.get("l");
    assertNotNull(l);
    assertEquals("java.util.LinkedList", l.getClass().getName());
  }

  public void testInvalidListType() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <list name='l' class='invalid-list-type'/>" +
      "</objects>"
    );

    assertTextPresent("class invalid-list-type could not be found", problems.get(0).getMsg());
  }

  public void testNotAListType() {
    List<Problem> problems = parseProblems(
        "<objects>" +
        "  <list name='l' class='"+HashSet.class.getName()+"'/>" +
        "</objects>"
    );

    assertTextPresent("class "+HashSet.class.getName()+" is not a "+List.class.getName(), problems.get(0).getMsg());
  }

  public void testProblemUnknownValue() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <list name='l'>" +
      "    <unknown-descriptor />" +
      "  </list>" +
      "</objects>"
    );

    assertEquals(problems.toString(), 1, problems.size());
    assertTextPresent("unrecognized element: <unknown-descriptor", problems.get(0).getMsg());
  }

  public static class TextElement {
    String text;
  }
  
  public void testElementsWithInitialization() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <list name='l'>" +
      "    <object class='"+TextElement.class.getName()+"'>" +
      "      <field name='text'>" +
      "        <string value='a'/>" +
      "      </field>" +
      "    </object>" +
      "    <object class='"+TextElement.class.getName()+"'>" +
      "      <field name='text'>" +
      "        <string value='b'/>" +
      "      </field>" +
      "    </object>" +
      "    <object class='"+TextElement.class.getName()+"'>" +
      "      <field name='text'>" +
      "        <string value='c'/>" +
      "      </field>" +
      "    </object>" +
      "  </list>" +
      "</objects>"
    );
    List<TextElement> l = (List<TextElement>) wireContext.get("l");
    assertEquals(l.toString(), 3, l.size());
    assertEquals("a", l.get(0).text);
    assertEquals("b", l.get(1).text);
    assertEquals("c", l.get(2).text);
  }
}

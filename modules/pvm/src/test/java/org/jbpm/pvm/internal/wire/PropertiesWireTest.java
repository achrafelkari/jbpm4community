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
import java.util.Properties;

import org.jbpm.pvm.internal.util.FileUtil;
import org.jbpm.pvm.internal.xml.Problem;

/**
 * @author Tom Baeyens
 */
public class PropertiesWireTest extends WireTestCase {

  public void testEmptyProperties() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <properties name='p' />" +
      "</objects>"
    );

    Properties p = (Properties) wireContext.get("p");
    assertEquals(p.toString(), 0, p.size());
  }

  public void testInlineProperties() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <properties name='p'>" +
      "    <property name='1' value='a' />" +
      "    <property name='2' value='b' />" +
      "    <property name='3' value='c' />" +
      "  </properties>" +
      "</objects>"
    );

    Properties p = (Properties) wireContext.get("p");
    assertEquals(p.toString(), 3, p.size());
    assertEquals("a", p.getProperty("1"));
    assertEquals("b", p.getProperty("2"));
    assertEquals("c", p.getProperty("3"));
  }

  public void testPropertyWithoutNameOrValue() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <properties name='p'>" +
      "    <property />" +
      "  </properties>" +
      "</objects>"
    );

    assertEquals(problems.toString(), 1, problems.size());
    assertTextPresent("property must have name and value attribute", problems.get(0).toString());
  }

  public void testFileProperties() throws Exception {
    String propertiesFile = FileUtil.getFileNameForResource("org/jbpm/pvm/internal/wire/file.properties"); 

    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <properties name='p' file='"+propertiesFile+"' />" +
      "</objects>"
    );

    Properties p = (Properties) wireContext.get("p");
    assertEquals(p.toString(), 3, p.size());
    assertEquals("flute", p.getProperty("1"));
    assertEquals("fagot", p.getProperty("2"));
    assertEquals("fecundity", p.getProperty("3"));
  }

  public void testUnexistingFileProperties() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <properties name='p' file='unexisting-file' />" +
      "</objects>"
    );

    try {
      wireContext.get("p");
    } catch (WireException e) {
      assertTextPresent("couldn't read properties from file unexisting-file", e.getMessage());
    }
  }

  public void testUrlProperties() {
    String testClassesUrl = PropertiesWireTest.class.getProtectionDomain().getCodeSource().getLocation().toString();
    String propertiesFile = testClassesUrl+"/org/jbpm/wire/url.properties"; 

    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <properties name='p' url='"+propertiesFile+"' />" +
      "</objects>"
    );

    Properties p = (Properties) wireContext.get("p");
    assertEquals(p.toString(), 3, p.size());
    assertEquals("umbrella", p.getProperty("1"));
    assertEquals("ultraviolet", p.getProperty("2"));
    assertEquals("ubiquitous", p.getProperty("3"));
  }

  public void testUnexistingUrlProperties() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <properties name='p' url='buzzzzz' />" +
      "</objects>"
    );

    try {
      wireContext.get("p");
    } catch (WireException e) {
      assertTextPresent("couldn't read properties from url buzzzzz", e.getMessage());
    }
  }

  public void testResourceProperties() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <properties name='p' resource='org/jbpm/wire/resource.properties' />" +
      "</objects>"
    );

    Properties p = (Properties) wireContext.get("p");
    assertEquals(p.toString(), 3, p.size());
    assertEquals("rubber", p.getProperty("1"));
    assertEquals("rack", p.getProperty("2"));
    assertEquals("ramblas", p.getProperty("3"));
  }

  public void testUnexistingResourceProperties() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <properties name='p' resource='unexisting-resource' />" +
      "</objects>"
    );

    try {
      wireContext.get("p");
    } catch (WireException e) {
      assertTextPresent("couldn't read properties from resource unexisting-resource", e.getMessage());
    }
  }

  public void testOverridenFileProperties() throws Exception {
    String propertiesFile = FileUtil.getFileNameForResource("/org/jbpm/wire/file.properties"); 

    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <properties name='p' file='"+propertiesFile+"'>" +
      "    <property name='2' value='overwritten' />" +
      "  </properties>" +
      "</objects>"
    );

    Properties p = (Properties) wireContext.get("p");
    assertEquals(p.toString(), 3, p.size());
    assertEquals("flute", p.getProperty("1"));
    assertEquals("overwritten", p.getProperty("2"));
    assertEquals("fecundity", p.getProperty("3"));
  }

  public void testOverridenUrlProperties() {
    String testClassesUrl = PropertiesWireTest.class.getProtectionDomain().getCodeSource().getLocation().toString();
    String propertiesFile = testClassesUrl+"/org/jbpm/wire/url.properties"; 

    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <properties name='p' url='"+propertiesFile+"'>" +
      "    <property name='2' value='overwritten' />" +
      "  </properties>" +
      "</objects>"
    );

    Properties p = (Properties) wireContext.get("p");
    assertEquals(p.toString(), 3, p.size());
    assertEquals("umbrella", p.getProperty("1"));
    assertEquals("overwritten", p.getProperty("2"));
    assertEquals("ubiquitous", p.getProperty("3"));
  }

  public void testOverridenResourceProperties() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <properties name='p' resource='org/jbpm/wire/resource.properties'>" +
      "    <property name='2' value='overwritten' />" +
      "  </properties>" +
      "</objects>"
    );

    Properties p = (Properties) wireContext.get("p");
    assertEquals(p.toString(), 3, p.size());
    assertEquals("rubber", p.getProperty("1"));
    assertEquals("overwritten", p.getProperty("2"));
    assertEquals("ramblas", p.getProperty("3"));
  }

}

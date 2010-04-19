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
import java.util.List;

import org.jbpm.pvm.internal.wire.WireContext;

/**
 * @author Tom Baeyens
 * @author Guillaume Porcher
 */
public class EagerInitTest extends WireTestCase {

  // test simple eager initialization /////////////////////////////////////////

  public static class A {
    static boolean isConstructed = false;
    static boolean isInitialized = false;
    public A() {
      isConstructed = true;
    }
  }

  /**
   * Tests that eager objects are created and initialized with the context.
   */
  public void testEagerInitialization() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='a' class='"+A.class.getName()+"' init='eager'>" +
      "    <field name='isInitialized'>" +
      "      <true/>" +
      "    </field>" +
      "  </object>" +
      "</objects>"
    );

    assertTrue(A.isConstructed);
    assertTrue(A.isInitialized);
    assertTrue(wireContext.hasCached("a"));
  }


  // test default lazy initialization /////////////////////////////////////////

  public static class B {
    static boolean isConstructed = false;
    public B() {
      isConstructed = true;
    }
  }

  public void testLazyInitializationDefault() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      // the default init behaviour is lazy
      "  <object name='b' class='"+B.class.getName()+"' />" +
      "</objects>"
    );

    assertFalse(B.isConstructed);
    assertFalse(wireContext.hasCached("b"));
  }


  // test eager initialization sequence ///////////////////////////////////////

  static List<Class<?>> sequence = new ArrayList<Class<?>>();

  public static class Seq0 { public Seq0() { sequence.add(Seq0.class); } }
  public static class Seq1 { public Seq1() { sequence.add(Seq1.class); } }
  public static class Seq2 { public Seq2() { sequence.add(Seq2.class); } }
  public static class Seq3 { public Seq3() { sequence.add(Seq3.class); } }
  public static class Seq4 { public Seq4() { sequence.add(Seq4.class); } }

  public void testEagerInitializationSequence() {
    sequence = new ArrayList<Class<?>>();
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='0' class='"+Seq0.class.getName()+"' init='eager' />" +
      "  <object name='1' class='"+Seq1.class.getName()+"' init='eager' />" +
      "  <object name='2' class='"+Seq2.class.getName()+"' init='eager' />" +
      "  <object name='3' class='"+Seq3.class.getName()+"' init='eager' />" +
      "  <object name='4' class='"+Seq4.class.getName()+"' init='eager' />" +
      "</objects>"
    );

    List<Class<?>> expectedSequence = new ArrayList<Class<?>>();
    expectedSequence.add(Seq0.class);
    expectedSequence.add(Seq1.class);
    expectedSequence.add(Seq2.class);
    expectedSequence.add(Seq3.class);
    expectedSequence.add(Seq4.class);

    assertEquals(expectedSequence, sequence);

    assertTrue(wireContext.hasCached("0"));
    assertTrue(wireContext.hasCached("1"));
    assertTrue(wireContext.hasCached("2"));
    assertTrue(wireContext.hasCached("3"));
    assertTrue(wireContext.hasCached("4"));
  }

  public void testEagerInitializationListSequence() {
    sequence = new ArrayList<Class<?>>();
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <list name='list1' init='eager'>" +
      "    <object name='0' class='"+Seq0.class.getName()+"'/>" +
      "    <object name='1' class='"+Seq1.class.getName()+"'/>" +
      "    <object name='2' class='"+Seq2.class.getName()+"'/>" +
      "    <object name='3' class='"+Seq3.class.getName()+"'/>" +
      "    <object name='4' class='"+Seq4.class.getName()+"'/>" +
      "  </list>" +
      "</objects>"
    );

    // in this test, we cannot be sure of the initialization sequence
    // in this config, the sequence is 2,3,4,0,1
    // if we rename list1 to list2 and list2 to list 1, the sequence would be 0,1,2,3,4
    assertEquals(5, sequence.size());

    assertTrue(wireContext.hasCached("0"));
    assertTrue(wireContext.hasCached("1"));
    assertTrue(wireContext.hasCached("2"));
    assertTrue(wireContext.hasCached("3"));
    assertTrue(wireContext.hasCached("4"));
}


  public void testImmediateInitializationListSequence() {
    sequence = new ArrayList<Class<?>>();
    WireContext wireContext = createWireContext(
        "<objects>" +
        "  <list name='l' init='immediate'>" +
        "    <object name='0' class='"+Seq0.class.getName()+"'/>" +
        "    <object name='1' class='"+Seq1.class.getName()+"'/>" +
        "    <object name='2' class='"+Seq2.class.getName()+"'/>" +
        "    <object name='3' class='"+Seq3.class.getName()+"'/>" +
        "    <object name='4' class='"+Seq4.class.getName()+"'/>" +
        "  </list>" +
        "</objects>"
    );

    List<Class<?>> expectedSequence = new ArrayList<Class<?>>();
    expectedSequence.add(Seq0.class);
    expectedSequence.add(Seq1.class);
    expectedSequence.add(Seq2.class);
    expectedSequence.add(Seq3.class);
    expectedSequence.add(Seq4.class);

    assertEquals(expectedSequence, sequence);

    assertTrue(wireContext.hasCached("0"));
    assertTrue(wireContext.hasCached("1"));
    assertTrue(wireContext.hasCached("2"));
    assertTrue(wireContext.hasCached("3"));
    assertTrue(wireContext.hasCached("4"));
  }

  /** 
   * Tests eager initialization of a descriptor that does not implement initializable. 
   * The object is eagerly created. 
   */
  public void testEagerInitNotInitializable() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      // the default init behaviour is lazy
      "  <string name='a' value='a-string' init='immediate' />" +
      "</objects>"
    );

    assertTrue(wireContext.hasCached("a"));
  }
  
  /**
   * Tests that the referenced object is created and initialized.
   */
  public static class C {
    static boolean isConstructed = false;
    static boolean isInitialized = false;
    public C() {
      isConstructed = true;
    }
  }
  
  public void testEagerRef() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      // the default init behaviour is lazy
      "  <object name='o' class='"+C.class.getName()+"'>" +
  		"    <field name='isInitialized'>" +
   		"      <true/>" +
   		"    </field>" +
      "  </object>" +
      "  <ref name='r' object='o' init='eager' />" +
      "</objects>"
    );

    assertTrue(C.isConstructed);
    assertTrue(C.isInitialized);
    assertTrue(wireContext.hasCached("o"));
    assertTrue(wireContext.hasCached("r"));
  }
}

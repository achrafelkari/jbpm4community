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

import org.jbpm.api.JbpmException;
import org.jbpm.pvm.internal.xml.Problem;

/**
 * @author Tom Baeyens
 * @author Guillaume Porcher
 */
public class ObjectWireTest extends WireTestCase {

  public static class DefaultConstructorClass {
  }

  public void testDefaultConstructor() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+DefaultConstructorClass.class.getName()+"' />" +
      "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals(DefaultConstructorClass.class, o.getClass());
  }

  public void testDefaultConstructorWithWrongArgs() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+DefaultConstructorClass.class.getName()+"' >" +
      "    <constructor>" +
      "      <arg>" +
      "        <string value='constructorparametervalue' />" +
      "      </arg>" +
      "    </constructor>" +
      "  </object>" +
      "</objects>"
    );

    try {
      wireContext.get("o");
      fail("expected exception");
    } catch (WireException e) {
      assertTextPresent("couldn't find constructor "+
          DefaultConstructorClass.class.getName() +
          " with args [constructorparametervalue]", e.getMessage());
    }
  }

  public void testEmptyArgDescriptor() {
    List<Problem> problems = parseProblems(
        "<objects>" +
        "  <object name='o' class='"+DefaultConstructorClass.class.getName()+"' >" +
        "    <constructor>" +
        "      <arg/>" +
        "    </constructor>" +
        "  </object>" +
        "</objects>"
    );
    assertNotNull(problems);
    assertTextPresent("arg must contain exactly one descriptor element out of ", problems.get(0).getMsg());
  }

  public void testMissingObjectClassName() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <object />" +
      "</objects>"
    );
    assertNotNull(problems);
    assertTextPresent("element 'object' must have one of {attribute 'class', attribute 'expr', attribute 'factory' or element 'factory'}", problems.get(0).getMsg());
  }

  public void testInvalidObjectType() {
    try {
      createWireContext(
        "<objects>" +
        "  <object name='o' class='invalid-object-type'/>" +
        "</objects>"
      );

      fail("expected exception");
    } catch (JbpmException e) {
      assertTextPresent("couldn't load class 'invalid-object-type'", e.getMessage());
    }
  }


  public static class StringConstructorClass {
    String text;
    public StringConstructorClass(String text) {
      this.text = text;
    }
  }

  public void testStringConstructor() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+StringConstructorClass.class.getName()+"'>" +
      "    <constructor>" +
      "      <arg>" +
      "        <string value='constructorparametervalue' />" +
      "      </arg>" +
      "    </constructor>" +
      "  </object>" +
      "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals(StringConstructorClass.class, o.getClass());
    assertEquals("constructorparametervalue", ((StringConstructorClass)o).text);
  }

  public static class StringStringConstructorClass {
    String one;
    String two;
    public StringStringConstructorClass(String one, String two) {
      this.one = one;
      this.two = two;
    }
  }

  public void testStringStringConstructor() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+StringStringConstructorClass.class.getName()+"'>" +
      "    <constructor>" +
      "      <arg>" +
      "        <string value='one' />" +
      "      </arg>" +
      "      <arg>" +
      "        <string value='two' />" +
      "      </arg>" +
      "    </constructor>" +
      "  </object>" +
      "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals(StringStringConstructorClass.class, o.getClass());
    assertEquals("one", ((StringStringConstructorClass)o).one);
    assertEquals("two", ((StringStringConstructorClass)o).two);
  }

  public void testMethodWithoutObjectNorClass() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <object name='o' method='m' />" +
      "</objects>"
    );
    assertNotNull(problems);
    assertTextPresent("element 'object' must have one of {attribute 'class', attribute 'expr', attribute 'factory' or element 'factory'}", problems.get(0).getMsg());
  }

  public static class StaticFactoryMethodWithoutParametersClass {
    private StaticFactoryMethodWithoutParametersClass() {
    }
    public static StaticFactoryMethodWithoutParametersClass create() {
      return new StaticFactoryMethodWithoutParametersClass();
    }
  }
  public void testStaticFactoryMethodWithoutParameters() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+StaticFactoryMethodWithoutParametersClass.class.getName()+"' method='create' />" +
      "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertSame(StaticFactoryMethodWithoutParametersClass.class, o.getClass());
  }

  public static class NonStaticFactoryMethodClass {
    private NonStaticFactoryMethodClass() {
    }
    public NonStaticFactoryMethodClass create() {
      return new NonStaticFactoryMethodClass();
    }
  }

  public void testNonStaticFactoryMethod(){
    WireContext wireContext = createWireContext(
        "<objects>" +
        "  <object name='o' class='"+NonStaticFactoryMethodClass.class.getName()+"' method='create' />" +
        "</objects>"
    );

    try {
      wireContext.get("o");
      fail("expected exception");
    } catch (WireException e) {
      assertTextPresent("method " + NonStaticFactoryMethodClass.class.getName() + ".create() is not static.", e.getMessage());
      assertTextPresent("It cannot be called on a null object.", e.getMessage());
    }
  }

  public static class StaticFactoryMethodWithParametersClass {
    String one;
    String two;
    StaticFactoryMethodWithParametersClass() {
      throw new RuntimeException("buzzz");
    }
    StaticFactoryMethodWithParametersClass(String one, String two, Integer dummy) {
      this.one = one;
      this.two = two;
    }
    public static StaticFactoryMethodWithParametersClass create(String one, String two) {
      return new StaticFactoryMethodWithParametersClass(one, two, null);
    }
  }

  public void testStaticFactoryMethodWithParameters() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+StaticFactoryMethodWithParametersClass.class.getName()+"' method='create'>" +
      "    <arg>" +
      "      <string value='one'/>" +
      "    </arg>" +
      "    <arg>" +
      "      <string value='two'/>" +
      "    </arg>" +
      "  </object>" +
      "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals(StaticFactoryMethodWithParametersClass.class, o.getClass());
    assertEquals("one", ((StaticFactoryMethodWithParametersClass)o).one);
    assertEquals("two", ((StaticFactoryMethodWithParametersClass)o).two);
  }

  public static class FactoryMethodWithoutParametersClass {
    public Object create() {
      return "factoried from another object";
    }
  }
  public void testObjectFactoryDescriptorWithoutParameters() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' method='create'>" +
      "    <factory>" +
      "      <object class='"+FactoryMethodWithoutParametersClass.class.getName()+"' />" +
      "    </factory>" +
      "  </object>" +
      "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals("factoried from another object", o);
  }

  public static class FactoryMethodWithParametersClass {
    public Object create(String one, String two) {
      return new String[]{one, two};
    }
  }

  public void testFactoryObjectWithParameters() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' method='create'>" +
      "    <factory>" +
      "      <object class='"+FactoryMethodWithParametersClass.class.getName()+"' />" +
      "    </factory>" +
      "    <arg>" +
      "      <string value='one'/>" +
      "    </arg>" +
      "    <arg>" +
      "      <string value='two'/>" +
      "    </arg>" +
      "  </object>" +
      "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals("one", ((String[])o)[0]);
    assertEquals("two", ((String[])o)[1]);
  }

  public void testReferencedFactoryObjectWithParameters() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' factory='f' method='create'>" +
      "    <arg>" +
      "      <string value='one'/>" +
      "    </arg>" +
      "    <arg>" +
      "      <string value='two'/>" +
      "    </arg>" +
      "  </object>" +
      "  <object name='f' class='"+FactoryMethodWithParametersClass.class.getName()+"' />" +
      "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals("one", ((String[])o)[0]);
    assertEquals("two", ((String[])o)[1]);
  }

  public static class InheritedFactoryMethodMethod extends FactoryMethodWithParametersClass {
  }

  public void testInheritedFactoryMethodWithParameters() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' method='create'>" +
      "    <factory>" +
      "      <object class='"+InheritedFactoryMethodMethod.class.getName()+"' />" +
      "    </factory>" +
      "    <arg>" +
      "      <string value='one'/>" +
      "    </arg>" +
      "    <arg>" +
      "      <string value='two'/>" +
      "    </arg>" +
      "  </object>" +
      "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals("one", ((String[])o)[0]);
    assertEquals("two", ((String[])o)[1]);
  }

  public void testReferencedInheritedFactoryMethodWithParameters() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' factory='f' method='create'>" +
      "    <arg>" +
      "      <string value='one'/>" +
      "    </arg>" +
      "    <arg>" +
      "      <string value='two'/>" +
      "    </arg>" +
      "  </object>" +
      "  <object name='f' class='"+InheritedFactoryMethodMethod.class.getName()+"' />" +
      "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals("one", ((String[])o)[0]);
    assertEquals("two", ((String[])o)[1]);
  }

  public static class OverloadedFactoryMethodMethod extends FactoryMethodWithParametersClass {
    public Object create(String one, String two, String three) {
      throw new RuntimeException("this method shouldn't be called");
    }
  }

  public void testOverloadedFactoryMethodWithParameters() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' method='create'>" +
      "    <factory>" +
      "      <object class='"+OverloadedFactoryMethodMethod.class.getName()+"' />" +
      "    </factory>" +
      "    <arg>" +
      "      <string value='one'/>" +
      "    </arg>" +
      "    <arg>" +
      "      <string value='two'/>" +
      "    </arg>" +
      "  </object>" +
      "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals("one", ((String[])o)[0]);
    assertEquals("two", ((String[])o)[1]);
  }

  public static class OverriddenFactoryMethodMethod extends FactoryMethodWithParametersClass {
    public Object create(String one, String two) {
      return one+two;
    }
  }

  public void testOverridenFactoryMethodWithParameters() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' method='create'>" +
      "    <factory>" +
      "      <object class='"+OverriddenFactoryMethodMethod.class.getName()+"' />" +
      "    </factory>" +
      "    <arg>" +
      "      <string value='one'/>" +
      "    </arg>" +
      "    <arg>" +
      "      <string value='two'/>" +
      "    </arg>" +
      "  </object>" +
      "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals("onetwo", o);
  }

  public static class FieldInjectionClass {
    static String INITVALUE = "";
    String txtOne = INITVALUE;
    String txtTwo = INITVALUE;
  }

  public void testFieldInjection() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+FieldInjectionClass.class.getName()+"'>" +
      "    <field name='txtOne'>" +
      "      <string value='hello' />" +
      "    </field>" +
      "    <field name='txtTwo'>" +
      "      <string value='world' />" +
      "    </field>" +
      "  </object>" +
      "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals(FieldInjectionClass.class, o.getClass());
    assertEquals("hello", ((FieldInjectionClass)o).txtOne);
    assertEquals("world", ((FieldInjectionClass)o).txtTwo);
  }

  public void testMissingFieldName() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <object name='o' class='java.lang.String'>" +
      "    <field><null/></field>" +
      "  </object>" +
      "</objects>"
    );
    assertNotNull(problems);
    assertTextPresent("field must have name", problems.get(0).getMsg());
  }

  public void testMissingFieldDescriptor() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <object name='o' class='java.lang.String'>" +
      "    <field name='a'></field>" +
      "  </object>" +
      "</objects>"
    );
    assertNotNull(problems);
    assertTextPresent("field must have 1 descriptor element out of", problems.get(0).getMsg());
  }

  public void testBadFieldDescriptor() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <object name='o' class='java.lang.String'>" +
      "    <field name='a'><bad-descriptor /></field>" +
      "  </object>" +
      "</objects>"
    );
    assertNotNull(problems);
    assertEquals(1, problems.size());
    assertTextPresent("unknown descriptor element bad-descriptor inside field operation: ", problems.get(0).getMsg());
  }

  public static class InheritedFieldInjectionClass extends FieldInjectionClass {
  }

  public void testInheritedFieldInjection() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+InheritedFieldInjectionClass.class.getName()+"'>" +
      "    <field name='txtOne'>" +
      "      <string value='hello' />" +
      "    </field>" +
      "    <field name='txtTwo'>" +
      "      <string value='world' />" +
      "    </field>" +
      "  </object>" +
      "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals(InheritedFieldInjectionClass.class, o.getClass());
    assertEquals("hello", ((InheritedFieldInjectionClass)o).txtOne);
    assertEquals("world", ((InheritedFieldInjectionClass)o).txtTwo);
  }

  public static class OverriddenFieldInjectionClass extends FieldInjectionClass {
    String txtOne;
  }

  public void testOverriddenFieldInjection() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+OverriddenFieldInjectionClass.class.getName()+"'>" +
      "    <field name='txtOne'>" +
      "      <string value='hello' />" +
      "    </field>" +
      "    <field name='txtTwo'>" +
      "      <string value='world' />" +
      "    </field>" +
      "  </object>" +
      "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals(OverriddenFieldInjectionClass.class, o.getClass());
    assertEquals("hello", ((OverriddenFieldInjectionClass)o).txtOne);
    assertEquals("world", ((OverriddenFieldInjectionClass)o).txtTwo);
  }

  public static class IntFieldInjectionClass {
    int val = 0;
    void addToVal(int a){
      val += a;
    }
  }

  public void testIntFieldInjection() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+IntFieldInjectionClass.class.getName()+"'>" +
      "    <field name='val'>" +
      "      <int value='10' />" +
      "    </field>" +
      "  </object>" +
      "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals(IntFieldInjectionClass.class, o.getClass());
    assertEquals(10L, ((IntFieldInjectionClass)o).val);
  }

  public void testIntFieldInjectionBadType() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+IntFieldInjectionClass.class.getName()+"'>" +
      "    <field name='val'>" +
      "      <string value='test' />" +
      "    </field>" +
      "  </object>" +
      "</objects>"
    );

    try{
      wireContext.get("o");
      fail("expected exception");
    } catch (WireException e){
      assertTextPresent("couldn't initialize object 'o': couldn't set val to test", e.getMessage());
    }
  }

  public void testIntPropertyInjectionWithNoSetter(){
    WireContext wireContext = createWireContext(
        "<objects>" +
        "  <object name='o' class='"+IntFieldInjectionClass.class.getName()+"'>" +
        "    <property name='val'>" +
        "      <int value='10' />" +
        "    </property>" +
        "  </object>" +
        "</objects>"
      );

    try{
      wireContext.get("o");
      fail("expected exception");
    } catch (WireException e){
      assertTextPresent("couldn't initialize object 'o': couldn't find property setter setVal for value 10", e.getMessage());
    }
  }

  public static class PropertyInjectionClass {
    String s;
    String propertyS;
    boolean z;
    boolean propertyZ;
    char c;
    char propertyC;
    int i;
    int propertyI;
    long l;
    long propertyL;
    float f;
    float propertyF;
    double d;
    double propertyD;
    
    public void setS(String s) {
      propertyS = s;
    }
    public void setZ(boolean z) {
      propertyZ = z;
    }
    public void setC(char c) {
      propertyC = c;
    }
    public void setI(int i) {
      propertyI = i;
    }
    public void setL(long l) {
      propertyL = l;
    }
    public void setF(float f) {
      propertyF = f;
    }
    public void setD(double d) {
      propertyD = d;
    }
  }

  public void testPropertyInjection() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+PropertyInjectionClass.class.getName()+"'>" +
      "    <property name='s'>" +
      "      <string value='hello' />" +
      "    </property>" +
      "    <property name='z'>" +
      "      <true/>" +
      "    </property>" +
      "    <property name='c'>" +
      "      <char value='x'/>" +
      "    </property>" +
      "    <property name='i'>" +
      "      <int value='32768'/>" +
      "    </property>" +
      "    <property name='l'>" +
      "      <long value='2147483648'/>" +
      "    </property>" +
      "    <property name='f'>" +
      "      <float value='3e9'/>" +
      "    </property>" +
      "    <property name='d'>" +
      "      <double value='1e39'/>" +
      "    </property>" +
      "  </object>" +
      "</objects>"
    );

    PropertyInjectionClass pic = (PropertyInjectionClass) wireContext.get("o");

    assertNull(pic.s);
    assertEquals("hello", pic.propertyS);
    assertFalse(pic.z);
    assertTrue(pic.propertyZ);
    assertEquals('\0', pic.c);
    assertEquals('x', pic.propertyC);
    assertEquals(0, pic.i);
    assertEquals(1 << 15, pic.propertyI);
    assertEquals(0, pic.l);
    assertEquals(1l << 31, pic.propertyL);
    assertEquals(0, pic.f, 0);
    assertEquals(3e9f, pic.propertyF, 0);
    assertEquals(0, pic.d, 0);
    assertEquals(1e39, pic.propertyD, 0);
  }

  public void testWideningPropertyInjection() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+PropertyInjectionClass.class.getName()+"'>" +
      "    <property name='s'>" +
      "      <string value='hello' />" +
      "    </property>" +
      "    <property name='i'>" +
      "      <char value=' '/>" +
      "    </property>" +
      "    <property name='l'>" +
      "      <int value='2147483647'/>" +
      "    </property>" +
      "    <property name='f'>" +
      "      <int value='16777216'/>" +
      "    </property>" +
      "    <property name='d'>" +
      "      <long value='9007199254740992'/>" +
      "    </property>" +
      "  </object>" +
      "</objects>"
    );

    PropertyInjectionClass pic = (PropertyInjectionClass) wireContext.get("o");

    assertEquals(0, pic.i);
    assertEquals(' ', pic.propertyI);
    assertEquals(0, pic.l);
    assertEquals(Integer.MAX_VALUE, pic.propertyL);
    assertEquals(0, pic.f, 0);
    assertEquals(1 << 24, pic.propertyF, 0);
    assertEquals(0, pic.d, 0);
    assertEquals(1l << 53, pic.propertyD, 0);
  }

  public void testPropertyInjectionWithSetter() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+PropertyInjectionClass.class.getName()+"'>" +
      "    <property setter='setS'>" +
      "      <string value='hello' />" +
      "    </property>" +
      "  </object>" +
      "</objects>"
    );

    PropertyInjectionClass pic = (PropertyInjectionClass) wireContext.get("o");

    assertNull(pic.s);
    assertEquals("hello", pic.propertyS);
  }

  public void testBadPropertyDescriptor() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <object name='o' class='java.lang.String'>" +
      "    <property name='p'><bad-descriptor /></property>" +
      "  </object>" +
      "</objects>"
    );
    assertNotNull(problems);
    assertEquals(1, problems.size());
    assertTextPresent("couldn't parse property content element as a value descriptor: ", problems.get(0).getMsg());
  }

  public void testMissingPropertySetter() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <object name='o' class='java.lang.String'>" +
      "    <property><null/></property>" +
      "  </object>" +
      "</objects>"
    );
    assertNotNull(problems);
    assertTextPresent("property must have name or setter", problems.get(0).getMsg());
  }

  public void testMissingPropertyValueDescriptor() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <object name='o' class='java.lang.String'>" +
      "    <property name='bar' />" +
      "  </object>" +
      "</objects>"
    );
    assertNotNull(problems);
    assertTextPresent("property must have 1 descriptor element out of", problems.get(0).getMsg());
  }

  public static class InheritedPropertyInjectionClass extends PropertyInjectionClass {
  }

  public void testInheritedPropertyInjection() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+InheritedPropertyInjectionClass.class.getName()+"'>" +
      "    <property name='s'>" +
      "      <string value='hello' />" +
      "    </property>" +
      "  </object>" +
      "</objects>"
    );

    InheritedPropertyInjectionClass ipic = (InheritedPropertyInjectionClass) wireContext.get("o");

    assertNull(ipic.s);
    assertEquals("hello", ipic.propertyS);
  }

  public static class OverwrittenPropertyInjectionClass extends PropertyInjectionClass {
    String overwrittenPropertyS;
    @Override
    public void setS(String s) {
      overwrittenPropertyS = s;
    }
  }

  public void testOverwrittenPropertyInjection() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+OverwrittenPropertyInjectionClass.class.getName()+"'>" +
      "    <property name='s'>" +
      "      <string value='hello' />" +
      "    </property>" +
      "  </object>" +
      "</objects>"
    );

    OverwrittenPropertyInjectionClass opic = (OverwrittenPropertyInjectionClass) wireContext.get("o");

    assertNull(opic.s);
    assertNull(opic.propertyS);
    assertEquals("hello", opic.overwrittenPropertyS);
  }

  public static class InvokeClass {
    String text = "";
    public void name(String name) {
      text+=name;
    }
    public void wasHere() {
      text+=" was here !";
    }
  }

  public void testInvoke() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+InvokeClass.class.getName()+"'>" +
      "    <invoke method='name'>" +
      "      <arg>" +
      "        <string value='Killroy' />" +
      "      </arg>" +
      "    </invoke>" +
      "    <invoke method='wasHere' />" +
      "  </object>" +
      "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals(InvokeClass.class, o.getClass());
    assertEquals("Killroy was here !", ((InvokeClass)o).text);
  }

  public void testInvokeBadMethod() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+InvokeClass.class.getName()+"'>" +
      "    <invoke method='this-is-not-a-method'>" +
      "      <arg>" +
      "        <string value='that-is-true' />" +
      "      </arg>" +
      "    </invoke>" +
      "  </object>" +
      "</objects>"
    );

    try{
      wireContext.get("o");
      fail("expected exception");
    } catch(WireException e) {
      assertTextPresent("couldn't initialize object 'o': method this-is-not-a-method(java.lang.String) unavailable", e.getMessage());
    }
  }

  public void testInvokeBadArgs(){
    WireContext wireContext = createWireContext(
        "<objects>" +
        "  <object name='o' class='"+IntFieldInjectionClass.class.getName()+"'>" +
        "    <invoke method='addToVal'>" +
        "      <arg type='int'>" +
        "        <string value='foo' />" +
        "      </arg>" +
        "    </invoke>" +
        "  </object>" +
        "</objects>"
      );

      try{
        wireContext.get("o");
        fail("expected exception");
      } catch(WireException e) {
        assertTextPresent("couldn't initialize object 'o': couldn't invoke method addToVal", e.getMessage());
        assertTextPresent("couldn't invoke 'addToVal' with [foo]", e.getMessage());
      }
  }

  public void testInvokeWithoutMethodName() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <object name='o' class='java.lang.String'>" +
      "    <invoke />" +
      "  </object>" +
      "</objects>"
    );
    assertNotNull(problems);
    assertTextPresent("invoke must have method", problems.get(0).getMsg());
  }

  public static class InheritedInvokeClass extends InvokeClass {
  }

  public void testInheritedInvoke() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+InheritedInvokeClass.class.getName()+"'>" +
      "    <invoke method='name'>" +
      "      <arg>" +
      "        <string value='Killroy' />" +
      "      </arg>" +
      "    </invoke>" +
      "    <invoke method='wasHere' />" +
      "  </object>" +
      "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals(InheritedInvokeClass.class, o.getClass());
    assertEquals("Killroy was here !", ((InheritedInvokeClass)o).text);
  }

  public static class OverwrittenInvokeClass extends InvokeClass {
    public void wasHere() {
      text += "'s presence got overwritten :)";
    }
  }

  public void testOverwrittenInvoke() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+OverwrittenInvokeClass.class.getName()+"'>" +
      "    <invoke method='name'>" +
      "      <arg>" +
      "        <string value='Killroy' />" +
      "      </arg>" +
      "    </invoke>" +
      "    <invoke method='wasHere' />" +
      "  </object>" +
      "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals(OverwrittenInvokeClass.class, o.getClass());
    assertEquals("Killroy's presence got overwritten :)", ((OverwrittenInvokeClass)o).text);
  }

  public void testFactoryAttributeWithoutMethod() {
    List<Problem> problems = parseProblems(
        "<objects>" +
        "  <string name='s' value='hello' />" +
        "  <object name='o' factory='s' />" +
        "</objects>"
    );
    assertNotNull(problems);
    assertTextPresent("element 'object' with a element 'factory' or a attribute 'factory' must have a attribute 'method'", problems.get(0).getMsg());
  }

  public void testFactoryElementWithoutMethod() {
    List<Problem> problems = parseProblems(
        "<objects>" +
        "  <object name='o' >" +
        "     <factory><string name='s' value='hello' /></factory>" +
        "  </object>" +
        "</objects>"
    );
    assertNotNull(problems);
    assertTextPresent("element 'object' with a element 'factory' or a attribute 'factory' must have a attribute 'method'", problems.get(0).getMsg());
  }

  public void testFactoryElementAndClass(){
    List<Problem> problems = parseProblems(
        "<objects>" +
        "  <object name='o' class='"+DefaultConstructorClass.class.getName()+"'>" +
        "     <factory><string name='s' value='hello' /></factory>" +
        "  </object>" +
        "</objects>"
    );
    assertNotNull(problems);
    assertTextPresent("element 'factory' is specified together with attribute 'class' in element 'object': ", problems.get(0).getMsg());
  }

  public void testFactoryAttributeAndClass(){
    List<Problem> problems = parseProblems(
        "<objects>" +
        "  <object name='o' class='"+DefaultConstructorClass.class.getName()+"' factory='foo' />" +
        "</objects>"
    );
    assertNotNull(problems);
    assertTextPresent("attribute 'factory' is specified together with attribute 'class' in element 'object': ", problems.get(0).getMsg());
  }

  public void testFactoryElementAndAttribute(){
    List<Problem> problems = parseProblems(
        "<objects>" +
        "  <object name='o' factory='foo'>" +
        "     <factory><string name='s' value='hello' /></factory>" +
        "  </object>" +
        "</objects>"
    );
    assertNotNull(problems);
    assertTextPresent("element 'factory' is specified together with attribute 'factory' in element 'object': ", problems.get(0).getMsg());
  }
  
  public static class Foo {
    public static Object bar() {
      return null;
    }
  }

  public void testClassConstructorAndMethod(){
    List<Problem> problems = parseProblems(
        "<objects>" +
        "  <object name='o' class='"+Foo.class.getName()+"' method='bar'>" +
        "     <constructor/>" +
        "  </object>" +
        "</objects>"
    );
    assertNotNull(problems);
    assertTextPresent("attributes 'class' and 'method' indicate static method and also a 'constructor' element is specified for element 'object': ", problems.get(0).getMsg());
  }

  /* this error message has been removed because object binding is reused in 
   * the context of jpdl parsing, in which case other elements are allowed. 
  public void testBadOperation(){
    List<Problem> problems = parseProblems(
        "<objects>" +
        "  <object name='o' class='java.lang.String'>" +
        "     <bad-operation/>" +
        "  </object>" +
        "</objects>"
    );
    assertNotNull(problems);
    assertEquals(problems.toString(), 2, problems.size());
    assertTextPresent("no element parser for tag bad-operation in category operation", problems.get(0).getMsg());
    assertTextPresent("element 'object' can only have 'factory', 'arg', 'constructor' elements or an operation element", problems.get(1).getMsg());
    assertTextPresent("Invalid element 'bad-operation' in: ", problems.get(1).getMsg());
  }
  */

  public void testConstructorAndFactoryAttribute(){
    List<Problem> problems = parseProblems(
        "<objects>" +
        "  <object name='o' factory='foo' method='bar'>" +
        "     <constructor/>" +
        "  </object>" +
        "</objects>"
    );
    assertNotNull(problems);
    assertTextPresent("element 'object' with a 'constructor' element must have 'class' attribute", problems.get(0).getMsg());
  }

  public void testConstructorAndFactoryElement(){
    List<Problem> problems = parseProblems(
        "<objects>" +
        "  <object name='o' method='bar'>" +
        "     <factory>" +
        "       <null/>" +
        "     </factory>" +
        "     <constructor/>" +
        "  </object>" +
        "</objects>"
    );
    assertNotNull(problems);
    assertTextPresent("element 'object' with a 'constructor' element must have 'class' attribute", problems.get(0).getMsg());
  }

  public void testFactoryAttributeUnknownObject(){
    WireContext context = createWireContext(
        "<objects>" +
        "  <object name='o'  factory='foo' method='bar'/>" +
        "</objects>"
    );

    try{
      context.get("o");
      fail("expected exception");
    } catch (WireException e) {
      assertTextPresent("can't invoke method 'bar' on null, resulted from fetching object 'foo' from this wiring environment", e.getMessage());
    }
  }

  public void testFactoryAttributeNullObject(){
    WireContext context = createWireContext(
        "<objects>" +
        "  <object name='o'  factory='foo' method='bar'/>" +
        "  <null name='foo'/>" +
        "</objects>"
    );

    try{
      context.get("o");
      fail("expected exception");
    } catch (WireException e) {
      assertTextPresent("can't invoke method 'bar' on null, resulted from fetching object 'foo' from this wiring environment", e.getMessage());
    }
  }

  public void testFactoryElementNullObject(){
    WireContext context = createWireContext(
        "<objects>" +
        "  <object name='o' method='bar'>" +
        "    <factory>" +
        "       <null/>" +
        "    </factory>" +
        "  </object>" +
        "</objects>"
    );

    try{
      context.get("o");
      fail("expected exception");
    } catch (WireException e) {
      assertTextPresent("created factory object is null, can't invoke method 'bar' on it", e.getMessage());
    }
  }
}

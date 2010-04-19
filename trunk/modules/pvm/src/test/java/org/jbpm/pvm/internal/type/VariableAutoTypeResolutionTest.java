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
package org.jbpm.pvm.internal.type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.jbpm.pvm.activities.WaitState;
import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.type.variable.BlobVariable;
import org.jbpm.pvm.internal.type.variable.DoubleVariable;
import org.jbpm.pvm.internal.type.variable.LongVariable;
import org.jbpm.pvm.internal.type.variable.StringVariable;
import org.jbpm.pvm.internal.type.variable.TextVariable;
import org.jbpm.pvm.test.EnvironmentTestCase;

/**
 * @author Tom Baeyens
 */
public class VariableAutoTypeResolutionTest extends EnvironmentTestCase {

  public static ExecutionImpl startProcessInstance() {
    return (ExecutionImpl) ProcessDefinitionBuilder
    .startProcess()
      .startActivity(WaitState.class).initial()
      .endActivity()
    .endProcess()
    .startProcessInstance();
  }
  
  public void testStringVariable() {
    ExecutionImpl execution = startProcessInstance();
    
    execution.setVariable("v", "hello");
    
    Variable variable = execution.getVariableObject("v");
    
    assertEquals(StringVariable.class, variable.getClass());
    StringVariable stringVariable = (StringVariable) variable;
    assertEquals("hello", stringVariable.getObject());
    
    assertEquals("hello", execution.getVariable("v"));
  }

  public void testLongVariable() {
    ExecutionImpl execution = startProcessInstance();
    
    execution.setVariable("v", new Long(5));
    
    Variable variable = execution.getVariableObject("v");
    assertEquals(LongVariable.class, variable.getClass());
    LongVariable longVariable = (LongVariable) variable;
    assertEquals(new Long(5), longVariable.getObject());

    assertEquals(new Long(5), execution.getVariable("v"));
  }

  public void testDoubleVariable() {
    ExecutionImpl execution = startProcessInstance();
    
    execution.setVariable("v", new Double(5.5));
    
    Variable variable = execution.getVariableObject("v");
    assertEquals(DoubleVariable.class, variable.getClass());
    DoubleVariable doubleVariable = (DoubleVariable) variable;
    assertEquals(new Double(5.5), doubleVariable.getObject());

    assertEquals(new Double(5.5), execution.getVariable("v"));
  }

  public void testDateVariable() {
    ExecutionImpl execution = startProcessInstance();
    
    Calendar calendar = new GregorianCalendar();
    calendar.set(Calendar.YEAR, 2007);
    calendar.set(Calendar.MONTH, 10); // (10 == november)
    calendar.set(Calendar.DAY_OF_MONTH, 22);
    calendar.set(Calendar.HOUR_OF_DAY, 15);
    calendar.set(Calendar.MINUTE, 28);
    calendar.set(Calendar.SECOND, 57);
    calendar.set(Calendar.MILLISECOND, 374);
    
    execution.setVariable("v", calendar.getTime());
    
    Variable variable = execution.getVariableObject("v");
    assertEquals(StringVariable.class, variable.getClass());
    StringVariable stringVariable = (StringVariable) variable;
    assertEquals("2007-11-22 15:28:57,374", stringVariable.getObject());

    assertEquals(calendar.getTime(), execution.getVariable("v"));
  }

  public void testBooleanVariable() {
    ExecutionImpl execution = startProcessInstance();

    execution.setVariable("affirmative", Boolean.TRUE);
    execution.setVariable("negative", Boolean.FALSE);
    
    Variable variable = execution.getVariableObject("affirmative");
    assertEquals(StringVariable.class, variable.getClass());
    StringVariable stringVariable = (StringVariable) variable;
    assertEquals("T", stringVariable.getObject());
    
    variable = execution.getVariableObject("negative");
    assertEquals(StringVariable.class, variable.getClass());
    stringVariable = (StringVariable) variable;
    assertEquals("F", stringVariable.getObject());

    assertEquals(Boolean.TRUE, execution.getVariable("affirmative"));
    assertEquals(Boolean.FALSE, execution.getVariable("negative"));
  }

  public void testCharacterVariable() {
    ExecutionImpl execution = startProcessInstance();
    
    execution.setVariable("v", new Character('c'));
    
    Variable variable = execution.getVariableObject("v");
    assertEquals(StringVariable.class, variable.getClass());
    StringVariable stringVariable = (StringVariable) variable;
    assertEquals("c", stringVariable.getObject());
    
    assertEquals(new Character('c'), execution.getVariable("v"));
  }

  public void testByteVariable() {
    ExecutionImpl execution = startProcessInstance();
    
    execution.setVariable("v", new Byte((byte)78));
    
    Variable variable = execution.getVariableObject("v");
    assertEquals(LongVariable.class, variable.getClass());
    LongVariable longVariable = (LongVariable) variable;
    assertEquals(new Long(78), longVariable.getObject());
    
    assertEquals(new Byte((byte)78), execution.getVariable("v"));
  }

  public void testShortVariable() {
    ExecutionImpl execution = startProcessInstance();
    
    execution.setVariable("v", new Short((short)78));
    
    Variable variable = execution.getVariableObject("v");
    assertEquals(LongVariable.class, variable.getClass());
    LongVariable longVariable = (LongVariable) variable;
    assertEquals(new Long(78), longVariable.getObject());
    
    assertEquals(new Short((short)78), execution.getVariable("v"));
  }

  public void testIntegerVariable() {
    ExecutionImpl execution = startProcessInstance();
    
    execution.setVariable("v", new Integer(78));
    
    Variable variable = execution.getVariableObject("v");
    assertEquals(LongVariable.class, variable.getClass());
    LongVariable longVariable = (LongVariable) variable;
    assertEquals(new Long(78), longVariable.getObject());
    
    assertEquals(new Integer(78), execution.getVariable("v"));
  }

  public void testFloatVariable() {
    ExecutionImpl execution = startProcessInstance();
    
    execution.setVariable("v", new Float(78.65));
    
    Variable variable = execution.getVariableObject("v");
    assertEquals(DoubleVariable.class, variable.getClass());
    DoubleVariable doubleVariable = (DoubleVariable) variable;
    assertEquals(new Double((float)78.65), doubleVariable.getObject());
    
    assertEquals(new Float(78.65), execution.getVariable("v"));
  }

  public void testBytesVariable() {
    ExecutionImpl execution = startProcessInstance();
    
    byte[] bytes = generateBytes("a lot of bytes ", 500);
    execution.setVariable("v", bytes);
    
    Variable variable = execution.getVariableObject("v");
    assertEquals(BlobVariable.class, variable.getClass());
    
    BlobVariable blobVariable = (BlobVariable) variable;
    byte[] blobVariableBytes = (byte[]) blobVariable.getValue(execution);
    assertTrue(Arrays.equals(bytes, blobVariableBytes));
  }

  public void testCharsVariable() {
    ExecutionImpl execution = startProcessInstance();
    
    char[] chars = generateChars("a lot of bytes ", 500);
    assertTrue(chars.length>4500);
    execution.setVariable("v", chars);
    
    Variable variable = execution.getVariableObject("v");
    assertEquals(TextVariable.class, variable.getClass());
    
    assertTrue(Arrays.equals(chars, (char[]) execution.getVariable("v")));
  }

  public static class TestSerializable implements Serializable {
    private static final long serialVersionUID = 1L;
    int member;
    TestSerializable(int member){this.member = member;}
    public boolean equals(Object o) {
      if (! (o instanceof TestSerializable)) return false;
      return ( member == ((TestSerializable)o).member );
    }
  }

  public void testSerializableVariable() throws Exception {
    ExecutionImpl execution = startProcessInstance();
    
    TestSerializable testSerializable = new TestSerializable(76);
    execution.setVariable("v", testSerializable);
    
    Variable variable = execution.getVariableObject("v");
    
    assertEquals(BlobVariable.class, variable.getClass());
    BlobVariable blobVariable = (BlobVariable) variable;

    // blobVariable.getObject(); is used to get the bare bytes.
    // blobVariable.getValue() would also use the converter and 
    // then the deserialized object is returned
    // ...good idea i'll test that as well below :-)
    byte[] blobVariableBytes = (byte[]) blobVariable.getObject();
    byte[] expected = serialize(testSerializable);

    assertTrue(Arrays.equals(expected, blobVariableBytes));
    
    Object deserialized = blobVariable.getValue(execution);
    assertNotNull(deserialized);
    assertEquals(testSerializable, deserialized);
  }

  private byte[] serialize(TestSerializable testSerializable) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(testSerializable);
      oos.flush();
      oos.close();
      return baos.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("couldn't serialize", e);
    }
  }
  
  String generateString(String base, int multiplier) {
  	StringBuilder text = new StringBuilder();
    for (int i=0; i<multiplier; i++) {
      text.append(base);
    }
    return text.toString();
  }

  byte[] generateBytes(String base, int multiplier) {
    return generateString(base, multiplier).getBytes();
  }

  char[] generateChars(String base, int multiplier) {
    return generateString(base, multiplier).toCharArray();
  }
}

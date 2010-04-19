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
package org.jbpm.test.variables;

import java.io.Serializable;
import java.util.Date;

import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class VariableBasicTypesTest extends JbpmTestCase {

  public void checkVariableValue(Object variableValue) {
    deployJpdlXmlString(
      "<process name='var'>" +
      "  <start name='a'>" +
      "    <transition to='b' />" +
      "  </start>" +
      "  <state name='b'/>" +
      "</process>"
    );
    
    executionService.startProcessInstanceByKey("var", "one");
    executionService.setVariable("var.one", "msg", variableValue);
    assertEquals(variableValue, executionService.getVariable("var.one", "msg"));
  }

  public void testVariableTypeString() {
    checkVariableValue("hello");
  }

  public void testVariableTypeCharacter() {
    checkVariableValue(new Character('x'));
  }

  public void testVariableTypeBoolean() {
    checkVariableValue(Boolean.TRUE);
  }

  public void testVariableTypeByte() {
    checkVariableValue(new Byte((byte)5));
  }

  public void testVariableTypeShort() {
    checkVariableValue(new Short((short)5));
  }

  public void testVariableTypeInteger() {
    checkVariableValue(new Integer(5));
  }

  public void testVariableTypeLong() {
    checkVariableValue(new Long(5));
  }

  public void testVariableTypeFloat() {
    checkVariableValue(new Float(5.7));
  }

  public void testVariableTypeDouble() {
    checkVariableValue(new Double(5.7));
  }

  public void testVariableTypeDate() {
    checkVariableValue(new Date());
  }
  
  public static class SerializeMe implements Serializable {
    private static final long serialVersionUID = 1L;
    String text;
    public SerializeMe(String text) {
      this.text = text;
    }
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((text == null) ? 0 : text.hashCode());
      return result;
    }
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      SerializeMe other = (SerializeMe) obj;
      if (text == null) {
        if (other.text != null)
          return false;
      } else if (!text.equals(other.text))
        return false;
      return true;
    }
  }
  
  public void testVariableTypeSerializable() {
    SerializeMe originalValue = new SerializeMe(generateString("a lot of text ", 500));
    checkVariableValue(originalValue);
    
    // now check if an update still works ok
    // updating a serialized object might fail when the blob is cached by hibernate
    SerializeMe newValue = new SerializeMe(generateString("another text ", 500));
    executionService.setVariable("var.one", "msg", newValue);
    assertEquals(newValue, executionService.getVariable("var.one", "msg"));
  }
  
  protected String generateString(String base, int multiplier) {
  	StringBuilder text = new StringBuilder();
    for (int i=0; i<multiplier; i++) {
      text.append(base);
    }
    return text.toString();
  }
}

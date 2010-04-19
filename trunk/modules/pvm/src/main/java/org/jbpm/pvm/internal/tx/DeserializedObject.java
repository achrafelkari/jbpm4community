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
package org.jbpm.pvm.internal.tx;

import java.util.Arrays;

import org.jbpm.pvm.internal.model.ScopeInstanceImpl;
import org.jbpm.pvm.internal.type.converter.SerializableToBytesConverter;
import org.jbpm.pvm.internal.type.variable.BlobVariable;


/**
 * @author Tom Baeyens
 */
public class DeserializedObject {
  
  private static final SerializableToBytesConverter serializableToBytesConverter = new SerializableToBytesConverter();

  Object deserializedObject;
  ScopeInstanceImpl scopeInstance;
  BlobVariable blobVariable;
  
  public DeserializedObject(Object deserializedObject, ScopeInstanceImpl scopeInstance, BlobVariable blobVariable) {
    this.deserializedObject = deserializedObject;
    this.scopeInstance = scopeInstance;
    this.blobVariable = blobVariable;
  }
  
  public void flush() {
    String variableName = blobVariable.getKey();
    Object currentValue = scopeInstance.getVariable(variableName);

    // first check if the deserialized object still is the value for that variable.
    // a different, new object (of any type) might have been set in the meantime in that key-value-pair 
    if ( (currentValue!=null)
         && (currentValue==deserializedObject)
       ) {
      // next, we check if the serialized object was actually changed or not
      byte[] newBytes = (byte[]) serializableToBytesConverter.convert(currentValue, null, null);
      byte[] persistedBytes = blobVariable.getLob().extractBytes();
      // if it is changed
      if (!Arrays.equals(persistedBytes, newBytes)) {
        // then do an automatic update
        blobVariable.setValue(deserializedObject, scopeInstance);
      }
    }
  }
}

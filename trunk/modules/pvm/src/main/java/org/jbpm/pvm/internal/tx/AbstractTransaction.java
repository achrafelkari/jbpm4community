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

import java.util.ArrayList;
import java.util.List;


/**
 * @author Tom Baeyens
 */
public abstract class AbstractTransaction implements Transaction {
  
  List<DeserializedObject> deserializedObjects = null;

  public void registerDeserializedObject(DeserializedObject deserializedObject) {
    if (deserializedObjects==null) {
      deserializedObjects = new ArrayList<DeserializedObject>();
    }
    
    // if this object was registered before...
    for (DeserializedObject registeredObject: deserializedObjects) {
      if (registeredObject.deserializedObject==deserializedObject.deserializedObject) {
        // ...don't add it to deserializedObjects collection
        return;
      }
    }

    deserializedObjects.add(deserializedObject);
  }

  public void flushDeserializedObjects() {
    if (deserializedObjects!=null) {
      for (DeserializedObject deserializedObject: deserializedObjects) {
        deserializedObject.flush();
      }
    }
  }
}

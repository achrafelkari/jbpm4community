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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.pvm.internal.env.UserProvidedEnvironmentObject;
import org.jbpm.pvm.internal.wire.descriptor.AbstractDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.ProvidedObjectDescriptor;


/**
 * map of {@link Descriptor}s that serve as input for a {@link WireContext}.
 *
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 */
public class WireDefinition implements Serializable {

  private static final long serialVersionUID = 1L;
  // static Log log = Log.getLog(WireDefinition.class.getName());

  /** maps object names to {@link Descriptor}s */
  Map<String, Descriptor> descriptors;
  Map<Class<?>, String> descriptorNames;
  boolean useTypes = true;

  /** references all objects that must eagerly initialized.
   * @see WireContext */
  List<String> eagerInitNames;
  

  public WireDefinition() {
  }

  public WireDefinition(WireDefinition other, List<UserProvidedEnvironmentObject> txWireObjects) {
    if (other.descriptors!=null) {
      this.descriptors = new HashMap<String, Descriptor>(other.descriptors);
    }
    if (other.descriptorNames!=null) {
      this.descriptorNames = new HashMap<Class<?>, String>(other.descriptorNames);
    }
    if (other.eagerInitNames!=null) {
      this.eagerInitNames = new ArrayList<String>(other.eagerInitNames);
    }
    this.useTypes = other.useTypes;
    
    for (UserProvidedEnvironmentObject userProvidedEnvironmentObject: txWireObjects) {
      ProvidedObjectDescriptor descriptor = new ProvidedObjectDescriptor(
        userProvidedEnvironmentObject.getObject(),
        userProvidedEnvironmentObject.isTypeExposed(),
        userProvidedEnvironmentObject.getName()
      );
      addDescriptor(descriptor);
    }
  }

  public void addDescriptor(Descriptor descriptor) {
    if (descriptor!=null) {

      String name = descriptor.getName();

      if (useTypes) {
        Class<?> type = descriptor.getType(this);

        if (type!=null) {
          if ( (name==null)
               && (descriptor instanceof AbstractDescriptor)
             ) {
            name = type.getName();
            ((AbstractDescriptor)descriptor).setName(type.getName());
          }
          
          if ( (name!=null)
               && ( (descriptors==null)
                    || (!descriptors.containsKey(name))
                  )
             ) {
            // add all superclasses and interfaces to map to this descriptor
            addDescriptorType(type, descriptor, name);
          }
        }
      }
      
      if ( (name!=null)
           && (! hasDescriptor(name))
         ) {
        putDescriptor(name, descriptor);

        if (descriptor.isEagerInit()) {
          addEagerInitObjectName(name);
        }
      }
      
    }
  }

  void putDescriptor(String name, Descriptor descriptor) {
    if (descriptors==null) {
      descriptors = new HashMap<String, Descriptor>();
    }
    descriptors.put(name, descriptor);
  }

  void addDescriptorType(Class<?> type, Descriptor descriptor, String descriptorName) {
    if (type!=null) {
      if (descriptorNames==null) {
        descriptorNames = new HashMap<Class<?>, String>();
      }
      if (!descriptorNames.containsKey(type)) {
        descriptorNames.put(type, descriptorName);
      }
      addDescriptorType(type.getSuperclass(), descriptor, descriptorName);
      Class<?>[] interfaceTypes = type.getInterfaces();
      if (interfaceTypes!=null) {
        for (Class<?> interfaceType: interfaceTypes) {
          addDescriptorType(interfaceType, descriptor, descriptorName);
        }
      }
    }
  }
  
  public String getDescriptorName(Class<?> type) {
    return (descriptorNames!=null ? descriptorNames.get(type) : null ); 
  }

  /** the descriptor with the given name from the WireDefinition or <code>null</code>
   * if the object doesn't have a descriptor.
   */
  public Descriptor getDescriptor(String objectName) {
    if (descriptors==null) {
      return null;
    }
    return descriptors.get(objectName);
  }

  /** @return previous Descriptor associated with the given name, or null if there was no Descriptor for this name. */
  public synchronized Descriptor addDescriptor(String objectName, Descriptor descriptor) {
    if (descriptors==null) {
      descriptors = new HashMap<String, Descriptor>();
    }
    return descriptors.put(objectName, descriptor);
  }

  /** @return previous Descriptor associated with the given name, or null if there was no Descriptor for this name. */
  public synchronized Descriptor removeDescriptor(String objectName) {
    if (descriptors!=null) {
      return descriptors.remove(objectName);
    }
    return null;
  }

  public boolean hasDescriptor(String objectName) {
    return (descriptors!=null)
           && (descriptors.containsKey(objectName));
  }

  public void addEagerInitObjectName(String eagerInitObjectName) {
    if (eagerInitObjectName!=null) {
      if (eagerInitNames==null) {
        eagerInitNames = new ArrayList<String>();
      }
      eagerInitNames.add(eagerInitObjectName);
    }
  }

  public void addWireDefinition(WireDefinition wireDefinition) {
    if ( (wireDefinition!=null)
         && (wireDefinition.getDescriptors()!=null)
       ) {
      for (Descriptor descriptor: wireDefinition.getDescriptors().values()) {
        addDescriptor(descriptor);
      }
    }
  }

  public Set<Class<?>> getDescriptorTypes() {
    return (descriptorNames!=null ? descriptorNames.keySet() : null);
  }
  public Map<String, Descriptor> getDescriptors() {
    return descriptors;
  }
  public List<String> getEagerInitNames() {
    return eagerInitNames;
  }
  public void setEagerInitNames(List<String> eagerInitNames) {
    this.eagerInitNames = eagerInitNames;
  }
  public void setDescriptors(Map<String, Descriptor> descriptors) {
    this.descriptors = descriptors;
  }
}

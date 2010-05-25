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
package org.jbpm.pvm.internal.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;

import org.jbpm.api.JbpmException;
import org.jbpm.pvm.internal.env.EnvironmentImpl;

/**
 * @author Tom Baeyens
 */
public class JbpmEnvironmentElResolver extends ELResolver {

  EnvironmentImpl environment;

  public JbpmEnvironmentElResolver(EnvironmentImpl environment) {
    this.environment = environment;
  }

  public Object getValue(ELContext context, Object base, Object property) {
    // this resolver only resolves top level variable names to execution
    // variable names.
    // only handle if this is a top level variable
    if (base == null) {
      // we assume a NPE-check for property is not needed
      // i don't think the next cast can go wrong. can it?
      String name = (String) property;

      try {
        Object object = environment.get(name, false);
        context.setPropertyResolved(true);
        return object;
      } catch (JbpmException je) {
        // Property not found... ignore in this case and return null below...
        // Will be interpreted as property not found
      }
    }

    return null;
  }

  public boolean isReadOnly(ELContext context, Object base, Object property) {
    return true;
  }

  public void setValue(ELContext context, Object base, Object property, Object value) {
  }

  public Class< ? > getType(ELContext context, Object base, Object property) {
    return Object.class;
  }
  public Class< ? > getCommonPropertyType(ELContext context, Object base) {
    return Object.class;
  }
  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
    return null;
  }
}

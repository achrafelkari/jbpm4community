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
package org.jbpm.pvm.internal.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireDefinition;

/**
 * @author Tom Baeyens
 */
public class WireProperties implements Serializable {
  
  private static final long serialVersionUID = 1L;

  protected long dbid;
  protected int dbversion;
  protected WireContext wireContext;

  public Object get(String key) {
    if (wireContext==null) {
      return null;
    }
    return wireContext.get(key);
  }

  public Set<String> keys() {
    if (wireContext==null) {
      return Collections.emptySet();
    }
    return wireContext.keys();
  }
  
  public void add(Descriptor descriptor) {
    if (wireContext==null) {
      wireContext = new WireContext(new WireDefinition());
    }
    wireContext.getWireDefinition().addDescriptor(descriptor);
  }

  public WireContext getWireContext() {
    return wireContext;
  }
  public void setWireContext(WireContext wireContext) {
    this.wireContext = wireContext;
  }
  public long getDbid() {
    return dbid;
  }
}

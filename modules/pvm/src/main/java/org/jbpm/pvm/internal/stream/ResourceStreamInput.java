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
package org.jbpm.pvm.internal.stream;

import java.io.InputStream;

import org.jbpm.api.JbpmException;

/**
 * @author Tom Baeyens
 */
public class ResourceStreamInput extends StreamInput {
  
  protected ClassLoader classLoader;
  protected String resource;
  
  /** @throws JbpmException if resource is null */
  public ResourceStreamInput(String resource) {
    this(resource, null);
  }

  /** @throws JbpmException if resource is null */
  public ResourceStreamInput(String resource, ClassLoader classLoader) {
    if (resource==null) {
      throw new JbpmException("resource is null");
    }
    this.name = "resource://"+resource;
    this.resource = resource;
    this.classLoader = classLoader;
  }

  public InputStream openStream() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream stream = classLoader.getResourceAsStream(resource);

    if (stream == null) {
      stream = ResourceStreamInput.class.getClassLoader().getResourceAsStream(resource);
    }

    if (stream==null) {
      throw new JbpmException("resource "+resource+" does not exist");
    }
    return stream;
  }
}

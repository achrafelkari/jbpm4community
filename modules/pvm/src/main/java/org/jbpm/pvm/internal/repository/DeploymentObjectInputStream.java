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
package org.jbpm.pvm.internal.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import org.jbpm.pvm.internal.util.ReflectUtil;

/**
 * Helper class responsible for providing classes while deserializing variables.
 *
 * @author Maciej Swiderski swiderski.maciej@gmail.com
 */
public class DeploymentObjectInputStream extends ObjectInputStream {

  private final String deploymentId;

  public DeploymentObjectInputStream(InputStream stream, String deploymentId)
    throws IOException {
    super(stream);
    this.deploymentId = deploymentId;
  }

  @Override
  protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
    ClassNotFoundException {
    try {
      return ReflectUtil.classForName(desc.getName());
    }
    catch (ClassNotFoundException e) {
      // trying to get it from deployment
      ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
      ClassLoader deploymentClassLoader =
        new DeploymentClassLoader(contextClassLoader, deploymentId);
      return Class.forName(desc.getName(), false, deploymentClassLoader);
    }
  }

}

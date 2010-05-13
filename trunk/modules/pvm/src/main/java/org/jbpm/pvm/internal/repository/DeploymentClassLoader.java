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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.jbpm.api.JbpmException;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.session.RepositorySession;

/**
 * @author Tom Baeyens
 */
public class DeploymentClassLoader extends ClassLoader {

  private final String deploymentId;

  public DeploymentClassLoader(ClassLoader parent, String deploymentId) {
    super(parent);
    this.deploymentId = deploymentId;
  }

  @Override
  public URL findResource(String name) {
    URL url = null;
    byte[] bytes = getDeployment().getBytes(name);
    if (bytes != null) {
      try {
        url =
          new URL(null, "jbpm://" + deploymentId + "/" + name, new BytesUrlStreamHandler(bytes));
      }
      catch (MalformedURLException e) {
        throw new JbpmException("couldn't create url", e);
      }
    }
    return url;
  }

  protected DeploymentImpl getDeployment() {
    RepositorySession repositorySession =
      EnvironmentImpl.getFromCurrent(RepositorySession.class);
    return repositorySession.getDeployment(deploymentId);
  }

  private static class BytesUrlStreamHandler extends URLStreamHandler {

    private final byte[] bytes;

    public BytesUrlStreamHandler(byte[] bytes) {
      this.bytes = bytes;
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
      return new BytesUrlConnection(bytes, u);
    }
  }

  private static class BytesUrlConnection extends URLConnection {

    private final byte[] bytes;

    public BytesUrlConnection(byte[] bytes, URL url) {
      super(url);
      this.bytes = bytes;
    }

    @Override
    public void connect() throws IOException {
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return new ByteArrayInputStream(bytes);
    }
  }

  @Override
  public Class<?> findClass(String name) throws ClassNotFoundException {
    // find class bytecode
    String fileName = name.replace('.', '/') + ".class";
    byte[] bytecode = getDeployment().getBytes(fileName);
    if (bytecode == null) {
      throw new ClassNotFoundException(name);
    }

    // define class
    Class<?> clazz = defineClass(name, bytecode, 0, bytecode.length);

    // define package, if not defined already
    int packageIndex = name.lastIndexOf('.');
    if (packageIndex != -1) {
      String packageName = name.substring(0, packageIndex);
      Package classPackage = getPackage(packageName);
      if (classPackage == null) {
        Package myPackage = getClass().getPackage();
        if (myPackage != null) {
          definePackage(packageName, myPackage.getSpecificationTitle(),
            myPackage.getSpecificationVersion(), myPackage.getSpecificationVendor(),
            myPackage.getImplementationTitle(), myPackage.getImplementationVersion(),
            myPackage.getImplementationVendor(), null);
        }
        else {
          definePackage(packageName, null, null, null, null, null, null, null);
        }
      }
    }
    return clazz;
  }
}

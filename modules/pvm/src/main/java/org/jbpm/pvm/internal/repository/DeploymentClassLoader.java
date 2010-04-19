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
import org.jbpm.pvm.internal.util.IoUtil;


/**
 * @author Tom Baeyens
 */
public class DeploymentClassLoader extends ClassLoader {

  private String deploymentId = null;

  public DeploymentClassLoader(ClassLoader parent, String deploymentId ) {
    super(parent);
    this.deploymentId = deploymentId;
  }

  public URL findResource(String name) {
    URL url = null;
    byte[] bytes = getDeployment().getBytes(name);
    if (bytes!=null) {
      InputStream inputStream = new ByteArrayInputStream(bytes);
      try {
        url = new URL(null, "jbpm://"+deploymentId+"/"+name, new BytesUrlStreamHandler(inputStream));
      } catch (MalformedURLException e) {
        throw new JbpmException("couldn't create url", e);
      }
    }
    return url;
  }

  protected DeploymentImpl getDeployment() {
    RepositorySession repositorySession = EnvironmentImpl.getFromCurrent(RepositorySession.class);
    return repositorySession.getDeployment(deploymentId);
  }
  
  public static class BytesUrlStreamHandler extends URLStreamHandler {
    InputStream inputStream;
    public BytesUrlStreamHandler(InputStream inputStream) {
      this.inputStream = inputStream;
    }
    protected URLConnection openConnection(URL u) throws IOException {
      return new BytesUrlConnection(inputStream, u);
    }
  }

  public static class BytesUrlConnection extends URLConnection {
    InputStream inputStream;
    public BytesUrlConnection(InputStream inputStream, URL url) {
      super(url);
      this.inputStream = inputStream;
    }
    public void connect() throws IOException {
    }
    public InputStream getInputStream() throws IOException {
      return inputStream;
    }
  }

  public Class findClass(String name) throws ClassNotFoundException {
    Class clazz = null;

    String fileName = name.replace( '.', '/' ) + ".class";
    byte[] bytes = getDeployment().getBytes(fileName);
    if (bytes!=null) {
      try {
        InputStream inputStream = new ByteArrayInputStream(bytes);
        byte[] classBytes = IoUtil.readBytes(inputStream);
        clazz = defineClass(name, classBytes, 0, classBytes.length);

        // Add the package information
        final int packageIndex = name.lastIndexOf('.');
        if (packageIndex != -1) {
          final String packageName = name.substring(0, packageIndex);
          final Package classPackage = getPackage(packageName);
          if (classPackage == null) {
            definePackage(packageName, null, null, null, null, null, null, null);
          }
        }

      } catch (JbpmException e) {
        clazz = null;
      }
    }

    if (clazz==null) {
      throw new ClassNotFoundException("class '"+name+"' could not be found in deployment "+deploymentId);
    }

    return clazz;
  }
}

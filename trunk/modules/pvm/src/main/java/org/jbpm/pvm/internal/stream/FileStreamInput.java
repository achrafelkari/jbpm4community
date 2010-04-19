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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.jbpm.api.JbpmException;

/**
 * @author Tom Baeyens
 */
public class FileStreamInput extends StreamInput {
  
  protected File file;

  /** @throws JbpmException if file is null */
  public FileStreamInput(File file) {
    if (file==null) {
      throw new JbpmException("file is null");
    }

    try {
      this.name = file.toURL().toString();
    } catch (MalformedURLException e) {
      this.name = file.toString();
    }

    this.file = file;
  }

  public InputStream openStream() {
    InputStream stream = null;
    
    try {
      if (!file.exists()) {
        throw new JbpmException("file "+file+" doesn't exist");
      }
      if (file.isDirectory()) {
        throw new JbpmException("file "+file+" is a directory");
      }
      stream = new FileInputStream(file);
      
    } catch (Exception e) {
      throw new JbpmException("couldn't access file "+file+": "+e.getMessage(), e);
    }
    
    return stream;
  }

}

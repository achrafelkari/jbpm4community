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
package org.jbpm.pvm.internal.wire.descriptor;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireDefinition;
import org.jbpm.pvm.internal.wire.WireException;

/**
 * @author Tom Baeyens
 */
public class PropertiesDescriptor extends MapDescriptor {

  private static final long serialVersionUID = 1L;

  protected String url;
  protected String file;
  protected String resource;
  protected boolean isXml;
  
  public PropertiesDescriptor() {
    className = Properties.class.getName(); 
  }

  public Object construct(WireContext wireContext) {
    return new Properties();
  }
   
  public void initialize(Object object, WireContext wireContext) {
    String description = null;
    try {
      if (url!=null) {
        description = "url "+url;
        InputStream inputStream = new URL(url).openStream();
        load(object, inputStream);
      }
      
      if (file!=null) {
        description = "file "+file;
        InputStream inputStream = new FileInputStream(file);
        load(object, inputStream);
      }
      
      if (resource!=null) {
        description = "resource "+resource;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resource);
        if (inputStream==null) {
          throw new RuntimeException("resource "+resource+" doesn't exist");
        }
        load(object, inputStream);
      }
      
    } catch (Exception e) {
      throw new WireException("couldn't read properties from "+description, e);
    }
    
    super.initialize(object, wireContext);
  }
  
  public Class<?> getType(WireDefinition wireDefinition) {
    return Properties.class;
  }

  protected void load(Object object, InputStream inputStream) throws Exception {
    Properties properties = (Properties) object;
    if (isXml) {
      properties.loadFromXML(inputStream);
    } else {
      properties.load(inputStream);
    }
  }

  public String getFile() {
    return file;
  }
  public void setFile(String file) {
    this.file = file;
  }
  public String getResource() {
    return resource;
  }
  public void setResource(String resource) {
    this.resource = resource;
  }
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  public boolean isXml() {
    return isXml;
  }
  public void setXml(boolean isXml) {
    this.isXml = isXml;
  }
}

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
package org.jbpm.pvm.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jbpm.api.JbpmException;
import org.jbpm.pvm.internal.xml.Entity;
import org.jbpm.pvm.internal.xml.Parser;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * an implementation of {@link Entity} based on a {@link URL} used in the 
 * {@link Parser}s implementation of {@link EntityResolver}.
 * 
 * @author Tom Baeyens
 */
public class UrlEntity implements Entity {

  protected String systemId = null;
  protected URL url = null;

  public UrlEntity(URL url, String systemId) {
    this.url = url;
    this.systemId = systemId;
  }
  
  public UrlEntity(String resource, ClassLoader classLoader) {
    this.url = classLoader.getResource(resource);
    if (url!=null) {
      this.systemId = url.toString();
    } else {
      throw new JbpmException("couldn't create Entity from resource "+resource);
    }
  }

  public InputSource getInputSource() {
    try {
      InputStream stream = url.openStream();
      InputSource inputSource = new InputSource(stream);
      inputSource.setSystemId(systemId);
      return inputSource;
    } catch (IOException e) {
      throw new JbpmException("couldn't open stream from url "+url, e);
    }
  }
}

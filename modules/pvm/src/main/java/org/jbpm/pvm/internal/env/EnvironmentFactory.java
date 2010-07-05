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
package org.jbpm.pvm.internal.env;

import java.io.InputStream;
import java.io.Serializable;

import org.xml.sax.InputSource;

/**
 * factory for {@link EnvironmentImpl}s.  
 * 
 * <p>Default implementation is 
 * {@link PvmEnvironmentFactory}. EnvironmentFactory is thread safe, you 
 * should use one environment factory for all your threads.
 * </p>
 * 
 * <p>Easiest way to obtain an EnvironmentFactory is with
 * <ul>
 *   <li>{@link #parseResource(String)}</li>
 *   <li>{@link #parseInputStream(InputStream)}</li> 
 *   <li>{@link #parseInputSource(InputSource)}</li>
 *   <li>{@link #parseXmlString(String)}</li>
 * </ul>
 * </p>
 * 
 * <p>For the default parser's XML schema, see {@link PvmEnvironmentFactoryParser}.</p>
 * 
 * @author Tom Baeyens
 */
public interface EnvironmentFactory extends Context, Serializable {
  
  /**
   * open a new EnvironmentImpl.  The client is responsible for 
   * closing the environment with {@link EnvironmentImpl#close()}.
   */
  EnvironmentImpl openEnvironment();
  
  /**
   * closes this environment factory and cleans any allocated 
   * resources.
   */
  void close();

  
}

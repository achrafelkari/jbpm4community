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
package org.jbpm.internal.log;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;


/**
 * @author Tom Baeyens
 */
public class Jdk14LogFactory implements LogFactory {
  
  public Jdk14LogFactory() {
    initializeJdk14Logging();
  }

  public Log getLog(String name) {
    return new Jdk14Log(Logger.getLogger(name));
  }

  /** redirects commons logging to JDK 1.4 logging.  This can be handy when 
   * you have log4j on the classpath, but still want to use the JDK logging. */
  public static synchronized void redirectCommonsToJdk14() {
    System.setProperty("org.apache.commons.logging.Log",
                       "org.apache.commons.logging.impl.Jdk14Logger" );
  }

  /** configures JDK 1.4 logging from the resource file <code>logging.properties</code> */
  public static synchronized void initializeJdk14Logging() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream("logging.properties");
    try {
      if (inputStream != null) {
        LogManager.getLogManager().readConfiguration(inputStream);

        String redirectCommons = LogManager.getLogManager().getProperty("redirect.commons.logging");
        if ( (redirectCommons!=null)
             && (! redirectCommons.equalsIgnoreCase("disabled"))
             && (! redirectCommons.equalsIgnoreCase("off"))
             && (! redirectCommons.equalsIgnoreCase("false"))
           ) {
          redirectCommonsToJdk14();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("couldn't initialize logging properly", e);
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}

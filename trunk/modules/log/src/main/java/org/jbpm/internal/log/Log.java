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


/**
 * @author Tom Baeyens
 */
public abstract class Log {
  
  static LogFactory logFactory;

  public static synchronized Log getLog(String name) {
    if (logFactory==null) {
      
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      // if logging.properties is available on the classpath
      if (classLoader.getResource("logging.properties")!=null) {
        logFactory = new Jdk14LogFactory();
        
      // if log4j is available on the classpath
      } else if (isLog4jAvailable(classLoader)) {
        logFactory = new Log4jLogFactory();
        
      } else {
        logFactory = new Jdk14LogFactory();
         
      }
    }
    return logFactory.getLog(name);
  }

  static boolean isLog4jAvailable(ClassLoader classLoader) {
    try {
      Class.forName("org.apache.log4j.LogManager", false, classLoader);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }


  public abstract void error(String msg);
  public abstract void error(String msg, Throwable exception);

  public abstract boolean isInfoEnabled();
  public abstract void info(String msg);
  public abstract void info(String msg, Throwable exception);

  public abstract boolean isDebugEnabled();
  public abstract void debug(String msg);
  public abstract void debug(String msg, Throwable exception);

  public abstract boolean isTraceEnabled();
  public abstract void trace(String msg);
  public abstract void trace(String msg, Throwable exception);
  
  public abstract boolean isWarnEnabled();
  public abstract void warn(String msg);
  public abstract void warn(String msg, Throwable exception);
  
}

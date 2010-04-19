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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jbpm.internal.log.Log;


/**
 * @author Tom Baeyens
 */
public class Jdk14Log extends Log {
  
  Logger log;

  public Jdk14Log(Logger logger) {
    this.log = logger;
  }

  public void error(String msg) {
    log.log(Level.SEVERE, msg);
  }

  public void error(String msg, Throwable exception) {
    log.log(Level.SEVERE, msg, exception);
  }

  public boolean isInfoEnabled() {
    return log.isLoggable(Level.INFO);
  }

  public void info(String msg) {
    log.log(Level.INFO, msg);
  }

  public void info(String msg, Throwable exception) {
    log.log(Level.INFO, msg, exception);
  }

  public boolean isDebugEnabled() {
    return log.isLoggable(Level.FINE);
  }

  public void debug(String msg) {
    log.log(Level.FINE, msg);
  }

  public void debug(String msg, Throwable exception) {
    log.log(Level.FINE, msg, exception);
  }

  public boolean isTraceEnabled() {
    return log.isLoggable(Level.FINEST);
  }

  public void trace(String msg) {
    log.log(Level.FINEST, msg);
  }

  public void trace(String msg, Throwable exception) {
    log.log(Level.FINEST, msg, exception);
  }
  
  public boolean isWarnEnabled() {
    return log.isLoggable(Level.WARNING);
  }
  
  public void warn(String msg) {
    log.warning(msg);
  }
  
  public void warn(String msg, Throwable exception) {
    log.log(Level.WARNING, msg, exception);
  }
  
}

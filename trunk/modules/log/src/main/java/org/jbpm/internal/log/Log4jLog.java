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

import org.apache.log4j.Level;

/**
 * @author Tom Baeyens
 */
public class Log4jLog extends Log {

  org.apache.log4j.Logger log;
  
  public Log4jLog(org.apache.log4j.Logger log) {
    this.log = log;
  }

  public void error(String msg) {
    log.error(msg);
  }

  public void error(String msg, Throwable exception) {
    log.error(msg, exception);
  }

  public boolean isInfoEnabled() {
    return log.isInfoEnabled();
  }

  public void info(String msg) {
    log.info(msg);
  }

  public void info(String msg, Throwable exception) {
    log.info(msg, exception);
  }

  public boolean isDebugEnabled() {
    return log.isDebugEnabled();
  }

  public void debug(String msg) {
    log.debug(msg);
  }

  public void debug(String msg, Throwable exception) {
    log.debug(msg, exception);
  }

  public boolean isTraceEnabled() {
    return log.isTraceEnabled();
  }

  public void trace(String msg) {
    log.trace(msg);
  }

  public void trace(String msg, Throwable exception) {
    log.trace(msg, exception);
  }
  
  public boolean isWarnEnabled() {
    return log.isEnabledFor(Level.WARN);
  }
  
  public void warn(String msg) {
    log.warn(msg);
  }
  
  public void warn(String msg, Throwable exception) {
    log.warn(msg, exception);
  }
  
}

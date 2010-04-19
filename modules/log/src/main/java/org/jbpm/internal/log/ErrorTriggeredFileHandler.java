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
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.MemoryHandler;


/**
 * @author Tom Baeyens
 */
public class ErrorTriggeredFileHandler extends Handler {

  private static final int    DEFAULT_SIZE       = 500;
  private static final Level  DEFAULT_PUSH_LEVEL = Level.SEVERE;
  private static final String DEFAULT_PATTERN    = "%h/jbpm%u.log";
  
  DecoratedMemoryHandler memoryHandler = null;
  FileHandler fileHandler = null;

  public ErrorTriggeredFileHandler() throws SecurityException, IOException {
    fileHandler = getConfiguredTarget();
    memoryHandler = new DecoratedMemoryHandler(fileHandler, getConfiguredSize(), getConfiguredPushLevel());
  }

  private static Level getConfiguredPushLevel() {
    LogManager manager = LogManager.getLogManager();
    String pushLevelText = manager.getProperty(ErrorTriggeredFileHandler.class.getName() + ".push");
    if (pushLevelText == null) {
      return DEFAULT_PUSH_LEVEL;
    }
    try {
      return Level.parse(pushLevelText.trim());
    } catch (Exception ex) {
      return DEFAULT_PUSH_LEVEL;
    }
  }

  protected static int getConfiguredSize() {
    LogManager manager = LogManager.getLogManager();
    String sizeText = manager.getProperty(ErrorTriggeredFileHandler.class.getName() + ".size");
    if (sizeText == null) {
      return DEFAULT_SIZE;
    }
    try {
      return Integer.parseInt(sizeText.trim());
    } catch (Exception ex) {
      return DEFAULT_SIZE;
    }
  }

  protected static FileHandler getConfiguredTarget() throws SecurityException, IOException {
    LogManager manager = LogManager.getLogManager();
    String pattern = manager.getProperty(ErrorTriggeredFileHandler.class.getName() + ".pattern");
    if (pattern == null) {
      pattern = DEFAULT_PATTERN;
    }
    return new FileHandler(pattern);
  }

  
  public class DecoratedMemoryHandler extends MemoryHandler {
    public DecoratedMemoryHandler(FileHandler target, int size, Level pushLevel) {
      super(target, size, pushLevel);
    }
    public void push() {
      fileHandler.setFormatter(new LogFormatter());
      super.push();
      LogRecord emptyLine = new LogRecord(Level.INFO, "");
      emptyLine.setLoggerName("");
      fileHandler.publish(emptyLine);
      LogRecord line = new LogRecord(Level.INFO, "---- END OF TRIGGERED PUSH ---------------------------------------------------");
      line.setLoggerName("");
      fileHandler.publish(line);
      fileHandler.publish(emptyLine);
      fileHandler.publish(emptyLine);
    }
  }

  public void close() throws SecurityException {
    memoryHandler.close();
  }

  public void flush() {
    memoryHandler.flush();
  }

  public void publish(LogRecord record) {
    memoryHandler.publish(record);
  }
}

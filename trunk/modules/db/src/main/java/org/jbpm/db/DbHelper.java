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
package org.jbpm.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.jbpm.api.JbpmException;
import org.jbpm.internal.log.Jdk14LogFactory;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.util.IoUtil;

/**
 * @author Tom Baeyens
 */
public class DbHelper {

  private static final Log log = Log.getLog(DbHelper.class.getName());

  private DbHelper() {
    // hide default constructor to prevent instantiation
  }

  public static void initializeLogging() {
    Jdk14LogFactory.initializeJdk14Logging();
  }

  public static void printSyntax(Class<?> clazz) {
    log.info("Syntax: java -cp ... " + clazz.getName() + " database [delimiter]");
    log.info("where database is one of {oracle, postgresql, mysql, hsqldb}");
    log.info("and delimiter is the db sql delimiter.  default delimiter is ;");
  }

  public static void executeSqlResource(String resource, Session session) {
    InputStream stream = DbHelper.class.getClassLoader().getResourceAsStream(resource);
    if (stream == null) {
      throw new JbpmException("resource not found: " + resource);
    }

    try {
      byte[] bytes = IoUtil.readBytes(stream);
      String fileContents = new String(bytes);
      List<String> commands = extractCommands(fileContents);

      log.info("--- Executing DB Commands -------------------------");
      for (String command : commands) {
        log.info(command);
        try {
          int result = session.createSQLQuery(command).executeUpdate();
          log.info("--- Result: " + result + " --------------------------");
        }
        catch (Exception e) {
          e.printStackTrace();
          log.info("-----------------------------------------------");
        }
      }
    }
    catch (IOException e) {
      throw new JbpmException("could not read resource: " + resource, e);
    }
    finally {
      IoUtil.close(stream);
    }
  }

  public static List<String> extractCommands(String fileContents) {
    List<String> commands = new ArrayList<String>();
    int i = 0;
    while (i < fileContents.length()) {
      int j = fileContents.indexOf(";", i);
      if (j == -1) {
        j = fileContents.length();
      }
      String command = fileContents.substring(i, j).trim();
      if (command.length() > 0) {
        commands.add(command);
      }
      i = j + 1;
    }
    return commands;
  }
}

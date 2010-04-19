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
package org.jbpm.pvm.internal.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class Launcher extends Thread {

  Task task;
  String[] command;
  String endMsg;
  File dir;
  Map<String, String> environmentVariables = new HashMap<String, String>();

  public Launcher(Task task, String[] command, String endMsg, String dir) {
    this.task = task;
    this.command = command;
    this.endMsg = endMsg;
    this.dir = (dir!=null ? new File(dir) : null);
  }

  public void run() {
    String logCommand = "";
    for (String commandPart: command) {
      logCommand += commandPart+" ";
    }
    try {
      task.log("starting '" + logCommand + "'...");
      ProcessBuilder processBuilder = new ProcessBuilder(command)
        .redirectErrorStream(true)
        .directory(dir);
      
      if (!environmentVariables.isEmpty()) {
        processBuilder.environment().putAll(environmentVariables);
      }
      
      Process process = processBuilder.start();

      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line = "";
      while (line.indexOf(endMsg) == -1) {
        line = reader.readLine();
        task.log(line);
      }

      task.log("'" + command[0] + "' started.");
    } catch (IOException e) {
      throw new BuildException("couldn't start '" + logCommand + "'", e);
    }
  }

  public void setEnvironmentVariable(String variableName, String value) {
    environmentVariables.put(variableName, value);
  }
  
  
}

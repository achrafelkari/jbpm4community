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

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class StartJBossTask extends Task {

  private static final String END_MESSAGE = " Started in ";

  String configuration;
  String jbosshome;
  String bindaddress;

  public void execute() throws BuildException {
    // get some environment variableInstances
    String fileSeparator = System.getProperty("file.separator");
    String os = getProject().getProperty("os.name").toLowerCase();

    // build the command string
    String program;
    if (os.indexOf("windows") != -1) {
      program = getJBossHome() + fileSeparator + "bin" + fileSeparator + "run.bat";
    }
    else if (os.indexOf("linux") != -1 || os.indexOf("mac") != -1) {
      program = getJBossHome() + fileSeparator + "bin" + fileSeparator + "run.sh";
    }
    else {
      throw new BuildException("os '" + os + "' not supported in the startjboss task.");
    }

    List<String> command = new ArrayList<String>();
    command.add(program);

    if (configuration != null) {
      command.add("-c");
      command.add(configuration);
    }

    if (bindaddress != null) {
      command.add("-b");
      command.add(bindaddress);
    }

    // launch the command and wait till the END_MESSAGE appears
    Thread launcher = new Launcher(this, command.toArray(new String[0]), END_MESSAGE, null);
    launcher.start();
    try {
      launcher.join();
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  String getJBossHome() {
    if (jbosshome != null) {
      return jbosshome;
    }
    String jbossHomeSysProp = getProject().getProperty("jboss.home");
    if (jbossHomeSysProp != null) {
      return jbossHomeSysProp;
    }
    throw new BuildException("jboss home not specified");
  }

  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }
  public void setJbosshome(String jbosshome) {
    this.jbosshome = jbosshome;
  }

  public void setBindaddress(String bindaddress) {
    this.bindaddress = bindaddress;
  }
}

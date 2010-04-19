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

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;
import org.jbpm.api.NewDeployment;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.RepositoryService;

/**
 * ant task for deploying process files and business archives.
 */
public class JbpmDeployTask extends MatchingTask {

  String jbpmCfg = null;
  File file = null;
  List fileSets = new ArrayList();
  boolean failOnError = true;

  public void execute() throws BuildException {
    Thread currentThread = Thread.currentThread();
    ClassLoader originalClassLoader = currentThread.getContextClassLoader();
    currentThread.setContextClassLoader(JbpmDeployTask.class.getClassLoader());
    try {
      // get the ProcessEngineImpl
      ProcessEngine processEngine = AntHelper.getProcessEngine(jbpmCfg);
      
      // if attribute process is set, deploy that process file
      if (file!=null) {
        deployFile(processEngine, file);
      }
      
      // loop over all files that are specified in the filesets
      Iterator iter = fileSets.iterator();
      while (iter.hasNext()) {
        FileSet fileSet = (FileSet) iter.next();
        DirectoryScanner dirScanner = fileSet.getDirectoryScanner(getProject());
        File baseDir = dirScanner.getBasedir();
        String[] includedFiles = dirScanner.getIncludedFiles();
        List excludedFiles = Arrays.asList(dirScanner.getExcludedFiles());

        for (int i = 0; i < includedFiles.length; i++) {
          String fileName = includedFiles[i];
          if (!excludedFiles.contains(fileName)) {
            File file = new File(baseDir, fileName);
            deployFile(processEngine, file);
          }
        }
      }
      
    } finally {
      currentThread.setContextClassLoader(originalClassLoader);
    }
  }

  protected void deployFile(ProcessEngine processEngine, File processFile) {
    RepositoryService repositoryService = processEngine.getRepositoryService();
    NewDeployment deployment = repositoryService.createDeployment();
    deployment.setName(processFile.getName());
    deployment.setTimestamp(System.currentTimeMillis());
    
    if (processFile.getName().endsWith(".xml")) {
      log("deploying process file "+processFile.getName());
      deployment.addResourceFromFile(processFile);
      
    } else if (processFile.getName().endsWith("ar")) {
      log("deploying business archive "+processFile.getName());
      try {
        FileInputStream fileInputStream = new FileInputStream(processFile);
        ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
        deployment.addResourcesFromZipInputStream(zipInputStream);
      } catch (Exception e) {
        throw new BuildException("couldn't read business archive "+processFile, e);
      }

    } else {
      throw new BuildException("unsupported extension: "+processFile+"  Only .xml files and .*ar archives are supported");
    }
    
    deployment.deploy();
  }

  public void addFileset(FileSet fileSet) {
    this.fileSets.add(fileSet);
  }
  public void setJbpmCfg(String jbpmCfg) {
    this.jbpmCfg = jbpmCfg;
  }
  public void setFile(File file) {
    this.file = file;
  }
  public void setFailOnError(boolean failOnError) {
    this.failOnError = failOnError;
  }
}

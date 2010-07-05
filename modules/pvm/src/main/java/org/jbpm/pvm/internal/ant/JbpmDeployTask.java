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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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

  private String jbpmCfg;
  private File file;
  private List<FileSet> fileSets = new ArrayList<FileSet>();

  public void execute() throws BuildException {
    Thread currentThread = Thread.currentThread();
    ClassLoader originalClassLoader = currentThread.getContextClassLoader();
    currentThread.setContextClassLoader(JbpmDeployTask.class.getClassLoader());
    try {
      // get the ProcessEngineImpl
      ProcessEngine processEngine = AntHelper.getProcessEngine(jbpmCfg);

      // if attribute process is set, deploy that process file
      if (file != null) {
        deployFile(processEngine, file);
      }

      // loop over all files that are specified in the filesets
      for (FileSet fileSet : fileSets) {
        DirectoryScanner dirScanner = fileSet.getDirectoryScanner(getProject());
        File baseDir = dirScanner.getBasedir();

        String[] excludedFiles = dirScanner.getExcludedFiles();
        // sort excluded files in preparation for binary search
        Arrays.sort(excludedFiles);

        for (String fileName : dirScanner.getIncludedFiles()) {
          if (Arrays.binarySearch(excludedFiles, fileName) < 0) {
            File file = new File(baseDir, fileName);
            deployFile(processEngine, file);
          }
        }
      }
    }
    finally {
      currentThread.setContextClassLoader(originalClassLoader);
    }
  }

  protected void deployFile(ProcessEngine processEngine, File processFile) {
    RepositoryService repositoryService = processEngine.getRepositoryService();
    String fileName = processFile.getName();

    NewDeployment deployment = repositoryService.createDeployment();
    deployment.setName(fileName);
    deployment.setTimestamp(System.currentTimeMillis());

    try {
      String contentType = new URL("file", null, processFile.getPath()).openConnection()
        .getContentType();
      // XXX getContentType() is supposed to return null if not known,
      // yet sun jdk returns "content/unknown"
      if (contentType == null || "content/unknown".equals(contentType)) {
        // guess content type from file name
        if (fileName.endsWith(".bar") || fileName.endsWith(".jar") || fileName.endsWith(".zip"))
          contentType = "application/zip";
        else if (fileName.endsWith(".xml"))
          contentType = "application/xml";
        else
          throw new BuildException("failed to determine content type of " + processFile);
      }

      if ("application/xml".equals(contentType)) {
        log("deploying process file " + fileName);
        deployment.addResourceFromFile(processFile);
      }
      else if ("application/zip".equals(contentType)) {
        log("deploying business archive " + fileName);
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(processFile));
        deployment.addResourcesFromZipInputStream(zipInputStream);
      }
      else {
        throw new BuildException("unsupported content type: " + contentType
          + " - only xml and zip files are allowed");
      }
    }
    catch (IOException e) {
      throw new BuildException("failed to read: " + processFile, e);
    }
    deployment.deploy();
  }

  public void addFileset(FileSet fileSet) {
    fileSets.add(fileSet);
  }
  public void setJbpmCfg(String jbpmCfg) {
    this.jbpmCfg = jbpmCfg;
  }
  public void setFile(File file) {
    this.file = file;
  }
}

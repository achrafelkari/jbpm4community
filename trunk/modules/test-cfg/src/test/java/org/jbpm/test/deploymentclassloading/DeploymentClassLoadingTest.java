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
package org.jbpm.test.deploymentclassloading;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.jbpm.pvm.internal.util.IoUtil;
import org.jbpm.test.JbpmCustomCfgTestCase;


/**
 * @author Tom Baeyens
 */
public class DeploymentClassLoadingTest extends JbpmCustomCfgTestCase {
  
  static String testClassesDir = 
      DeploymentClassLoadingTest.class
          .getProtectionDomain()
          .getCodeSource()
          .getLocation()
          .getFile();

  public void testCustomEventListener() throws Exception {
    String originalFileName = testClassesDir+"org/jbpm/test/deploymentclassloading/CustomEventListener.class";
    File originalFile = new File(originalFileName);
    
    FileInputStream fileInputStream = new FileInputStream(originalFileName);
    byte[] classBytes = IoUtil.readBytes(fileInputStream);
    fileInputStream.close();
    
    String hiddenFileName = originalFileName+".hiddenFromTestClasspath";
    File hiddenFile = new File(hiddenFileName);
    assertTrue(originalFile.renameTo(hiddenFile));
    
    try {
      String deploymentDbid = repositoryService.createDeployment()
          .addResourceFromString("xmlstring.jpdl.xml", 
                "<process name='CustomEventListenerClassLoading'>" +
                "  <start>" +
                "    <transition to='wait'>" +
                "      <event-listener class='org.jbpm.test.deploymentclassloading.CustomEventListener' />" +
                "    </transition>" +
                "  </start>" +
                "  <state name='wait' />" +
                "</process>" )
          .addResourceFromInputStream("org/jbpm/test/deploymentclassloading/CustomEventListener.class", new ByteArrayInputStream(classBytes))
          .deploy();
  
      registerDeployment(deploymentDbid);
      
      String processInstanceId = executionService.startProcessInstanceByKey("CustomEventListenerClassLoading").getId();
      
      assertEquals("Executed", executionService.getVariable(processInstanceId, "CustomEventListener"));

    } finally { 
      hiddenFile.renameTo(originalFile);
    }
  }

  public void testCustomActivityBehaviour() throws Exception {
    String originalFileName = testClassesDir+"org/jbpm/test/deploymentclassloading/CustomActivity.class";
    File originalFile = new File(originalFileName);
    
    FileInputStream fileInputStream = new FileInputStream(originalFileName);
    byte[] classBytes = IoUtil.readBytes(fileInputStream);
    fileInputStream.close();
    
    String hiddenFileName = originalFileName+".hiddenFromTestClasspath";
    File hiddenFile = new File(hiddenFileName);
    assertTrue(originalFile.renameTo(hiddenFile));
    try {
      
      String deploymentDbid = repositoryService.createDeployment()
          .addResourceFromString("xmlstring.jpdl.xml", 
                "<process name='CustomActivityClassLoading'>" +
                "  <start>" +
                "    <transition to='c' />" +
                "  </start>" +
                "  <custom name='c' class='org.jbpm.test.deploymentclassloading.CustomActivity'>" +
                "    <transition to='wait' />" +
                "  </custom>" +
                "  <state name='wait' />" +
                "</process>" )
          .addResourceFromInputStream("org/jbpm/test/deploymentclassloading/CustomActivity.class", new ByteArrayInputStream(classBytes))
          .deploy();
  
      registerDeployment(deploymentDbid);
      
      String processInstanceId = executionService.startProcessInstanceByKey("CustomActivityClassLoading").getId();
      
      assertEquals("Executed", executionService.getVariable(processInstanceId, "CustomActivity"));
      
    } finally { 
      hiddenFile.renameTo(originalFile);
    }
  }
}

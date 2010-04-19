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
package org.jbpm.test.deploy;

import java.io.InputStream;
import java.util.Arrays;

import org.jbpm.api.ProcessDefinition;
import org.jbpm.pvm.internal.util.IoUtil;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class ImageTest extends JbpmTestCase {

  public void testImage() {
    String deploymentId = repositoryService
        .createDeployment()
        .addResourceFromClasspath("org/jbpm/test/deploy/ImageTest.jpdl.xml")
        .addResourceFromClasspath("org/jbpm/test/deploy/ImageTest.png")
        .deploy();
    
    ProcessDefinition processDefinition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("ImageTest")
        .uniqueResult();
    
    String imageResourceName = processDefinition.getImageResourceName();
    assertEquals("org/jbpm/test/deploy/ImageTest.png", imageResourceName);
    
    InputStream inputStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), imageResourceName);
    byte[] imageBytes = IoUtil.readBytes(inputStream);
    
    inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/jbpm/test/deploy/ImageTest.png");
    byte[] expectedImageBytes = IoUtil.readBytes(inputStream);
    
    assertTrue(Arrays.equals(expectedImageBytes, imageBytes));
    
    repositoryService.deleteDeploymentCascade(deploymentId);
  }
}

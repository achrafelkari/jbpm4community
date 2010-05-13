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
/**
 * 
 */
package org.jbpm.test.deploy;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import org.jbpm.api.NewDeployment;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.test.JbpmTestCase;

/**
 * Test case for the various ways of setting a process, image, etc as input of a deployment.
 * 
 * @author Joram Barrez
 */
public class DeploymentResourceTest extends JbpmTestCase {

  public void testZippedResourceDeployment() throws IOException {
    InputStream inputStream = getClass().getResourceAsStream("process.zip");
    ZipInputStream zipInputStream = new ZipInputStream(inputStream);
    try {
      NewDeployment newDeployment = repositoryService.createDeployment();
      newDeployment.addResourcesFromZipInputStream(zipInputStream);
      String deployId = newDeployment.deploy();

      ProcessDefinition procDef = repositoryService
        .createProcessDefinitionQuery()
        .deploymentId(deployId)
        .uniqueResult();
      assertNotNull(procDef);
      assertEquals("ImageTest", procDef.getName());
      repositoryService.deleteDeploymentCascade(deployId);
    }
    finally {
      zipInputStream.close();
    }
  }

}

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
package org.jbpm.test.process;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class DeploymentResourcesTest extends JbpmTestCase {

  public void testProcessWithNameOnly() {
    byte[] lotOfBytes = generateString("a lot of bytes ", 5000).getBytes();
    byte[] otherBytes = generateString("other bytes ", 5000).getBytes();
    
    String deploymentDbiId = 
    repositoryService.createDeployment()
      .addResourceFromString("xmlstring.jpdl.xml", 
                 "<process name='Insurance claim'>" +
                 "  <start />" +
                 "</process>")
      .addResourceFromInputStream("a lot of attachment", new ByteArrayInputStream(lotOfBytes))
      .addResourceFromInputStream("other attachment", new ByteArrayInputStream(otherBytes))
      .deploy();
    
    registerDeployment(deploymentDbiId);

    InputStream stream = repositoryService.getResourceAsStream(deploymentDbiId, "a lot of attachment");
    byte[] retrievedLotOfBytes = readBytes(stream);
    assertNotNull(retrievedLotOfBytes);
    assertTrue(Arrays.equals(lotOfBytes, retrievedLotOfBytes));
    
    stream = repositoryService.getResourceAsStream(deploymentDbiId, "other attachment");
    byte[] retrievedOtherBytes = readBytes(stream);
    assertNotNull(retrievedOtherBytes);
    assertTrue(Arrays.equals(otherBytes, retrievedOtherBytes));
  }

  public void testLoadProcessAsResource() {
    ClassLoader classLoader = DeploymentResourcesTest.class.getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream("org/jbpm/test/process/process.jpdl.xml");
    assertNotNull(inputStream);
    
    String deploymentId = 
    repositoryService.createDeployment()
      .addResourceFromInputStream("process.jpdl.xml", inputStream)
      .deploy();
    
    registerDeployment(deploymentId);

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
      .processDefinitionKey("Resource")
      .uniqueResult();
    
    assertNotNull(processDefinition);
  }

  protected String generateString(String base, int multiplier) {
  	StringBuilder text = new StringBuilder();
    for (int i=0; i<multiplier; i++) {
      text.append(base);
    }
    return text.toString();
  }

  public static byte[] readBytes(InputStream inputStream) {
    byte[] bytes = null;
    if (inputStream==null) {
      throw new JbpmException("inputStream is null");
    }
    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      transfer(inputStream, outputStream);
      bytes = outputStream.toByteArray();
      outputStream.close();
      return bytes;
    } catch (IOException e) {
      throw new JbpmException("couldn't read bytes from inputStream", e);
    }
  }
  public static int transfer(InputStream in, OutputStream out) {
    int total = 0;
    byte[] buffer = new byte[4096];
    try {
      int bytesRead = in.read( buffer );
      while ( bytesRead != -1 ) {
        out.write( buffer, 0, bytesRead );
        total += bytesRead;
        bytesRead = in.read( buffer );
      }
      return total;
    } catch (IOException e) {
      throw new JbpmException("couldn't write bytes to output stream", e);
    }
  }
}

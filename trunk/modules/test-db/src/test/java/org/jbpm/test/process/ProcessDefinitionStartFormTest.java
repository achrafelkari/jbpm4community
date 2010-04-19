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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jbpm.api.ProcessDefinition;
import org.jbpm.pvm.internal.util.IoUtil;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionStartFormTest extends JbpmTestCase {
  
  public void testFormInUnnamedStartActivity() {
    String deploymentDbid =
      repositoryService.createDeployment()
          .addResourceFromString("xmlstring.jpdl.xml",       
            "<process name='make print'>" +
            "  <start form='org/jbpm/test/process/ProcessDefinitionStartForm.form' />" +
            "</process>"
          )
          .addResourceFromClasspath("org/jbpm/test/process/ProcessDefinitionStartForm.form")       
          .deploy();

    registerDeployment(deploymentDbid);

    ProcessDefinition processDefinition = 
      repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionName("make print")
        .uniqueResult();
    
    String processDefinitionId = processDefinition.getId();
    
    List<String> startActivityNames = repositoryService.getStartActivityNames(processDefinitionId );
    List<String> expectedStartActivityNames = new ArrayList<String>();
    expectedStartActivityNames.add(null);
    
    assertEquals(expectedStartActivityNames, startActivityNames);

    String startFormResourceName = repositoryService.getStartFormResourceName(processDefinitionId, null);
    
    assertEquals("org/jbpm/test/process/ProcessDefinitionStartForm.form", startFormResourceName);
    
    InputStream formInputStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), startFormResourceName);
    
    String formContents = new String(IoUtil.readBytes(formInputStream));
    assertEquals("start task form", formContents);
  }

  public void testFormInNamedStartActivity() {
    String deploymentDbid =
      repositoryService.createDeployment()
          .addResourceFromString("xmlstring.jpdl.xml",       
            "<process name='make print'>" +
            "  <start name='start' form='org/jbpm/test/process/ProcessDefinitionStartForm.form' />" +
            "</process>"
          )
          .addResourceFromClasspath("org/jbpm/test/process/ProcessDefinitionStartForm.form")       
          .deploy();

    registerDeployment(deploymentDbid);

    ProcessDefinition processDefinition = 
      repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionName("make print")
        .uniqueResult();
    
    String processDefinitionId = processDefinition.getId();
    
    List<String> startActivityNames = repositoryService.getStartActivityNames(processDefinitionId );
    List<String> expectedStartActivityNames = new ArrayList<String>();
    expectedStartActivityNames.add("start");
    
    assertEquals(expectedStartActivityNames, startActivityNames);

    String startFormResourceName = repositoryService.getStartFormResourceName(processDefinitionId, "start");
    
    assertEquals("org/jbpm/test/process/ProcessDefinitionStartForm.form", startFormResourceName);
    
    InputStream formInputStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), startFormResourceName);
    
    String formContents = new String(IoUtil.readBytes(formInputStream));
    assertEquals("start task form", formContents);
  }
}

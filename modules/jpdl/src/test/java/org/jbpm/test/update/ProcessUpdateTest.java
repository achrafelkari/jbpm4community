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
package org.jbpm.test.update;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.jbpm.api.ProcessInstance;
import org.jbpm.pvm.internal.repository.RepositoryServiceImpl;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.xml.Parser;
import org.jbpm.test.JbpmTestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * @author Tom Baeyens
 */
public class ProcessUpdateTest extends JbpmTestCase {
  
  public void testUpdateProcessDescription() {
    String deploymentId = deployJpdlXmlString(
      "<process name='UpdateProcessDescription'>" +
      "  <start>" +
      "    <transition to='s' />" +
      "  </start>" +
      "  <state name='s' />" +
      "</process>"
    );

    updateJpdlXmlString( 
      deploymentId, 
      "<process-update>" +
      "  <description>" +
      "    This is a description" +
      "  </description>" +
      "</process-update>"
    );

    InputStream inputStream = repositoryService.getResourceAsStream(deploymentId, "xmlstring.jpdl.xml");
    
    Document document = new Parser()
      .createParse()
      .setInputStream(inputStream)
      .execute()
      .getDocument();
      
    Element documentElement = document.getDocumentElement();
    Element descriptionElement = XmlUtil.element(documentElement, "description");
    assertNotNull(descriptionElement);
    String description = XmlUtil.getContentText(descriptionElement);
    assertTextPresent("This is a description", description);
    
    Element stateSElement = XmlUtil.element(documentElement, "state");
    assertNotNull(stateSElement);
    assertEquals("s", stateSElement.getAttribute("name"));
  }

  public void testReplaceActivity() {
    String deploymentId = deployJpdlXmlString(
      "<process name='ReplaceActivity'>" +
      "  <start>" +
      "    <transition to='getDataFromDb' />" +
      "  </start>" +
      "  <sql name='getDataFromDb' var='nbrOfOrders'>" +
      "    <query>COUNT * FROM ORDERS</query>" +
      "    <transition to='wait' />" +
      "  </sql>" +
      "  <state name='wait' />" +
      "</process>"
    );

    updateJpdlXmlString( 
      deploymentId, 
      "<process-update>" +
      "  <assign name='getDataFromDb' expr='#{541}' var='nbrOfOrders'>" +
      "    <transition to='wait' />" +
      "  </assign>" +
      "</process-update>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ReplaceActivity");
    processInstance.isActive("wait");
    
    assertEquals(541L, executionService.getVariable(processInstance.getId(), "nbrOfOrders"));
  }

  public void updateJpdlXmlString(String deploymentId, String xmlString) {
    InputStream inputStream = new ByteArrayInputStream(xmlString.getBytes());
    RepositoryServiceImpl repositoryServiceImpl = (RepositoryServiceImpl) repositoryService;
    repositoryServiceImpl.updateDeploymentResource(deploymentId, "xmlstring.jpdl.xml", inputStream);
  }
}

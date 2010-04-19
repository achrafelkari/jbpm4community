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

import org.jbpm.api.ProcessDefinition;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class DescriptionTest extends JbpmTestCase {

  public void testProcessDefinitionDescription() {
    deployJpdlXmlString(
      "<process name='make print'>" +
      "  <description>This process makes a print</description>" +
      "  <start />" +
      "</process>"
    );
    
    ProcessDefinition processDefinition = repositoryService
      .createProcessDefinitionQuery()
      .processDefinitionName("make print")
      .uniqueResult();
    
    assertEquals("This process makes a print", processDefinition.getDescription());
  }

//  activity's are not yet available through the api
//
//  public void testActivityDescription() {
//    deployJpdlXmlString(
//      "<process name='make print' xmlns='"+JpdlParser.JPDL_NAMESPACE+"' >" +
//      "  <start>" +
//      "    <description>This process is started when the user clicks a button</description>" +
//      "  </start>" +
//      "</process>"
//    );
//    
//    ProcessDefinition processDefinition = repositoryService
//      .createProcessDefinitionQuery()
//      .processDefinitionName("make print")
//      .uniqueResult();
//    
//  }
}

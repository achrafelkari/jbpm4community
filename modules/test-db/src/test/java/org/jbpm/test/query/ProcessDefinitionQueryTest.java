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
package org.jbpm.test.query;

import java.util.List;

import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessDefinitionQuery;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionQueryTest extends JbpmTestCase {

  public void testQueryProcessDefinitionsEmpty() {
    List<ProcessDefinition> processDefinitions = repositoryService
      .createProcessDefinitionQuery()
      .list();
    
    assertEquals(0, processDefinitions.size());
  }

  public void testQueryProcessDefinitionsNameLike() {
    deployJpdlXmlString(
      "<process name='make print'>" +
      "  <start />" +
      "</process>"
    );

    deployJpdlXmlString(
      "<process name='use phone'>" +
      "  <start />" +
      "</process>"
    );

    deployJpdlXmlString(
      "<process name='make friends'>" +
      "  <start />" +
      "</process>"
    );

    deployJpdlXmlString(
      "<process name='clean whiteboard'>" +
      "  <start />" +
      "</process>"
    );

    deployJpdlXmlString(
      "<process name='fix coffeemaker'>" +
      "  <start />" +
      "</process>"
    );

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
      .processDefinitionNameLike("%make%")
      .orderAsc(ProcessDefinitionQuery.PROPERTY_NAME)
      .list();

    assertEquals("fix coffeemaker", processDefinitions.get(0).getName());
    assertEquals("make friends",    processDefinitions.get(1).getName());
    assertEquals("make print",      processDefinitions.get(2).getName());
  }

  public void testQueryProcessDefinitionsKeyLike() {
    deployJpdlXmlString(
      "<process name='make print'>" +
      "  <start />" +
      "</process>"
    );

    deployJpdlXmlString(
      "<process name='use phone'>" +
      "  <start />" +
      "</process>"
    );

    deployJpdlXmlString(
      "<process name='make friends'>" +
      "  <start />" +
      "</process>"
    );

    deployJpdlXmlString(
      "<process name='make friends'>" +
      "  <start />" +
      "</process>"
    );

    deployJpdlXmlString(
      "<process name='make friends'>" +
      "  <start />" +
      "</process>"
    );

    deployJpdlXmlString(
      "<process name='clean whiteboard'>" +
      "  <start />" +
      "</process>"
    );

    deployJpdlXmlString(
      "<process name='fix coffeemaker'>" +
      "  <start />" +
      "</process>"
    );

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
      .processDefinitionNameLike("make%")
      .orderAsc(ProcessDefinitionQuery.PROPERTY_ID)
      .list();

    assertEquals("make_friends-1", processDefinitions.get(0).getId());
    assertEquals("make_friends-2", processDefinitions.get(1).getId());
    assertEquals("make_friends-3", processDefinitions.get(2).getId());
    assertEquals("make_print-1",   processDefinitions.get(3).getId());
  }
}

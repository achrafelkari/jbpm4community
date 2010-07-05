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
package org.jbpm.test.custom.type;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmCustomCfgTestCase;


/**
 * @author Tom Baeyens
 */
public class CustomVariableTypeTest extends JbpmCustomCfgTestCase {

  public void testCustomVariable() {
    deployJpdlXmlString(
      "<process name='CustomVariable'>" +
      "  <start>" +
      "    <transition to='s' />" +
      "  </start>" +
      "  <state name='s' />" +
      "</process>"
    );

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("dog", new Husky("max"));
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("CustomVariable", variables);

    String pid = processInstance.getId();
    Husky husky = (Husky) executionService.getVariable(pid, "dog");
    assertEquals("max", husky.getName());
  }
}

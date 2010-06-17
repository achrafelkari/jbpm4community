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

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import org.jbpm.api.NewDeployment;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.test.JbpmTestCase;

/**
 * JBPM-2635.
 *
 * @author Huisheng Xu
 */
public class DeploymentInvalidTest extends JbpmTestCase {

  public void testDeployWithInvalidVersion() {
    deployJpdlXmlString(
      "<process name='InvalidVersion'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a' />" +
      "  <end name='end' />" +
      "</process>"
    );
    executionService.startProcessInstanceByKey("InvalidVersion");

    try {
      deployJpdlXmlString(
        "<process name='InvalidVersion' version='1.0.0'>" +
        "  <start>" +
        "    <transition to='a' />" +
        "  </start>" +
        "  <state name='a' />" +
        "  <end name='end' />" +
        "</process>"
      );
      fail();
    } catch(Exception ex) {
      assertTrue(true);
    }
    executionService.startProcessInstanceByKey("InvalidVersion");
  }

}

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
package org.jbpm.test.activity.subprocess;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;


/**
 * JBPM-2856.
 *
 * @author Huisheng Xu
 */
public class SubProcessParameterOutTest extends JbpmTestCase {

  private static final String MAIN_PROCESS =
    "<process name='mainProcess'>" +
    "  <start>" +
    "    <transition to='review' />" +
    "  </start>" +
    "  <sub-process name='review' sub-process-key='SubProcessReview'>" +
    "    <parameter-out var='reviewResult' subvar='result' />" +
    "    <transition name='ok' to='wait'/>" +
    "  </sub-process>" +
    "  <state name='wait'>" +
    "    <transition to='close'/>" +
    "  </state>" +
    "  <end name='close'/>" +
    "</process>";

  private static final String SUB_PROCESS =
    "<process name='SubProcessReview'>" +
    "  <start>" +
    "    <transition to='script'/>" +
    "  </start>" +
    "  <script name='script' var='result' expr='#{\"result\"}'>" +
    "    <transition name='ok' to='ok'/>" +
    "  </script>" +
    "  <end name='ok' />" +
    "</process>";


  public void testSubProcessParameterOut() {
    deployJpdlXmlString(SUB_PROCESS);
    deployJpdlXmlString(MAIN_PROCESS);

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("mainProcess");
    assertEquals(1, executionService.createProcessInstanceQuery().list().size());

    assertEquals("result", executionService.getVariable(processInstance.getId(), "reviewResult"));
  }
}

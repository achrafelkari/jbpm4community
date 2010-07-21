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
package org.jbpm.jpdl.parsing;

import java.util.List;

import org.jbpm.api.model.Activity;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.xml.Problem;

/**
 * @author Tom Baeyens
 */
public class ActivityParsingTest extends JpdlParseTestCase {

  public void testSlashInActivityName() {
    ClientProcessDefinition processDefinition = parse(
      "<process name='p'>" +
      "  <start name='slash / activityname' />" +
      "</process>"
    );

    Activity initial = processDefinition.getInitial();
    assertEquals("slash / activityname", initial.getName());
  }

  public void testEmptyActivityName() {
    List<Problem> problems = parseProblems(
      "<process name='p'>" +
      "  <state name='' />" +
      "</process>"
    );
    assertTextPresent("attribute <state name=\"\" is empty", problems.get(0).getMsg());
  }

  public void testDescription() {
    ClientProcessDefinition processDefinition = parse(
      "<process name='p'>" +
      "  <description>process definition description</description>" +
      "  <start name='start'>" +
      "    <description>start description</description>" +
      "  </start>" +
      "</process>"
    );
    assertEquals("process definition description", processDefinition.getDescription());

    ActivityImpl activity = (ActivityImpl) processDefinition.getInitial();
    assertEquals("start description", activity.getDescription());
  }
}

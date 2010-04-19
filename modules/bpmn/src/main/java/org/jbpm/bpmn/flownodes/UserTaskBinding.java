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
package org.jbpm.bpmn.flownodes;

import org.jbpm.bpmn.parser.BpmnParser;
import org.jbpm.pvm.internal.model.ScopeElementImpl;
import org.jbpm.pvm.internal.task.TaskDefinitionImpl;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.xml.Parse;
import org.w3c.dom.Element;

/**
 * @author Tom Baeyens
 * @author Ronald van Kuijk (kukeltje)
 */
public class UserTaskBinding extends AbstractTaskBinding {

  private static final String TAG = "userTask";

  public UserTaskBinding() {
    super(TAG);
  }
  
  public Object parse(Element element, Parse parse, BpmnParser bpmnParser) {
    UserTaskActivity taskActivity = new UserTaskActivity();

    ScopeElementImpl scopeElement = parse.contextStackFind(ScopeElementImpl.class);
    TaskDefinitionImpl taskDefinition = bpmnParser.parseTaskDefinition(element, parse, scopeElement);
    
    addActivityResources(taskDefinition, taskActivity, element, parse);

    Element rendering = XmlUtil.element(element, "rendering", false, parse);
    if (rendering != null) {
      Element jBPMForm = XmlUtil.element(rendering, "form", false, parse);
      taskDefinition.setFormResourceName(jBPMForm != null ? jBPMForm.getTextContent().trim() : null);
    }

    taskActivity.setTaskDefinition(taskDefinition);
    taskActivity.setDefault(getDefault());

    return taskActivity;
  }

}

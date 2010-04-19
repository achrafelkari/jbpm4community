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

import java.util.List;

import org.jbpm.bpmn.model.BpmnProcessDefinition;
import org.jbpm.bpmn.parser.BpmnParser;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.descriptor.ArgDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;
import org.jbpm.pvm.internal.wire.operation.InvokeOperation;
import org.jbpm.pvm.internal.xml.Parse;
import org.w3c.dom.Element;

public class ServiceTaskBinding extends BpmnBinding {

  static final String TAG = "serviceTask";
  
  public ServiceTaskBinding() {
    super(TAG);
  }
  
  public Object parse(Element element, Parse parse, BpmnParser bpmnParser) {
    JavaServiceTaskActivity javaActivity = new JavaServiceTaskActivity();
    
    BpmnProcessDefinition bpmnProcessDefinition = parse.contextStackFind(BpmnProcessDefinition.class);
        
    // Operation parsing
    String operationRef = XmlUtil.attribute(element, "operationRef", true, parse, null);
    Element operationElement = bpmnProcessDefinition.getOperations().get(operationRef);
    if (operationElement == null) {
        parse.addProblem("No operation found for operationRef " + operationRef, operationElement);
    }
    
    // Interface parsing
    Element interfaceElement = (Element)operationElement.getParentNode();
    
    
    javaActivity.setMethodName(operationElement.getAttribute("name"));

    // Out message
    String outMessageRef = XmlUtil.element(operationElement, "outMessageRef").getTextContent().trim();
    String outStructureRef = bpmnProcessDefinition.getMessages().get(outMessageRef).getAttribute("structureRef");
    Element outItemDefinition = bpmnProcessDefinition.getItemDefinitions().get(outStructureRef);  
    Element var = XmlUtil.element(outItemDefinition, "var");
    javaActivity.setVariableName(var.getAttribute("name"));

    // In message
    String inMessageRef = XmlUtil.element(operationElement, "inMessageRef").getTextContent().trim();
    String inStructureRef = bpmnProcessDefinition.getMessages().get(inMessageRef).getAttribute("structureRef");
    Element itemDefinition = bpmnProcessDefinition.getItemDefinitions().get(inStructureRef);
    
    // Arguments
    List<Element> argElements = XmlUtil.elements(itemDefinition, "arg");
    if (!argElements.isEmpty()) {
      List<ArgDescriptor> argDescriptors = wireParser.parseArgs(argElements, parse);
      InvokeOperation invokeOperation = new InvokeOperation();
      invokeOperation.setArgDescriptors(argDescriptors);
      javaActivity.setInvokeOperation(invokeOperation);
    }

    if (interfaceElement.getAttribute("name") != null) {
      ObjectDescriptor objectDescriptor = new ObjectDescriptor();

      objectDescriptor.setClassName(interfaceElement.getAttribute("name"));
      Object target = WireContext.create(objectDescriptor);
      if (target == null) {
        parse.addProblem("name attribute must resolv to a class in "+TAG, element);
      }
      javaActivity.setTarget(target);
    } else {
      parse.addProblem("no target specified in " + TAG +" : must specify attribute 'class' or 'expr'", element);
    }
    
    javaActivity.setDefault(getDefault());
     
    return javaActivity;
  }

}

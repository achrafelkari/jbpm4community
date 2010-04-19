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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.binding.ObjectBinding;
import org.jbpm.pvm.internal.wire.xml.WireParser;
import org.jbpm.pvm.internal.xml.Parse;
import org.w3c.dom.Element;

/**
 * Abstract super class for all gateway types.
 * 
 * @author Ronald Van kuijk
 * @author Joram Barrez
 */
public abstract class AbstractGatewayBinding extends BpmnBinding {

  protected static final Log log = Log.getLog(AbstractGatewayBinding.class.getName());

  protected int incoming;
  protected List<Element> inSequenceFlows;
  
  protected int outgoing;
  protected List<Element> outSequenceFlows;
  
  protected String id;
  protected String name;
  
  protected String gatewayDirection; 
  protected boolean valid = true;
  
  static ObjectBinding objectBinding = new ObjectBinding();
  static WireParser wireParser = WireParser.getInstance();

  public AbstractGatewayBinding(String tagName) {
    super(tagName);
  }
  
  /**
   * Subclasses should call this method while parsing.
   * Common attributes and elements will be parsed and stored in the protected member fields.
   */
  public void parse(Element element, Parse parse) {
    
    resetMemberFields();
    
    id = element.getAttribute("id");
    name = element.getAttribute("name");
    
    // 'gatewayDirection' is a constraint on any gateway type. 
    // Since this is an optional attribute, we can't rely on it at runtime.
    // As such, it is only used at parsing time to check if the constraints are met.
    if (element.hasAttribute("gatewayDirection")) {
      gatewayDirection = element.getAttribute("gatewayDirection");
    } else {
      gatewayDirection = "unspecified"; // default
    }

    // Count in/out sequence flow to validate gatewaydirection
    List<Element> allSequenceFlow = XmlUtil.elements((Element) element.getParentNode(), "sequenceFlow");
    Iterator<Element> iterator = allSequenceFlow.iterator();
    
    while (iterator.hasNext()) {
      
      Element sequenceFlowElement = iterator.next();
      String sourceRef = sequenceFlowElement.getAttribute("sourceRef");
      if (id.equals(sourceRef)) {
        outgoing++;
        outSequenceFlows.add(sequenceFlowElement);
      } else if (sequenceFlowElement.getAttribute("targetRef").equals(id)) {
        incoming++;
        inSequenceFlows.add(sequenceFlowElement);
      }
      
    }
    
    valid = validGatewayDirection(parse, name, element);
    
  }

  private void resetMemberFields() {
    incoming = 0;
    inSequenceFlows = new ArrayList<Element>();
    outgoing = 0;
    outSequenceFlows = new ArrayList<Element>();
    valid = true;
  }

  protected boolean validGatewayDirection(Parse parse, String elementName, Element element) {
   
    if (log.isDebugEnabled()) {
      log.debug("Defined gatewayDirection: " + gatewayDirection + ". Nr of incomming: " + incoming + ", nr of outgoing: " + outgoing);
    }
    
    boolean valid = !(("converging".equals(gatewayDirection) && (!(incoming > 1) || outgoing != 1))
            || ("diverging".equals(gatewayDirection) && (incoming != 1 || !(outgoing > 1)))
            || ("mixed".equals(gatewayDirection) && (incoming <= 1 || outgoing <= 1)));

    if (!valid) {
      parse.addProblem(tagName+ " '" + elementName + "' has the wrong number of incomming (" + incoming + ") and outgoing (" + outgoing
            + ") transitions for gatewayDirection='" + gatewayDirection + "'", element);
    }
    return valid;
  }
  
  /**
   * For some gateway types (eg. exclusive), conditions are required on every outgoing
   * sequence flow. This operation will check if all sequence flow (excluding the default
   * sequence flow) have such a condition defined.
   * 
   * Also the reference from the default attribute is verified if it points to an existing
   * sequence flow defined in the process.
   * 
   * The operation will add problems to the given {@link Parse} object if problems are found.
   * Also the 'valid' boolean wil be changed if needed. 
   */
  protected void validateConditionOnAllSequenceFlow(Parse parse, Element element) {
    boolean defaultExists = false;
    for (Element outSeqFlow : outSequenceFlows) {
      
      String sourceRef = outSeqFlow.getAttribute("sourceRef");
      Element conditionalExpression = XmlUtil.element(outSeqFlow, "conditionExpression");
      
      if (id.equals(sourceRef)) {
        
        if (outSeqFlow.getAttribute("id").equals(default_)) {
          defaultExists = true;
        } else if (default_ != null && conditionalExpression == null) {  // All but the 'default' sequenceFlow need to have a condition
          parse.addProblem("exclusiveGateway '" + name + "' has default sequenceFlow '" + default_ 
                  + "' but " + outSeqFlow.getAttribute("id")
                  + " does not have a required conditionExpression", element);
          valid = false; // do not break. Parsing may find other issues;
        }
      }
      
    }
      
    if (default_ != null && !defaultExists) {
      parse.addProblem("exclusiveGateway '" + name + "' default sequenceFlow '" + default_ 
              + "' does not exist or is not related to this node", element);
      valid = false;
    }
      
  }

}
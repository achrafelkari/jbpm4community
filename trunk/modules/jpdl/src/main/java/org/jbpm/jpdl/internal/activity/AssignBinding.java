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
package org.jbpm.jpdl.internal.activity;

import java.util.List;
import java.util.Set;

import org.jbpm.jpdl.internal.xml.JpdlParser;
import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.el.UelValueExpression;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.xml.WireParser;
import org.jbpm.pvm.internal.xml.Parse;
import org.w3c.dom.Element;


/**
 * @author Tom Baeyens
 */
public class AssignBinding extends JpdlBinding {
  
  public AssignBinding() {
    super("assign");
  }

  public Object parseJpdl(Element element, Parse parse, JpdlParser parser) {
    AssignActivity assignActivity = new AssignActivity();

    String toVar = XmlUtil.attribute(element, "to-var");
    String toExpr = XmlUtil.attribute(element, "to-expr");
    if (toVar!=null) {
      assignActivity.setToVariableName(toVar);
    } else if (toExpr!=null) {
      Expression expression = Expression.create(toExpr, Expression.LANGUAGE_UEL_VALUE);
      assignActivity.setToExpression((UelValueExpression) expression);
    }

    String exprText = XmlUtil.attribute(element, "expr");
    String exprType = XmlUtil.attribute(element, "expr-type");

    // if there is an expr specified
    if (exprText!=null) {
      Expression expression = Expression.create(exprText, exprType);
      assignActivity.setFromExpression(expression);

    } else { // there is no expr specified
      Set<String> descriptorTagNames = WireParser.getInstance().getBindings().getTagNames(WireParser.CATEGORY_DESCRIPTOR);
      Descriptor valueDescriptor = null;
      List<Element> assignContentElements = XmlUtil.elements(element);
      
      for (int i=0; ((i<assignContentElements.size()) && (valueDescriptor==null)); i++) {
        Element assignContentElement = assignContentElements.get(i);
        String assignContentElementTagName = XmlUtil.getTagLocalName(assignContentElement);
        if (descriptorTagNames.contains(assignContentElementTagName)) {
          valueDescriptor = parser.parseDescriptor(assignContentElement, parse);
        }
      }

      if (valueDescriptor!=null) {
        assignActivity.setFromValueDescriptor(valueDescriptor);
      } else {
        parse.addProblem("no value for assignment specified.  'assign' must have attribute 'expr' or a wire object element.", element);
      }
    }
    
    return assignActivity;
  }
}

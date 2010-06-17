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

  private static final String FROM_EXPR = "from-expr";
  private static final String LANG = "lang";
  private static final String FROM_VAR = "from-var";
  private static final String FROM_DESC = "from";
  private static final String TO_EXPR = "to-expr";
  private static final String TO_VAR = "to-var";

  public AssignBinding() {
    super("assign");
  }

  public Object parseJpdl(Element element, Parse parse, JpdlParser parser) {
    AssignActivity assignActivity = new AssignActivity();

    // from expression
    if (element.hasAttribute(FROM_EXPR)) {
      String lang = XmlUtil.attribute(element, LANG);
      Expression fromExpression = Expression.create(element.getAttribute(FROM_EXPR), lang);
      assignActivity.setFromExpression(fromExpression);
    }
    // from variable
    else if (element.hasAttribute(FROM_VAR)) {
      assignActivity.setFromVariable(element.getAttribute(FROM_VAR));
    }
    else {
      // neither expression nor variable
      Element fromElement = XmlUtil.element(element, FROM_DESC);
      if (fromElement != null) {
        Set<String> descriptorTags = WireParser.getInstance()
          .getBindings()
          .getTagNames(WireParser.CATEGORY_DESCRIPTOR);
        Element descriptorElement = XmlUtil.element(fromElement);
        if (descriptorElement != null
          && descriptorTags.contains(descriptorElement.getTagName())) {
          Descriptor descriptor = parser.parseDescriptor(descriptorElement, parse);
          assignActivity.setFromDescriptor(descriptor);
        }
        else {
          parse.addProblem("missing descriptor element", fromElement);
        }
      }
      else {
        parse.addProblem("missing " + FROM_EXPR + " attribute, " + FROM_VAR + " attribute or "
          + FROM_DESC + " element", element);
      }
    }

    if (element.hasAttribute(TO_VAR)) {
      assignActivity.setToVariable(element.getAttribute(TO_VAR));
    }
    else if (element.hasAttribute(TO_EXPR)) {
      Expression expression = Expression.create(element.getAttribute(TO_EXPR), Expression.LANGUAGE_UEL_VALUE);
      assignActivity.setToExpression((UelValueExpression) expression);
    }

    return assignActivity;
  }
}

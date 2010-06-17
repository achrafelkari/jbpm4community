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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.jpdl.internal.xml.JpdlParser;
import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.xml.WireParser;
import org.jbpm.pvm.internal.xml.Parse;
import org.w3c.dom.Element;


/**
 * @author Tom Baeyens
 */
public class SubProcessBinding extends JpdlBinding {

  public SubProcessBinding() {
    super("sub-process");
  }

  public Object parseJpdl(Element element, Parse parse, JpdlParser parser) {
    SubProcessActivity subProcessActivity = new SubProcessActivity();

    String subProcessKey = XmlUtil.attribute(element, "sub-process-key");
    subProcessActivity.setSubProcessKey(subProcessKey);

    String subProcessId = XmlUtil.attribute(element, "sub-process-id");
    subProcessActivity.setSubProcessId(subProcessId);

    List<SubProcessInParameterImpl> inParameters = new ArrayList<SubProcessInParameterImpl>();
    for (Element inElement: XmlUtil.elements(element, "parameter-in")) {
      SubProcessInParameterImpl inParameter = new SubProcessInParameterImpl();
      parseParameter(inElement, inParameter);
      inParameters.add(inParameter);

      if (inParameter.getSubVariableName()==null) {
        parse.addProblem("no 'subvar' specified for parameter-in", element);
      }
      if ( (inParameter.getExpression()==null)
           && (inParameter.getVariableName()==null)
         ) {
        parse.addProblem("no 'expr' or 'variable' specified for parameter-in '"+inParameter.getSubVariableName()+"'", element);
      }
      if ( (inParameter.getExpression()!=null)
           && (inParameter.getVariableName()!=null)
         ) {
        parse.addProblem("attributes 'expr' and 'variable' are mutually exclusive on parameter-in", element);
      }
    }
    subProcessActivity.setInParameters(inParameters);

    List<SubProcessOutParameterImpl> outParameters = new ArrayList<SubProcessOutParameterImpl>();
    for (Element outElement: XmlUtil.elements(element, "parameter-out")) {
      SubProcessOutParameterImpl outParameter = new SubProcessOutParameterImpl();
      parseParameter(outElement, outParameter);
      outParameters.add(outParameter);

      if (outParameter.getVariableName()==null) {
        parse.addProblem("no 'variable' specified for parameter-in", element);
      }
      if ( (outParameter.getExpression()==null)
           && (outParameter.getSubVariableName()==null)
         ) {
        parse.addProblem("no 'expr' or 'subvar' specified for parameter-out '"+outParameter.getVariableName()+"'", element);
      }
      if ( (outParameter.getExpression()!=null)
           && (outParameter.getSubVariableName()!=null)
         ) {
        parse.addProblem("attributes 'expr' and 'subvar' are mutually exclusive on parameter-out '"+outParameter.getVariableName()+"'", element);
      }
    }
    subProcessActivity.setOutParameters(outParameters);

    Map<String, String> swimlaneMappings = parseSwimlaneMappings(element, parse);
    subProcessActivity.setSwimlaneMappings(swimlaneMappings);

    Map<Object, String> outcomeVariableMappings = new HashMap<Object, String>();

    String outcomeExpressionText = XmlUtil.attribute(element, "outcome");
    String outcomeLanguage = XmlUtil.attribute(element, "outcome-lang");
    if (outcomeExpressionText!=null) {
      Expression outcomeExpression = Expression.create(outcomeExpressionText, outcomeLanguage);
      subProcessActivity.setOutcomeExpression(outcomeExpression);

      for (Element transitionElement: XmlUtil.elements(element, "transition")) {
        Element outcomeValueElement = XmlUtil.element(transitionElement, "outcome-value");
        if (outcomeValueElement!=null) {
          String transitionName = XmlUtil.attribute(transitionElement, "name");
          if (transitionName==null) {
            parse.addProblem("transitions with an outcome-value must have a name", transitionElement);
          }
          Element valueElement = XmlUtil.element(outcomeValueElement);
          if (valueElement!=null) {
            Descriptor descriptor = (Descriptor) WireParser.getInstance().parseElement(valueElement, parse);
            Object value = WireContext.create(descriptor);
            outcomeVariableMappings.put(value, transitionName);
            subProcessActivity.setOutcomeVariableMappings(outcomeVariableMappings);
          } else {
            parse.addProblem("outcome-value must contain exactly one element", outcomeValueElement);
          }
        }
      }

    }

    return subProcessActivity;
  }

  void parseParameter(Element element, SubProcessParameterImpl parameter) {
    String name = XmlUtil.attribute(element, "subvar");
    parameter.setSubVariableName(name);

    String expressionText = XmlUtil.attribute(element, "expr");
    String language = XmlUtil.attribute(element, "expr-lang");
    if (expressionText!=null) {
      Expression expression = Expression.create(expressionText, language);
      parameter.setExpression(expression);
    }

    String variable = XmlUtil.attribute(element, "var");
    if (variable!=null) {
      parameter.setVariableName(variable);
    }
  }

  public static Map<String, String> parseSwimlaneMappings(Element element, Parse parse) {
    Map<String, String> swimlaneMappings = new HashMap<String, String>();

    for (Element inElement: XmlUtil.elements(element, "swimlane-mapping")) {
      String swimlane = XmlUtil.attribute(inElement, "swimlane", parse);
      String subSwimlane = XmlUtil.attribute(inElement, "sub-swimlane", parse);

      swimlaneMappings.put(swimlane, subSwimlane);
    }

    return swimlaneMappings;
  }
}

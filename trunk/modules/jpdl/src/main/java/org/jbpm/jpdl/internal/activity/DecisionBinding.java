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

import org.jbpm.jpdl.internal.xml.JpdlParser;
import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.ExpressionCondition;
import org.jbpm.pvm.internal.model.TransitionImpl;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.usercode.UserCodeCondition;
import org.jbpm.pvm.internal.wire.usercode.UserCodeReference;
import org.jbpm.pvm.internal.xml.Parse;
import org.w3c.dom.Element;


/**
 * @author Tom Baeyens
 */
public class DecisionBinding extends JpdlBinding {

  public DecisionBinding() {
    super("decision");
  }

  public Object parseJpdl(Element element, Parse parse, JpdlParser parser) {
    if (element.hasAttribute("expr")) {
      DecisionExpressionActivity decisionExpressionActivity = new DecisionExpressionActivity();
      String expressionText = element.getAttribute("expr");
      String language = XmlUtil.attribute(element, "lang");
      Expression expression = Expression.create(expressionText, language);
      decisionExpressionActivity.setExpression(expression);
      return decisionExpressionActivity;
    }

    Element handlerElement = XmlUtil.element(element, "handler");
    if (handlerElement!=null) {
      DecisionHandlerActivity decisionHandlerActivity = new DecisionHandlerActivity();
      UserCodeReference decisionHandlerReference = parser.parseUserCodeReference(handlerElement, parse);
      decisionHandlerActivity.setDecisionHandlerReference(decisionHandlerReference);
      return decisionHandlerActivity;
    }
    
    boolean hasConditions = false;
    List<Element> transitionElements = XmlUtil.elements(element, "transition");
    ActivityImpl activity = parse.contextStackFind(ActivityImpl.class);
    List<TransitionImpl> transitions = (List) activity.getOutgoingTransitions();
    
    for (int i=0; i<transitionElements.size(); i++) {
      TransitionImpl transition = transitions.get(i);
      Element transitionElement = transitionElements.get(i);

      Element conditionElement = XmlUtil.element(transitionElement, "condition");
      if (conditionElement!=null) {
        hasConditions = true;
        
        if (conditionElement.hasAttribute("expr")) {
          ExpressionCondition expressionCondition = new ExpressionCondition();
          expressionCondition.setExpression(conditionElement.getAttribute("expr"));
          expressionCondition.setLanguage(XmlUtil.attribute(conditionElement, "lang"));
          transition.setCondition(expressionCondition);
          
        } else {
          Element conditionHandlerElement = XmlUtil.element(conditionElement, "handler");
          if (conditionHandlerElement != null) {
            UserCodeCondition userCodeCondition = new UserCodeCondition();
            
            UserCodeReference conditionReference = parser.parseUserCodeReference(conditionHandlerElement, parse);
            userCodeCondition.setConditionReference(conditionReference);
            
            transition.setCondition(userCodeCondition);
          }
        }
      }
    }
    
    if (hasConditions) {
      return new DecisionConditionActivity();
    } else {
      parse.addProblem("decision '"+element.getAttribute("name")+"' must have one of: expr attribute, handler attribute, handler element or condition expressions", element);
    }
    
    return null;
  }
}

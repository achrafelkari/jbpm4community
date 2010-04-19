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
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.xml.Parse;
import org.w3c.dom.Element;


/**
 * @author Tom Baeyens
 */
public class RulesBinding extends JpdlBinding {

  public RulesBinding() {
    super("rules");
  }

  public Object parseJpdl(Element element, Parse parse, JpdlParser parser) {
    RulesActivity rulesActivity = new RulesActivity();
    
    List<Element> factElements = XmlUtil.elements(element, "fact");
    for (Element factElement: factElements) {
      RulesFact rulesFact = new RulesFact();
      
      String factVar = XmlUtil.attribute(factElement, "var");
      if (factVar!=null) {
        rulesFact.setVariableName(factVar);
        
      } else {
        String factExpr = XmlUtil.attribute(factElement, "expr");
        
        if (factExpr!=null) {
          String factLang = XmlUtil.attribute(factElement, "lang");
          Expression expression = Expression.create(factExpr, factLang);
          rulesFact.setExpression(expression);

        } else { 
          parse.addProblem("'fact' element inside 'rules' activity requires attribute 'var' or 'expr'", element);
        }
      }
      
      rulesActivity.addRulesFact(rulesFact);
    }

    return rulesActivity;
  }
}

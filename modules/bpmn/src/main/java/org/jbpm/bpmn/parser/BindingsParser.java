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
package org.jbpm.bpmn.parser;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.util.ReflectUtil;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.xml.Binding;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;
import org.w3c.dom.Element;

/**
 * @author Tom Baeyens
 * @author bernd.ruecker@camunda.com
 */
public class BindingsParser extends Parser {

  private static final Log log = Log.getLog(BindingsParser.class.getName());

  public Object parseDocumentElement(Element documentElement, Parse parse) {
    List<Binding> bindings = new ArrayList<Binding>();
    parse.setDocumentObject(bindings);

    for (Element bindingElement : XmlUtil.elements(documentElement)) {
      Binding binding = instantiateBinding(bindingElement, parse);
      bindings.add(binding);
    }

    return bindings;
  }

  protected Binding instantiateBinding(Element bindingElement, Parse parse) {
    String bindingClassName = XmlUtil.attribute(bindingElement, "binding", true, parse);

    log.trace("adding bpmn binding " + bindingClassName);

    if (bindingClassName != null) {
      try {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class< ? > bindingClass = Class.forName(bindingClassName, true, classLoader);
        return (Binding) bindingClass.newInstance();
      } catch (Exception e) {
        parse.addProblem("couldn't instantiate activity binding " + bindingClassName, e);
      }
    }
    return null;
  }
}

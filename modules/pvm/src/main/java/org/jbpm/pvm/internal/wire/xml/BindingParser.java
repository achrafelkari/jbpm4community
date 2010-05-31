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
package org.jbpm.pvm.internal.wire.xml;

import java.util.List;

import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.util.ReflectUtil;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.xml.Binding;
import org.jbpm.pvm.internal.xml.Bindings;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;
import org.w3c.dom.Element;

public class BindingParser extends Parser {

  private static final Log log = Log.getLog(BindingParser.class.getName());

  public Object parseDocumentElement(Element documentElement, Parse parse) {
    List<Element> elements = XmlUtil.elements(documentElement, "binding");
    for (Element bindingElement : elements) {
      String bindingClassName = XmlUtil.attribute(bindingElement, "class");

      log.trace("adding wire binding for "+bindingClassName);

      Binding binding = null;
      if (bindingClassName!=null) {
        try {
          Class<?> bindingClass = ReflectUtil.classForName(bindingClassName);
          binding = (Binding) bindingClass.newInstance();
        } catch (Exception e) {
          log.trace("couldn't instantiate binding "+bindingClassName);
        }
      } else {
        parse.addProblem("class is a required attribute in a binding "+XmlUtil.toString(bindingElement), documentElement);
      }

      if (binding!=null) {
        Bindings bindings = parse.contextStackFind(Bindings.class);
        bindings.addBinding(binding);
      }
    }

    return null;
  }
}

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

import org.w3c.dom.Element;

import org.jbpm.jpdl.internal.xml.JpdlParser;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.util.TagBinding;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;

/**
 * @author Tom Baeyens
 */
public abstract class JpdlBinding extends TagBinding {
  
  public JpdlBinding(String tagName) {
    super(tagName, null, null);
  }

  public abstract Object parseJpdl(Element element, Parse parse, JpdlParser parser);

  public final Object parse(Element element, Parse parse, Parser parser) {
    return parseJpdl(element, parse, (JpdlParser) parser);
  }

  public void parseName(Element element, ActivityImpl activity, Parse parse) {
    String name = XmlUtil.attribute(element, "name", isNameRequired() ? parse : null);
    
    if (name!=null) {
      // basic name validation
      if (name.length()==0) {
        parse.addProblem(XmlUtil.errorMessageAttribute(element, "name", name, "is empty"), element);
      }
      activity.setName(name);
    }
  }

  public boolean isNameRequired() {
    return true;
  }
}

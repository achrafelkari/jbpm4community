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

import org.jbpm.bpmn.parser.BpmnParser;
import org.jbpm.pvm.internal.util.TagBinding;
import org.jbpm.pvm.internal.wire.xml.WireParser;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;
import org.w3c.dom.Element;

public abstract class BpmnBinding extends TagBinding {

  protected static final WireParser wireParser = BpmnParser.wireParser;

  protected String default_;

  public BpmnBinding(String tagName) {
    super(tagName, "http://schema.omg.org/spec/BPMN/2.0", null);
  }
  
  public Object parse(Element element, Parse parse, Parser parser) {
    this.default_ = null;
    if (element.hasAttribute("default")) {
      default_ = element.getAttribute("default");
    }
    return parse(element, parse, (BpmnParser) parser);
  }
  
  public abstract Object parse(Element element, Parse parse, BpmnParser bpmnParser);

  public String getDefault() {
    return default_;
  }

}

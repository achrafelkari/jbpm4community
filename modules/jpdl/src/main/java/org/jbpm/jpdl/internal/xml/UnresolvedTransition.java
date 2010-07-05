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
package org.jbpm.jpdl.internal.xml;

import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;
import org.jbpm.pvm.internal.model.TransitionImpl;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.xml.Parse;
import org.w3c.dom.Element;

public class UnresolvedTransition {
  TransitionImpl transition;
  Element transitionElement;
  public UnresolvedTransition(TransitionImpl transition, Element transitionElement) {
    this.transition = transition;
    this.transitionElement = transitionElement;
  }
  public void resolve(ProcessDefinitionImpl processDefinition, Parse parse) {
    String to = XmlUtil.attribute(transitionElement, "to", parse);
    if (to!=null) {
      ActivityImpl destination = processDefinition.findActivity(to);
      if (destination!=null) {
        destination.addIncomingTransition(transition);
      } else {
        parse.addProblem(XmlUtil.errorMessageAttribute(transitionElement, "to", to, "doesn't reference an existing activity name"), transitionElement);
      }
    }
  }
}
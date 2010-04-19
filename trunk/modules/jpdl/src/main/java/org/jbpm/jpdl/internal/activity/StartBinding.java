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

import org.jbpm.jpdl.internal.model.JpdlProcessDefinition;
import org.jbpm.jpdl.internal.xml.JpdlParser;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.Continuation;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.xml.Parse;
import org.w3c.dom.Element;

/**
 * @author Tom Baeyens
 */
public class StartBinding extends JpdlBinding {

  public StartBinding() {
    super("start");
  }

  public Object parseJpdl(Element element, Parse parse, JpdlParser parser) {
    ActivityImpl startActivity = parse.contextStackFind(ActivityImpl.class);
    JpdlProcessDefinition processDefinition = parse.contextStackFind(JpdlProcessDefinition.class);

    if (processDefinition.getInitial() == null) {
      processDefinition.setInitial(startActivity);

    } else if (startActivity.getParentActivity() == null) {
      parse.addProblem("multiple start events not yet supported", element);
    }

    StartActivity startActivityBehaviour = new StartActivity();

    if (startActivity.getContinuation() == Continuation.ASYNCHRONOUS && startActivity.getName() == null) {
      parse.addProblem("Using continuation=\"async\" on a start node requires a name to be present", element);
    }

    startActivityBehaviour.setFormResourceName(XmlUtil.attribute(element, "form"));

    return startActivityBehaviour;
  }

  public boolean isNameRequired() {
    // This factually is dependent on having an async continuation on the
    // startnode or not. Cannot be decided here, so added it to the parse part
    return false;
  }
}

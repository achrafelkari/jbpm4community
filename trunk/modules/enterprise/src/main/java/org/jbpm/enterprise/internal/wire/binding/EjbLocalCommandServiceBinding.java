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
package org.jbpm.enterprise.internal.wire.binding;

import org.jbpm.enterprise.internal.ejb.EjbLocalCommandService;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.binding.WireDescriptorBinding;
import org.jbpm.pvm.internal.wire.descriptor.JndiDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;
import org.w3c.dom.Element;

/**
 * @author Alejandro Guizar
 */
public class EjbLocalCommandServiceBinding extends WireDescriptorBinding {

  public EjbLocalCommandServiceBinding() {
    super("ejb-local-command-service");
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    ObjectDescriptor descriptor = new ObjectDescriptor(EjbLocalCommandService.class);
    // retrieve home name
    String homeJndiName = "java:comp/env/ejb/LocalCommandExecutor";
    Element homeElement = XmlUtil.element(element, "home");
    if (homeElement != null && homeElement.hasAttribute("jndi-name")) {
      homeJndiName = homeElement.getAttribute("jndi-name");
    }
    // inject home name
    descriptor.addInjection("commandExecutorHome", new JndiDescriptor(homeJndiName));
    return descriptor;
  }

}

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
package org.jbpm.pvm.internal.wire.binding;

import java.util.List;

import org.w3c.dom.Element;

import org.jbpm.pvm.internal.cmd.CommandService;
import org.jbpm.pvm.internal.svc.AsyncCommandService;
import org.jbpm.pvm.internal.svc.DefaultCommandService;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.descriptor.CommandServiceDescriptor;
import org.jbpm.pvm.internal.wire.xml.WireParser;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;

/** parses a descriptor for a creating {@link DefaultCommandService}.
 * 
 * See schema docs for more details.
 *
 * @author Tom Baeyens
 */
public class CommandServiceBinding extends WireDescriptorBinding {

  public CommandServiceBinding() {
    super("command-service");
  }
  
  protected CommandServiceBinding(String tagName) {
    super(tagName);
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    CommandServiceDescriptor commandServiceDescriptor = new CommandServiceDescriptor();

    CommandService commandService = getCommandService(element, parse, parser);
    commandServiceDescriptor.setCommandService(commandService);
    
    List<Element> interceptorElements = XmlUtil.elements(element);
    for (Element interceptorElement : interceptorElements) {
      Descriptor interceptorDescriptor = (Descriptor) parser.parseElement(interceptorElement, parse, WireParser.CATEGORY_INTERCEPTOR);
      commandServiceDescriptor.addInterceptorDescriptor(interceptorDescriptor);
    }

    return commandServiceDescriptor;
  }

  protected CommandService getCommandService(Element element, Parse parse, Parser parser) {
    Boolean async = XmlUtil.attributeBoolean(element, "async", parse);
    if (Boolean.TRUE.equals(async)) {
      AsyncCommandService asyncCommandService = new AsyncCommandService();

      Boolean propagateUserId = XmlUtil.attributeBoolean(element, "propagate-auth", parse);
      if (propagateUserId!=null) {
        asyncCommandService.setPropagateUserId(propagateUserId);
      }
      return asyncCommandService;
    }
    
    return new DefaultCommandService();
  }

}

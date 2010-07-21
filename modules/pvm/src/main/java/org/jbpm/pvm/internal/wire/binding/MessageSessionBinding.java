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

import org.w3c.dom.Element;

import org.jbpm.pvm.internal.jms.JmsMessageSession;
import org.jbpm.pvm.internal.jobexecutor.JobExecutorMessageSession;
import org.jbpm.pvm.internal.session.DbSession;
import org.jbpm.pvm.internal.session.MessageSession;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.descriptor.ContextTypeRefDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.JndiDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;

/** parses a descriptor for creating a {@link MessageSession}.
 * 
 * See schema docs for more details.
 * 
 * @author Tom Baeyens
 * @author Huisheng Xu
 */
public class MessageSessionBinding extends WireDescriptorBinding {

  public MessageSessionBinding() {
    super("message-session");
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    ObjectDescriptor objectDescriptor = new ObjectDescriptor();
    
    String target = XmlUtil.attribute(element, "target");
    if ("jms".equals(target)) {
      objectDescriptor.setClassName(JmsMessageSession.class.getName());
      objectDescriptor.addInjection("dbSession", new ContextTypeRefDescriptor(DbSession.class));
      
      if (element.hasAttribute("session-jndi")) {
        String jmsSessionJndiName = element.getAttribute("session-jndi");
        JndiDescriptor jndiDescriptor = new JndiDescriptor(jmsSessionJndiName);
        objectDescriptor.addInjection("jmsSession", jndiDescriptor);
      } else {
        parse.addProblem("attribute <"+element.getLocalName()+" session-jndi=\"...\" is required when target=\"jms\"", element);
      }
      
      if (element.hasAttribute("destination-jndi")) {
        String jmsDestinationJndiName = element.getAttribute("destination-jndi");
        JndiDescriptor jndiDescriptor = new JndiDescriptor(jmsDestinationJndiName);
        objectDescriptor.addInjection("jmsDestination", jndiDescriptor);
      } else {
        parse.addProblem("attribute <"+element.getLocalName()+" destination-jndi=\"...\" is required when target=\"jms\"", element);
      }

    } else {
      objectDescriptor.setClassName(JobExecutorMessageSession.class.getName());
      objectDescriptor.addInjection("dbSession", new ContextTypeRefDescriptor(DbSession.class));
      objectDescriptor.addInjection("jobAdditionNotifier",
        TimerSessionBinding.getJobAdditionNotifierDescriptor(parse));
    }

    return objectDescriptor;
  }
}

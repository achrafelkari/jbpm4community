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

import org.hibernate.Session;
import org.w3c.dom.Element;

import org.jbpm.pvm.internal.jobexecutor.JobAdditionNotifier;
import org.jbpm.pvm.internal.jobexecutor.JobExecutor;
import org.jbpm.pvm.internal.jobexecutor.JobExecutorTimerSession;
import org.jbpm.pvm.internal.session.TimerSession;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.WireDefinition;
import org.jbpm.pvm.internal.wire.descriptor.ContextTypeRefDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.EnvDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.TransactionRefDescriptor;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;

/** parses a descriptor for creating a {@link TimerSession}.
 * 
 * See schema docs for more details.
 *
 * @author Tom Baeyens, Pascal Verdage
 * @author Huisheng Xu
 */
public class TimerSessionBinding extends WireDescriptorBinding {

  public TimerSessionBinding() {
    super("timer-session");
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    ObjectDescriptor objectDescriptor = new ObjectDescriptor();
    
    String target = XmlUtil.attribute(element, "target");

    if ((target!=null) && ("ejb".equalsIgnoreCase(target))) {
      objectDescriptor.setClassName("org.jbpm.enterprise.internal.ejb.EnterpriseTimerSession");

    } else {
      // wire the JobExecutorTimerSession
      objectDescriptor.setClassName(JobExecutorTimerSession.class.getName());

      // inject fields
      objectDescriptor.addInjection("session", new ContextTypeRefDescriptor(Session.class));
      objectDescriptor.addInjection("jobAdditionNotifier",
        getJobAdditionNotifierDescriptor(parse));
    }

    return objectDescriptor;
  }

  static ObjectDescriptor getJobAdditionNotifierDescriptor(Parse parse) {
    String jobAdditionNotifierName;
    ObjectDescriptor jobAdditionNotifierDescriptor;

    WireDefinition wireDefinition = parse.contextStackFind(WireDefinition.class);
    if (wireDefinition != null
      && (jobAdditionNotifierName = wireDefinition.getDescriptorName(JobAdditionNotifier.class)) != null) {
      jobAdditionNotifierDescriptor = (ObjectDescriptor) wireDefinition.getDescriptor(jobAdditionNotifierName);
    }
    else {
      jobAdditionNotifierDescriptor = new ObjectDescriptor(JobAdditionNotifier.class);
      // inject fields
      jobAdditionNotifierDescriptor.addInjection("transaction", new TransactionRefDescriptor());
      jobAdditionNotifierDescriptor.addInjection("jobExecutor", new EnvDescriptor(JobExecutor.class));
      wireDefinition.addDescriptor(jobAdditionNotifierDescriptor);
    }
    return jobAdditionNotifierDescriptor;
  }
}


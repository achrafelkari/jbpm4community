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

import org.jbpm.pvm.internal.cmd.CommandService;
import org.jbpm.pvm.internal.jobexecutor.JobExecutor;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.descriptor.AbstractDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.IntegerDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.JobExecutorDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.ReferenceDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.StringDescriptor;
import org.jbpm.pvm.internal.wire.operation.InvokeOperation;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;
import org.w3c.dom.Element;

/** parses a descriptor for creating a {@link JobExecutor}.
 * 
 * See schema docs for more details.
 * 
 * @author Tom Baeyens
 * @author Pascal Verdage
 */
public class JobExecutorBinding extends WireDescriptorBinding {

  public JobExecutorBinding() {
    super("job-executor");
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    // create a jobImpl executor object
    JobExecutorDescriptor descriptor = new JobExecutorDescriptor();

    // inject the command executor

    if (element.hasAttribute("command-service")) {
      descriptor.addInjection("commandService", 
          new ReferenceDescriptor(element.getAttribute("command-service"))
      );
    } else {
      descriptor.addInjection("commandService", 
          new ReferenceDescriptor(CommandService.NAME_TX_REQUIRED_COMMAND_SERVICE)
      );
    }

    if (element.hasAttribute("name")) {
      descriptor.addInjection("name", new StringDescriptor(element.getAttribute("name")));
    }

    parseIntAttribute(element, "threads", descriptor, "nbrOfThreads", parse);
    parseIntAttribute(element, "idle", descriptor, "idleMillis", parse);
    parseIntAttribute(element, "idle-max", descriptor, "idleMillisMax", parse);
    parseIntAttribute(element, "history", descriptor, "historySize", parse);
    parseIntAttribute(element, "lock", descriptor, "lockMillis", parse);

    // by default invoke the start method, unless auto-start is disabled
    Boolean autoStart = XmlUtil.attributeBoolean(element, "auto-start", parse);
    if (autoStart == null || autoStart.booleanValue()) {
      InvokeOperation invokeStartOperation = new InvokeOperation();
      invokeStartOperation.setMethodName("start");
      descriptor.addOperation(invokeStartOperation);
      descriptor.setAutoStart(true);
      descriptor.setInit(AbstractDescriptor.INIT_EAGER);
    }

    return descriptor;
  }

  private void parseIntAttribute(Element element, String attributeName, ObjectDescriptor descriptor, String fieldName, Parse parse) {
    Integer intValue = XmlUtil.attributeInteger(element, attributeName, parse);
    if (intValue!=null) {
      descriptor.addInjection(fieldName, new IntegerDescriptor(intValue));
    }
  }
}

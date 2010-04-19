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

import org.jbpm.pvm.internal.repository.DeployerManager;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.WireDefinition;
import org.jbpm.pvm.internal.wire.descriptor.ListDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;
import org.jbpm.pvm.internal.wire.operation.FieldOperation;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;
import org.w3c.dom.Element;


/** parses a descriptor for creating a {@link DeployerManager}.
 * 
 * See schema docs for more details.
 * 
 * @author Tom Baeyens
 */
public class DeployerManagerBinding extends WireDescriptorBinding {
  
  private static final String DEPLOYER_MANAGER_TAG = "deployer-manager";
  private static ListBinding listBinding = new ListBinding();
  
  public DeployerManagerBinding() {
    super(DEPLOYER_MANAGER_TAG);
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    ObjectDescriptor objectDescriptor = new ObjectDescriptor(DeployerManager.class);

    ListDescriptor listDescriptor = (ListDescriptor) listBinding.parse(element, parse, parser);

    WireDefinition wireDefinition = parse.contextStackFind(WireDefinition.class);
    String descriptorName = wireDefinition.getDescriptorName(DeployerManager.class);
    if (descriptorName==null) {
      // merge the new deployment managers with the existing ones
      objectDescriptor = new ObjectDescriptor(DeployerManager.class);
      objectDescriptor.addInjection("deployers", listDescriptor);
      
    } else {
      // merge the new deployment managers with the existing ones
      objectDescriptor = (ObjectDescriptor) wireDefinition.getDescriptor(descriptorName);
      FieldOperation injection = (FieldOperation) objectDescriptor.getOperations().get(0);
      ListDescriptor existingDescriptor = (ListDescriptor) injection.getDescriptor();
      List<Descriptor> valueDescriptors = existingDescriptor.getValueDescriptors();
      valueDescriptors.addAll(listDescriptor.getValueDescriptors());
    }

    return objectDescriptor;
  }
}

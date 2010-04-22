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
package org.jbpm.pvm.internal.wire.descriptor;

import java.util.List;

import org.jbpm.pvm.internal.history.HistorySession;
import org.jbpm.pvm.internal.history.HistorySessionChain;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireDefinition;


/**
 * @author Tom Baeyens
 */
public class HistorySessionDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;
  
  CollectionDescriptor listDescriptor = new ListDescriptor();

  public Object construct(WireContext wireContext) {
    List<HistorySession> historySessions = (List<HistorySession>) wireContext.create(listDescriptor, false);
    if (historySessions.size()==1) {
      return historySessions.get(0);
    } 
    return new HistorySessionChain(historySessions);
  }

  public void add(CollectionDescriptor otherListDescriptor) {
    this.listDescriptor.addValueDescriptors(otherListDescriptor.getValueDescriptors());
  }
  
  public Class<?> getType(WireDefinition wireDefinition) {
    return HistorySession.class;
  }
}

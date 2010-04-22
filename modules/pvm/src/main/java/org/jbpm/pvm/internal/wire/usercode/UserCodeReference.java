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
package org.jbpm.pvm.internal.wire.usercode;

import java.io.Serializable;

import org.jbpm.api.Execution;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;
import org.jbpm.pvm.internal.model.ScopeInstanceImpl;
import org.jbpm.pvm.internal.model.TransitionImpl;
import org.jbpm.pvm.internal.util.ReflectUtil;
import org.jbpm.pvm.internal.wire.Descriptor;

/**
 * @author Tom Baeyens
 */
public class UserCodeReference implements Serializable {

  private static final long serialVersionUID = 1L;

  protected boolean isCached = true;
  protected Object cachedObject;
  protected Descriptor descriptor;

  public Object getObject(Execution execution) {
    return getObject(null, execution);
  }

  public Object getObject(ProcessDefinitionImpl processDefinition) {
    return getObject(processDefinition, null);
  }

  protected Object getObject(ProcessDefinitionImpl processDefinition, Execution execution) {
    if (cachedObject!=null) {
      return cachedObject;
    }
    if (descriptor!=null) {
      if (processDefinition==null) {
        processDefinition = getProcessDefinition(execution);
      }

      Object usedObject = ReflectUtil.instantiateUserCode(descriptor, processDefinition, (ScopeInstanceImpl) execution);
      if (isCached) {
        cachedObject = usedObject;
      }
      return usedObject;
    }
    return null;
  }

  protected ProcessDefinitionImpl getProcessDefinition(Execution execution) {
    ExecutionImpl executionImpl = (ExecutionImpl) execution;
    ActivityImpl activity = executionImpl.getActivity();
    if (activity!=null) {
      return activity.getProcessDefinition();
    }

    TransitionImpl transition = executionImpl.getTransition();
    if (transition!=null) {
      return transition.getProcessDefinition();
    }

    return null;
  }

  public void setCached(boolean isCached) {
    this.isCached = isCached;
  }
  public void setDescriptor(Descriptor descriptor) {
    this.descriptor = descriptor;
  }
  public Descriptor getDescriptor() {
    return descriptor;
  }
}

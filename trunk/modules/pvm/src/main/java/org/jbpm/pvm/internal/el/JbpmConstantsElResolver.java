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
package org.jbpm.pvm.internal.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;

import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.model.ScopeInstanceImpl;
import org.jbpm.pvm.internal.task.TaskImpl;


/**
 * @author Tom Baeyens
 */
public class JbpmConstantsElResolver extends ELResolver {

  private static final String NAME_EXECUTION = "execution";
  private static final String NAME_PROCESSINSTANCE = "processInstance";
  private static final String NAME_TASK = "task";

  ExecutionImpl execution;
  ExecutionImpl processInstance;
  TaskImpl task;
  
  public JbpmConstantsElResolver(ScopeInstanceImpl scopeInstance) {
    if (scopeInstance instanceof ExecutionImpl) {
      this.execution = (ExecutionImpl) scopeInstance;
      this.processInstance = execution.getProcessInstance();
      
    } else {
      this.task = (TaskImpl) scopeInstance;
      this.execution = task.getExecution();
      if (this.execution!=null) {
        this.processInstance = execution.getProcessInstance();
      }
    }
  }
  
  public Object getValue(ELContext context, Object base, Object property) {
    // this resolver only resolves top level variable names to execution variable names.
    // only handle if this is a top level variable
    if (base==null) {
      // we assume a NPE-check for property is not needed
      // i don't think the next cast can go wrong.  can it?
      String name = (String) property;

      if (execution!=null && NAME_EXECUTION.equals(name)) {
        context.setPropertyResolved(true);
        return execution;
      }
        
      if (processInstance!=null && NAME_PROCESSINSTANCE.equals(name)) {
        context.setPropertyResolved(true);
        return processInstance;
      }
        
      if (task!=null && NAME_TASK.equals(name)) {
        context.setPropertyResolved(true);
        return task;
      }
    }

    return null;
  }

  public boolean isReadOnly(ELContext context, Object base, Object property) {
    // this resolver only resolves top level variable names to execution variable names.
    // only handle if this is a top level variable
    if (base==null) {
      // we assume a NPE-check for property is not needed
      // i don't think the next cast can go wrong.  can it?
      String name = (String) property;

      if (NAME_EXECUTION.equals(name)
          || NAME_PROCESSINSTANCE.equals(name)
          || NAME_TASK.equals(name)) {
        return true;
      }
    }

    return true;
  }

  public void setValue(ELContext context, Object base, Object property, Object value) {
  }

  public Class< ? > getType(ELContext context, Object base, Object property) {
    return Object.class;
  }
  public Class< ? > getCommonPropertyType(ELContext context, Object base) {
    return Object.class;
  }
  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
    return null;
  }
}

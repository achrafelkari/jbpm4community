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

import org.hibernate.Query;
import org.hibernate.Session;
import org.jbpm.api.JbpmException;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.model.ScopeInstanceImpl;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.descriptor.ListDescriptor;


/**
 * @author Tom Baeyens
 */
public class HqlActivity extends JpdlAutomaticActivity {
  
  private static final Log log = Log.getLog(HqlActivity.class.getName());

  private static final long serialVersionUID = 1L;
  
  protected String query;
  protected ListDescriptor parametersDescriptor;
  protected String resultVariableName;
  protected boolean isResultUnique;

  public void perform(OpenExecution execution) {
    EnvironmentImpl environment = EnvironmentImpl.getCurrent();
    if (environment==null) {
      throw new JbpmException("no environment for jpdl activity "+HqlBinding.TAG);
    }
    Session session = environment.get(Session.class);
    
    Query q = createQuery(session);
    
    if (parametersDescriptor!=null) {
      for (Descriptor valueDescriptor: parametersDescriptor.getValueDescriptors()) {
        String parameterName = valueDescriptor.getName();
        Object value = WireContext.create(valueDescriptor, (ScopeInstanceImpl) execution);
        applyParameter(q, parameterName, value);
      }
    }
    
    Object result = null;
    if (isResultUnique) {
      result = q.uniqueResult();
    } else {
      result = q.list();
    }
    
    execution.setVariable(resultVariableName, result);
  }

  protected Query createQuery(Session session) {
    return session.createQuery(query);
  }

  public void applyParameter(Query q, String parameterName, Object value) {
    if (value instanceof String) {
      q.setString(parameterName, (String) value);
    } else if (value instanceof Long) {
      q.setLong(parameterName, (Long) value);
    } else {
      log.error("unknown hql parameter type: "+value.getClass().getName());
    }
  }

  public void setQuery(String query) {
    this.query = query;
  }
  public void setParametersDescriptor(ListDescriptor parametersDescriptor) {
    this.parametersDescriptor = parametersDescriptor;
  }
  public void setResultUnique(boolean isResultUnique) {
    this.isResultUnique = isResultUnique;
  }
  public void setResultVariableName(String resultVariableName) {
    this.resultVariableName = resultVariableName;
  }
}

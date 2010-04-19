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

import org.jbpm.api.model.OpenExecution;
import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.el.UelValueExpression;
import org.jbpm.pvm.internal.model.ScopeInstanceImpl;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.WireContext;


/**
 * @author Tom Baeyens
 */
public class AssignActivity extends JpdlAutomaticActivity {

  private static final long serialVersionUID = 1L;

  protected Expression fromExpression;
  protected Descriptor fromValueDescriptor;

  protected String toVariableName;
  protected UelValueExpression toExpression;

  void perform(OpenExecution execution) throws Exception {
    Object value = null;
    
    if (fromExpression!=null) {
      value = fromExpression.evaluate(execution);
      
    } else if (fromValueDescriptor!=null) {
      value = WireContext.create(fromValueDescriptor, (ScopeInstanceImpl) execution);
    }
    
    if (toVariableName!=null) {
      execution.setVariable(toVariableName, value);
    } else {
      toExpression.setValue(execution, value);
    }
  }

  public void setToVariableName(String variableName) {
    this.toVariableName = variableName;
  }
  public void setFromValueDescriptor(Descriptor valueDescriptor) {
    this.fromValueDescriptor = valueDescriptor;
  }
  public void setFromExpression(Expression fromExpression) {
    this.fromExpression = fromExpression;
  }
  public void setToExpression(UelValueExpression toExpression) {
    this.toExpression = toExpression;
  }
}

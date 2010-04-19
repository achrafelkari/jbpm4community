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
package org.jbpm.pvm.internal.model;

import java.io.Serializable;

import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.WireContext;

/** a declaration of a variable.
 * 
 * The source properties denote how the variable gets initialized upon creation.
 * 
 * The destination properties indicate how information from the current scope
 * gets propagated back into the outer scope.
 * 
 * @author Tom Baeyens
 */
public class VariableDefinitionImpl implements Serializable {

  private static final long serialVersionUID = 1L;

  protected String name;
  protected String typeName;
  protected boolean isHistoryEnabled;

  protected Expression initExpression;
  protected Descriptor initDescriptor;

  public Object getInitValue(ExecutionImpl execution) {
    if (initDescriptor!=null) {
      return WireContext.create(initDescriptor, execution);
    }
    if (initExpression!=null) {
      return initExpression.evaluateInScope(execution);
    }
    return null;
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public Expression getInitExpression() {
    return initExpression;
  }
  public void setInitExpression(Expression initExpression) {
    this.initExpression = initExpression;
  }
  public Descriptor getInitDescriptor() {
    return initDescriptor;
  }
  public void setInitDescriptor(Descriptor inDescriptor) {
    this.initDescriptor = inDescriptor;
  }
  public String getTypeName() {
    return typeName;
  }
  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }
  public boolean isHistoryEnabled() {
    return isHistoryEnabled;
  }
  public void setHistoryEnabled(boolean isHistoryEnabled) {
    this.isHistoryEnabled = isHistoryEnabled;
  }
}

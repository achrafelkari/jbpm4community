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

import java.util.List;

import javax.naming.InitialContext;

import org.jbpm.api.JbpmException;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.pvm.internal.util.ReflectUtil;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireDefinition;
import org.jbpm.pvm.internal.wire.descriptor.ArgDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;
import org.jbpm.pvm.internal.wire.usercode.UserCodeReference;


/**
 * @author Tom Baeyens
 * @author Koen Aers
 */
public class JavaActivity extends JpdlAutomaticActivity {

  private static final long serialVersionUID = 1L;

  protected UserCodeReference invocationReference;

  protected String methodName = null;
  protected List<ArgDescriptor> argDescriptors = null;

  protected String variableName;
  protected String jndiName;

  public void perform(OpenExecution execution) throws Exception {

    Object target = null;
    if (invocationReference != null) {
      target = invocationReference.getObject(execution);
    } else if (jndiName != null) {
      target = new InitialContext().lookup(jndiName);
    } else {
      throw new JbpmException("no target specified");
    }

    Class<?> clazz;
    // method invocation on object or static method invocation in case object is null
    if (target!=null) {
      clazz = target.getClass();
    } else {
      ObjectDescriptor objectDescriptor = (ObjectDescriptor) invocationReference.getDescriptor();
      String className = objectDescriptor.getClassName();
      clazz = ReflectUtil.classForName(className);
    }

    WireContext wireContext = new WireContext(new WireDefinition());
    Object returnValue = ObjectDescriptor.invokeMethod(methodName, argDescriptors, wireContext, target, clazz);

    if (variableName!=null) {
      execution.setVariable(variableName, returnValue);
    }
  }

  public void setInvocationReference(UserCodeReference invocationReference) {
    this.invocationReference = invocationReference;
  }
  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }
  public String getMethodName() {
    return methodName;
  }
  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }
  public void setJndiName(String jndiName) {
    this.jndiName = jndiName;
  }
  public List<ArgDescriptor> getArgDescriptors() {
    return argDescriptors;
  }
  public void setArgDescriptors(List<ArgDescriptor> argDescriptors) {
    this.argDescriptors = argDescriptors;
  }
  public UserCodeReference getInvocationReference() {
    return invocationReference;
  }
  public String getVariableName() {
    return variableName;
  }
  public String getJndiName() {
    return jndiName;
  }
}

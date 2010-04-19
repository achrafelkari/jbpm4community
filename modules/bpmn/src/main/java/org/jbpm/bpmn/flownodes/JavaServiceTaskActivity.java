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
package org.jbpm.bpmn.flownodes;

import java.lang.reflect.Method;
import java.util.List;

import org.jbpm.api.JbpmException;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.util.ReflectUtil;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireException;
import org.jbpm.pvm.internal.wire.descriptor.ArgDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;
import org.jbpm.pvm.internal.wire.operation.InvokeOperation;


/**
 * @author Tom Baeyens
 */
public class JavaServiceTaskActivity extends BpmnAutomaticActivity {

  private static final long serialVersionUID = 1L;
  
  protected Expression targetExpression;
  protected Object target;

  protected String methodName;
  protected String variableName;
  protected InvokeOperation invokeOperation;
  
  public void perform(OpenExecution execution) {
    
    Object invocationTarget = null;

    WireContext wireContext = new WireContext();

    if (target!=null) {
      invocationTarget = target;

    } else if (targetExpression!=null) {
      invocationTarget = targetExpression.evaluate(execution);
    
    } else {
      throw new JbpmException("no target specified");
    }

    try {
      List<ArgDescriptor> argDescriptors = null;
      Object[] args = null;
      if (invokeOperation!=null) {
        argDescriptors = invokeOperation.getArgDescriptors();
        args = ObjectDescriptor.getArgs(wireContext, argDescriptors);
      }
      
      Class<?> clazz = invocationTarget.getClass();
      Method method = ReflectUtil.findMethod(clazz, methodName, argDescriptors, args);
      if (method==null) {
        throw new WireException("method "+ReflectUtil.getSignature(methodName, argDescriptors, args)+" unavailable");
      }

      Object returnValue = ReflectUtil.invoke(method, invocationTarget, args);
      
      if (variableName!=null) {
        execution.setVariable(variableName, returnValue);
      }
      
    } catch (WireException e) {
      throw e;
    } catch (Exception e) {
      throw new WireException("couldn't invoke method "+methodName+": "+e.getMessage(), e);
    }
    proceed((ExecutionImpl)execution, findOutgoingSequenceFlow((ExecutionImpl)execution, CONDITIONS_CHECKED));
  }

  public void setTarget(Object target) {
    this.target = target;
  }
  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }
  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }
  public void setInvokeOperation(InvokeOperation invokeOperation) {
    this.invokeOperation = invokeOperation;
  }
  public void setTargetExpression(Expression expression) {
    this.targetExpression = expression;
  }
}

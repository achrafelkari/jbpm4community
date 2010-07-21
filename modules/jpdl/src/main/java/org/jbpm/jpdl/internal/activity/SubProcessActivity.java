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
import java.util.Map;

import javax.el.PropertyNotFoundException;

import org.jbpm.api.JbpmException;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.model.Activity;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.env.Context;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.env.ExecutionContext;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.session.RepositorySession;
import org.jbpm.pvm.internal.task.SwimlaneImpl;

/**
 * @author Tom Baeyens
 */
public class SubProcessActivity extends JpdlExternalActivity {

  private static final long serialVersionUID = 1L;

  protected String subProcessKey;
  protected String subProcessId;
  protected Map<String, String> swimlaneMappings;

  protected List<SubProcessInParameterImpl> inParameters;
  protected List<SubProcessOutParameterImpl> outParameters;

  protected Expression outcomeExpression;
  protected Map<Object, String> outcomeVariableMappings;

  public void execute(ActivityExecution execution) {
    ExecutionImpl executionImpl = (ExecutionImpl) execution;

    RepositorySession repositorySession = EnvironmentImpl.getFromCurrent(RepositorySession.class);

    ClientProcessDefinition processDefinition = null;

    if (subProcessId != null) {
      Expression subProcessKeyExpression = Expression.create(subProcessId, Expression.LANGUAGE_UEL_VALUE);
      String subProcessIdEval = (String) subProcessKeyExpression.evaluate(execution);
      processDefinition = repositorySession.findProcessDefinitionById(subProcessIdEval);

      if (processDefinition == null) {
        throw new JbpmException("cannot find process definition by id: ["
          + subProcessId + "(" + subProcessIdEval + ")" + "]");
      }
    } else {
      String subProcessKeyEval = null;
      try {
        Expression subProcessKeyExpression = Expression.create(subProcessKey, Expression.LANGUAGE_UEL_VALUE);
        subProcessKeyEval = (String) subProcessKeyExpression.evaluate(execution);
        if (subProcessKeyEval == null) {
          throw new JbpmException("Subprocess key '" + subProcessKey +"' resolved to null.");
        }
      } catch (PropertyNotFoundException e) {
        throw new JbpmException("Subprocess key '" + subProcessKey +"' could not be resolved.");
      }

      processDefinition = repositorySession.findProcessDefinitionByKey(subProcessKeyEval);
      if (processDefinition == null) {
        throw new JbpmException("Subprocess '" + subProcessKeyEval + "' could not be found.");
      }

      if (processDefinition == null) {
        throw new JbpmException("cannot find process definition by key: ["
          + subProcessKey + "(" + subProcessKeyEval + ")" + "]");
      }
    }

    ExecutionImpl subProcessInstance = (ExecutionImpl) processDefinition.createProcessInstance(null, execution);

    for (String swimlaneName: swimlaneMappings.keySet()) {
      String subSwimlaneName = swimlaneMappings.get(swimlaneName);
      SwimlaneImpl subSwimlane = subProcessInstance.createSwimlane(subSwimlaneName);
      SwimlaneImpl swimlane = executionImpl.getSwimlane(swimlaneName);
      if (swimlane!=null) {
        subSwimlane.initialize(swimlane);
      }
    }

    for (SubProcessInParameterImpl inParameter: inParameters) {
      inParameter.produce(executionImpl, subProcessInstance);
    }

    executionImpl.historyActivityStart();

    subProcessInstance.start();

    execution.waitForSignal();
  }

  public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
    signal((ExecutionImpl)execution, signalName, parameters);
  }

  public void signal(ExecutionImpl execution, String signalName, Map<String, ?> parameters) throws Exception {
    ExecutionImpl subProcessInstance = execution.getSubProcessInstance();

    String transitionName = null;

    ExecutionContext originalExecutionContext = null;
    ExecutionContext subProcessExecutionContext = null;
    EnvironmentImpl environment = EnvironmentImpl.getCurrent();
    if (environment!=null) {
      originalExecutionContext = (ExecutionContext) environment.removeContext(Context.CONTEXTNAME_EXECUTION);
      subProcessExecutionContext = new ExecutionContext(subProcessInstance);
      environment.setContext(subProcessExecutionContext);
    }

    try {
      subProcessInstance.setSuperProcessExecution(null);
      execution.setSubProcessInstance(null);


      for (SubProcessOutParameterImpl outParameter: outParameters) {
        outParameter.consume(execution, subProcessInstance);
      }

      Activity activity = execution.getActivity();
      String subProcessActivityName = subProcessInstance.getActivityName();

      if (outcomeExpression!=null) {
        Object value = outcomeExpression.evaluate(execution);
        // if the value is a String and matches the name of an outgoing transition
        if ( (value instanceof String)
             && (activity.hasOutgoingTransition(((String) value)))
           ) {
          // then take that one
          transitionName = (String) value;
        } else {
          // else see if there is a value mapping
          transitionName = outcomeVariableMappings.get(value);
        }

      } else if (activity.hasOutgoingTransition(subProcessActivityName)) {
        transitionName = subProcessActivityName;
      }

    } finally {
      if (subProcessExecutionContext!=null) {
        environment.removeContext(subProcessExecutionContext);
      }
      if (originalExecutionContext!=null) {
        environment.setContext(originalExecutionContext);
      }
    }

    execution.historyActivityEnd();

    if (transitionName!=null) {
      execution.take(transitionName);
    } else {
      execution.takeDefaultTransition();
    }
  }

  public void setSwimlaneMappings(Map<String, String> swimlaneMappings) {
    this.swimlaneMappings = swimlaneMappings;
  }
  public void setOutcomeVariableMappings(Map<Object, String> outcomeVariableMappings) {
    this.outcomeVariableMappings = outcomeVariableMappings;
  }
  public void setSubProcessKey(String subProcessKey) {
    this.subProcessKey = subProcessKey;
  }
  public void setSubProcessId(String subProcessId) {
    this.subProcessId = subProcessId;
  }
  public void setOutcomeExpression(Expression outcomeExpression) {
    this.outcomeExpression = outcomeExpression;
  }
  public List<SubProcessInParameterImpl> getInParameters() {
    return inParameters;
  }
  public void setInParameters(List<SubProcessInParameterImpl> inParameters) {
    this.inParameters = inParameters;
  }
  public List<SubProcessOutParameterImpl> getOutParameters() {
    return outParameters;
  }
  public void setOutParameters(List<SubProcessOutParameterImpl> outParameters) {
    this.outParameters = outParameters;
  }
}

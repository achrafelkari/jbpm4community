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
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.cal.CronExpression;
import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.history.HistoryEvent;
import org.jbpm.pvm.internal.history.events.VariableCreate;
import org.jbpm.pvm.internal.id.DbidGenerator;
import org.jbpm.pvm.internal.job.TimerImpl;
import org.jbpm.pvm.internal.task.TaskImpl;
import org.jbpm.pvm.internal.type.Converter;
import org.jbpm.pvm.internal.type.Type;
import org.jbpm.pvm.internal.type.TypeSet;
import org.jbpm.pvm.internal.type.Variable;
import org.jbpm.pvm.internal.type.variable.NullVariable;
import org.jbpm.pvm.internal.type.variable.UnpersistableVariable;
import org.jbpm.pvm.internal.util.Clock;


/**
 * @author Tom Baeyens
 */
public class ScopeInstanceImpl implements Serializable {

  private static final long serialVersionUID = 1L;
  private static Log log = Log.getLog(ScopeInstanceImpl.class.getName());
  
  protected long dbid;
  protected int dbversion;

  protected boolean hasVariables;
  protected Map<String, Variable> variables = new HashMap<String, Variable>();
  
  protected String state;
  protected String suspendHistoryState;

  /** {@link Expression}s can cache the elContext for this execution here */
  transient protected Object elContext = null;

  // variables ////////////////////////////////////////////////////////////////

  protected void initializeVariables(ScopeElementImpl scope, ExecutionImpl outerExecution) {
    // loop over all variable definitions
    List<VariableDefinitionImpl> variableDefinitions = scope.getVariableDefinitions();
    if (!variableDefinitions.isEmpty()){
      if (log.isTraceEnabled()) {
        log.trace("initializing variables in scope "+scope);
      }
      for (VariableDefinitionImpl variableDefinition: variableDefinitions) {
        String key = variableDefinition.getName();
        Object value = variableDefinition.getInitValue(outerExecution);
        String typeName = variableDefinition.getTypeName();
        boolean isHistoryEnabled = variableDefinition.isHistoryEnabled();
        createVariable(key, value, typeName, isHistoryEnabled);
      }
    }
  }

  public void createVariable(String key, Object value) {
    createVariable(key, value, null, false);
  }

  public void createVariable(String key, Object value, String typeName, boolean isHistoryEnabled) {
    Variable variable = createVariableObject(key, value, typeName, isHistoryEnabled);
    variables.put(variable.getKey(), variable);
    hasVariables = true;
  }

  protected Variable createVariableObject(String key, Object value, String typeName, boolean isHistoryEnabled) {
    log.debug("create variable '"+key+"' in '"+this+"' with value '"+value+"'");
    
    Type type = findType(key, value, typeName);
    Variable variable;

    if (type!=null) {
      Class<?> variableClass = type.getVariableClass();
      try {
        log.trace("creating new "+type+" variable "+key);
        variable = (Variable) variableClass.newInstance();
      } catch (Exception e) {
        throw new JbpmException("couldn't instantiate variable instance class '"+variableClass.getName()+"'");
      }
      Converter converter = type.getConverter();
      variable.setConverter(converter);

    } else {
      if (value==null) {
        log.trace("creating null variable for "+key);
        variable = new NullVariable();
      } else {
        log.trace("creating new unpersistable variable for "+key);
        variable = new UnpersistableVariable();
      }
    }

    variable.setKey(key);
    variable.setExecution(getExecution());
    variable.setTask(getTask());
    variable.setHistoryEnabled(isHistoryEnabled);
    variable.setValue(value, this);
    
    long dbid = DbidGenerator.getDbidGenerator().getNextId();
    variable.setDbid(dbid);

    if (isHistoryEnabled) {
      HistoryEvent.fire(new VariableCreate(variable));
    }
    
    return variable;
  }

  private static Type findType(String key, Object value, String typeName) {
    TypeSet typeSet = EnvironmentImpl.getFromCurrent(TypeSet.class, false);
    if (typeSet == null) return null;

    Type type;
    if (typeName == null || (type = typeSet.findTypeByName(typeName)) == null) {
      type = typeSet.findTypeByMatch(key, value);
    }
    return type;
  }

  public void setVariable(String key, Object value) {
    if (key==null) {
      throw new JbpmException("variableName is null");
    }

    Variable variable = getVariableObject(key);
    // if there is already a variable instance and it doesn't support the current type...
    if ( (variable!=null) 
         && (!variable.supports(value, this))
       ) {
      // delete the old variable instance
      log.debug("variable type change. deleting '"+key+"' from '"+this+"'");
      removeVariable(key);
      variable = null;
    }

    if (variable!=null) {
      log.debug("updating variable '"+key+"' in '"+this+"' to value '"+value+"'");
      variable.setValue(value, this);
    } else if (getParentVariableScope()==null) {
      createVariable(key, value, null, false);
    } else {
      getParentVariableScope().setVariable(key,value);
    }
  }

  public void setVariables(Map<String, ?> variables) {
    if (variables!=null) {
      for (Map.Entry<String, ?> entry : variables.entrySet()) {
        setVariable(entry.getKey(), entry.getValue());
      }
    }
  }
  
  public Object getVariable(String key) {
    Variable variable = getVariableObject(key);
    if (variable!=null) {
      return variable.getValue(this);
    }
    
    ScopeInstanceImpl parentScope = getParentVariableScope();
    if (parentScope!=null) {
      return parentScope.getVariable(key);
    }

    return null;
  }

  public Variable getVariableObject(String key) {
    return (hasVariables ? (Variable) variables.get(key) : null);
  }

  public boolean hasVariable(String key) {
    ScopeInstanceImpl parentScope = getParentVariableScope();
    return ( (hasVariables && variables.containsKey(key))
             || (parentScope!=null && parentScope.hasVariable(key))
           );
  }

  public Set<String> getVariableKeys() {
    Set<String> variableKeys = null;
    ScopeInstanceImpl parentScope = getParentVariableScope();
    if (parentScope!=null) {
      variableKeys = parentScope.getVariableKeys();
    } else {
      variableKeys = new TreeSet<String>();
    }
    if (hasVariables) {
      variableKeys.addAll(variables.keySet());
    }
    return variableKeys;
  }

  public Map<String, Object> getVariables() {
    Map<String, Object> values = null;
    ScopeInstanceImpl parentScope = getParentVariableScope();
    if (parentScope!=null) {
      values = parentScope.getVariables();
    } else {
      values = new TreeMap<String, Object>();
    }
    if (hasVariables) {
      for (Map.Entry<String, Variable> entry: variables.entrySet()) {
        String name = entry.getKey();
        Variable variable = entry.getValue();
        Object value = variable.getValue(this);
        values.put(name, value);
      }
    }
    return values;
  }
  
  public boolean hasVariables() {
    ScopeInstanceImpl parentScope = getParentVariableScope();
    return ( hasVariables
             || (parentScope!=null && parentScope.hasVariables())
           );
  }

  public boolean removeVariable(String key) {
    Variable variable = null;
    if (hasVariables) {
      variable = variables.remove(key);
      if (variables.isEmpty()) {
        hasVariables = false;
      }
      if (variable!=null) {
        return true;
      }
    }
    ScopeInstanceImpl parentScope = getParentVariableScope();
    if (parentScope!=null) {
      return parentScope.removeVariable(key);
    }
    // the actual value is not returned to prevent that an object 
    // has to be fetched from the db for it to be deleted
    return false;
  }

  public void removeVariables() {
    if (hasVariables) {
      variables.clear();
    }
    hasVariables = false;
  }

  // timers ///////////////////////////////////////////////////////////////////

  protected TimerImpl newTimer() {
    return new TimerImpl();
  }
  
  public TimerImpl createTimer() {
    return createTimer(null);
  }

  public TimerImpl createTimer(TimerDefinitionImpl timerDefinition) {
    if (log.isDebugEnabled()) {
      log.debug("creating timer on "+this.toString());
    }

    TimerImpl timer = newTimer();
    timer.setExecution(getTimerExecution());
    
    if (timerDefinition!=null) {
      timer.setEventName(timerDefinition.getEventName());
      timer.setSignalName(timerDefinition.getSignalName());
      timer.setDueDate(timerDefinition.getDueDate());
      timer.setDueDateDescription(timerDefinition.getDueDateDescription());
      
      if (timer.getDueDate() == null && timerDefinition.getCronExpression() != null) {
        try {
          timer.setDueDate(new CronExpression(timerDefinition.getCronExpression()).getNextValidTimeAfter(Clock.getTime()));
        } catch (ParseException pe) {
          throw new JbpmException("Can't parse cron expression " + timerDefinition.getCronExpression(), pe);
        }
      }
      
      Boolean isExclusive = timerDefinition.isExclusive();
      if (isExclusive!=null) {
        timer.setExclusive(isExclusive);
      }
      Integer retries = timerDefinition.getRetries();
      if (retries!=null) {
        timer.setRetries(retries);
     }
      // support for repeat attribute given as expression 
      // only if repeat is specified
      if (timerDefinition.getRepeat() != null) {
        Object repeatEl = Expression.create(timerDefinition.getRepeat(), Expression.LANGUAGE_UEL_VALUE).evaluate();
        timer.setRepeat(repeatEl.toString());
      } else {
        timer.setRepeat(timerDefinition.getRepeat());
      }
    }
    
    return timer;
  }

  protected void initializeTimers(ScopeElementImpl scope) {
    // initialize the timers
    Set<TimerDefinitionImpl> timerDefinitions = scope.getTimerDefinitions();
    if (!timerDefinitions.isEmpty()) {
      for (TimerDefinitionImpl timerDefinition: timerDefinitions) {
        TimerImpl timer = createTimer(timerDefinition);
        timer.schedule();
      }
    }
  }

  protected void destroyTimers(CompositeElementImpl scope) {
  }

  // state ////////////////////////////////////////////////////////////////////

  /** @see Execution#suspend() */
  public void suspend() {
    if (Execution.STATE_SUSPENDED.equals(state)) {
      return;
    }
    suspendHistoryState = state;
    state = Execution.STATE_SUSPENDED;
  }

  /** @see Execution#resume() */
  public void resume() {
    if (!Execution.STATE_SUSPENDED.equals(state)) {
      return;
    }
    state = suspendHistoryState;
    suspendHistoryState = null;
  }

  public void setState(String state) {
    this.state = state;
  }


  /** @see Execution#getState() */
  public String getState() {
    return state;
  }

  /** @see Execution#isActive() */
  public boolean isActive() {
    return Execution.STATE_ACTIVE_ROOT.equals(state)
           || Execution.STATE_ACTIVE_CONCURRENT.equals(state);
  }

  public boolean isSuspended() {
    return Execution.STATE_SUSPENDED.equals(state);
  }

  // customizable methods /////////////////////////////////////////////////////
  
  public ExecutionImpl getExecution() {
    return null;
  }

  public TaskImpl getTask() {
    return null;
  }

  public ExecutionImpl getTimerExecution() {
    return null;
  }

  public ScopeInstanceImpl getParentVariableScope() {
    return null;
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public long getDbid() {
    return dbid;
  }
  public Object getElContext() {
    return elContext;
  }
  public void setElContext(Object elContext) {
    this.elContext = elContext;
  }
}

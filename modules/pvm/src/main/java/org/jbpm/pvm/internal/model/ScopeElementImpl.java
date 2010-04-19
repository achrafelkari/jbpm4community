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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Tom Baeyens
 */
public class ScopeElementImpl extends ObservableElementImpl {

  private static final long serialVersionUID = 1L;

  protected List<VariableDefinitionImpl> variableDefinitions = new ArrayList<VariableDefinitionImpl>();
  protected Set<TimerDefinitionImpl> timerDefinitions = new HashSet<TimerDefinitionImpl>();
  
  public boolean isLocalScope() {
    return hasVariableDefinitions() || hasTimerDefinitions();
  }

  // variable definitions /////////////////////////////////////////////////////
  
  public boolean hasVariableDefinitions() {
    return !variableDefinitions.isEmpty();
  }

  public List<VariableDefinitionImpl> getVariableDefinitions() {
    if (!hasVariableDefinitions()) {
      return Collections.emptyList();
    }
    return variableDefinitions;
  }

  public VariableDefinitionImpl createVariableDefinition() {
    VariableDefinitionImpl variableDefinition = new VariableDefinitionImpl();
    variableDefinitions.add(variableDefinition);
    return variableDefinition;
  }

  // timer definitions ////////////////////////////////////////////////////////

  public boolean hasTimerDefinitions() {
    return ( (timerDefinitions!=null)
            && (!timerDefinitions.isEmpty())
          );
  }

  public Set<TimerDefinitionImpl> getTimerDefinitions() {
    if (!hasTimerDefinitions()) {
      return Collections.emptySet();
    }
    return timerDefinitions;
  }
  
  /** 
   * Creates an empty {@link TimerDefinitionImpl} and associates it with this scope element.
   */
  public TimerDefinitionImpl createTimerDefinition() {
    TimerDefinitionImpl timerDefinition = new TimerDefinitionImpl();
    timerDefinitions.add(timerDefinition);
    return timerDefinition;
  }
  
  /**
   * Associates the given timer definition with this scope element.
   */
  public void addTimerDefinition(TimerDefinitionImpl timerDefinition) {
    timerDefinitions.add(timerDefinition);
  }
  
}

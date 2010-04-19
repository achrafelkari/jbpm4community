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

import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.model.Activity;



/**
 * a graph (or tree) structure that can be executed.
 * 
 * <h2>Purpose</h2>
 * <p>ProcessDefinition is a base implementation that can be leveraged to build 
 * graph based execution languages.  While the ProcessDefinition class is concrete and 
 * can be used as-is (e.g. by aggregation), most likely processDefinition languages
 * will inherit from this ProcessDefinition and create more specialized implementations.  
 * </p>
 * 
 * <p>The specialized processDefinition language classes can extend this ProcessDefinition  
 * with new datastructures relevant for that perticular processDefinition language.
 * </p>
 * 
 * <h2>Structure</h2>
 * <p>A processDefinition contains a set of activities.  Activities can be connected with 
 * transitions or activities can have nested activities.  But the transitions and 
 * nested activities can be combined.
 * </p>
 * 
 * <h2>Execution</h2>
 * <p>To create a new execution for a given processDefinition, see {@link #startExecution()}.
 * </p>
 * 
 * @author Tom Baeyens
 */
public interface OpenProcessDefinition extends ProcessDefinition, CompositeElement {
  
  /** the initial activity of this process definition */
  Activity getInitial();

}
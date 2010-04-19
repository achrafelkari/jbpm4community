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
package org.jbpm.api.model;

/**
 * a transition in a {@link OpenProcessDefinition} graph.
 * 
 * <h3 id="guardconditions">Guard conditions</h3>
 * <p>TODO</p>
 * 
 * <h3 id="transitionsaswaitstates">Transitions as wait states</h3>
 * <p>A wait condition indicates wether a transition is to be taken synchronously
 * or wether the transition will behave as a wait state.
 * </p>
 * 
 * <p>Transitions that are wait states can occur when an analyst has 
 * an actual state (e.g. 'making loss') and a desired state (e.g. 'making profit')
 * and models a transition between those states.  In that case the transition 
 * might take a long time and hence it results into a wait state for the system.
 * </p>
 * 
 * <p>If the wait condition is null or if it returns false, then the transition 
 * will be taking synchronously.  Otherwise the transition will behave as 
 * a wait state and wait for a signal on the execution.
 * </p>
 * 
 * <p>Transitions as wait states has every thing to do with matching the 
 * process graph to transactions on the server.  If the transition is taken 
 * in one (the current) transaction, then the async condition should be empty 
 * or evaluate to false.  If the arrival of the execution in the destination 
 * activity should occur in a separate execution 
 * 
 * @author Tom Baeyens
 */
public interface Transition {
  /** the short display name given to this element. */
  String getName();
  
  /** the activity from which this transition leaves. */
  Activity getSource();

  /** the activity in which this transition arrives. */ 
  Activity getDestination();
}
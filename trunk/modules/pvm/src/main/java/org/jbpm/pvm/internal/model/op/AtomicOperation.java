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
package org.jbpm.pvm.internal.model.op;

import java.io.Serializable;

import org.jbpm.api.JbpmException;
import org.jbpm.pvm.internal.job.MessageImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;

/**
 * @author Tom Baeyens
 */
public abstract class AtomicOperation implements Serializable {

  private static final long serialVersionUID = 1L;
  public static final AtomicOperation EXECUTE_ACTIVITY = new ExecuteActivity();
  public static final AtomicOperation PROPAGATE_TO_PARENT = new MoveToParentActivity();
  public static final AtomicOperation TRANSITION_TAKE = new TransitionTake();
  public static final AtomicOperation TRANSITION_START_ACTIVITY = new TransitionStartActivity();
  public static final AtomicOperation EXECUTE_EVENT_LISTENER = new ExecuteEventListener();
  public static final AtomicOperation TRANSITION_END_ACTIVITY = new TransitionEndActivity();

  public abstract boolean isAsync(ExecutionImpl execution);
  public abstract MessageImpl createAsyncMessage(ExecutionImpl execution);
  public abstract void perform(ExecutionImpl execution);
  
  public static AtomicOperation parseAtomicOperation(String text) {
    if (text==null) {
      return null;
    }

    if (TRANSITION_END_ACTIVITY.toString().equals(text)) return TRANSITION_END_ACTIVITY; 
    if (EXECUTE_EVENT_LISTENER.toString().equals(text)) return EXECUTE_EVENT_LISTENER; 
    if (EXECUTE_ACTIVITY.toString().equals(text)) return EXECUTE_ACTIVITY; 
    if (TRANSITION_TAKE.toString().equals(text)) return TRANSITION_TAKE; 
    if (TRANSITION_START_ACTIVITY.toString().equals(text)) return TRANSITION_START_ACTIVITY; 
    if (PROPAGATE_TO_PARENT.toString().equals(text)) return PROPAGATE_TO_PARENT;

    throw new JbpmException("invalid atomic operation text: "+text);
  }
}

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

import java.util.List;

import org.jbpm.api.listener.EventListener;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.job.MessageImpl;
import org.jbpm.pvm.internal.model.Continuation;
import org.jbpm.pvm.internal.model.EventImpl;
import org.jbpm.pvm.internal.model.EventListenerReference;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.model.ObservableElement;
import org.jbpm.pvm.internal.model.ObservableElementImpl;

/**
 * @author Tom Baeyens
 */
public class ExecuteEventListener extends AtomicOperation {
  
  private static final long serialVersionUID = 1L;
  
  private static Log log = Log.getLog(ExecuteEventListener.class.getName());
  
  public boolean isAsync(ExecutionImpl execution) {
    int eventListenerIndex = execution.getEventListenerIndex();
    EventImpl event = execution.getEvent();
    
    if ( (eventListenerIndex==0)
         && (event.isAsync())
       ) {
      return true;
    }
    
    List<EventListenerReference> eventListenerReferences = event.getListenerReferences();
    if ( (eventListenerReferences==null)
         || (eventListenerReferences.isEmpty())
       ) {
      return false;
    }

    EventListenerReference eventListenerReference = eventListenerReferences.get(eventListenerIndex);
    return eventListenerReference.isAsync();
  }

  public void perform(ExecutionImpl execution) {
    EventImpl event = execution.getEvent();
    ObservableElementImpl observableElement = event.getObservableElement();
    int eventListenerIndex = execution.getEventListenerIndex();
    List<EventListenerReference> eventListenerReferences = event.getListenerReferences();
    if ( (eventListenerReferences!=null)
         && (!eventListenerReferences.isEmpty())
       ) {
      EventListenerReference eventListenerReference = eventListenerReferences.get(eventListenerIndex);
      ObservableElement eventSource = execution.getEventSource();
      if ((eventSource == observableElement) || (eventListenerReference.isPropagationEnabled())) {
        EventListener eventListener = eventListenerReference.getEventListener();
        log.trace("executing " + eventListener + " for " + event);
        try {
          // TODO can/should this invocation be unified with the exception handler invocation of the event notification method?
          eventListener.notify(execution);
        } catch (Exception e) {
          log.trace("exception during action: " + e);
          execution.handleException(observableElement, event, eventListenerReference, e, "couldn't run action " + eventListener);
        }
      }
      // increment the event listener index
      eventListenerIndex++;
      execution.setEventListenerIndex(eventListenerIndex);
    }

    // if there are more listeners in this event
    if ( (eventListenerReferences!=null)
         && (eventListenerIndex < eventListenerReferences.size())
       ) {
      // execute the next listener
      execution.performAtomicOperation(AtomicOperation.EXECUTE_EVENT_LISTENER);

    } else {
      // there are no more listeners in this event

      ObservableElementImpl parent = observableElement.getParent();
      // find the next event with listeners
      EventImpl propagatedEvent = ExecutionImpl.findEvent(parent, event.getName());

      // if there is an propagated event with listeners 
      if (propagatedEvent != null) {
        // propagate to the that event
        execution.setEvent(propagatedEvent);
        execution.setEventListenerIndex(0);
        execution.performAtomicOperation(AtomicOperation.EXECUTE_EVENT_LISTENER);

      } else {
        // event is completed, perform the eventCompletedOperation 
        AtomicOperation eventCompletedOperation = execution.getEventCompletedOperation();

        execution.setEvent(null);
        execution.setEventSource(null);
        execution.setEventListenerIndex(0);
        execution.setEventCompletedOperation(null);

        if (eventCompletedOperation != null) {
          execution.performAtomicOperation(eventCompletedOperation);
        }
      }
    }
  }

  public MessageImpl createAsyncMessage(ExecutionImpl execution) {
    ExecuteEventListenerMessage message = new ExecuteEventListenerMessage(execution);
    if (execution.getEvent().getContinuation() == Continuation.EXCLUSIVE) {
      message.setExclusive(true);
    }
    return message;
  }

  public String toString() {
    return "ExecuteEventListener";
  }
}

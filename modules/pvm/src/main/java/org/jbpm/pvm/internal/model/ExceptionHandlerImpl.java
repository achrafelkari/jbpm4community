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
import java.util.ArrayList;
import java.util.List;


import org.jbpm.api.JbpmException;
import org.jbpm.api.listener.EventListener;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.cmd.CommandService;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.model.op.AtomicOperation;
import org.jbpm.pvm.internal.model.op.MoveToChildActivity;
import org.jbpm.pvm.internal.tx.Transaction;
import org.jbpm.pvm.internal.wire.Descriptor;

/**
 * @author Tom Baeyens
 */
public class ExceptionHandlerImpl implements Serializable {

  private static final long serialVersionUID = 1L;
  static final Log log = Log.getLog(ExceptionHandlerImpl.class.getName());

  protected long dbid;
  protected int dbversion;
  protected ProcessDefinitionImpl processDefinition;
  protected String exceptionClassName;
  protected boolean isTransactional;
  protected boolean isRethrowMasked;
  protected List<EventListenerReference> eventListenerReferences;
  protected String transitionName; // mutually exclusive with activityName
  protected String activityName;       // mutually exclusive with transitionName
  
  // construction methods /////////////////////////////////////////////////////
  
  public EventListenerReference createEventListenerReference(EventListener eventListener) {
    EventListenerReference eventListenerReference = createEventListenerReference();
    eventListenerReference.setEventListener(eventListener);
    return eventListenerReference;
  }

  public EventListenerReference createEventListenerReference(Descriptor descriptor) {
    EventListenerReference eventListenerReference = createEventListenerReference();
    eventListenerReference.setEventListenerDescriptor(descriptor);
    return eventListenerReference;
  }

  public EventListenerReference createEventListenerReference() {
    if (eventListenerReferences==null) {
      eventListenerReferences = new ArrayList<EventListenerReference>();
    }
    EventListenerReference eventListenerReference = new EventListenerReference();
    eventListenerReference.setProcessDefinition(processDefinition);
    eventListenerReferences.add(eventListenerReference);
    return eventListenerReference;
  }
  
  // runtime behaviour methods ////////////////////////////////////////////////
  
  public boolean matches(Exception exception) {
    return matches(exception.getClass());
  }

  public boolean matches(Class<?> exceptionClass) {
    if (exceptionClass==null) {
      return false;
    }
    if ( (exceptionClassName==null)
         || (exceptionClass.getName().equals(exceptionClassName)) 
       ) {
      return true;
    }
    Class<?> superClass = exceptionClass.getSuperclass();
    if (superClass!=null) {
      return matches(superClass);
    }
    return false;
  }
  
  public void handle(ExecutionImpl execution, Exception exception) {
    if (isTransactional) {
      EnvironmentImpl environment = EnvironmentImpl.getCurrent();
      Transaction transaction = (environment!=null ? environment.get(Transaction.class) : null);
      if (transaction!=null) {
        log.trace("registering exception handler to "+transaction);
        CommandService commandService = environment.get(CommandService.class);
        if (commandService==null) {
          throw new JbpmException("environment doesn't have a command service for registering transactional exception handler", exception);
        }
        ExceptionHandlerSynchronization exceptionHandlerSynchronization = new ExceptionHandlerSynchronization(
                this, execution, 
                exception,
                commandService
        );
        // registration of the synchronization is delegated to the AfterTxCompletionListener
        // to avoid a dependency on class Synchronization
        exceptionHandlerSynchronization.register(transaction);
        log.trace("registering exception handler to "+transaction);
        throw new JbpmException("transaction exception handler registered handler after transaction completed.  make sure this transaction is rolled back", exception); 
      } else {
        throw new JbpmException("no transaction present in the environment for transactional exception handler", exception);
      }
    } else {
      executeHandler(execution, exception);
    }
  }
  
  void executeHandler(ExecutionImpl execution, Exception exception) {
    if (eventListenerReferences!=null) {
      for (EventListenerReference eventListenerReference: eventListenerReferences) {
        
        EventListener eventListener = eventListenerReference.getEventListener();
        
        log.trace("executing "+eventListener+" for "+this);
        try {
          eventListener.notify(execution);
        } catch (RuntimeException e) {
          throw e;
        } catch (Exception e) {
          throw new JbpmException("couldn't execute "+eventListener, e);
        }
      }
    }
    
    if (transitionName!=null) {
      ActivityImpl activity = execution.getActivity();
      if (activity==null) {
        // If the execution is not positioned in a activity, it must be 
        // positioned in a transition.  In that case we check if the 
        // transition is present on the enclosing activity.
        
        // The weird way of getting checking and fetching the parent activity
        // is because hibernate doesn't support instanceof.  The transition-->parent
        // relation is mapped as a ProcessElementImpl.  So we can't cast it to 
        // a ActivityImpl.  Ouch.
        // The workaround is to check if the parent is equal to the 
        // process definition.  If that is not the case, we can be sure 
        // that the parent is a activity.  In that case we look up the activity 
        // from the process definition by name.  OuchOuchSquare :-)
        
        TransitionImpl transition = execution.getTransition();
        log.trace("no current activity.  searching for transition from parent of "+transition);
        if (transition!=null) {
          OpenProcessDefinition processDefinition = transition.getProcessDefinition();
          ObservableElementImpl transitionParent = transition.getParent();
          
          if ( (transitionParent!=null)
               && (! transitionParent.equals(processDefinition))
             ) {
            activity = (ActivityImpl) processDefinition.findActivity(transitionParent.getName());
          }
        }
      }
      
      if (activity!=null) {
        TransitionImpl transition = activity.findOutgoingTransition(transitionName);
        if (transition!=null) {
          log.trace(toString()+" takes transition "+transitionName);
          execution.setTransition(transition);
          execution.performAtomicOperationSync(AtomicOperation.TRANSITION_END_ACTIVITY);
        } else {
          log.info("WARNING: "+toString()+" couldn't find transition "+transitionName+" on "+activity);
        }
      } else {
        log.info("WARNING: "+toString()+" couldn't find current activity to take transition "+transitionName);
      }

    } else if (activityName!=null) {
      // execute child activity
      ActivityImpl activity = execution.getActivity();
      ActivityImpl childActivity = ( activity!=null ? activity.getActivity(activityName) : null );
      if (childActivity!=null) {
        log.trace(toString()+" takes transition "+transitionName);
        execution.performAtomicOperationSync(new MoveToChildActivity(childActivity));
      } else {
        log.info("WARNING: "+toString()+" couldn't find child activity "+activityName);
      }
    }
  }

  public static void rethrow(Exception exception, String prefixMessage) {
    log.trace("rethrowing "+exception);
    if (exception instanceof RuntimeException) {
      throw (RuntimeException) exception;
    } else {
      throw new JbpmException(prefixMessage+": "+exception.getMessage(), exception);
    }
  }

  public String toString() {
    return (exceptionClassName!=null ? "exception-handler("+exceptionClassName+")" : "exception-handler");
  }

  // getters and setters //////////////////////////////////////////////////////

  public long getDbid() {
    return dbid;
  }
  public String getExceptionClassName() {
    return exceptionClassName;
  }
  public void setExceptionClassName(String exceptionClassName) {
    this.exceptionClassName = exceptionClassName;
  }
  public boolean isTransactional() {
    return isTransactional;
  }
  public void setTransactional(boolean isTransactional) {
    this.isTransactional = isTransactional;
  }
  public String getTransitionName() {
    return transitionName;
  }
  public void setTransitionName(String transitionName) {
    this.transitionName = transitionName;
  }
  public String getActivityName() {
    return activityName;
  }
  public void setActivityName(String activityName) {
    this.activityName = activityName;
  }
  public boolean isRethrowMasked() {
    return isRethrowMasked;
  }
  public void setRethrowMasked(boolean isRethrowMasked) {
    this.isRethrowMasked = isRethrowMasked;
  }
  public List<EventListenerReference> getEventListenerReferences() {
    return eventListenerReferences;
  }
  public void setEventListenerReferences(List<EventListenerReference> eventListenerReferences) {
    this.eventListenerReferences = eventListenerReferences;
  }
  public ProcessDefinitionImpl getProcessDefinition() {
    return processDefinition;
  }
  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
  }
}

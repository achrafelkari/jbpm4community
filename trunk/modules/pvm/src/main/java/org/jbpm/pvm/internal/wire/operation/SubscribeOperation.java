package org.jbpm.pvm.internal.wire.operation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.env.Context;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.util.FilterListener;
import org.jbpm.pvm.internal.util.Listener;
import org.jbpm.pvm.internal.util.Observable;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireDefinition;
import org.jbpm.pvm.internal.wire.WireException;
import org.jbpm.pvm.internal.wire.descriptor.ArgDescriptor;


/**
 * subscribes to an {@link Observable observable}.
 *
 * <p>The target object can be a {@link Listener}
 * or a specific method to call can be specified (by {@link #setMethodName(String)})</p>
 *
 * <p>The event can be filtered by specifying a {@link Context} (with {@link #setContextName(String)}),
 * objects to observe (with {@link #setObjectNames(List)}) and events to observe
 * (with {@link #setEventNames(List)}). If the objects or events are not specified,
 * then all objects and events are observed.</p>
 *
 * <p>The {@link #setWireEvents(boolean)} specifies if the object or the {@link Descriptor} events
 * should be observed.</p>
 *
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 *
 */
public class SubscribeOperation implements Operation {

  private static final long serialVersionUID = 1L;
  private static Log log = Log.getLog(SubscribeOperation.class.getName());

  String contextName = null;
  List<String> eventNames = null;

  boolean wireEvents = false;
  List<String> objectNames = null;
  String methodName = null;
  List<ArgDescriptor> argDescriptors = null;

  public void apply(Object target, WireContext targetWireContext) {
    Listener listener = null;

    // if a method has to be invoked, rather then using the observable interface
    if (methodName!=null) {
      listener = new MethodInvokerListener(
        methodName,
        argDescriptors,
        targetWireContext,
        target
      );
    } else {
      try {
        listener = (Listener) target;
      } catch (ClassCastException e) {
        throw new WireException("couldn't subscribe object "+target+ " because it is not a Listener");
      }
    }

    // if there is a filter specified on the event names
    if ( (eventNames!=null)
         && (! eventNames.isEmpty())
       ) {
      listener = new FilterListener(listener, eventNames);
    }

    // identify the wireContext
    WireContext wireContext = null;
    if (contextName!=null) {
      EnvironmentImpl environment = EnvironmentImpl.getCurrent();
      if (environment!=null) {
        try {
          wireContext = (WireContext) environment.getContext(contextName);
          if (wireContext==null) {
            throw new WireException("couldn't subscribe because context "+contextName+" doesn't exist");
          }
        } catch (ClassCastException e) {
          throw new WireException("couldn't subscribe because context "+contextName+" is not a WireContext", e);
        }
      } else {
        throw new WireException("couldn't get context "+contextName+" for subscribe because no environment available in context "+targetWireContext);
      }
    } else {
      wireContext = targetWireContext;
    }

    if (wireEvents) {
      WireDefinition wireDefinition = wireContext.getWireDefinition();
      
      // if there are objectNames specified
      if (objectNames!=null) {
        // subscribe to the descriptors for the all objectNames
        for (String objectName: objectNames) {
          Descriptor descriptor = wireDefinition.getDescriptor(objectName);
          subscribe(listener, descriptor);
        }
        
      // if no objectNames are specified, subscribe to all the descriptors
      } else { 
        Set<Descriptor> descriptors = new HashSet<Descriptor>(wireDefinition.getDescriptors().values());
        for(Descriptor descriptor: descriptors) {
          subscribe(listener, descriptor);
        }
      }

    } else if ( (objectNames!=null)
                && (!objectNames.isEmpty())
              ) {
      // for every objectName
      for (String objectName: objectNames) {
        // subscribe to the objects themselves
        Object object = wireContext.get(objectName);
        if (object==null) {
          throw new WireException("couldn't subscribe to object in context "+wireContext.getName()+": object "+objectName+" unavailable");
        }
        if (! (object instanceof Observable)) {
          throw new WireException("couldn't subscribe to object in context "+wireContext.getName()+": object "+objectName+" ("+object.getClass().getName()+") isn't "+Observable.class.getName());
        }
        subscribe(listener, (Observable)object);
      }

    } else {
      // subscribe to the context
      subscribe(listener, wireContext);
    }
  }

  void subscribe(Listener listener, Observable observable) {
    log.trace("adding "+listener+" as listener to "+observable);
    observable.addListener(listener);
  }


  /**
   * Gets the list of argDescriptor used to create the arguments given to the method (only if a specific method has to be called).
   */
  public List<ArgDescriptor> getArgDescriptors() {
    return argDescriptors;
  }
  /**
   * Sets the list of argDescriptor used to create the arguments given to the method.
   */
  public void setArgDescriptors(List<ArgDescriptor> argDescriptors) {
    this.argDescriptors = argDescriptors;
  }

  /**
   * Gets the list of events to listen to.
   */
  public List<String> getEventNames() {
    return eventNames;
  }
  /**
   * Sets the list of events to listen to.
   */
  public void setEventNames(List<String> eventNames) {
    this.eventNames = eventNames;
  }

  /**
   * Gets the name of the method to invoke when an event is received.
   */
  public String getMethodName() {
    return methodName;
  }

  /**
  * Sets the name of the method to invoke when an event is received.
  */
  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  /**
   * Gets the name of the WireContext where the Observable should be found.
   */
  public String getContextName() {
    return contextName;
  }

  /**
   * Sets the name of the WireContext where the Observable should be found.
   */
  public void setContextName(String contextName) {
    this.contextName = contextName;
  }

  /**
   * Gets the list of name of the Observable objects to observe.
   */
  public List<String> getObjectNames() {
    return objectNames;
  }

  /**
   * Sets the list of name of the Observable objects to observe.
   */
  public void setObjectNames(List<String> objectNames) {
    this.objectNames = objectNames;
  }

  /**
   * <p><code>true</code> if the target object will listen to Descriptor related events.</p>
   * <p><code>false</code> if the target object will listen to the object instance events.</p>
   */
  public boolean isWireEvents() {
    return wireEvents;
  }
  /**
   * Sets if the object should listen to descriptor events or to events fired by the named object.
   * <p><code>true</code> if the target object will listen to Descriptor related events.</p>
   * <p><code>false</code> if the target object will listen to the object instance events.</p>
   */
  public void setWireEvents(boolean wireEvents) {
    this.wireEvents = wireEvents;
  }
}

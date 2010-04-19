package org.jbpm.pvm.internal.util;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.api.JbpmException;

/** default implementation of the {@link Observable} interface.
 * 
 * @author Tom Baeyens
 */
public class DefaultObservable implements Observable {

  protected List<Listener> listeners = null;
  
  public void addListener(Listener listener) {
    if (listener==null) {
      throw new JbpmException("listener is null");
    }
    if (listeners==null) {
      listeners = new ArrayList<Listener>();
    }
    listeners.add(listener);
  }

  public void removeListener(Listener listener) {
    if (listener==null) {
      throw new JbpmException("listener is null");
    }
    if (listeners!=null) {
      listeners.remove(listener);
    }
  }

  public Listener addListener(Listener listener, String eventName) {
    if (eventName==null) {
      throw new JbpmException("eventName is null");
    }

    List<String> eventNames = new ArrayList<String>();
    eventNames.add(eventName);

    return addListener(listener, eventNames);
  }


  public Listener addListener(Listener listener, List<String> eventNames) {
    if (listener==null) {
      throw new JbpmException("listener is null");
    }
    if (eventNames==null) {
      throw new JbpmException("eventNames is null");
    }
    FilterListener filterListener = new FilterListener(listener, eventNames);
    addListener(filterListener);
    return filterListener;
  }

  public void fire(String eventName) {
    fire(eventName, null);
  }

  public void fire(String eventName, Object info) {
    if (listeners!=null) {
      for (Listener listener: listeners)  {
        listener.event(this, eventName, info);
      }
    }
  }
  
  public List<Listener> getListeners() {
    return listeners;
  }
}

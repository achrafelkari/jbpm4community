package org.jbpm.pvm.internal.util;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.api.JbpmException;

/** listener that only delegates events to a given listener if 
 * they pass the filter based on event names. */
public class FilterListener implements Listener {

  protected Listener listener;
  protected List<String> eventNames;

  public FilterListener(Listener listener, String eventName) {
    if (listener==null) throw new JbpmException("listener is null");
    this.listener = listener;
    if (eventName==null) throw new JbpmException("eventName is null");
    this.eventNames = new ArrayList<String>();
    this.eventNames.add(eventName);
  }

  public FilterListener(Listener listener, List<String> eventNames) {
    if (listener==null) throw new JbpmException("listener is null");
    this.listener = listener;
    if (eventNames==null) throw new JbpmException("eventNames is null");
    this.eventNames = eventNames;
  }
  
  public void event(Object source, String name, Object info) {
    if (! isFiltered(name)) {
      listener.event(source, name, info);
    }
  }

  public boolean isFiltered(String eventName) {
    if (eventNames.contains(eventName)) {
      return false;
    }
    return true;
  }

  public boolean equals(Object object) {
    if (object==null) return false;
    if (object==this) return true;
    if ( (object instanceof Listener)
         && (listener.equals(object))
       ) {
      return true;
    }
    return false;
  }
  public int hashCode() {
    return 17+listener.hashCode();
  }
}
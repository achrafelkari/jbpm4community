package org.jbpm.pvm.internal.util;

/** listener to events that are produced by an {@link Observable}.
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 */
public interface Listener {

  /** is called by the {@link Observable} when an event is fired.
   * @param source {@link Observable} that fired the event.
   * @param eventName name of the event.
   * @param info more information about the fired event.  See the concrete
   * observable docs for more information about what information is provided.
   */
  public void event(Object source, String eventName, Object info);
}

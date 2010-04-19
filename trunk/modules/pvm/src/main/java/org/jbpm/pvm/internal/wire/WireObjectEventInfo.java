package org.jbpm.pvm.internal.wire;

public class WireObjectEventInfo {

  String eventName;
  String objectName;
  Object object;

  public WireObjectEventInfo(String eventName, String objectName, Object object) {
    this.eventName = eventName;
    this.objectName = objectName;
    this.object = object;
  }

  public String getEventName() {
    return eventName;
  }
  public Object getObject() {
    return object;
  }
  public String getObjectName() {
    return objectName;
  }
  public String toString() {
    return eventName+"("+objectName+")";
  }
}
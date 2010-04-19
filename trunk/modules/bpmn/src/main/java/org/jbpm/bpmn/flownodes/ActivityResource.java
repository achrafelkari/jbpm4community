package org.jbpm.bpmn.flownodes;

import org.jbpm.bpmn.common.Resource;

public class ActivityResource {
  
  String id;
  Resource resourceRef;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
    
  public Resource getResourceRef() {
    return resourceRef;
  }
  
  public void setResourceRef(Resource resourceRef) {
    this.resourceRef = resourceRef;
  }
}

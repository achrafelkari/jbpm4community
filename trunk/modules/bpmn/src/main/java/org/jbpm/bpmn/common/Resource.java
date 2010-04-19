package org.jbpm.bpmn.common;

import java.util.Map;

public class Resource {

  String id;
  String name;
  Map<String, ResourceParameter> parameters;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, ResourceParameter> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, ResourceParameter> parameters) {
    this.parameters = parameters;
  }

}

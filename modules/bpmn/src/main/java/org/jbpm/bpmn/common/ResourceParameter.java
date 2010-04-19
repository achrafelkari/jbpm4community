package org.jbpm.bpmn.common;

import javax.xml.namespace.QName;

import org.jbpm.pvm.internal.model.VariableDefinitionImpl;


public class ResourceParameter extends VariableDefinitionImpl {
  
  String id;
  QName type;
  boolean required;
  
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
   this.id = id;
  }
  
  public QName getType() {
    return type;
  }
  
  public void setType(QName type) {
    this.type = type;
  }
  
  public boolean isRequired() {
    return required;
  }
  
  public void setRequired(boolean required) {
    this.required = required;
  }
}

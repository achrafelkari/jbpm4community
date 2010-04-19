package org.jbpm.pvm.internal.migration;

import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessInstance;


public interface MigrationHandler {
  
  void migrateInstance(
          ProcessDefinition newProcessDefinition, 
          ProcessInstance processInstance,
          MigrationDescriptor migrationDescriptor);

}

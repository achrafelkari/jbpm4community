package org.jbpm.pvm.internal.migration;

import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessInstance;
import org.jbpm.pvm.internal.env.EnvironmentImpl;


public class AbortMigrationHandler implements MigrationHandler {

  public void migrateInstance(ProcessDefinition newProcessDefinition, ProcessInstance processInstance, MigrationDescriptor migrationDescriptor) {
    ExecutionService executionService = EnvironmentImpl.getFromCurrent(ExecutionService.class);
    if (executionService == null) return;
    executionService.endProcessInstance(processInstance.getId(), "aborted");
  }

}

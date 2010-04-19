package org.jbpm.pvm.internal.migration;

import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.model.Activity;
import org.jbpm.pvm.internal.history.HistoryEvent;
import org.jbpm.pvm.internal.history.events.ProcessInstanceMigration;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;


public class DefaultMigrationHandler implements MigrationHandler {

  public void migrateInstance(
          ProcessDefinition newProcessDefinition, 
          ProcessInstance processInstance, 
          MigrationDescriptor migrationDescriptor) {
    migrateExecutions(newProcessDefinition, processInstance, migrationDescriptor);
    logMigration(processInstance, newProcessDefinition);
  }

  private void migrateChildExecutions(
          ProcessDefinition processDefinition, 
          Execution execution, 
          MigrationDescriptor migrationDescriptor) {
    for (Execution child : execution.getExecutions()) {
      migrateExecutions(processDefinition, child, migrationDescriptor);
    }
  }

  private void migrateExecutions(ProcessDefinition processDefinition, Execution execution, MigrationDescriptor migrationDescriptor) {
    migrateChildExecutions(processDefinition, execution, migrationDescriptor);
    if (!(execution instanceof ExecutionImpl) || !(processDefinition instanceof ProcessDefinitionImpl))
      return;
    ((ExecutionImpl)execution).setProcessDefinition((ProcessDefinitionImpl)processDefinition);
    String oldName = ((ExecutionImpl) execution).getActivityName();
    if (oldName == null)
      return;
    String newName = migrationDescriptor.getNewName(MigrationDescriptor.ACTIVITY_TYPE, oldName);
    if (newName == null)
      newName = oldName;
    Activity newActivity = ((ProcessDefinitionImpl) processDefinition).getActivity(newName);
    if (newActivity != null) {
      ((ExecutionImpl) execution).setActivity(newActivity);
    } else {
      throw new JbpmException("the activity " + newActivity + " could not be found in the new process definition.");
    }
  }
  
  private static void logMigration(ProcessInstance processInstance, ProcessDefinition processDefinition) {
    ProcessInstanceMigration processInstanceMigrate = new ProcessInstanceMigration(processInstance, processDefinition);
    HistoryEvent.fire(processInstanceMigrate);
  }
  
}

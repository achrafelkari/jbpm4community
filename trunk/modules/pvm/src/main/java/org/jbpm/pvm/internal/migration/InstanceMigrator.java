package org.jbpm.pvm.internal.migration;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.api.Execution;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessDefinitionQuery;
import org.jbpm.api.ProcessInstance;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.session.RepositorySession;
import org.jbpm.pvm.internal.util.ReflectUtil;

public class InstanceMigrator {

  private static Log log = Log.getLog(InstanceMigrator.class.getName());

  public static void migrateAll(ProcessDefinition processDefinition, MigrationDescriptor migrationDescriptor) {
//    String oldVersionId = getOldVersionId(processDefinition.getName());
//    ExecutionService executionService = (ExecutionService) EnvironmentImpl.getFromCurrent(ExecutionService.class);
//    ProcessInstanceQuery processInstanceQuery = executionService
//      .createProcessInstanceQuery()
//      .processDefinitionId(oldVersionId);
//    List<ProcessInstance> processInstances = processInstanceQuery.list();
    List<ProcessInstance> processInstances = getProcessInstancesToMigrate(processDefinition.getName(), migrationDescriptor);
    for (ProcessInstance processInstance : processInstances) {
      migrateInstance(processDefinition, processInstance, migrationDescriptor);
    }
  }

  public static void migrateInstance(
          ProcessDefinition processDefinition, 
          ProcessInstance processInstance, 
          MigrationDescriptor migrationDescriptor) {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    for (String className : migrationDescriptor.getMigrationHandlerClassNames()) {
      try {
        Class<?> clazz = classLoader.loadClass(className);
        MigrationHandler handler = (MigrationHandler)ReflectUtil.newInstance(clazz);
        handler.migrateInstance(processDefinition, processInstance, migrationDescriptor);
      } catch (ClassNotFoundException e) {
        log.error("Class " + className + " not found on the classpath.", e);
      }
    }
    //migrateExecutions(processDefinition, processInstance, migrationDescriptor);
    // migrateSwimlanes(processInstance, migrationDescriptor);
    // migrateVariables(processInstance, migrationDescriptor);
    //logMigration(processInstance, processDefinition);
  }

  private static void migrateExecutions(ProcessDefinition processDefinition, Execution execution, MigrationDescriptor migrationDescriptor) {
    migrateChildExecutions(processDefinition, execution, migrationDescriptor);
//    if (!(execution instanceof ExecutionImpl) || !(processDefinition instanceof ProcessDefinitionImpl))
//      return;
//    ((ExecutionImpl)execution).setProcessDefinition((ProcessDefinitionImpl)processDefinition);
//    String oldName = ((ExecutionImpl) execution).getActivityName();
//    if (oldName == null)
//      return;
//    String newName = migrationDescriptor.getNewName(MigrationDescriptor.ACTIVITY_TYPE, oldName);
//    if (newName == null)
//      return;
//    Activity newActivity = ((ProcessDefinitionImpl) processDefinition).getActivity(newName);
//    if (newActivity != null) {
//      ((ExecutionImpl) execution).setActivity(newActivity);
//    }
  }
  
  private static void migrateChildExecutions(ProcessDefinition processDefinition, Execution execution, MigrationDescriptor migrationDescriptor) {
    for (Execution child : execution.getExecutions()) {
      migrateExecutions(processDefinition, child, migrationDescriptor);
    }
  }

//  private static void logMigration(ProcessInstance processInstance, ProcessDefinition processDefinition) {
//    ProcessInstanceMigration processInstanceMigrate = new ProcessInstanceMigration(processInstance, processDefinition);
//    HistoryEvent.fire(processInstanceMigrate);
//  }
  
  private static List<ProcessInstance> getProcessInstancesToMigrate(String processDefinitionName, MigrationDescriptor migrationDescriptor) {
    List<ProcessInstance> result = new ArrayList<ProcessInstance>();
    ExecutionService executionService = EnvironmentImpl.getFromCurrent(ExecutionService.class);
    List<ProcessDefinition> processesToMigrate = getProcessesToMigrate(processDefinitionName, migrationDescriptor);
    for (ProcessDefinition processDefinition : processesToMigrate) {
      result.addAll(
              executionService
                .createProcessInstanceQuery()
                .processDefinitionId(processDefinition.getId()).list());
    }
    return result;
  }

  private static List<ProcessDefinition> getProcessesToMigrate(String processDefinitionName, MigrationDescriptor migrationDescriptor) {
    RepositorySession repositorySession = EnvironmentImpl.getFromCurrent(RepositorySession.class);
    List<ProcessDefinition> processDefinitions = repositorySession
        .createProcessDefinitionQuery()
        .processDefinitionName(processDefinitionName)
        .orderAsc(ProcessDefinitionQuery.PROPERTY_VERSION)
        .list();
    int startIndex = calculateStartIndex(processDefinitions.size() - 1, migrationDescriptor);
    int endIndex = calculateEndIndex(processDefinitions.size() - 1, migrationDescriptor);
    if (startIndex > endIndex || startIndex < 0) {
      startIndex = endIndex;
    }
    return processDefinitions.subList(startIndex, endIndex);
  }
  
  private static int calculateStartIndex(int max, MigrationDescriptor migrationDescriptor) {
    int result = max - 1;
    if (migrationDescriptor.getStartVersion() != -1) {
      result = migrationDescriptor.getStartVersion() - 1;
    } else if (migrationDescriptor.getStartOffset() != -1) {
      result = max - migrationDescriptor.getStartOffset();
    }
    if (result < 0) result = 0;
    if (result > max - 1) result = max - 1;
    return result;
  }
  
  private static int calculateEndIndex(int max, MigrationDescriptor migrationDescriptor) {
    int result = max;
    if (migrationDescriptor.getEndVersion() != -1) {
      result = migrationDescriptor.getEndVersion();
    } else if (migrationDescriptor.getEndOffset() != -1) {
      result = max - migrationDescriptor.getEndOffset() + 1;
    }
    if (result < 1) result = 1;
    if (result > max - 1) result = max;
    return result;
  }
  
}

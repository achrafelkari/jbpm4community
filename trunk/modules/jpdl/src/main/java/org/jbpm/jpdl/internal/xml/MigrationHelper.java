package org.jbpm.jpdl.internal.xml;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.pvm.internal.migration.AbortMigrationHandler;
import org.jbpm.pvm.internal.migration.DefaultMigrationHandler;
import org.jbpm.pvm.internal.migration.MigrationDescriptor;
import org.jbpm.pvm.internal.xml.Parse;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class MigrationHelper {

  @SuppressWarnings("unchecked")
  public static void parseMigrationDescriptor(Element migrationElement, Parse parse, ProcessDefinition processDefinition) {
    Map<ProcessDefinition, MigrationDescriptor> migrations = (Map<ProcessDefinition, MigrationDescriptor>)parse.contextMapGet(Parse.CONTEXT_KEY_MIGRATIONS);
    if (migrations == null) {
      migrations = new HashMap<ProcessDefinition, MigrationDescriptor>();
      parse.contextMapPut(Parse.CONTEXT_KEY_MIGRATIONS, migrations);
    }
    MigrationDescriptor migrationDescriptor = new MigrationDescriptor();
    String action = migrationElement.getAttribute("action");
    if ("end".equals(action)) {
      migrationDescriptor.addMigrationHandlerClassName(AbortMigrationHandler.class.getName());
    }
    parseMigrationHandlers(migrationElement, migrationDescriptor);
    if (!"end".equals(action)) {
      migrationDescriptor.addMigrationHandlerClassName(DefaultMigrationHandler.class.getName());
      parseActivityMappings(migrationElement, migrationDescriptor);
    }
    String versions = migrationElement.getAttribute("versions");
    if (versions != null && !"".equals(versions)) {
      addVersionInformation(versions, migrationDescriptor);
    }
    migrations.put(processDefinition, migrationDescriptor);
  }
  
  private static void addVersionInformation(String versions, MigrationDescriptor migrationDescriptor) {
    boolean isStartInfoRelative = false;
    boolean isEndInfoRelative = false;
    int startValue = -1;
    int endValue = -1;
    if ("*".equals(versions)) {
      migrationDescriptor.setStartVersion(1);
      migrationDescriptor.setEndVersion(Integer.MAX_VALUE);
    } else {
      int separatorIndex = versions.indexOf("..");
      if (separatorIndex == -1) 
        throw new JbpmException("Wrong version information in migrate-instances descriptor.");
      String start = versions.substring(0, separatorIndex).trim();
      int minusIndex = start.indexOf('-');
      if (minusIndex != -1) {
        if (!"x".equals(start.substring(0, minusIndex).trim())) 
          throw new JbpmException("Relative version info should be of the form 'x - n'");
        isStartInfoRelative = true;
        start = start.substring(minusIndex + 1).trim();
      }
      try {
        startValue = new Integer(start);
        if (isStartInfoRelative) {
          migrationDescriptor.setStartOffset(startValue); 
        } else {
          migrationDescriptor.setStartVersion(startValue);
        }
      } catch (NumberFormatException e) {
        throw new JbpmException("Version information should be numeric.");
      }
      String end = versions.substring(separatorIndex + 2).trim();
      if ("x".equals(end)) return;
      minusIndex = end.indexOf('-');
      if (minusIndex != -1) {
        if (!"x".equals(end.substring(0, minusIndex).trim()))
          throw new JbpmException("Relative version info should be of the form 'x - n'");
        isEndInfoRelative = true;
        end = end.substring(minusIndex + 1).trim();
      }
      try {
        endValue = new Integer(end);
        if (isEndInfoRelative) {
          migrationDescriptor.setEndOffset(endValue);
        } else {
          migrationDescriptor.setEndVersion(endValue);
        } 
      } catch (NumberFormatException e) {
        throw new JbpmException("Version information should be numeric.");
      }      
    }
  }

  private static void parseActivityMappings(Element migrationElement, MigrationDescriptor migrationDescriptor) {
    NodeList activityMappings = migrationElement.getElementsByTagName("activity-mapping");
    for (int i = 0; i < activityMappings.getLength(); i++) {
      Node activityMapping = activityMappings.item(i);
      if (activityMapping instanceof Element) {
        String oldName = ((Element)activityMapping).getAttribute("old-name");
        String newName = ((Element)activityMapping).getAttribute("new-name");
        migrationDescriptor.addMigrationElement(MigrationDescriptor.ACTIVITY_TYPE, oldName, newName);
      }
    }
  }

  private static void parseMigrationHandlers(Element migrationElement, MigrationDescriptor migrationDescriptor) {
    NodeList migrationHandlers = migrationElement.getElementsByTagName("migration-handler");
    for (int i = 0; i < migrationHandlers.getLength(); i++) {
      Node migrationHandler = migrationHandlers.item(i);
      if (migrationHandler instanceof Element) {
        String className = ((Element)migrationHandler).getAttribute("class");
        if (className != null && !"".equals(className)) {
          migrationDescriptor.addMigrationHandlerClassName(className);
        }
      }
    }
  }
}

package org.jbpm.pvm.internal.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MigrationDescriptor {

  public static final String ACTIVITY_TYPE = "org.jbpm.pvm.internal.migration.activity";
  public static final String VARIABLE_TYPE = "org.jbpm.pvm.internal.migration.variable";
  public static final String SWIMLANE_TYPE = "org.jbpm.pvm.internal.migration.swimlane";

  private Map<String, Map<String, String>> migrationMap = new HashMap<String, Map<String, String>>();
  private List<String> migrationHandlerClassNames = new ArrayList<String>();
  
  private int startOffset = -1;
  private int endOffset = -1;
  
  private int startVersion = -1;
  private int endVersion = -1;

  public void addMigrationElement(String type, String oldName, String newName) {
    Map<String, String> typeMap = migrationMap.get(type);
    if (typeMap == null) {
      typeMap = new HashMap<String, String>();
      migrationMap.put(type, typeMap);
    }
    typeMap.put(oldName, newName);
  }

  public String getNewName(String type, String oldName) {
    String result = null;
    Map<String, String> typeMap = migrationMap.get(type);
    if (typeMap != null) {
      result = typeMap.get(oldName);
    }
    return result;
  }
  
  public void addMigrationHandlerClassName(String className) {
    migrationHandlerClassNames.add(className);
  }
  
  public List<String> getMigrationHandlerClassNames() {
    return migrationHandlerClassNames;
  }
  
  public int getStartOffset() {
    return startOffset;
  }
  
  public void setStartOffset(int startOffset) {
    this.startOffset = startOffset;
  }
  
  public int getEndOffset() {
    return endOffset;
  }
  
  public void setEndOffset(int endOffset) {
    this.endOffset = endOffset;
  }
  
  public int getStartVersion() {
    return startVersion;
  }
  
  public void setStartVersion(int startVersion) {
    this.startVersion = startVersion;
  }
  
  public int getEndVersion() {
    return endVersion;
  }
  
  public void setEndVersion(int endVersion) {
    this.endVersion = endVersion;
  }

}

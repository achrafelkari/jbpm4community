package org.jbpm.test.variables;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.List;

import org.codehaus.janino.DebuggingInformation;
import org.codehaus.janino.util.StringPattern;
import org.codehaus.janino.util.enumerator.EnumeratorSet;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;


public class DeserializeVariableTest extends JbpmTestCase {

  String deploymentId;
  
  protected void setUp() throws Exception {
    
    super.setUp();
    
    generateBeanClass();
    
    deploymentId = repositoryService.createDeployment()
    .addResourceFromClasspath("org/jbpm/test/variables/DeserializeTaskTest.jpdl.xml")
    .addResourceFromInputStream("org/jbpm/test/variables/Bean.class", new FileInputStream(new File("target/generated/org/jbpm/test/variables/Bean.class")))
    .deploy();

  }

  protected void tearDown() throws Exception {
    repositoryService.deleteDeploymentCascade(deploymentId);
    super.tearDown();
  }
  
  
  public void testDeserializeVariable() {
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("DeserializeTaskTest");
    String processInstanceId = processInstance.getId();

    List<Task> tasks = taskService.findPersonalTasks("alex");
    
    assertTrue(tasks.size() == 1);
    
    Task task = tasks.get(0);
    
    assertNotNull(taskService.getVariable(task.getId(), "bean"));
    
    assertNotNull(executionService.getVariable(processInstanceId, "bean"));
    
    taskService.completeTask(task.getId());
  }
  
  /**
   * This method is used to generate source class that will be used within a process but should not be 
   * on class path while executing process but retrieved from db.
   * 
   * Uses Janinio to compile the source.
   */
  public void generateBeanClass() {

    log.debug("Inside generateBeanClass method");
    File directory = new File("target/generated/org/jbpm/test/variables");

    if (!directory.exists()) {
      log.debug("No generated class directory, about to create source file");
      try {
        directory.mkdirs();
        File sourceFile = new File(directory, "Bean.java");
        FileWriter writer = new FileWriter(sourceFile);

        StringBuffer source = new StringBuffer();

        source.append("package org.jbpm.test.variables;\n");
        source.append("import java.io.Serializable;\n");
        source.append("public class Bean implements Serializable {\n");
        source.append("private static final long serialVersionUID = -5510563444135221777L;\n");
        source.append("  private String value;\n");
        source.append("  public Bean getResult() {\n");
        source.append("    this.value = \"test\";\n");
        source.append("        return this;\n");
        source.append("  }\n");
        source.append("}\n");

        writer.write(source.toString());

        writer.close();
        log.debug("Source file create in " + directory.getAbsolutePath());

        File destinationDirectory = org.codehaus.janino.Compiler.NO_DESTINATION_DIRECTORY;
        File[] optionalSourcePath = null;
        File[] classPath = { new File(".") };
        File[] optionalExtDirs = null;
        File[] optionalBootClassPath = null;
        String optionalCharacterEncoding = null;
        boolean verbose = false;
        EnumeratorSet debuggingInformation = DebuggingInformation.DEFAULT_DEBUGGING_INFORMATION;
        StringPattern[] warningHandlePatterns = org.codehaus.janino.Compiler.DEFAULT_WARNING_HANDLE_PATTERNS;
        boolean rebuild = false;
        
        log.debug("About to run Janinio compiler");
        
        org.codehaus.janino.Compiler javac = new org.codehaus.janino.Compiler(optionalSourcePath, classPath, optionalExtDirs, optionalBootClassPath,
                destinationDirectory, optionalCharacterEncoding, verbose, debuggingInformation, warningHandlePatterns, rebuild);
        javac.compile(new File[] { sourceFile });

        log.debug("Class compiled successfully");
      } catch (Exception e) {

        log.error("Error while creating additional resources", e);
      }
    }

  }

}

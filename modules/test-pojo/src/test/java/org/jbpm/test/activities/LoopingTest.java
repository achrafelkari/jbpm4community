package org.jbpm.test.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;
import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.client.ClientProcessInstance;
import org.jbpm.test.BaseJbpmTestCase;

public class LoopingTest extends BaseJbpmTestCase {
  
  public static class For implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;

    int startIndex = 0;
    int loops;
    int increment = 1;
    String indexKey = "index";

    public For(int loops) {
      this.loops = loops;
    }

    public void execute(ActivityExecution execution) throws Exception {
      Integer index = (Integer) execution.getVariable(indexKey);

      if (index==null) {
        execution.setVariable(indexKey, startIndex);
        execution.take("loop");
        
      } else {
        index++;
        if (index<(startIndex+loops)){
          execution.setVariable(indexKey, index);
          execution.take("loop");
          
        } else {
          execution.removeVariable(indexKey);
          execution.take("done");
        }
      }
    }
    public void signal(ActivityExecution execution, String signal, Map<String, ?> parameters) throws Exception {
      throw new UnsupportedOperationException();
    }
    
    public void setStartIndex(int startIndex) {
      this.startIndex = startIndex;
    }
    public void setLoops(int loops) {
      this.loops = loops;
    }
    public void setIncrement(int increment) {
      this.increment = increment;
    }
    public void setIndexVariable(String indexVariable) {
      this.indexKey = indexVariable;
    }
  }

  static List<Object> recordedIndexes = new ArrayList<Object>();
  
  public static class Recorder implements ActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) throws Exception {
      recordedIndexes.add(execution.getVariable("index"));
    }
  }

  public static class WaitState implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) {
      execution.waitForSignal();
    }
    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters){
    }
  }

  public void testWhile() {
    int loops = 20;
    
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess()
      .startActivity("for", new For(loops))
        .initial()
        .transition("recorder", "loop")
        .transition("end", "done")
      .endActivity()
      .startActivity("recorder", new Recorder())
        .transition("for")
      .endActivity()
      .startActivity("end", new WaitState())
      .endActivity()
    .endProcess();

    ClientProcessInstance processInstance = processDefinition.startProcessInstance();
    
    List<Object> expectedIndexes = new ArrayList<Object>();
    for (int i=0; i<loops; i++) expectedIndexes.add(i); 

    assertEquals(expectedIndexes, recordedIndexes);
    assertTrue(processInstance.isActive("end"));
  }
}

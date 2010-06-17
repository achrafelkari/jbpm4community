package org.jbpm.test.activity.script;

import java.util.HashMap;

import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmTestCase;


public class ScriptTest  extends JbpmTestCase {

  public void testIncompleteScriptDefinition() {
   
    try {
      deployJpdlXmlString(
        "<process name='ScriptProcess'> " +
        "  <start name='start'> " +
        "  <transition to='script'/> " +
        "  </start> " +
        "  <script name='script'> " +
        "  </script> " +
        "</process>"
      );
      
      fail("Should fail sine script definition is incomplete");
      
    } catch (Exception e) {
      String expectedErrorMsg = "error: <script...> element must have either expr attribute or nested text element";

      assertTrue(e.getMessage().trim().startsWith(expectedErrorMsg));
    }
  }
  
  public void testScriptDefinitionWithExpr() {
    
    deployJpdlXmlString(
      "<process name='ScriptProcess'> " +
      "  <start name='start'> " +
      "  <transition to='script'/> " +
      "  </start> " +
      "  <script name='script' expr='Send packet to #{receiver}' var='result'> " +
      "    <transition to='wait' />" +
      "  </script> " +
      "  <state name='wait' />" +
      "</process>"
    );
    
    HashMap<String, String> variables = new HashMap<String, String>();
    variables.put("receiver", "johndoe");
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ScriptProcess", variables);
    
    String resultVariable = (String) executionService.getVariable(processInstance.getId(), "result");
    
    assertEquals("Send packet to johndoe", resultVariable);
  }
  
  public void testScriptDefinitionWithTextElement() {
    
    
    deployJpdlXmlString(
      "<process name='ScriptProcess'> " +
      "  <start name='start'> " +
      "  <transition to='script'/> " +
      "  </start> " +
      "  <script name='script' var='result'> " +
      "    <text>Send packet to #{receiver}</text> " +
      "    <transition to='wait' />" +
      "  </script> " +
      "  <state name='wait' />" +
      "</process>"
    );
      
    HashMap<String, String> variables = new HashMap<String, String>();
    variables.put("receiver", "johndoe");
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ScriptProcess", variables);
    
    String resultVariable = (String) executionService.getVariable(processInstance.getId(), "result");
    
    assertEquals("Send packet to johndoe", resultVariable);
  }
}

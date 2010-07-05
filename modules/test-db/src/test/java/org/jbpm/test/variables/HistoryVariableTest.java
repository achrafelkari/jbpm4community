package org.jbpm.test.variables;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jbpm.api.JbpmException;
import org.jbpm.test.JbpmTestCase;

public class HistoryVariableTest extends JbpmTestCase {

  public void testDeclaredVariableWithHistory() {
    deployJpdlXmlString("<process name='var'>"
      + "  <variable name='test' type='string' init-expr='history' history='true'/>"
      + "  <start name='a'>"
      + "    <transition to='b' />"
      + "  </start>"
      + "  <state name='b'/>"
      + "</process>");

    executionService.startProcessInstanceByKey("var", "one");

    Set<String> variableNames = executionService.getVariableNames("var.one");
    assertEquals(1, variableNames.size());
    assertEquals("test", variableNames.iterator().next());

    String executionValue = (String) executionService.getVariable("var.one", "test");
    assertEquals("history", executionValue);

    Set<String> historyVariables = historyService.getVariableNames("var.one");
    assertEquals(1, historyVariables.size());
    assertEquals("test", historyVariables.iterator().next());

    String historyValue = (String) historyService.getVariable("var.one", "test");
    assertEquals("history", historyValue);
  }

  public void testDeclaredVariablesWithHistory() {
    deployJpdlXmlString("<process name='var'>"
      + " <variable name='test' type='string' init-expr='test value' history='true'/>"
      + " <variable name='real' type='string' init-expr='real value' history='true'/>"
      + "  <start name='a'>"
      + "    <transition to='b' />"
      + "  </start>"
      + "  <state name='b'/>"
      + "</process>");

    executionService.startProcessInstanceByKey("var", "one");

    Set<String> variableNames = executionService.getVariableNames("var.one");
    assertEquals(2, variableNames.size());

    String executionValue = (String) executionService.getVariable("var.one", "test");
    assertEquals("test value", executionValue);

    Set<String> historyVariables = historyService.getVariableNames("var.one");
    assertEquals(2, historyVariables.size());

    String historyValue = (String) historyService.getVariable("var.one", "real");
    assertEquals("real value", historyValue);
  }

  public void testCreateVariableWithHistory() {
    deployJpdlXmlString("<process name='var'>"
      + "  <start name='a'>"
      + "    <transition to='b' />"
      + "  </start>"
      + "  <state name='b'/>"
      + "</process>");

    executionService.startProcessInstanceByKey("var", "one");
    executionService.createVariable("var.one", "test2", "test3", true);

    Set<String> variableNames = executionService.getVariableNames("var.one");
    assertEquals(1, variableNames.size());
    assertEquals("test2", variableNames.iterator().next());

    String executionValue = (String) executionService.getVariable("var.one", "test2");
    assertEquals("test3", executionValue);

    Set<String> historyVariables = historyService.getVariableNames("var.one");
    assertEquals(1, historyVariables.size());
    assertEquals("test2", historyVariables.iterator().next());

    String historyValue = (String) historyService.getVariable("var.one", "test2");
    assertEquals("test3", historyValue);
  }

  public void testCreateVariablesWithHistory() {
    deployJpdlXmlString("<process name='var'>"
      + "  <start name='a'>"
      + "    <transition to='b' />"
      + "  </start>"
      + "  <state name='b'/>"
      + "</process>");

    executionService.startProcessInstanceByKey("var", "one");

    Map<String, String> variables = new HashMap<String, String>();
    variables.put("simple-var", "hello history");
    variables.put("test-var", "good day");
    variables.put("my-var", "cheers");
    executionService.createVariables("var.one", variables, true);

    Set<String> variableNames = executionService.getVariableNames("var.one");
    assertEquals(3, variableNames.size());

    String executionValue = (String) executionService.getVariable("var.one", "test-var");
    assertEquals("good day", executionValue);

    Set<String> historyVariables = historyService.getVariableNames("var.one");
    assertEquals(3, historyVariables.size());

    String historyValue = (String) historyService.getVariable("var.one", "simple-var");
    assertEquals("hello history", historyValue);
  }

  public void testDeclaredVariablesMixed() {
    deployJpdlXmlString("<process name='var'>"
      + " <variable name='test' type='string' init-expr='test value' history='true'/>"
      + " <variable name='real' type='string' init-expr='real value' history='false'/>"
      + "  <start name='a'>"
      + "    <transition to='b' />"
      + "  </start>"
      + "  <state name='b'/>"
      + "</process>");

    executionService.startProcessInstanceByKey("var", "one");

    Set<String> variableNames = executionService.getVariableNames("var.one");
    assertEquals(2, variableNames.size());

    String executionValue = (String) executionService.getVariable("var.one", "test");
    assertEquals("test value", executionValue);

    Set<String> historyVariables = historyService.getVariableNames("var.one");
    assertEquals(1, historyVariables.size());

    String historyValue = (String) historyService.getVariable("var.one", "test");
    assertEquals("test value", historyValue);

    assertNull(historyService.getVariable("var.one", "real"));
  }

  public void testDeclaredIntegerVariableWithHistory() {
    deployJpdlXmlString("<process name='var'>"
      + " <variable name='test' type='integer' init-expr='#{testV}' history='true'/>"
      + "  <start name='a'>"
      + "    <transition to='b' />"
      + "  </start>"
      + "  <state name='b'/>"
      + "</process>");

    Map<String, ?> vars = Collections.singletonMap("testV", 35);
    executionService.startProcessInstanceByKey("var", vars, "one");

    Set<String> variableNames = executionService.getVariableNames("var.one");
    assertEquals(2, variableNames.size());
    assertEquals("test", variableNames.iterator().next());

    Integer executionValue = (Integer) executionService.getVariable("var.one", "test");
    assertEquals(35, executionValue.intValue());

    Set<String> historyVariables = historyService.getVariableNames("var.one");
    assertEquals(1, historyVariables.size());
    assertEquals("test", historyVariables.iterator().next());

    String historyValue = (String) historyService.getVariable("var.one", "test");
    assertEquals("35", historyValue);
  }

  public void testDeclaredSerializableVariableWithHistory() {
    deployJpdlXmlString("<process name='var'>"
      + "  <variable name='test' type='serializable' history='true'>"
      + "    <object class='java.util.Date'>"
      + "      <constructor><arg><long value='1276086573250'/></arg></constructor>"
      + "    </object>"
      + "  </variable>"
      + "  <start name='a'>"
      + "    <transition to='b' />"
      + "  </start>"
      + "  <state name='b'/>"
      + "</process>");

    executionService.startProcessInstanceByKey("var", "one");

    Set<String> variableNames = executionService.getVariableNames("var.one");
    assertEquals(1, variableNames.size());
    assertEquals("test", variableNames.iterator().next());

    Date variableValue = (Date) executionService.getVariable("var.one", "test");
    assertEquals(1276086573250L, variableValue.getTime());

    Set<String> historyVariables = historyService.getVariableNames("var.one");
    assertEquals(1, historyVariables.size());
    assertEquals("test", historyVariables.iterator().next());

    String historyValue = (String) historyService.getVariable("var.one", "test");
    assertEquals(variableValue.toString(), historyValue);
  }
  public void testDeclaredVariableWithHistoryWrongProcessInstanceId() {
    deployJpdlXmlString("<process name='var'>"
        + "  <variable name='test' type='string' init-expr='history' history='true'/>"
        + "  <start name='a'>"
        + "    <transition to='b' />"
        + "  </start>"
        + "  <state name='b'/>"
        + "</process>");

      executionService.startProcessInstanceByKey("var", "one");

      Set<String> variableNames = executionService.getVariableNames("var.one");
      assertEquals(1, variableNames.size());
      assertEquals("test", variableNames.iterator().next());

      String executionValue = (String) executionService.getVariable("var.one", "test");
      assertEquals("history", executionValue);

      Set<String> historyVariables = historyService.getVariableNames("var.one");
      assertEquals(1, historyVariables.size());
      assertEquals("test", historyVariables.iterator().next());

      String wrongProcessInstanceId = "var.one.1";
      try {
        historyService.getVariable(wrongProcessInstanceId, "test");
        fail("should fail since it uses wrong process instance id");
      } catch (JbpmException e) {
        String message = e.getMessage();
        assertTrue(message, message.contains(wrongProcessInstanceId));
      }
    }
  
  public void testDeclaredVariableWithHistoryWrongProcess() {
    deployJpdlXmlString("<process name='var'>"
        + "  <variable name='test' type='string' init-expr='history' history='true'/>"
        + "  <start name='a'>"
        + "    <transition to='b' />"
        + "  </start>"
        + "  <state name='b'/>"
        + "</process>");

      executionService.startProcessInstanceByKey("var", "one");

      Set<String> variableNames = executionService.getVariableNames("var.one");
      assertEquals(1, variableNames.size());
      assertEquals("test", variableNames.iterator().next());

      String executionValue = (String) executionService.getVariable("var.one", "test");
      assertEquals("history", executionValue);
      try {
        
        historyService.getVariables(null, null);
        fail("should fail since process instance id is null");
      } catch (Exception e) {
        assertEquals("processInstanceId is null", e.getMessage());
      }
      try {
        
        historyService.getVariables("var.one", null);
        fail("should fail since variable names set is null");
      } catch (Exception e) {
        assertEquals("variableNames is null", e.getMessage());
      }
    }
}

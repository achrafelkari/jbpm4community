package org.jbpm.jpdl.parsing;

import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.api.model.Activity;
import org.jbpm.jpdl.internal.activity.TaskActivity;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.el.StaticTextExpression;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.task.TaskDefinitionImpl;

public class TaskParsingTest extends JpdlParseTestCase {

  public void testTaskParse() {
    ClientProcessDefinition definition = parse(
        "<process name='task parse'>" +
        "  <start>" +
        "    <transition to='t1' />" +
        "  </start>" +
        "  <task name='t1' duedate='1 day' priority='3' form='aForm'>" +
        "    <description>first task</description>" +
        "  </task>" +
        "</process>");

    Activity activity = definition.findActivity("t1");
    assert activity instanceof ActivityImpl : activity.getClass();
    ActivityImpl activityImpl = (ActivityImpl) activity;

    ActivityBehaviour behaviour = activityImpl.getActivityBehaviour();
    assert behaviour instanceof TaskActivity : behaviour.getClass();

    TaskActivity taskActivity = (TaskActivity) behaviour;
    TaskDefinitionImpl taskDefinition = taskActivity.getTaskDefinition();

    // check for properties not previously parsed
    StaticTextExpression descriptionExpression = (StaticTextExpression) taskDefinition.getDescription();
    assertEquals("first task", descriptionExpression.getText());
    assertEquals(3, taskDefinition.getPriority());
    assertEquals("aForm", taskDefinition.getFormResourceName());
    assertEquals("1 day", taskDefinition.getDueDateDescription());
  }
}

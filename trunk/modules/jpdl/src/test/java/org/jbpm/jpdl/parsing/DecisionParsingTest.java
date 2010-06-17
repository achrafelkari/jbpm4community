package org.jbpm.jpdl.parsing;

import java.util.List;
import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.api.model.Activity;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.jpdl.internal.activity.DecisionConditionActivity;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.el.StaticTextExpression;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.Condition;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.model.TransitionImpl;
import org.jbpm.pvm.internal.wire.usercode.UserCodeCondition;

public class DecisionParsingTest extends JpdlParseTestCase {

  public void testDecitionConditionParse() {
    ClientProcessDefinition definition = parse(
        "<process name='decision parse'>" +
        "  <start>" +
        "    <transition to='d1' />" +
        "  </start>" +
        "  <decision name='d1'>" +
        "    <transition to='end1'>" +
        "      <condition>" +
        "        <handler class='org.jbpm.jpdl.parsing.DecisionParingTest$CustomCondition'/>" +
        "      </condition>" +
        "    </transition>" +
        "  </decision>" +
        "  <end name='end1'/>" +
        "</process>");

    Activity activity = definition.findActivity("d1");
    assert activity instanceof ActivityImpl : activity.getClass();
    ActivityImpl activityImpl = (ActivityImpl) activity;

    ActivityBehaviour behaviour = activityImpl.getActivityBehaviour();
    assert behaviour instanceof DecisionConditionActivity : behaviour.getClass();

    List<TransitionImpl> outgoingTransitions = (List) activity.getOutgoingTransitions();
    assert outgoingTransitions.size() == 1 : outgoingTransitions.size();

    TransitionImpl transition = outgoingTransitions.get(0);
    Condition condition = transition.getCondition();

    assert condition instanceof UserCodeCondition : condition.getClass();
  }

  static class CustomCondition implements Condition {
    public boolean evaluate(OpenExecution execution) {
      return true;
    }
  }
}

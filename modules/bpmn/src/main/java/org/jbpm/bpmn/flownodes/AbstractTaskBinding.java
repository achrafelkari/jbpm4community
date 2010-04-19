package org.jbpm.bpmn.flownodes;

import java.util.List;

import org.jbpm.bpmn.model.BpmnProcessDefinition;
import org.jbpm.bpmn.parser.BpmnParser;
import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.task.TaskDefinitionImpl;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.xml.Parse;
import org.w3c.dom.Element;

/**
 * The AbstractTask is the task superclass and doesn't define own Behavior.
 * 
 * Spec: A Task which is not further specified is called Abstract Task (this was
 * referred to as the None Task in BPMN 1.2).
 * 
 * @author bernd.ruecker@camunda.com
 */
public class AbstractTaskBinding extends BpmnBinding {

  protected static final String HUMAN_PERFORMER = "humanPerformer";
  protected static final String PERFORMER = "performer";
  protected static final String POTENTIAL_OWNER = "potentialOwner";

  public AbstractTaskBinding(String tagName) {
    super(tagName);
  }
  
  public Object parse(Element element, Parse parse, BpmnParser bpmnParser) {
    ManualTaskActivity manualTaskActivity = new ManualTaskActivity();
    manualTaskActivity.setDefault(getDefault());
    return manualTaskActivity;
  }

  protected void addActivityResources(TaskDefinitionImpl taskDefinition, BpmnActivity taskActivity, Element element, Parse parse) {
    Element performer = XmlUtil.element(element, PERFORMER, false, parse);
    if (performer != null) {
      taskActivity.addActivityResource(addPerformer(taskDefinition, PERFORMER, performer, parse));
    }

    // Overrides 'performer'
    Element humanPerformer = XmlUtil.element(element, HUMAN_PERFORMER, false, parse);
    if (humanPerformer != null) {
      taskActivity.addActivityResource(addPerformer(taskDefinition, HUMAN_PERFORMER, humanPerformer, parse));
    }

    List<Element> potentialOwners = XmlUtil.elements(element, "potentialOwner");
    for (Element potentialOwner : potentialOwners) {
      taskActivity.addActivityResource(addPerformer(taskDefinition, POTENTIAL_OWNER, potentialOwner, parse));
    }
  }

  private ActivityResource addPerformer(TaskDefinitionImpl taskDefinition, String type, Element performer, Parse parse) {

    String resourceRef = XmlUtil.attribute(performer, "resourceRef", true, parse);
    
    BpmnProcessDefinition bpmnProcessDefinition = parse.contextStackFind(BpmnProcessDefinition.class);
    
    ActivityResource activityResource = new ActivityResource();
    activityResource.setResourceRef(bpmnProcessDefinition.getResource(resourceRef));

    String scope = XmlUtil.attribute(performer, "jbpm:type", false, parse); // Todo refactor for namespaces

    Element rae = XmlUtil.element(performer, "resourceAssignmentExpression", false, parse);
    if (rae != null) {
      String formalExpression = XmlUtil.element(rae, "formalExpression", true, parse).getTextContent().trim();
      String lang = XmlUtil.attribute(rae, "language", false, parse);
      Expression expression = Expression.create(formalExpression, lang);
      if (PERFORMER.equals(type) || HUMAN_PERFORMER.equals(type)) {
        taskDefinition.setAssigneeExpression(expression);
      } else if (POTENTIAL_OWNER.equals(type) && "user".equals(scope)) { // default is group
        taskDefinition.setCandidateUsersExpression(expression);
      } else { 
        taskDefinition.setCandidateGroupsExpression(expression);
      }
    }

    return activityResource;

  }

}
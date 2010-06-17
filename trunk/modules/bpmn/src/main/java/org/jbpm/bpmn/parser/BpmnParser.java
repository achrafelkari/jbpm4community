/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jbpm.bpmn.parser;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.jbpm.api.JbpmException;
import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.api.model.Activity;
import org.jbpm.api.model.Transition;
import org.jbpm.bpmn.common.Resource;
import org.jbpm.bpmn.common.ResourceParameter;
import org.jbpm.bpmn.flownodes.BpmnActivity;
import org.jbpm.bpmn.flownodes.BpmnBinding;
import org.jbpm.bpmn.flownodes.BpmnEvent;
import org.jbpm.bpmn.flownodes.NoneStartEventActivity;
import org.jbpm.bpmn.flownodes.SubProcessActivity;
import org.jbpm.bpmn.model.BpmnProcessDefinition;
import org.jbpm.bpmn.model.SequenceflowCondition;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.cal.CronExpression;
import org.jbpm.pvm.internal.cal.Duration;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.CompositeElementImpl;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;
import org.jbpm.pvm.internal.model.ScopeElementImpl;
import org.jbpm.pvm.internal.model.TimerDefinitionImpl;
import org.jbpm.pvm.internal.model.TransitionImpl;
import org.jbpm.pvm.internal.model.VariableDefinitionImpl;
import org.jbpm.pvm.internal.task.TaskDefinitionImpl;
import org.jbpm.pvm.internal.util.TagBinding;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.binding.ObjectBinding;
import org.jbpm.pvm.internal.wire.xml.WireParser;
import org.jbpm.pvm.internal.xml.Bindings;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;
import org.w3c.dom.Element;

/**
 * @author Tom Baeyens
 * @author Bernd Ruecker (bernd.ruecker@camunda.com)
 * @author Joram Barrez
 */
public class BpmnParser extends Parser {

  private static final Log log = Log.getLog(BpmnParser.class.getName());

  static ObjectBinding objectBinding = new ObjectBinding();
  public static final WireParser wireParser = WireParser.getInstance();

  static final String[] DEFAULT_ACTIVITIES_RESOURCES = { "jbpm.bpmn.flownodes.xml" };
  static final String CATEGORY_ACTIVITY = "activity";

  static BindingsParser bindingsParser = new BindingsParser();

  public BpmnParser() {
    
    initialize(); // initialises underlying SAX parser
    parseBindings(); // initialises bindings
    
    // Setting BPMN2 xsd schema
    List<String> schemaResources = new ArrayList<String>();
    schemaResources.add("BPMN20.xsd");
    schemaResources.add("DiagramDefinition.xsd");
    schemaResources.add("DiagramInterchange.xsd");
    schemaResources.add("BpmnDi.xsd");
    setSchemaResources(schemaResources);
    
  }

  public Object parseDocumentElement(Element documentElement, Parse parse) {
    List<ProcessDefinitionImpl> processDefinitions = new ArrayList<ProcessDefinitionImpl>();

    parseDefinition(documentElement, parse);

    for (Element processElement : XmlUtil.elements(documentElement, "process")) {
      ProcessDefinitionImpl processDefinition = parseProcess(processElement, parse);
      processDefinitions.add(processDefinition);
    }

    return processDefinitions;
  }

  public ProcessDefinitionImpl parseProcess(Element processElement, Parse parse) {
    BpmnProcessDefinition processDefinition = new BpmnProcessDefinition();

    parse.contextStackPush(processDefinition);
    try {

      String id = XmlUtil.attribute(processElement, "id", parse);
      String name = XmlUtil.attribute(processElement, "name");
      
      if (id != null && !"".equals(id)) {
        processDefinition.setName(id);        
      } else {
        parse.addProblem("Process has no or an empty id");
      }

      if (name != null) {
        processDefinition.setKey(name);
      }

      Element descriptionElement = XmlUtil.element(processElement, "documentation");
      if (descriptionElement != null) {
        String description = XmlUtil.getContentText(descriptionElement);
        processDefinition.setDescription(description);
      }

      parseResources((Element)processElement.getParentNode(), parse, processDefinition);
      
      parseInterfaces((Element)processElement.getParentNode(), parse, processDefinition);
      
      parseItemDefinitions((Element)processElement.getParentNode(), parse, processDefinition);

      parseMessages((Element)processElement.getParentNode(), parse, processDefinition);

      parseDataObjects(processElement, parse, processDefinition);

      // activities
      parseActivities(processElement, parse, processDefinition);

      // bind activities to their destinations
      parseSequenceFlow(processElement, parse, processDefinition);

    } finally {
      parse.contextStackPop();
    }

    return processDefinition;
  }

  // /////////////////////////////////////////////////////////////////////////////////////////

  protected void parseBindings() {
    Bindings bindings = new Bindings();
    setBindings(bindings);

    for (String activityResource : DEFAULT_ACTIVITIES_RESOURCES) {
      Enumeration<URL> resourceUrls = getResources(activityResource);
      if (resourceUrls.hasMoreElements()) {
        while (resourceUrls.hasMoreElements()) {
          URL resourceUrl = resourceUrls.nextElement();
          log.trace("loading bpmn activities from resource: " + resourceUrl);
          List<BpmnBinding> activityBindings = (List<BpmnBinding>) bindingsParser.createParse().setUrl(resourceUrl).execute().checkErrors(
                  "bpmn activities from " + resourceUrl.toString()).getDocumentObject();

          for (TagBinding binding : activityBindings) {
            binding.setCategory(CATEGORY_ACTIVITY);
            bindings.addBinding(binding);
          }
        }
      } else {
        log.trace("skipping unavailable activities resource: " + activityResource);
      }
    }
  }

  protected Enumeration<URL> getResources(String resourceName) {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Enumeration<URL> resourceUrls;
    try {
      resourceUrls = classLoader.getResources(resourceName);

      if (!resourceUrls.hasMoreElements()) {
        resourceUrls = BpmnParser.class.getClassLoader().getResources(resourceName);
      }
    } catch (Exception e) {
      throw new JbpmException("couldn't get resource urls for "+resourceName, e);
    }
    return resourceUrls;
  }

  private void parseDataObjects(Element element, Parse parse, BpmnProcessDefinition processDefinition) {
    List<VariableDefinitionImpl> variableDefinitions = new ArrayList<VariableDefinitionImpl>();

    for (Element dataElement : XmlUtil.elements(element, "dataObject")) {
      VariableDefinitionImpl variableDefinition = new VariableDefinitionImpl();

      String name = XmlUtil.attribute(dataElement, "id", parse);
      variableDefinition.setName(name);

      String typeRef = XmlUtil.attribute(dataElement, "itemSubjectRef");
      variableDefinition.setTypeName(processDefinition.getType(typeRef));

      variableDefinitions.add(variableDefinition);
    }

    processDefinition.setVariableDefinition(variableDefinitions);
  }

  public void parseActivities(Element element, Parse parse, CompositeElementImpl compositeElement) {
    List<Element> elements = XmlUtil.elements(element);
    for (Element nestedElement : elements) {
      String tagName = nestedElement.getLocalName();
      String name = XmlUtil.attribute(nestedElement, "name");
      String id = XmlUtil.attribute(nestedElement, "id", parse);

      TagBinding activityBinding = (TagBinding) getBinding(nestedElement, CATEGORY_ACTIVITY);
      if (activityBinding == null) {
        if (!"sequenceFlow".equals(tagName)) {
          log.debug("unrecognized activity: " + tagName);
        }
        continue;
      }

      ActivityImpl activity = compositeElement.createActivity();
      parse.contextStackPush(activity);
      try {
        activity.setType(activityBinding.getTagName());
        activity.setName(id);
        activity.setDescription(name);
        
        if (log.isDebugEnabled()) {
          log.debug("Parsing Activity: " + name + "(id=" + id + ")");          
        }
        
        ActivityBehaviour activityBehaviour = (ActivityBehaviour) activityBinding.parse(nestedElement, parse, this);
        activity.setActivityBehaviour(activityBehaviour);
      } finally {
        parse.contextStackPop();
      }
    }
  }

  public void parseSequenceFlow(Element element, Parse parse, BpmnProcessDefinition processDefinition) {
    List<Element> transitionElements = XmlUtil.elements(element, "sequenceFlow");
    for (Element transitionElement : transitionElements) {
      
      // Parse attributes
      String transitionName = XmlUtil.attribute(transitionElement, "name");
      String transitionId = XmlUtil.attribute(transitionElement, "id", parse);
      String sourceRef = XmlUtil.attribute(transitionElement, "sourceRef", parse);
      String targetRef = XmlUtil.attribute(transitionElement, "targetRef", parse);

      if (log.isDebugEnabled()) {
        log.debug(transitionId + ": " + sourceRef + " -> " + targetRef);        
      }
     
      // Create new outgoing transition on sourceActivity
      ActivityImpl sourceActivity = processDefinition.findActivity(sourceRef);
      TransitionImpl transition = null;
      if (sourceActivity != null) {
        transition = sourceActivity.createOutgoingTransition();
        transition.setName(transitionId);
        transition.setDescription(transitionName);
      } else {
        parse.addProblem("SourceRef " + sourceRef + " cannot be found");
      }
      
      // Create incoming transition on targetActivity
      ActivityImpl destinationActivity = processDefinition.findActivity(targetRef);
      if (destinationActivity != null) {
        destinationActivity.addIncomingTransition(transition);
      } else {
        parse.addProblem("TargetRef '" + targetRef + "' cannot be found");
      }

      // Set default sequence flow if applicable
      try {
        // If something went wrong parsing the activity, there is no behaviour and an exception is thrown in .getBehaviour()
        ActivityBehaviour behaviour = sourceActivity.getActivityBehaviour();
        if (behaviour instanceof BpmnActivity) {
          BpmnActivity bpmnActivity = (BpmnActivity) behaviour;
          String defaultSeqFlow = bpmnActivity.getDefault();
          if (bpmnActivity.isDefaultEnabled() && defaultSeqFlow != null) {
            if (transitionId.equals(defaultSeqFlow)) {
              processDefinition.findActivity(sourceRef).setDefaultOutgoingTransition(transition);
            }
          } else {
            processDefinition.findActivity(sourceRef).setDefaultOutgoingTransition(null);
          }
        } else {
          // Other flownodes do not have default sequenceFlows, so set it to null
          processDefinition.findActivity(sourceRef).setDefaultOutgoingTransition(null);
        }

      } catch (JbpmException je) {
        // catch it and only re-throw if not this specific exception.
        if (!je.getMessage().contains("no behaviour on")) {
          throw je;
        }
      }

      parseConditionOnSequenceFlow(parse, transitionElement, transitionId, transition);

      processDefinition.addSequenceFlow(transitionId, transition);
    }
  }

  public void parseConditionOnSequenceFlow(Parse parse, Element transitionElement, String transitionId, TransitionImpl transition) {
    Element conditionElement = XmlUtil.element(transitionElement, "conditionExpression");
    if (conditionElement != null) {
      String type = conditionElement.getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "type");
      if ("bpmn:tFormalExpression".equals(type) || "tFormalExpression".equals(type)) {

        String expr = conditionElement.getTextContent();
        String lang = XmlUtil.attribute(conditionElement, "language");
        // TODO: add looking up the default language in the document under definitions if lang  is null.
        
        SequenceflowCondition condition = new SequenceflowCondition();
        condition.setExpression(expr);
        condition.setLanguage(lang);
        transition.setCondition(condition);

      } else {
        parse.addProblem("Type of the conditionExpression on sequenceFlow with id=" + 
                transitionId + " is of onsupported type 'bpmn:tExpression'", transitionElement);
      }
    }
  }
  
  public void parseDefinition(Element documentElement, Parse parse) {
    parseImports(documentElement, parse);
  }

  public TaskDefinitionImpl parseTaskDefinition(Element element, Parse parse, ScopeElementImpl scopeElement) {
    TaskDefinitionImpl taskDefinition = new TaskDefinitionImpl();

    String taskName = XmlUtil.attribute(element, "name");
    taskDefinition.setName(taskName);

    BpmnProcessDefinition processDefinition = parse.contextStackFind(BpmnProcessDefinition.class);
    if (processDefinition.getTaskDefinition(taskName) != null) {
      parse.addProblem("duplicate task name " + taskName, element);
    } else {
      processDefinition.addTaskDefinitionImpl(taskDefinition);
    }

    return taskDefinition;
  }
  
  /**
   * Parses a <timerEventDefinition> element:
   *    * sets dueDate if 'timeDate' is used
   *    * sets duedateDescription if a duration expression is used
   *    * set cronExpression if a cron expression is used
   * 
   * @param timerEventDefinitionElement The XML element that defines the timer definition
   * @param activity The activity on which the timer definition must be created
   * @param eventId The id of the event on which the timer is defined
   */
  public TimerDefinitionImpl parseTimerEventDefinition(Element timerEventDefinitionElement, Parse parse, String eventId) {
    
    Element timeDate = XmlUtil.element(timerEventDefinitionElement, "timeDate");
    Element timeCycle = XmlUtil.element(timerEventDefinitionElement, "timeCycle");
    
    if ( (timeDate != null && timeCycle != null)
            || (timeDate == null && timeCycle == null) ) {
      parse.addProblem("timerEventDefinition for event '" + eventId +
              "' requires either a timeDate or a timeCycle definition (but not both)");
      return null;
    }
    
    TimerDefinitionImpl timerDefinition = new TimerDefinitionImpl();
    
    if (timeDate != null) {
      parseTimeDate(eventId, parse, timeDate, timerDefinition);
    }
    
    if (timeCycle != null) {
      parseTimeCycle(eventId, parse, timeCycle, timerDefinition);
    } 
    
    return timerDefinition;
  }

  protected void parseTimeDate(String catchEventId, Parse parse, Element timeDate, TimerDefinitionImpl timerDefinition) {
    String dueDateTime = timeDate.getTextContent();
    String dueDateTimeFormatText = (String) EnvironmentImpl.getFromCurrent("jbpm.duedatetime.format", false);
    if (dueDateTimeFormatText==null) {
      dueDateTimeFormatText = "dd/MM/yyyy HH:mm:ss";
    }
    SimpleDateFormat dateFormat = new SimpleDateFormat(dueDateTimeFormatText);
    try {
      Date duedatetimeDate = dateFormat.parse(dueDateTime);
      timerDefinition.setDueDate(duedatetimeDate);
    } catch (ParseException e) {
      parse.addProblem("couldn't parse timeDate '"+ dueDateTime 
              + "' on intermediate catch timer event " + catchEventId, e);
    }
  }
  
  protected void parseTimeCycle(String catchEventId, Parse parse, Element timeCycle, TimerDefinitionImpl timerDefinition) {
    String cycleExpression = timeCycle.getTextContent();
    if (Duration.isValidExpression(cycleExpression)) {
      timerDefinition.setDueDateDescription(cycleExpression);
    } else if (CronExpression.isValidExpression(cycleExpression)) {
      timerDefinition.setCronExpression(cycleExpression);
    } else {
      parse.addProblem("couldn't parse timeDate duration '"+ cycleExpression 
              + "' on intermediate catch timer event " + catchEventId);
    }
  }

  public void parseResources(Element documentElement, Parse parse, BpmnProcessDefinition processDefinition) {

    for (Element resourceElement : XmlUtil.elements(documentElement, "resource")) {

      Resource resource = new Resource();

      resource.setId(XmlUtil.attribute(resourceElement, "id"));
      resource.setName(XmlUtil.attribute(resourceElement, "name"));

      for (Element resourceParameterElement : XmlUtil.elements(documentElement, "resourceParameter")) {

        ResourceParameter resourceParameter = new ResourceParameter();
        resourceParameter.setId(XmlUtil.attribute(resourceParameterElement, "id"));
        resourceParameter.setName(XmlUtil.attribute(resourceParameterElement, "name"));
        resourceParameter.setType(QName.valueOf(XmlUtil.attribute(resourceParameterElement, "name")));

        resource.getParameters().put(XmlUtil.attribute(resourceParameterElement, "name"), resourceParameter);
      }

      processDefinition.getResources().put(resource.getName(), resource);
    }

  }

  public void parseInterfaces(Element documentElement, Parse parse, BpmnProcessDefinition processDefinition) {
    
    for (Element interfaceElement : XmlUtil.elements(documentElement, "interface")) {
      for (Element operationElement : XmlUtil.elements(interfaceElement, "operation")) {
        processDefinition.getOperations().put(XmlUtil.attribute(operationElement, "id"), operationElement);
      }
      processDefinition.getInterfaces().put(XmlUtil.attribute(interfaceElement, "id"), interfaceElement);
    }
  }

  public void parseMessages(Element documentElement, Parse parse, BpmnProcessDefinition processDefinition) {

    for (Element messageElement : XmlUtil.elements(documentElement, "message")) {
      processDefinition.getMessages().put(XmlUtil.attribute(messageElement, "id"), messageElement);
    }
  }

  public void parseItemDefinitions(Element documentElement, Parse parse, BpmnProcessDefinition processDefinition) {

    for (Element itemDefinitionElement : XmlUtil.elements(documentElement, "itemDefinition")) {
      processDefinition.getItemDefinitions().put(XmlUtil.attribute(itemDefinitionElement, "id"), itemDefinitionElement);
    }

  }

  public void parseImports(Element documentElement, Parse parse) {

  }
  
}

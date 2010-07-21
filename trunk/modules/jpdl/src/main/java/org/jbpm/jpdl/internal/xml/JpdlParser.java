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
package org.jbpm.jpdl.internal.xml;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.w3c.dom.Element;

import org.jbpm.api.JbpmException;
import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.api.listener.EventListener;
import org.jbpm.api.model.Event;
import org.jbpm.internal.log.Log;
import org.jbpm.jpdl.internal.activity.JpdlBinding;
import org.jbpm.jpdl.internal.activity.MailListener;
import org.jbpm.jpdl.internal.model.JpdlProcessDefinition;
import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.email.impl.MailProducerImpl;
import org.jbpm.pvm.internal.email.impl.MailTemplate;
import org.jbpm.pvm.internal.email.impl.MailTemplateRegistry;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.model.ActivityCoordinatesImpl;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.CompositeElementImpl;
import org.jbpm.pvm.internal.model.Continuation;
import org.jbpm.pvm.internal.model.EventImpl;
import org.jbpm.pvm.internal.model.EventListenerReference;
import org.jbpm.pvm.internal.model.ObservableElementImpl;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;
import org.jbpm.pvm.internal.model.ScopeElementImpl;
import org.jbpm.pvm.internal.model.TimerDefinitionImpl;
import org.jbpm.pvm.internal.model.TransitionImpl;
import org.jbpm.pvm.internal.model.VariableDefinitionImpl;
import org.jbpm.pvm.internal.repository.DeploymentImpl;
import org.jbpm.pvm.internal.task.AssignableDefinitionImpl;
import org.jbpm.pvm.internal.task.SwimlaneDefinitionImpl;
import org.jbpm.pvm.internal.task.TaskDefinitionImpl;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.binding.MailTemplateBinding;
import org.jbpm.pvm.internal.wire.binding.ObjectBinding;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.ProvidedObjectDescriptor;
import org.jbpm.pvm.internal.wire.usercode.UserCodeReference;
import org.jbpm.pvm.internal.wire.xml.WireParser;
import org.jbpm.pvm.internal.xml.Bindings;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;
import org.jbpm.pvm.internal.xml.ProblemImpl;

/**
 * @author Tom Baeyens
 */
public class JpdlParser extends Parser {

  private static final Log log = Log.getLog(JpdlParser.class.getName());

  public static final String NAMESPACE_JPDL_40 = "http://jbpm.org/4.0/jpdl";
  public static final String NAMESPACE_JPDL_42 = "http://jbpm.org/4.2/jpdl";
  public static final String NAMESPACE_JPDL_43 = "http://jbpm.org/4.3/jpdl";
  public static final String NAMESPACE_JPDL_44 = "http://jbpm.org/4.4/jpdl";

  public static final String CURRENT_VERSION_JBPM = "4.4";
  public static final String CURRENT_VERSION_NAMESPACE =
    "http://jbpm.org/" + CURRENT_VERSION_JBPM + "/jpdl";
  public static final String CURRENT_VERSION_PROCESS_LANGUAGE_ID =
    "jpdl-" + CURRENT_VERSION_JBPM;

  private static final String[] SCHEMA_RESOURCES =
    { "jpdl-4.0.xsd", "jpdl-4.2.xsd", "jpdl-4.3.xsd", "jpdl-4.4.xsd" };

  // array elements are mutable, even when final
  // never make a static array public
  private static final String[] DEFAULT_BINDING_RESOURCES =
    { "jbpm.jpdl.bindings.xml", "jbpm.user.bindings.xml" };

  private static JpdlBindingsParser jpdlBindingsParser = new JpdlBindingsParser();

  public static final String CATEGORY_ACTIVITY = "activity";
  public static final String CATEGORY_EVENT_LISTENER = "eventlistener";

  public JpdlParser() {
    parseBindings();
    setSchemaResources(SCHEMA_RESOURCES);
  }

  protected void parseBindings() {
    this.bindings = new Bindings();

    for (String activityResource : DEFAULT_BINDING_RESOURCES) {
      Enumeration<URL> resourceUrls = getResources(activityResource);
      if (resourceUrls.hasMoreElements()) {
        while (resourceUrls.hasMoreElements()) {
          URL resourceUrl = resourceUrls.nextElement();
          log.trace("loading jpdl bindings from resource: " + resourceUrl);
          jpdlBindingsParser.createParse()
            .setUrl(resourceUrl)
            .contextMapPut(Parse.CONTEXT_KEY_BINDINGS, bindings)
            .execute()
            .checkErrors("jpdl bindings from " + resourceUrl.toString());
        }
      }
      else {
        log.trace("skipping unavailable jpdl activities resource: " + activityResource);
      }
    }
  }

  protected Enumeration<URL> getResources(String resourceName) {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Enumeration<URL> resourceUrls;
    try {
      resourceUrls = classLoader.getResources(resourceName);

      if (!resourceUrls.hasMoreElements()) {
        resourceUrls = JpdlParser.class.getClassLoader().getResources(resourceName);
      }
    }
    catch (Exception e) {
      throw new JbpmException("couldn't get resource urls for " + resourceName, e);
    }
    return resourceUrls;
  }

  public Object parseDocumentElement(Element documentElement, Parse parse) {
    JpdlProcessDefinition processDefinition = instantiateNewJpdlProcessDefinition();
    parse.contextStackPush(processDefinition);

    List<ProcessDefinitionImpl> processDefinitions = new ArrayList<ProcessDefinitionImpl>();
    processDefinitions.add(processDefinition);
    try {
      // process attribues
      String name = XmlUtil.attribute(documentElement, "name", parse);
      processDefinition.setName(name);

      // make the process language version available for bindings
      // to allow for specific parsing behaviour per version

      // first check if the langid is available as a deployment property
      DeploymentImpl deployment =
        (DeploymentImpl) parse.contextMapGet(Parse.CONTEXT_KEY_DEPLOYMENT);
      if (deployment != null) {
        String processLanguageId = deployment.getProcessLanguageId(name);
        if (processLanguageId == null) {
          // if it is not available as a deployment property, check if the
          // jpdlparser attribute specifies a specific jpdl version.
          // this is the case for certain compatibility tests in our test suite
          String jpdlParser = XmlUtil.attribute(documentElement, "jpdlparser");
          if (jpdlParser != null) {
            processLanguageId = "jpdl-" + jpdlParser;
          }
          else {
            // if none of the above, check if this is a parser test run for a specific verion
            // specify the jpdltestversion with "mvn -Djpdlparser=jpdl-4.4 clean install"
            // that way, the whole test suite will be use the specified parser
            jpdlParser = System.getProperty("jpdlparser");
            if (jpdlParser != null) {
              processLanguageId = "jpdl-" + jpdlParser;
            }
            else {
              // if this process has a namespace, then use the namespace
              // to see what jpdl parser version should be used
              String namespaceUri = documentElement.getNamespaceURI();
              if (namespaceUri != null) {
                processLanguageId = "jpdl-" + namespaceUri.substring(16, 19);
              }
              else {
                // if none of the above, just deploy it as the current library version
                processLanguageId = CURRENT_VERSION_PROCESS_LANGUAGE_ID;
              }
            }
          }
          // saving the process language will make sure that
          // the right parser version is used after an upgrade of jbpm
          // as the old format xml will still be in the db
          deployment.setProcessLanguageId(name, processLanguageId);
        }
        parse.contextMapPut(Parse.CONTEXT_KEY_PROCESS_LANGUAGE_ID, processLanguageId);
      }

      String packageName = XmlUtil.attribute(documentElement, "package");
      processDefinition.setPackageName(packageName);

      Integer version = XmlUtil.attributeInteger(documentElement, "version", parse);
      if (version != null) {
        processDefinition.setVersion(version);
      }

      String key = XmlUtil.attribute(documentElement, "key");
      if (key != null) {
        processDefinition.setKey(key);
      }

      Element descriptionElement = XmlUtil.element(documentElement, "description");
      if (descriptionElement != null) {
        String description = XmlUtil.getContentText(descriptionElement);
        processDefinition.setDescription(description);
      }

      UnresolvedTransitions unresolvedTransitions = new UnresolvedTransitions();
      parse.contextStackPush(unresolvedTransitions);

      // swimlanes
      List<Element> swimlaneElements = XmlUtil.elements(documentElement, "swimlane");
      for (Element swimlaneElement : swimlaneElements) {
        String swimlaneName = XmlUtil.attribute(swimlaneElement, "name", parse);
        if (swimlaneName != null) {
          SwimlaneDefinitionImpl swimlaneDefinition =
            processDefinition.createSwimlaneDefinition(swimlaneName);
          parseAssignmentAttributes(swimlaneElement, swimlaneDefinition, parse);
        }
      }

      // variable declarations
      parseVariableDefinitions(documentElement, parse, processDefinition);

      // on events
      parseOnEvents(documentElement, parse, processDefinition);

      // activities
      parseActivities(documentElement, parse, processDefinition);

      // bind activities to their destinations
      resolveTransitionDestinations(parse, processDefinition, unresolvedTransitions);

      // process migration information
      Element migrationElement = XmlUtil.element(documentElement, "migrate-instances");
      if (migrationElement != null) {
        MigrationHelper.parseMigrationDescriptor(migrationElement, parse, processDefinition);
      }
    }
    finally {
      parse.contextStackPop();
    }

    if (processDefinition.getInitial() == null) {
      parse.addProblem("no start activity in process", documentElement);
    }

    return processDefinitions;
  }

  protected JpdlProcessDefinition instantiateNewJpdlProcessDefinition() {
    return new JpdlProcessDefinition();
  }

  protected void resolveTransitionDestinations(Parse parse,
    JpdlProcessDefinition processDefinition, UnresolvedTransitions unresolvedTransitions) {
    for (UnresolvedTransition unresolvedTransition : unresolvedTransitions.list) {
      unresolvedTransition.resolve(processDefinition, parse);
    }
  }

  public void parseActivities(Element documentElement, Parse parse,
    CompositeElementImpl compositeElement) {
    List<Element> elements = XmlUtil.elements(documentElement);
    for (Element nestedElement : elements) {
      String tagName = nestedElement.getLocalName();
      if ("on".equals(tagName) || "timer".equals(tagName) || "swimlane".equals(tagName)
        || "migrate-instances".equals(tagName) || "description".equals(tagName))
        continue;

      JpdlBinding activityBinding = (JpdlBinding) getBinding(nestedElement, CATEGORY_ACTIVITY);
      if (activityBinding == null) {
        log.debug("unrecognized activity: " + tagName);
        continue;
      }

      ActivityImpl activity = compositeElement.createActivity();
      parse.contextStackPush(activity);
      try {
        activity.setType(activityBinding.getTagName());
        activityBinding.parseName(nestedElement, activity, parse);
        parseTransitions(nestedElement, activity, parse);
        parseVariableDefinitions(nestedElement, parse, activity);

        Element descriptionElement = XmlUtil.element(nestedElement, "description");
        if (descriptionElement != null) {
          String description = XmlUtil.getContentText(descriptionElement);
          activity.setDescription(description);
        }

        String continuationText = XmlUtil.attribute(nestedElement, "continue");
        if (continuationText != null) {
          if ("async".equals(continuationText)) {
            activity.setContinuation(Continuation.ASYNCHRONOUS);
          }
          else if ("exclusive".equals(continuationText)) {
            activity.setContinuation(Continuation.EXCLUSIVE);
          }
        }

        Object parseResult = activityBinding.parse(nestedElement, parse, this);
        if (parseResult instanceof ActivityBehaviour) {
          ActivityBehaviour activityBehaviour = (ActivityBehaviour) parseResult;
          activity.setActivityBehaviour(activityBehaviour);
        }
        else {
          Descriptor activityBehaviourDescriptor = (Descriptor) parseResult;
          activity.setActivityBehaviourDescriptor(activityBehaviourDescriptor);
        }

        parseOnEvents(nestedElement, parse, activity);

        String g = XmlUtil.attribute(nestedElement, "g");
        if (g == null)
          continue;

        StringTokenizer stringTokenizer = new StringTokenizer(g, ",");
        ActivityCoordinatesImpl coordinates = null;
        if (stringTokenizer.countTokens() == 4) {
          try {
            int x = Integer.parseInt(stringTokenizer.nextToken());
            int y = Integer.parseInt(stringTokenizer.nextToken());
            int width = Integer.parseInt(stringTokenizer.nextToken());
            int height = Integer.parseInt(stringTokenizer.nextToken());
            coordinates = new ActivityCoordinatesImpl(x, y, width, height);
          }
          catch (NumberFormatException e) {
            coordinates = null;
          }
        }
        if (coordinates != null) {
          activity.setCoordinates(coordinates);
        }
        else {
          parse.addProblem("invalid coordinates g=\"" + g + "\" in " + activity, nestedElement);
        }
      }
      finally {
        parse.contextStackPop();
      }
    }
  }

  public TimerDefinitionImpl parseTimerDefinition(Element timerElement, Parse parse,
    ScopeElementImpl scopeElement) {
    TimerDefinitionImpl timerDefinition = scopeElement.createTimerDefinition();

    String duedate = XmlUtil.attribute(timerElement, "duedate");
    String duedatetime = XmlUtil.attribute(timerElement, "duedatetime");

    if (duedate != null) {
      timerDefinition.setDueDateDescription(duedate);
    }
    else if (duedatetime != null) {
      String dueDateTimeFormatText =
        (String) EnvironmentImpl.getFromCurrent("jbpm.duedatetime.format", false);
      if (dueDateTimeFormatText == null) {
        dueDateTimeFormatText = "HH:mm dd/MM/yyyy";
      }
      SimpleDateFormat dateFormat = new SimpleDateFormat(dueDateTimeFormatText);
      try {
        Date duedatetimeDate = dateFormat.parse(duedatetime);
        timerDefinition.setDueDate(duedatetimeDate);
      }
      catch (ParseException e) {
        parse.addProblem("couldn't parse duedatetime " + duedatetime, e);
      }
    }
    else {
      parse.addProblem("either duedate or duedatetime is required in timer", timerElement);
    }

    String repeat = XmlUtil.attribute(timerElement, "repeat");
    timerDefinition.setRepeat(repeat);

    return timerDefinition;
  }

  public void parseOnEvents(Element element, Parse parse, ScopeElementImpl scopeElement) {
    // event listeners
    List<Element> onElements = XmlUtil.elements(element, "on");
    for (Element onElement : onElements) {
      String eventName = XmlUtil.attribute(onElement, "event", parse);
      parseOnEvent(onElement, parse, scopeElement, eventName);

      Element timerElement = XmlUtil.element(onElement, "timer");
      if (timerElement != null) {
        TimerDefinitionImpl timerDefinitionImpl =
          parseTimerDefinition(timerElement, parse, scopeElement);
        timerDefinitionImpl.setEventName(eventName);
      }
    }
  }

  public void parseOnEvent(Element element, Parse parse,
    ObservableElementImpl observableElement, String eventName) {
    if (eventName != null) {
      EventImpl event = observableElement.getEvent(eventName);
      if (event == null) {
        event = observableElement.createEvent(eventName);
      }

      String continuationText = XmlUtil.attribute(element, "continue");
      if (continuationText != null) {
        if ("async".equals(continuationText)) {
          event.setContinuation(Continuation.ASYNCHRONOUS);
        }
        else if ("exclusive".equals(continuationText)) {
          event.setContinuation(Continuation.EXCLUSIVE);
        }
      }

      for (Element eventListenerElement : XmlUtil.elements(element)) {
        JpdlBinding eventBinding =
          (JpdlBinding) getBinding(eventListenerElement, CATEGORY_EVENT_LISTENER);
        if (eventBinding != null) {
          EventListenerReference eventListenerReference = null;
          Object parseResult = eventBinding.parse(eventListenerElement, parse, this);
          if (parseResult instanceof EventListener) {
            EventListener eventListener = (EventListener) parseResult;
            eventListenerReference = event.createEventListenerReference(eventListener);
          }
          else {
            Descriptor eventListenerDescriptor = (Descriptor) parseResult;
            eventListenerReference =
              event.createEventListenerReference(eventListenerDescriptor);
          }

          Boolean propagationEnabled =
            XmlUtil.attributeBoolean(eventListenerElement, "propagation", parse);
          if (propagationEnabled != null) {
            eventListenerReference.setPropagationEnabled(propagationEnabled);
          }

          continuationText = XmlUtil.attribute(eventListenerElement, "continue");
          if (continuationText != null) {
            if (observableElement instanceof ActivityImpl) {
              if (observableElement.getName() == null) {
                parse.addProblem("async continuation on event listener requires activity name", eventListenerElement);
              }
            }
            else if (observableElement instanceof TransitionImpl) {
              TransitionImpl transition = (TransitionImpl) observableElement;
              if (transition.getSource().getName() == null) {
                parse.addProblem("async continuation on event listener requires name in the transition source activity", eventListenerElement);
              }
            }
            if ("async".equals(continuationText)) {
              eventListenerReference.setContinuation(Continuation.ASYNCHRONOUS);
            }
            else if ("exclusive".equals(continuationText)) {
              eventListenerReference.setContinuation(Continuation.EXCLUSIVE);
            }
          }
        }
        else {
          String tagName = eventListenerElement.getLocalName();
          if (!(observableElement instanceof TransitionImpl && ("condition".equals(tagName) || "timer".equals(tagName)))) {
            parse.addProblem("unrecognized event listener: " + tagName, null, ProblemImpl.TYPE_WARNING, eventListenerElement);
          }
        }
      }
    }
  }

  public void parseTransitions(Element element, ActivityImpl activity, Parse parse) {
    UnresolvedTransitions unresolvedTransitions =
      parse.contextStackFind(UnresolvedTransitions.class);

    List<Element> transitionElements = XmlUtil.elements(element, "transition");
    for (Element transitionElement : transitionElements) {
      String transitionName = XmlUtil.attribute(transitionElement, "name");

      Element timerElement = XmlUtil.element(transitionElement, "timer");
      if (timerElement != null) {
        TimerDefinitionImpl timerDefinitionImpl =
          parseTimerDefinition(timerElement, parse, activity);
        timerDefinitionImpl.setSignalName(transitionName);
      }

      TransitionImpl transition = activity.createOutgoingTransition();
      transition.setName(transitionName);

      unresolvedTransitions.add(transition, transitionElement);
      parseOnEvent(transitionElement, parse, transition, Event.TAKE);
    }
  }

  public void parseAssignmentAttributes(Element element,
    AssignableDefinitionImpl assignableDefinition, Parse parse) {
    Element descriptionElement = XmlUtil.element(element, "description");
    if (descriptionElement != null) {
      String descriptionText = XmlUtil.getContentText(descriptionElement);
      Expression descriptionExpression =
        Expression.create(descriptionText, Expression.LANGUAGE_UEL_VALUE);
      assignableDefinition.setDescription(descriptionExpression);
    }

    Element assignmentHandlerElement = XmlUtil.element(element, "assignment-handler");
    if (assignmentHandlerElement != null) {
      UserCodeReference assignmentHandlerReference =
        parseUserCodeReference(assignmentHandlerElement, parse);
      assignableDefinition.setAssignmentHandlerReference(assignmentHandlerReference);
    }

    String assigneeExpressionText = XmlUtil.attribute(element, "assignee");
    if (assigneeExpressionText != null) {
      String assigneeExpressionLanguage = XmlUtil.attribute(element, "assignee-lang");
      Expression assigneeExpression =
        Expression.create(assigneeExpressionText, assigneeExpressionLanguage);
      assignableDefinition.setAssigneeExpression(assigneeExpression);
    }

    String candidateUsersExpressionText = XmlUtil.attribute(element, "candidate-users");
    if (candidateUsersExpressionText != null) {
      String candidateUsersExpressionLanguage =
        XmlUtil.attribute(element, "candidate-users-lang");
      Expression candidateUsersExpression =
        Expression.create(candidateUsersExpressionText, candidateUsersExpressionLanguage);
      assignableDefinition.setCandidateUsersExpression(candidateUsersExpression);
    }

    String candidateGroupsExpressionText = XmlUtil.attribute(element, "candidate-groups");
    if (candidateGroupsExpressionText != null) {
      String candidateGroupsExpressionLanguage =
        XmlUtil.attribute(element, "candidate-groups-lang");
      Expression candidateGroupsExpression =
        Expression.create(candidateGroupsExpressionText, candidateGroupsExpressionLanguage);
      assignableDefinition.setCandidateGroupsExpression(candidateGroupsExpression);
    }
  }

  public TaskDefinitionImpl parseTaskDefinition(Element element, Parse parse,
    ScopeElementImpl scopeElement) {
    TaskDefinitionImpl taskDefinition = new TaskDefinitionImpl();

    String taskName = XmlUtil.attribute(element, "name");
    taskDefinition.setName(taskName);

    String form = XmlUtil.attribute(element, "form");
    taskDefinition.setFormResourceName(form);

    String duedate = XmlUtil.attribute(element, "duedate");
    taskDefinition.setDueDateDescription(duedate);

    Integer priority = XmlUtil.attributeInteger(element, "priority", parse);
    if (priority != null) {
      taskDefinition.setPriority(priority);
    }

    ProcessDefinitionImpl processDefinition =
      parse.contextStackFind(ProcessDefinitionImpl.class);
    if (processDefinition.getTaskDefinition(taskName) != null) {
      parse.addProblem("duplicate task name " + taskName, element);
    }
    else {
      processDefinition.addTaskDefinitionImpl(taskDefinition);
    }

    String swimlaneName = XmlUtil.attribute(element, "swimlane");
    if (swimlaneName != null) {
      JpdlProcessDefinition jpdlProcessDefinition =
        parse.contextStackFind(JpdlProcessDefinition.class);
      SwimlaneDefinitionImpl swimlaneDefinition =
        jpdlProcessDefinition.getSwimlaneDefinition(swimlaneName);
      if (swimlaneDefinition != null) {
        taskDefinition.setSwimlaneDefinition(swimlaneDefinition);
      }
      else {
        parse.addProblem("swimlane " + swimlaneName + " not declared", element);
      }
    }

    parseAssignmentAttributes(element, taskDefinition, parse);

    // parse notification mail producer
    Element notificationElement = XmlUtil.element(element, "notification");
    if (notificationElement != null) {
      parseMailEvent(notificationElement, parse, scopeElement, Event.ASSIGN);
    }

    Element reminderElement = XmlUtil.element(element, "reminder");
    if (reminderElement != null) {
      parseMailEvent(reminderElement, parse, scopeElement, Event.REMIND);
      // associate timer to event
      TimerDefinitionImpl timerDefinition =
        parseTimerDefinition(reminderElement, parse, scopeElement);
      timerDefinition.setEventName(Event.REMIND);
    }

    return taskDefinition;
  }

  public void parseVariableDefinitions(Element element, Parse parse,
    ScopeElementImpl scopeElement) {
    List<VariableDefinitionImpl> variableDefinitions = new ArrayList<VariableDefinitionImpl>();

    for (Element variableElement : XmlUtil.elements(element, "variable")) {
      VariableDefinitionImpl variableDefinition = scopeElement.createVariableDefinition();

      String name = XmlUtil.attribute(variableElement, "name", parse);
      variableDefinition.setName(name);

      String type = XmlUtil.attribute(variableElement, "type", parse);
      variableDefinition.setTypeName(type);

      Boolean isHistoryEnabled = XmlUtil.attributeBoolean(variableElement, "history", parse);
      if (isHistoryEnabled != null) {
        variableDefinition.setHistoryEnabled(isHistoryEnabled);
      }

      int sources = 0;

      String initExpr = XmlUtil.attribute(variableElement, "init-expr");
      String initExprType = XmlUtil.attribute(variableElement, "init-expr-type");
      if (initExpr != null) {
        Expression initExpression = Expression.create(initExpr, initExprType);
        variableDefinition.setInitExpression(initExpression);
        sources++;
      }

      Element initDescriptorElement = XmlUtil.element(variableElement);
      if (initDescriptorElement != null) {
        Descriptor initValueDescriptor =
          (Descriptor) WireParser.getInstance().parseElement(initDescriptorElement, parse);
        variableDefinition.setInitDescriptor(initValueDescriptor);
        sources++;
      }

      if (sources > 1) {
        parse.addProblem("init attribute and init element are mutually exclusive on element variable", variableElement);
      }

      variableDefinitions.add(variableDefinition);
    }
  }

  public void parseMailEvent(Element element, Parse parse,
    ObservableElementImpl observableElement, String eventName) {
    // obtain assign event
    EventImpl event = observableElement.getEvent(eventName);
    if (event == null) {
      event = observableElement.createEvent(eventName);
    }
    // register event listener
    MailListener eventListener = new MailListener();
    EventListenerReference eventListenerRef = event.createEventListenerReference(eventListener);
    // set continuation mode
    String continuationText = XmlUtil.attribute(element, "continue");
    if ("async".equals(continuationText)) {
      eventListenerRef.setContinuation(Continuation.ASYNCHRONOUS);
    }
    else if ("exclusive".equals(continuationText)) {
      eventListenerRef.setContinuation(Continuation.EXCLUSIVE);
    }

    // https://jira.jboss.org/jira/browse/JBPM-2466
    String mailTemplateName = eventName;
    if (Event.ASSIGN.equals(eventName)) {
      mailTemplateName = "task-notification";
    }
    else if (Event.REMIND.equals(eventName)) {
      mailTemplateName = "task-reminder";
    }

    // associate mail producer to event listener
    UserCodeReference mailProducer = parseMailProducer(element, parse, mailTemplateName);
    eventListener.setMailProducerReference(mailProducer);
  }

  public UserCodeReference parseMailProducer(Element element, Parse parse,
    String defaultTemplateName) {
    // check whether the element is a generic object descriptor
    if (ObjectBinding.isObjectDescriptor(element)) {
      return parseUserCodeReference(element, parse);
    }

    // parse the default mail producer
    MailTemplate mailTemplate = parseMailTemplate(element, parse, defaultTemplateName);
    ObjectDescriptor descriptor = new ObjectDescriptor(MailProducerImpl.class);
    descriptor.addPropertyInjection("template", new ProvidedObjectDescriptor(mailTemplate));

    UserCodeReference userCodeReference = new UserCodeReference();
    userCodeReference.setDescriptor(descriptor);
    return userCodeReference;
  }

  private MailTemplate parseMailTemplate(Element element, Parse parse,
    String defaultTemplateName) {
    if (element.hasAttribute("template")) {
      // fetch template from configuration
      return findMailTemplate(element, parse, element.getAttribute("template"));
    }
    if (!XmlUtil.isTextOnly(element)) {
      // parse inline template
      return MailTemplateBinding.parseMailTemplate(element, parse);
    }
    if (defaultTemplateName != null) {
      // fetch default template
      return findMailTemplate(element, parse, defaultTemplateName);
    }
    parse.addProblem("mail template must be referenced in the 'template' attribute "
      + "or specified inline", element);
    return null;
  }

  private MailTemplate findMailTemplate(Element element, Parse parse, String templateName) {
    MailTemplateRegistry templateRegistry =
      EnvironmentImpl.getFromCurrent(MailTemplateRegistry.class);
    if (templateRegistry != null) {
      MailTemplate template = templateRegistry.getTemplate(templateName);
      if (template != null)
        return template;
    }
    parse.addProblem("mail template not found: " + templateName, element);
    return null;
  }

  public UserCodeReference parseUserCodeReference(Element element, Parse parse) {
    UserCodeReference userCodeReference = new UserCodeReference();

    ObjectDescriptor objectDescriptor = parseObjectDescriptor(element, parse);
    userCodeReference.setDescriptor(objectDescriptor);

    if (objectDescriptor.getExpression() != null) {
      // expressions are not cached by default
      userCodeReference.setCached(false);
    }

    Boolean isCached = XmlUtil.attributeBoolean(element, "cache", parse);
    if (isCached != null) {
      userCodeReference.setCached(isCached.booleanValue());
    }

    return userCodeReference;
  }

  public ObjectDescriptor parseObjectDescriptor(Element element, Parse parse) {
    return ObjectBinding.parseObjectDescriptor(element, parse, WireParser.getInstance());
  }

  public Descriptor parseDescriptor(Element element, Parse parse) {
    return (Descriptor) WireParser.getInstance().parseElement(element, parse);
  }

  public Set<String> getActivityTagNames() {
    return getBindings().getTagNames(CATEGORY_ACTIVITY);
  }
}

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
package org.jbpm.jpdl.internal.convert;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jbpm.jpdl.internal.convert.action.Action;
import org.jbpm.jpdl.internal.convert.action.ActionConverterTypes;
import org.jbpm.jpdl.internal.convert.action.CreateTimerAction;
import org.jbpm.jpdl.internal.convert.exception.ConvertException;
import org.jbpm.jpdl.internal.convert.node.Node;
import org.jbpm.jpdl.internal.convert.node.NodeConverterTypes;
import org.jbpm.jpdl.internal.convert.node.StartState;
import org.jbpm.jpdl.internal.convert.node.TaskNode;
import org.jbpm.jpdl.internal.convert.node.VariableAccess;
import org.jbpm.jpdl.internal.convert.problem.Problem;
import org.jbpm.jpdl.internal.convert.problem.ProblemListener;
import org.jbpm.jpdl.internal.xml.JpdlParser;
import org.jbpm.pvm.internal.util.CollectionUtil;

import org.xml.sax.InputSource;

/**
 * Trimmed down version JpdlXmlReader (jBPM3 code base), 
 * to have exactly the same parsing as in jBPM3.
 * 
 * Note that the foremost requirement for this class is readability and
 * extensibility. People who want to adapt the conversion logic 
 * for their own processes are advised to adapt this class.
 *
 * @author Jim Ma
 * @author jbarrez
 */
public class Jpdl3Converter implements ProblemListener {
  
  private static final long serialVersionUID = 1L;
  
  protected InputSource inputSource;
  
  protected Document jpdl3Document;
  
  /* Contains any problems occurred after conversion of the jpdl3 process */
  protected List<Problem> problems = new ArrayList<Problem>();
  
  /* 
   * Use the constructor with a custom ProblemListener to have the conversion
   * problems sent to another problem listener/
   */
  protected ProblemListener problemListener;
  
  //the return converted jpdl4 dom model
  protected Document jpdl4Document;
  
  /* Contains the transition destinations which arent resolved yet */
  protected Collection<Object[]> unresolvedTransitionDestinations;
  
  /* 
   * Contains the actions which arent resolved yet 
   * (ie the converter hasnt encountered them yet)
   */
  protected Collection<Object[]> unresolvedActionReferences;
  
  /* Contains the nodes of the process */
  protected Map<String,Element> nodeCollection = new java.util.concurrent.ConcurrentHashMap<String, Element>();
  
  /* Contains the encountered swimlanes */
  protected Map<String,Element> swimlanesCollection = new java.util.concurrent.ConcurrentHashMap<String, Element>();

  /* Used for autonumbering anonymous timers */
  private int timerNumber; 
  
  /**
   * Constructor using an URL pointing to a jpdl3 process file.
   */
  public Jpdl3Converter(URL url)  {
     try {
		File file = new File(url.toURI());
		FileReader reader = new FileReader(file);
		this.inputSource = new InputSource(reader);
	} catch (Exception e) {
		//Do nothing , we validated it before
	}
  }
  
  /**
   * Constructor using an InputSource pointing to a jpdl3 process file.
   */
  public Jpdl3Converter(InputSource inputSource)  {
    this.inputSource = inputSource;
  }

  /**
   * Constructor using a Reader pointing to a jpdl3 process file.
   */
  public Jpdl3Converter(Reader reader)  {
    this(new InputSource(reader));
  }

  public void close() throws IOException {
    InputStream byteStream = inputSource.getByteStream();
    if (byteStream != null) {
      byteStream.close();
    }
    else {
      Reader charStream = inputSource.getCharacterStream();
      if (charStream != null) {
        charStream.close();
      }
    }
    jpdl3Document = null;
  }

  

  public void addProblem(Problem problem) {
    problems.add(problem);
    if (problemListener != null) {
      problemListener.addProblem(problem);
    }
  }

  public void addError(String description) {
    log.error("invalid process xml: " + description);
    addProblem(new Problem(Problem.LEVEL_ERROR, description));
  }

  public void addError(String description, Throwable exception) {
    log.error("invalid process xml: " + description, exception);
    addProblem(new Problem(Problem.LEVEL_ERROR, description, exception));
  }

  public void addWarning(String description) {
    log.warn("process xml warning: " + description);
    addProblem(new Problem(Problem.LEVEL_WARNING, description));
  }

  /**
   * Call this method to convert the jpdl3 process.
   * The return value will be a converted jpdl4 XML model.
   */
  public Document readAndConvert() {
    
    // create a new definition
    jpdl4Document = DocumentHelper.createDocument();

    // initialize lists
    problems = new ArrayList<Problem>();
    unresolvedTransitionDestinations = new ArrayList<Object[]>();
    unresolvedActionReferences = new ArrayList<Object[]>();

    try {
      
      // parse the document into a XML dom tree
      jpdl3Document = Jpdl3ConverterParser.parse(inputSource, this);
      jpdl4Document.setXMLEncoding(jpdl3Document.getXMLEncoding());
      
      // Convert the root element 
      Element jpdl3Root = jpdl3Document.getRootElement();
      Element jpdl4Root = parseProcessDefinitionAttributes(jpdl3Root);

      // convert process description as comment
      String description = jpdl3Root.elementTextTrim("description");
      if (description != null) {
        jpdl4Root.addComment(description);
      }

      // first pass
      convertSwimlanes(jpdl3Root, jpdl4Root);
      convertActions(jpdl3Root, null, null); // Todo: refactor
      convertNodes(jpdl3Root, jpdl4Root);
      convertEvents(jpdl3Root, jpdl4Root);
      convertExceptionHandlers(jpdl3Root, jpdl4Root);
      convertTasks(jpdl3Root, jpdl4Root);
      
      // second pass processing: process any unresolved elements 
      resolveTransitionDestinations();
      //resolveActionReferences(); // not yet implemented
      verifySwimlaneAssignments();

    }
    catch (Exception e) {
      log.error("couldn't parse process definition", e);
      addProblem(new Problem(Problem.LEVEL_ERROR, "couldn't parse process definition", e));
    }
    
    // After conversion: check if there were any conversion problems
    
    if (Problem.containsProblemsOfLevel(problems, Problem.LEVEL_ERROR)) {
      throw new ConvertException(problems);
    }

    if (problems != null) {
      for (Problem problem : problems) {
        log.warn("process parse warning: " + problem.getDescription());
      }
    }

    return jpdl4Document;
  }

  protected Element parseProcessDefinitionAttributes(Element root)
  {
	Element element = jpdl4Document.addElement("process", JpdlParser.CURRENT_VERSION_NAMESPACE);
    element.addAttribute("name", root.attributeValue("name"));
    return element;
  }

  protected void convertSwimlanes(Element jpdl3Element, Element jpdl4Element) {
    
    Iterator<?> iter = jpdl3Element.elementIterator("swimlane");
    
    while (iter.hasNext()) {
      
      Element swimlaneElement = (Element)iter.next();
      String swimlaneName = swimlaneElement.attributeValue("name");
      
      if (swimlaneName == null) {
        addWarning("there's a swimlane without a name");
      }
      else {
        
    	Element swimlane4 = jpdl4Element.addElement("swimlane");
    	swimlane4.addAttribute("name", swimlaneName);
        Element assignmentElement = swimlaneElement.element("assignment");
        swimlanesCollection.put(swimlaneName, swimlane4);
        if (assignmentElement != null) {

          if ((assignmentElement.attribute("actor-id") != null))          {
        	swimlane4.addAttribute("assignee", assignmentElement.attributeValue("actor-id"));
          }
          if (assignmentElement.attribute("pooled-actors") != null) {
        	swimlane4.addAttribute("candidate-users" ,assignmentElement.attributeValue("pooled-actors"));
          }
          else  {
            //readAssignmentDelegation(assignmentElement, swimlane4);
            //swimlane.setAssignmentDelegation(assignmentDelegation);
          }
        }
      }
    }
  }
  
  public void convertActions(Element jpdl3Element, Element jpdl4Element, String eventType) {

      Iterator< ? > nodeElementIter = jpdl3Element.elementIterator();
      
      while (nodeElementIter.hasNext()) {
          Element actionElement = (Element) nodeElementIter.next();
          String actionName = actionElement.getName();

          if ("cancel-timer".equalsIgnoreCase(actionName) || "mail".equalsIgnoreCase(actionName)) {
              this.addWarning("Unsupported " + actionName + " conversion on Element : " + actionElement.asXML());
          }

          if (ActionConverterTypes.hasActionName(actionName)) {
            createAction(actionElement, jpdl4Element);
          }
      }
  }

  public void convertNodes(Element jpdl3Element, Element jpdl4Element) {
    
    Iterator<?> nodeElementIter = jpdl3Element.elementIterator();
    
    while (nodeElementIter.hasNext()) {
      
      Element nodeElement = (Element)nodeElementIter.next();
      String nodeName = nodeElement.getName();
      
      Class<?> nodeType = NodeConverterTypes.getNodeType(nodeName);
      if (nodeType != null) {

        Node node = null;
        try {
          node = (Node)nodeType.newInstance();
        }
        catch (Exception e)
        {
          log.error("couldn't instantiate node '" + nodeName + "', of type '" + nodeType.getName() + "'", e);
          continue;
        }
        
        //Implement it
        List<?> starts = jpdl4Element.elements("start-state");

        // check for duplicate start-states
        if ((node instanceof StartState) && !starts.isEmpty()) {
          addError("max one start-state allowed in a process");
        } else if (node instanceof TaskNode) {
        	convertTasks(nodeElement, jpdl4Element);
        } else {
          
          // read the common node parts of the element
          node.setNodeElement(nodeElement);
          Element actionElement = nodeElement.element("action");
          //The node without action element can not be converted, report error
          if (nodeName.equals("node") && actionElement == null) {
        	  addError("Could not convert a node without action element:" + nodeElement.asXML());
        	  return;
          } 
          
          node.createConvertedElement(jpdl4Element);
          convertNode(nodeElement,node.getConvertedElement());

          // if the has special configuration to parse, delegate to the specific node
          node.read(this);
          
        }
      }
    }
  }
  
  public void convertEvents(Element jpdl3Element, Element jpdl4Element) {
    
    Iterator<?> iter = jpdl3Element.elementIterator("event");
    
    while (iter.hasNext()) {
      
      Element eventElement = (Element)iter.next();
      //String eventType = eventElement.attributeValue("type");
      //TODO:how to handle the event type
      Element onElement = jpdl4Element.addElement("on");
      String type = eventElement.attributeValue("type");
      onElement.addAttribute("event", type);
      convertActions(eventElement, onElement, type);
    }
  }
  
  public void convertTasks(Element jpdl3Element, Element jpdl4Element) {
    
    List<?> elements = jpdl3Element.elements("task");
    Element[] tasks = elements.toArray(new Element[0]);
    
    if (tasks != null && tasks.length > 0) {
      
		for (int i = 0 ; i < tasks.length; i++) {
		  
			Element tmpTask = tasks[i];
	        Element task4 = convertTask(tmpTask, jpdl4Element);
	      
			if (i ==0 ) {
				task4.addAttribute("name", jpdl3Element.attributeValue("name"));
			} else{
				task4.addAttribute("name", tmpTask.attributeValue("name"));
			}
			
			// In jBPM4, tasks are not nested in a task node
			// this is solved by adding seperate task to the jpdl4 model
			// each containing a transition to the next one
			if (i+1 < tasks.length) {
				Element newTransistion = task4.addElement("transition");
				String to = tasks[i+1].attributeValue("name");
				newTransistion.addAttribute("name", to);
				newTransistion.addAttribute("to", to);
			} else {
				//The last task node has a transition to the node after the original TaskNode
				List<?> transitions = jpdl3Element.elements("transition");
				for (Element trans : CollectionUtil.checkList(transitions, Element.class)) {
					Element transElement = task4.addElement("transition");
					String transName = trans.attributeValue("name") == null ? trans.attributeValue("to") :trans.attributeValue("name"); 
					transElement.addAttribute("name", transName);
					transElement.addAttribute("to", trans.attributeValue("to"));					
				}
			}			
		}
	}    
  }

  public Element convertTask(Element taskElement, Element jpdlElement) {
    
	Element task4 = jpdlElement.addElement("task");
    String name = taskElement.attributeValue("name");
    if (name != null) {
      task4.attributeValue("name", name);
    }

    String description = taskElement.elementTextTrim("description");
    if (description != null) {
      task4.addComment(description);
    }
    
    String condition = taskElement.elementTextTrim("condition");
    if (condition == null) {
    	condition = taskElement.attributeValue("condition");
    } 
    if (condition == null) {
    	addWarning("Unsupported condition attribute converstion for task : " + taskElement.asXML());
    }
    
  
    //The converted the elements in task should be in this sequence 
    //assignment-handler, on, notification, reminder, timer
    
    // assignment
    String swimlaneName = taskElement.attributeValue("swimlane");
    Element assignmentElement = taskElement.element("assignment");

    // if there is a swimlane attribute specified
    if (swimlaneName != null) {
      
      Element swimLane = swimlanesCollection.get(swimlaneName);
      if (swimLane == null) {
        addWarning("task references unknown swimlane '" + swimlaneName + "':" + taskElement.asXML());
      }
      else {
        task4.addAttribute("swimlane", swimlaneName);
      }
    }
    else if (assignmentElement != null) {
      if ((assignmentElement.attribute("actor-id") != null) || (assignmentElement.attribute("pooled-actors") != null)) {
        
    	String actorid = assignmentElement.attributeValue("actor-id");
    	String pooledactors = assignmentElement.attributeValue("pooled-actors");
    	
        if (actorid != null)  {
        	task4.addAttribute("assignee", actorid);
        }
        if (pooledactors != null)  {
        	task4.addAttribute("candidate-groups", pooledactors);
        }

      }
      else
      {
        convertAssignmentDelegation(assignmentElement, task4);
      }
    }
    else
    {
      // the user has to manage assignment manually, so we better inform him/her.
      log.info("process xml information: no swimlane or assignment specified for task '" + taskElement.asXML() + "'");
    }
    
    //notification attribute
    String notificationsText = taskElement.attributeValue("notify");
    if (notificationsText != null && ("true".equalsIgnoreCase(notificationsText) 
            || "on".equalsIgnoreCase(notificationsText) || "yes".equalsIgnoreCase(notificationsText))) {
        //TODO:Review if there is "continue" attribute to converted
    	Element notify = task4.addElement("notification");
    	notify.addAttribute("continue", "sync");
    }
    //Reminder elements
    convertTaskReminders(taskElement, task4);

    //event elements  
    convertEvents(taskElement, task4);

    //timer elements
    convertTaskTimers(taskElement, task4);

    convertExceptionHandlers(taskElement, task4);

    String duedateText = taskElement.attributeValue("duedate");
    
    if (duedateText != null) {
    	addWarning("Unsupported duedateDate attribute converstion for task : " + taskElement.asXML());
    }
    
    String priorityText = taskElement.attributeValue("priority");
    if (priorityText != null) {
    	addWarning("Unsupported priorityText attribute converstion for task : " + taskElement.asXML());
    }

    String blockingText = taskElement.attributeValue("blocking");
    if (blockingText != null) {
    	addWarning("Unsupported blocking attribute converstion for task : " + taskElement.asXML());
    }

    String signallingText = taskElement.attributeValue("signalling");
    if (signallingText != null) {
    	addWarning("Unsupported signallingText attribute converstion for task : " + taskElement.asXML());
    }

    Element taskControllerElement = taskElement.element("controller");
    if (taskControllerElement != null) {
      addWarning("Unsupported controller converstion for task : " + taskElement.asXML());
    }
    
    return task4;
  }  
  
  protected void convertAssignmentDelegation(Element jpdl3AssignmentElement, Element jpdl4Task) {
  	  
      String expression = jpdl3AssignmentElement.attributeValue("expression");
      String actorId = jpdl3AssignmentElement.attributeValue("actor-id");
      String pooledActors = jpdl3AssignmentElement.attributeValue("pooled-actors");
  
      if (expression != null) {
        
        // TODO:How to convert default assignmenthandler?
        // assignmentDelegation.setClassName("org.jbpm.identity.assignment.ExpressionAssignmentHandler");
        // assignmentDelegation.setConfiguration("<expression>" + expression +
        // "</expression>");
        
      } else if ((actorId != null) || (pooledActors != null)) {
        
        jpdl4Task.addComment("Please Update the AssignmentHandler and implement org.jbpm.api.task.AssignmentHandler to create your own AssignmentHandler.");
        Element assignmentHandler = jpdl4Task.addElement("assignment-handler");
        assignmentHandler.addAttribute("class", "org.jbpm.taskmgmt.assignment.ActorAssignmentHandler");
  
        String configuration = "";
        if (actorId != null) {
          configuration += "<actorId>" + actorId + "</actorId>";
        }
        if (pooledActors != null) {
          configuration += "<pooledActors>" + pooledActors + "</pooledActors>";
        }
  
      } else {
        String claz = jpdl3AssignmentElement.attributeValue("class");
        Element assignmentHandler = jpdl4Task.addElement("assignment-handler");
        assignmentHandler.addAttribute("class", claz);
      }
  }

  public List<VariableAccess> convertVariableAccesses(Element element) {
    
    List<VariableAccess> variableAccesses = new ArrayList<VariableAccess>();
    Iterator<?> iter = element.elementIterator("variable");
    while (iter.hasNext()) {
      Element variableElement = (Element)iter.next();

      String variableName = variableElement.attributeValue("name");
      if (variableName == null)
      {
        addProblem(new Problem(Problem.LEVEL_WARNING, "the name attribute of a variable element is required: " + variableElement.asXML()));
      }
      String access = variableElement.attributeValue("access", "read,write");
      String mappedName = variableElement.attributeValue("mapped-name");

      variableAccesses.add(new VariableAccess(variableName, access, mappedName));
    }
    return variableAccesses;
  }
  
  public void convertNode(Element jpdl3Element, Element jpdl4Element) {
	  
    String name = jpdl3Element.attributeValue("name");
    if (name != null) {
    	jpdl4Element.addAttribute("name", name);
    	nodeCollection.put(name, jpdl4Element);
    }

    // get the node description
    String description = jpdl3Element.elementTextTrim("description");
    if (description != null) {
    	jpdl4Element.addComment(description);
    }
    
    String asyncText = jpdl3Element.attributeValue("async");
    if ("true".equalsIgnoreCase(asyncText)) {
    	jpdl4Element.addAttribute("continue", "async");
    }
    else if ("exclusive".equalsIgnoreCase(asyncText)) {
    	jpdl4Element.addAttribute("continue", "exclusive");
    }//else if -> uses the default continue="sync"

    // parse common subelements

    convertNodeTimers(jpdl3Element, jpdl4Element);
    convertEvents(jpdl3Element, jpdl4Element);
    convertExceptionHandlers(jpdl3Element, jpdl4Element);

    // save the transitions and parse them at the end
    addUnresolvedTransitionDestination(jpdl3Element, jpdl4Element);
  }

  protected void convertNodeTimers(Element nodeElement, Element jpdl4Element) {
    Iterator<?> iter = nodeElement.elementIterator("timer");
    while (iter.hasNext()) {
      Element timerElement = (Element)iter.next();
      convertNodeTimer(timerElement, jpdl4Element);
    }
  }

  protected void convertNodeTimer(Element timerElement,  Element jpdl4Element) {
	  String name = timerElement.attributeValue("name", timerElement.getName());
	    if (name == null)
	      name = generateTimerName();

	    CreateTimerAction createTimerAction = new CreateTimerAction();
	    Element onElement = jpdl4Element.addElement("on");
	    //TODO: Look at how to map the event type : start , end and take
	    onElement.addAttribute("event", "timeout");
	    createTimerAction.createConvertedElement(timerElement, onElement);
	    createTimerAction.read(timerElement, this);
  }

  private String generateTimerName() {
    return "timer-" + (timerNumber++);
  }
  
  protected void convertTaskTimers(Element taskElement, Element jdpl4Element) {
    Iterator<?> iter = taskElement.elementIterator();
    while (iter.hasNext()) {
      Element element = (Element)iter.next();
      if (("timer".equals(element.getName())))
      {
        convertTaskTimer(element, jdpl4Element);
      }
    }
  }
  
  
   protected void convertTaskReminders(Element taskElement, Element jdpl4Element) {
	    Iterator<?> iter = taskElement.elementIterator();
	    while (iter.hasNext()) {
	      Element element = (Element)iter.next();
	      if ("reminder".equals(element.getName()))
	      {
	        convertTaskTimer(element, jdpl4Element);
	      }
	    }
	  }
  
  
  
  

  protected void convertTaskTimer(Element timerElement, Element jpdl4Element)  {
    if ("timer".equals(timerElement.getName())) {
      
    	String name = timerElement.attributeValue("name", timerElement.getName());
        if (name == null)
          name = generateTimerName();
;
        Element timer = jpdl4Element.addElement("timer");
        
        String dueDate = timerElement.attributeValue("duedate");
        if (dueDate==null) {
          addWarning("no duedate specified in create timer action '"+ timerElement.asXML()+"'");
        } else {
        	timer.addAttribute("duedate", dueDate);
        }
        String repeat = timerElement.attributeValue("repeat");
        if ( "true".equalsIgnoreCase(repeat)
             || "yes".equalsIgnoreCase(repeat) ) {
          repeat = dueDate;
        }
        
        timer.addAttribute("repeat", repeat);
        
        String transitionName = timerElement.attributeValue("transition");
        
        if ( (transitionName!=null)
             && (repeat!=null) 
           ) {
          repeat = null;
          addProblem(new Problem(Problem.LEVEL_WARNING, "ignoring repeat on timer with transition "+ timerElement.asXML()));
        }
        this.convertSingleAction(timerElement, timer);           
    }
    else
    {
      Element reminder = jpdl4Element.addElement("reminder");
      String dueDate = timerElement.attributeValue("duedate");
      if (dueDate==null) {
        addWarning("no duedate specified in reminder element '"+ timerElement.asXML()+"'");
      } else {
    	reminder.addAttribute("duedate", dueDate);
      }
      String repeat = timerElement.attributeValue("repeat");
      if ( "true".equalsIgnoreCase(repeat)
           || "yes".equalsIgnoreCase(repeat) ) {
        repeat = dueDate;
      }
      
      reminder.addAttribute("repeat", repeat);
      //TODO: review it if there is no "continue" attribute to converted
      
    }
  }
  
  public Element convertSingleAction(Element jpdl3NodeElement, Element jpdl4Element) {
    
	    Element jpdl4Action = null;
	    Iterator<?> iter = jpdl3NodeElement.elementIterator();
	    while (iter.hasNext()) {
	      
	      Element candidate = (Element)iter.next();
	      if (ActionConverterTypes.hasActionName(candidate.getName())) {
	        // parse the action and assign it to this node
	    	  jpdl4Action = createAction(candidate, jpdl4Element);
	      }
	    }
	    return jpdl4Action;
	  
  }

  public Element createAction(Element actionElement, Element jpdl4Element) {
    
	    // create a new instance of the action
	    Action action = null;
	    String actionName = actionElement.getName();	    
	    Class<? extends Action> actionType = ActionConverterTypes.getActionType(actionName);
	    try
	    {
	      action = actionType.newInstance();
	    }
	    catch (Exception e)
	    {
	      log.error("couldn't instantiate action '" + actionName + "', of type '" + actionType.getName() + "'", e);
	    }

	    Element action4 = action.createConvertedElement(actionElement, jpdl4Element);
	    action.read(actionElement, this);
	    return action4;
  }


  protected void convertExceptionHandlers(Element jpdl3Element, Element jpdl4Element) {
	Iterator<?> iter = jpdl3Element.elementIterator("exception-handler");
    if (iter.hasNext()) {
			addWarning("Unsupported exception handler conversion for element : <"
					+ jpdl3Element.getName()
					+ " name=\""
					+ jpdl3Element.attributeValue("name") + "\"/>");
		}
  }

  // transition destinations are parsed in a second pass //////////////////////

  public void addUnresolvedTransitionDestination(Element nodeElement, Element jpdl4Element)
  {
	  unresolvedTransitionDestinations.add(new Object[]{nodeElement, jpdl4Element});
  }

  public void resolveTransitionDestinations()
  {
    for (Object[] unresolvedTransition : unresolvedTransitionDestinations)
    {
      Element nodeElement = (Element)unresolvedTransition[0];
      Element jpdl4Element = (Element)unresolvedTransition[1];
      resolveTransitionDestinations(nodeElement.elements("transition"), jpdl4Element);
    }
  }

  public void resolveTransitionDestinations(List<?> transitionElements, Element jpdl4Element)
  {
    for (Object transitionElement : transitionElements)
    {
      resolveTransitionDestination((Element)transitionElement, jpdl4Element);
    }
  }

  /*
   * creates the transition object and configures it by the read attributes
   * @return the created <code>org.jbpm.graph.def.Transition</code> object (useful, if you want to override this method to read additional configuration properties)
   */
  public void resolveTransitionDestination(Element transitionElement, Element jpdl4Element) {
    
    Element transition4 = jpdl4Element.addElement("transition");
    transition4.addAttribute("name", transitionElement.attributeValue("name"));
    if (transitionElement.elementTextTrim("description") != null) {
       transition4.addComment(transitionElement.elementTextTrim("description"));
    }    
    //Get condition from jpdl3 element
    String condition = transitionElement.attributeValue("condition");
    if (condition == null)
    {
      Element conditionElement = transitionElement.element("condition");
      if (conditionElement != null)
      {
        condition = conditionElement.getTextTrim();
        // for backwards compatibility
        if ((condition == null) || (condition.length() == 0))
        {
          condition = conditionElement.attributeValue("expression");
        }
      }
    }
   
    
    if (condition != null && condition.length() > 0) {
        Element condition4 =  transition4.addElement("condition");
        condition4.addAttribute("expr", condition);
    }
    

    // set destinationNode of the transition
    String toName = transitionElement.attributeValue("to");
    if (toName == null)
    {
      addWarning("node '" + transitionElement.getPath() + "' has a transition without a 'to'-attribute to specify its destinationNode");
    }
    else
    {    	
      Element to = this.findNode(toName);
      if (to == null)
      {
        addWarning("transition to='" + toName + "' on node '" + transitionElement.getName()  + "' cannot be resolved");
      }
      
      transition4.addAttribute("to", toName);
    }

    // read the actions
    convertActions(transitionElement, transition4, "");

    convertExceptionHandlers(transitionElement, transition4);
  }

  // action references are parsed in a second pass ////////////////////////////

  public void addUnresolvedActionReference(Element actionElemen)
  {
    //unresolvedActionReferences.add(new Object[] { actionElemen});
  }

  public void resolveActionReferences()
  {
    
  }

  // verify swimlane assignments in second pass ///////////////////////////////
  public void verifySwimlaneAssignments()
  {
    //
  }

  // mail delegations /////////////////////////////////////////////////////////

  public void createMailDelegation(String template, String actors, String to, String subject, String text)
  {
   //
  }

  public String getProperty(String property, Element element)
  {
    String value = element.attributeValue(property);
    if (value == null)
    {
      Element propertyElement = element.element(property);
      if (propertyElement != null)
      {
        value = propertyElement.getText();
      }
    }
    return value;
  }
  //New added method 
  private Element findNode(String name) {
	  return nodeCollection.get(name);
  }
  
  public Document getJpdl4Document()
  {
    return jpdl4Document;
  }
  
  private static final Log log = LogFactory.getLog(Jpdl3Converter.class);
}

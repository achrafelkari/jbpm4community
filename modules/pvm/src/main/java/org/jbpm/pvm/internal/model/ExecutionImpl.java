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
package org.jbpm.pvm.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;

import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.job.Job;
import org.jbpm.api.job.Timer;
import org.jbpm.api.listener.EventListenerExecution;
import org.jbpm.api.model.Activity;
import org.jbpm.api.model.Event;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.api.model.Transition;
import org.jbpm.api.task.Assignable;
import org.jbpm.api.task.AssignmentHandler;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.client.ClientProcessInstance;
import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.env.Context;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.env.ExecutionContext;
import org.jbpm.pvm.internal.history.HistoryEvent;
import org.jbpm.pvm.internal.history.events.ActivityEnd;
import org.jbpm.pvm.internal.history.events.ActivityStart;
import org.jbpm.pvm.internal.history.events.AutomaticEnd;
import org.jbpm.pvm.internal.history.events.DecisionEnd;
import org.jbpm.pvm.internal.history.events.ProcessInstanceCreate;
import org.jbpm.pvm.internal.history.events.ProcessInstanceEnd;
import org.jbpm.pvm.internal.id.DbidGenerator;
import org.jbpm.pvm.internal.id.IdComposer;
import org.jbpm.pvm.internal.job.JobImpl;
import org.jbpm.pvm.internal.job.MessageImpl;
import org.jbpm.pvm.internal.model.op.AtomicOperation;
import org.jbpm.pvm.internal.model.op.MoveToChildActivity;
import org.jbpm.pvm.internal.model.op.Signal;
import org.jbpm.pvm.internal.session.DbSession;
import org.jbpm.pvm.internal.session.MessageSession;
import org.jbpm.pvm.internal.session.RepositorySession;
import org.jbpm.pvm.internal.session.TimerSession;
import org.jbpm.pvm.internal.task.AssignableDefinitionImpl;
import org.jbpm.pvm.internal.task.SwimlaneDefinitionImpl;
import org.jbpm.pvm.internal.task.SwimlaneImpl;
import org.jbpm.pvm.internal.task.TaskImpl;
import org.jbpm.pvm.internal.type.Variable;
import org.jbpm.pvm.internal.util.EqualsUtil;
import org.jbpm.pvm.internal.util.Priority;
import org.jbpm.pvm.internal.wire.usercode.UserCodeReference;

/**
 * @author Tom Baeyens
 * @author Huisheng Xu
 */
public class ExecutionImpl extends ScopeInstanceImpl implements ClientProcessInstance,
  ActivityExecution, EventListenerExecution {

  private static final long serialVersionUID = 1L;

  private static final Log log = Log.getLog(ExecutionImpl.class.getName());

  /** an optional name for this execution.  can be used to
   * differentiate concurrent paths of execution like e.g.
   * the 'shipping' and 'billing' paths. */
  protected String name;

  /** a key for this execution. typically this is an externally provided reference
   * that is unique within the scope of the process definition.  */
  protected String key;

  /** a unique id for this execution. */
  protected String id;

  /** are concurrent executions that related to this execution. */
  protected Collection<ExecutionImpl> executions = new ArrayList<ExecutionImpl>();

  /** the parent child relation of executions is convenient for some forms of
   * concurrency. */
  protected ExecutionImpl parent;
  protected ExecutionImpl processInstance;

  /** the super process link in case this is a sub process execution */
  protected ExecutionImpl superProcessExecution;

  /** the sub process link in case of sub process execution */
  protected ExecutionImpl subProcessInstance;

  /** swimlanes */
  protected Map<String, SwimlaneImpl> swimlanes = new HashMap<String, SwimlaneImpl>();

  /** reference to the current activity instance history record */
  protected Long historyActivityInstanceDbid;

  /** start time of the activity for history purposes (not persisted) */
  protected Date historyActivityStart;

  protected int priority = Priority.NORMAL;

  protected Map<String, Variable> systemVariables = new HashMap<String, Variable>();

  // persistent indicators of the current position ////////////////////////////

  /** persistent process definition reference */
  protected String processDefinitionId;

  /** persistent activity reference */
  protected String activityName;

  // transient cached indicators of the current position //////////////////////

  /** transient cached process definition.  persistence is managed in {@link #processDefinitionId} */
  protected ProcessDefinitionImpl processDefinition;

  /** transient cached current activity pointer.  persistence is managed in {@link #activityName} */
  private ActivityImpl activity;

  /** transition is not to be made persistable by default */
  protected TransitionImpl transition;

  protected EventImpl event;

  protected AtomicOperation eventCompletedOperation;

  protected int eventListenerIndex;

  protected ObservableElementImpl eventSource;

  // cached named executions //////////////////////////////////////////////////

  /** caches the child executions by execution name.  This member might be
   * null and is only created from the executions in case its needed.  Note
   * that not all executions are forced to have a name and duplicates are allowed.
   * In case the {@link #executions} change, the executionsMap can be nulled or
   * also updated (but a check needs to be added whether it exists). */
  protected transient Map<String, Execution> executionsMap = null;

  /** the queue of atomic operations to be performed for this execution. */
  protected Queue<AtomicOperation> atomicOperations;

  public enum Propagation {
    UNSPECIFIED, WAIT, EXPLICIT
  }

  protected Propagation propagation;

  // construction /////////////////////////////////////////////////////////////

  public void initializeProcessInstance(ProcessDefinitionImpl processDefinition, String key) {
    setProcessDefinition(processDefinition);
    setActivity(processDefinition.getInitial());
    this.processInstance = this;
    this.state = STATE_CREATED;
    this.key = key;

    save();

    HistoryEvent.fire(new ProcessInstanceCreate(), this);
  }

  protected void save() {
    this.dbid = DbidGenerator.getDbidGenerator().getNextId();
    DbSession dbSession = EnvironmentImpl.getFromCurrent(DbSession.class, false);

    composeIds();

    if (dbSession != null) {
      dbSession.save(this);
    }
  }

  protected void composeIds() {
    this.id = IdComposer.getIdComposer().createId(processDefinition, parent, this);
  }

  // execution method : start /////////////////////////////////////////////////

  public void start() {
    if (!STATE_CREATED.equals(state)) {
      throw new JbpmException(toString()+" is already begun: "+state);
    }
    this.state = STATE_ACTIVE_ROOT;
    ExecutionImpl scopedExecution = initializeScopes();

    fire(Event.START, getProcessDefinition());
    if (getActivity()!=null) {
      scopedExecution.performAtomicOperation(AtomicOperation.EXECUTE_ACTIVITY);
    }
  }

  protected ExecutionImpl initializeScopes() {
    LinkedList<ActivityImpl> enteredActivities = new LinkedList<ActivityImpl>();

    ActivityImpl initial = getProcessDefinition().getInitial();
    ExecutionImpl scopedExecution = null;

    if (initial!=null) {
      enteredActivities.add(initial);
      ActivityImpl parentActivity = initial.getParentActivity();
      while (parentActivity!=null) {
        enteredActivities.addFirst(parentActivity);
        parentActivity = parentActivity.getParentActivity();
      }

      scopedExecution = this;

      initializeVariables(getProcessDefinition(), this);
      initializeTimers(getProcessDefinition());

      for (ActivityImpl enteredActivity: enteredActivities) {
        if (enteredActivity.isLocalScope()) {
          scopedExecution.setActivity(enteredActivity);
          scopedExecution = scopedExecution.createScope(enteredActivity);
        }
      }

      scopedExecution.setActivity(initial);
    }
    return scopedExecution;
  }

  public ExecutionImpl createScope(ScopeElementImpl scope) {
    ExecutionImpl child = createExecution(scope.getName());

    setState(STATE_INACTIVE_SCOPE);
    child.setState(STATE_ACTIVE_ROOT);

    // copy the current state from the child execution to the parent execution
    child.setActivity(getActivity());
    child.setTransition(getTransition());
    child.setPropagation(getPropagation());

    child.initializeVariables(scope, this);
    child.initializeTimers(scope);

    return child;
  }

  public ExecutionImpl destroyScope(CompositeElementImpl scope) {
    destroyTimers(scope);

    // copy the current state from the child execution to the parent execution
    parent.setActivity(getActivity());
    parent.setTransition(getTransition());
    parent.setPropagation(getPropagation());

    ExecutionImpl parentsParent = parent.getParent();
    if (parentsParent!=null
      && STATE_INACTIVE_CONCURRENT_ROOT.equals(parentsParent.getState())) {
      parent.setState(STATE_ACTIVE_CONCURRENT);
    }
    else {
      parent.setState(STATE_ACTIVE_ROOT);
    }

    // capture the parent execution cause the
    // subsequent invocation of end() will set the parent to null
    ExecutionImpl parent = this.parent;

    end();

    return parent;
  }

  @Override
  protected void destroyTimers(CompositeElementImpl scope) {
    TimerSession timerSession = EnvironmentImpl.getFromCurrent(TimerSession.class, false);
    if (timerSession!=null) {
      log.debug("destroying timers of "+this);
      List<Timer> timers = timerSession.findTimersByExecution(this);
      for (Timer timer: timers) {
        Job job = EnvironmentImpl.getFromCurrent(JobImpl.class, false);
        if (timer!=job) {
          timerSession.cancel(timer);
        }
      }
    }
  }

  // basic object methods /////////////////////////////////////////////////////

  @Override
  public String toString() {
    if (getId()!=null) {
      return "execution["+id+"]";
    }
    if (parent==null) {
      return "process-instance";
    }
    return "execution";
  }

  // execution method : end ///////////////////////////////////////////////////

  public void end() {
    end(Execution.STATE_ENDED);
  }

  public void end(String state) {
    if (state==null) {
      throw new JbpmException("state is null");
    }

    if (state.equals(STATE_CREATED)
        || state.equals(STATE_ACTIVE_ROOT)
        || state.equals(STATE_ACTIVE_CONCURRENT)
        || state.equals(STATE_INACTIVE_CONCURRENT_ROOT)
        || state.equals(STATE_INACTIVE_SCOPE)
        || state.equals(STATE_INACTIVE_JOIN)
        || state.equals(STATE_SUSPENDED)
        || state.equals(STATE_ASYNC)) {
      throw new JbpmException("invalid end state: "+state);
    }

    if (log.isDebugEnabled()) {
      if (state==STATE_ENDED) {
        log.debug(toString()+" ends");
      }
      else {
        log.debug(toString()+" ends with state "+state);
      }
    }

    // end all child executions
   // making a copy of the executions to prevent ConcurrentMoidificationException
    List<ExecutionImpl> executionsToEnd = new ArrayList<ExecutionImpl>(executions);
    for (ExecutionImpl child: executionsToEnd) {
      child.end(state);
    }

    setState(state);

    this.propagation = Propagation.EXPLICIT;

    DbSession dbSession = EnvironmentImpl.getFromCurrent(DbSession.class, false);



    if (parent!=null) {

      if (dbSession!=null) {

        // make sure task attached to this execution are completed or skipped
        TaskImpl task = dbSession.findTaskByExecution(this);
        if (task != null && !task.isCompleted()) {
          task.skip(null);
        }

        dbSession.delete(this);
      }
      parent.removeExecution(this);
    }
    else {
      // this is a process instance
      HistoryEvent.fire(new ProcessInstanceEnd(), this);
      fire(Event.END, getProcessDefinition());

      if (superProcessExecution!=null) {
        if (dbSession!=null) {
          dbSession.delete(this);
        }
        log.trace(toString()+" signals super process execution");
        superProcessExecution.signal();
      }
      else if (dbSession != null) {
        dbSession.deleteProcessInstance(id, false);
      }
    }
  }

  public void end(OpenExecution executionToEnd) {
    ((ExecutionImpl)executionToEnd).end();
  }

  public void end(OpenExecution executionToEnd, String state) {
    ((ExecutionImpl)executionToEnd).end(state);
  }

  // execution method : signal ////////////////////////////////////////////////

  public void signal() {
    signal(null, (Map<String,?>)null);
  }

  public void signal(String signal) {
    signal(signal, (Map<String,?>)null);
  }

  public void signal(Map<String, ?> parameters) {
    signal(null, parameters);
  }

  public void signal(String signal, Map<String, ?> parameters) {
    checkActive();
    if (getProcessDefinition().isSuspended()) {
      throw new JbpmException("process definition "+getProcessDefinition().getId()+" is suspended");
    }
    propagation = Propagation.EXPLICIT;
    if (getActivity()!=null) {
      performAtomicOperation(new Signal(signal, parameters));
    }
    else if (transition != null) {
      performAtomicOperation(AtomicOperation.TRANSITION_START_ACTIVITY);
    }
    else {
      throw new JbpmException("execution is not in a activity or in a transition");
    }
  }

  public void signal(Execution execution) {
    ((ExecutionImpl)execution).signal(null, (Map<String,?>)null);
  }

  public void signal(String signalName, Execution execution) {
    ((ExecutionImpl)execution).signal(signalName, (Map<String,?>)null);
  }

  public void signal(Map<String, ?> parameters, Execution execution) {
    ((ExecutionImpl)execution).signal(null, parameters);
  }

  public void signal(String signalName, Map<String, ?> parameters, Execution execution) {
    ((ExecutionImpl)execution).signal(signalName, parameters);
  }

  // execution method : take ////////////////////////////////////////////////

  /** @see Execution#takeDefaultTransition() */
  public void takeDefaultTransition() {
    TransitionImpl defaultTransition = getActivity().getDefaultOutgoingTransition();
    if (defaultTransition==null) {
      throw new JbpmException("there is no default transition in "+getActivity());
    }
    take(defaultTransition);
  }

  /** @see Execution#take(String) */
  public void take(String transitionName) {
    if (getActivity()==null) {
      throw new JbpmException(toString()+" is not positioned in activity");
    }
    TransitionImpl transition = findTransition(transitionName);
    if (transition==null) {
      throw new JbpmException("there is no transition "+transitionName+" in "+getActivity());
    }
    take(transition);
  }

  /** @see Execution#takeDefaultTransition() */
  public void take(Transition transition) {
    checkActive();

    setPropagation(Propagation.EXPLICIT);

    setTransition((TransitionImpl) transition);

    fire(Event.END, getActivity(), AtomicOperation.TRANSITION_END_ACTIVITY);
  }

  public void take(Transition transition, Execution execution) {
    ((ExecutionImpl)execution).take(transition);
  }

  // execution method : execute ///////////////////////////////////////////////

  /** @see Execution#execute(String) */
  public void execute(String activityName) {
    if (getActivity()==null) {
      throw new JbpmException("activity is null");
    }
    Activity nestedActivity = getActivity().getActivity(activityName);
    if (nestedActivity==null) {
      throw new JbpmException("activity "+activityName+" doesn't exist in "+getActivity());
    }
    execute(nestedActivity);
  }

  /** @see Execution#execute(Activity) */
  public void execute(Activity activity) {
    if (activity==null) {
      throw new JbpmException("activity is null");
    }
    checkActive();

    this.propagation = Propagation.EXPLICIT;
    performAtomicOperation(new MoveToChildActivity((ActivityImpl) activity));
  }

  // execution method : waitForSignal /////////////////////////////////////////

  public void waitForSignal() {
    propagation = Propagation.WAIT;
  }

  // execution method : proceed ///////////////////////////////////////////////

  public void proceed() {
    checkActive();

    // in graph based processDefinition languages we assume that a
    // default transition is available
    TransitionImpl defaultTransition = findDefaultTransition();
    if (defaultTransition!=null) {
      take(defaultTransition);
    }
    // in block structured processDefinition languages we assume that
    // there is no default transition and that there is a
    // parent activity of the current activity
    else {
      ActivityImpl parentActivity = getActivity().getParentActivity();

      // if there is a parent activity
      if (parentActivity!=null) {
        // propagate to the parent
        performAtomicOperation(AtomicOperation.PROPAGATE_TO_PARENT);
      }
      else {
        // When we don't know how to proceed, i don't know if it's best to
        // throw new PvmException("don't know how to proceed");
        // or to end the execution.  Because of convenience for testing,
        // I opted to end the execution.
        end();
      }
    }
  }

  public void setActivity(Activity activity, Execution execution) {
    ((ExecutionImpl)execution).setActivity(activity);
  }

  public void setActivity(Activity activity) {
    setActivity((ActivityImpl) activity);
  }

  // events ///////////////////////////////////////////////////////////////////

  public void fire(String eventName, ObservableElement eventSource) {
    fire(eventName, (ObservableElementImpl) eventSource, null);
  }

  public void fire(String eventName, ObservableElementImpl observableElement,
    AtomicOperation eventCompletedOperation) {
    EventImpl event = findEvent(observableElement, eventName);
    if (event!=null) {
      setEvent(event);
      setEventSource(observableElement);
      setEventListenerIndex(0);
      setEventCompletedOperation(eventCompletedOperation);
      performAtomicOperation(AtomicOperation.EXECUTE_EVENT_LISTENER);
    }
    else if (eventCompletedOperation != null) {
      performAtomicOperationSync(eventCompletedOperation);
    }
  }

  public static EventImpl findEvent(ObservableElementImpl observableElement, String eventName) {
    if (observableElement==null) {
      return null;
    }

    EventImpl event = observableElement.getEvent(eventName);
    if (event!=null) {
      return event;
    }

    return findEvent(observableElement.getParent(), eventName);
  }

  // execution : internal methods /////////////////////////////////////////////

  public void moveTo(ActivityImpl destination) {
    // move the execution to the destination
    setActivity(destination);
    transition = null;
  }

  public ExecutionImpl startActivity(ActivityImpl activity) {
    ExecutionImpl propagatingExecution = this;
    if (activity.isLocalScope()) {
      propagatingExecution = createScope(activity);
    }
    fire(Event.START, activity);
    return propagatingExecution;
  }

  public ExecutionImpl endActivity(ActivityImpl activity) {
    ExecutionImpl propagatingExecution = this;
    fire(Event.END, activity);
    if (activity.isLocalScope()) {
      propagatingExecution = destroyScope(activity);
    }
    return propagatingExecution;
  }

  // asynchronous continuations ////////////////////////////////////////////////

  public synchronized void performAtomicOperation(AtomicOperation operation) {
    if (operation.isAsync(this)) {
      sendContinuationMessage(operation);
    }
    else {
      performAtomicOperationSync(operation);
    }
  }

  public void sendContinuationMessage(AtomicOperation operation) {
    EnvironmentImpl environment = EnvironmentImpl.getCurrent();
    MessageSession messageSession = environment.get(MessageSession.class);
    if (messageSession==null) {
      throw new JbpmException("no message-session configured to send asynchronous continuation message");
    }
    MessageImpl asyncMessage = operation.createAsyncMessage(this);
    setState(Execution.STATE_ASYNC);
    messageSession.send(asyncMessage);
  }

  public void performAtomicOperationSync(AtomicOperation operation) {
    if (atomicOperations==null) {
      // initialise the fifo queue of atomic operations
      atomicOperations = new LinkedList<AtomicOperation>();
      atomicOperations.offer(operation);

      ExecutionContext originalExecutionContext = null;
      ExecutionContext executionContext = null;
      EnvironmentImpl environment = EnvironmentImpl.getCurrent();
      if (environment!=null) {
        originalExecutionContext = (ExecutionContext) environment
          .getContext(Context.CONTEXTNAME_EXECUTION);
        if (originalExecutionContext != null
          && originalExecutionContext.getExecution() == this) {
          originalExecutionContext = null;
        }
        else {
          executionContext = new ExecutionContext(this);
          environment.setContext(executionContext);
        }
      }

      try {
        while (! atomicOperations.isEmpty()) {
          AtomicOperation atomicOperation = atomicOperations.poll();
          atomicOperation.perform(this);
        }
      }
      finally {
        atomicOperations = null;

        if (executionContext!=null) {
          environment.removeContext(executionContext);
        }
        if (originalExecutionContext!=null) {
          environment.setContext(originalExecutionContext);
        }
      }
    }
    else {
      atomicOperations.offer(operation);
    }
  }

  /**
   * Important: Only use this if resolving an expression on another execution then the current execution
   *
   * TODO: remove this operation once the environment/executionContext is refactored
   */
  public Object resolveExpression(String expression, String language) {
    ExecutionContext originalExecutionContext = null;
    ExecutionContext executionContext = null;
    EnvironmentImpl environment = EnvironmentImpl.getCurrent();
    if (environment!=null) {
      originalExecutionContext = (ExecutionContext) environment
        .getContext(Context.CONTEXTNAME_EXECUTION);
      if (originalExecutionContext != null
        && originalExecutionContext.getExecution() == this) {
        originalExecutionContext = null;
      }
      else {
        executionContext = new ExecutionContext(this);
        environment.setContext(executionContext);
      }
    }

    try {
      return Expression.create(expression, language).evaluate();
    }
    catch (RuntimeException e) {
      log.error("Error while evaluation script " + expression, e);
      throw e;
    }
    finally {
      if (executionContext != null) {
        environment.removeContext(executionContext);
      }
      if (originalExecutionContext != null) {
        environment.setContext(originalExecutionContext);
      }
    }
  }

  public void handleException(ObservableElementImpl observableElement, EventImpl event,
    EventListenerReference eventListenerReference, Exception exception, String rethrowMessage) {

    List<ProcessElementImpl> processElements = new ArrayList<ProcessElementImpl>();
    if (eventListenerReference!=null) {
      processElements.add(eventListenerReference);
    }
    if (event!=null) {
      processElements.add(event);
    }
    while (observableElement!=null) {
      processElements.add(observableElement);
      observableElement = observableElement.getParent();
    }

    for (ProcessElementImpl processElement: processElements) {
      List<ExceptionHandlerImpl> exceptionHandlers = processElement.getExceptionHandlers();
      if (exceptionHandlers!=null) {
        for (ExceptionHandlerImpl exceptionHandler: exceptionHandlers) {
          if (exceptionHandler.matches(exception)) {
            try {
              exceptionHandler.handle(this, exception);
              return;
            }
            catch (Exception rethrowException) {
              if (!exceptionHandler.isRethrowMasked()) {
                exception = rethrowException;
              }
            }
            break;
          }
        }
      }
    }

    log.trace("rethrowing exception cause no exception handler for "+exception);
    ExceptionHandlerImpl.rethrow(exception, rethrowMessage+": "+exception.getMessage());
  }

  // tasks ////////////////////////////////////////////////////////////////////

  /** tasks and swimlane assignment.
   * SwimlaneDefinitionImpl is base class for TaskDefinitionImpl.
   * Both Task and Swimlane implement Assignable. */
  public void initializeAssignments(AssignableDefinitionImpl assignableDefinition,
    Assignable assignable) {
    Expression assigneeExpression = assignableDefinition.getAssigneeExpression();
    if (assigneeExpression!=null) {
      String assignee = (String) assigneeExpression.evaluate(this);
      assignable.setAssignee(assignee);

      if (log.isTraceEnabled()) log.trace("task "+name+" assigned to "+assignee+" using expression "+assigneeExpression);
    }

    Expression candidateUsersExpression = assignableDefinition.getCandidateUsersExpression();
    if (candidateUsersExpression!=null) {
      String candidateUsers = (String) candidateUsersExpression.evaluate(this);

      StringTokenizer tokenizer = new StringTokenizer(candidateUsers, ",");
      while (tokenizer.hasMoreTokens()) {
        String candidateUser = tokenizer.nextToken().trim();
        assignable.addCandidateUser(candidateUser);
      }
    }

    Expression candidateGroupsExpression = assignableDefinition.getCandidateGroupsExpression();
    if (candidateGroupsExpression!=null) {
      String candidateGroups = (String) candidateGroupsExpression.evaluate(this);
      StringTokenizer tokenizer = new StringTokenizer(candidateGroups, ",");
      while (tokenizer.hasMoreTokens()) {
        String candidateGroup = tokenizer.nextToken();
        assignable.addCandidateGroup(candidateGroup);
      }
    }

    UserCodeReference assignmentHandlerReference = assignableDefinition
      .getAssignmentHandlerReference();
    if (assignmentHandlerReference!=null) {
      // JBPM-2758
      // TODO Find out why processdefinition is null in at this time....
      if (processDefinition == null) {
        processDefinition = getProcessDefinition();
      }
      AssignmentHandler assignmentHandler = (AssignmentHandler) assignmentHandlerReference
        .getObject(processDefinition);
      if (assignmentHandler!=null) {
        try {
          assignmentHandler.assign(assignable, this);
        }
        catch (Exception e) {
          throw new JbpmException("assignment handler threw exception: " + e, e);
        }
      }
    }
  }

  protected String resolveAssignmentExpression(String expression, String expressionLanguage) {
    Object result = Expression.create(expression, expressionLanguage).evaluate();
    if (result == null || result instanceof String) {
      return (String) result;
    }
    throw new JbpmException("result of assignment expression " + expression
        + " is " + result + " (" + result.getClass().getName() + ") instead of String");
  }

  // swimlanes ////////////////////////////////////////////////////////////////

  public void addSwimlane(SwimlaneImpl swimlane) {
    swimlanes.put(swimlane.getName(), swimlane);
    swimlane.setExecution(this);
  }

  public SwimlaneImpl getSwimlane(String swimlaneName) {
    return swimlanes.get(swimlaneName);
  }

  public void removeSwimlane(SwimlaneImpl swimlane) {
    swimlanes.remove(swimlane.getName());
    swimlane.setExecution(null);
  }

  public SwimlaneImpl getInitializedSwimlane(SwimlaneDefinitionImpl swimlaneDefinition) {
    String swimlaneName = swimlaneDefinition.getName();
    SwimlaneImpl swimlane = swimlanes.get(swimlaneName);

    // lookup in parent execution
    if ((swimlane == null) && (parent != null)) {
      swimlane = parent.getInitializedSwimlane(swimlaneDefinition);
    }

    if (swimlane==null) {
      swimlane = createSwimlane(swimlaneName);
      initializeAssignments(swimlaneDefinition, swimlane);
    }

    return swimlane;
  }

  public SwimlaneImpl createSwimlane(String swimlaneName) {
    SwimlaneImpl swimlane = new SwimlaneImpl();
    long dbid = EnvironmentImpl.getFromCurrent(DbidGenerator.class).getNextId();
    swimlane.setDbid(dbid);
    swimlane.setName(swimlaneName);
    swimlane.setExecution(this);
    swimlanes.put(swimlaneName, swimlane);
    return swimlane;
  }

  // child executions /////////////////////////////////////////////////////////

  public ExecutionImpl createExecution() {
    return createExecution(null);
  }

  public ExecutionImpl createExecution(String name) {
    // when an activity calls createExecution, propagation is explicit.
    // this means that the default propagation (proceed()) will not be called
    propagation = Propagation.EXPLICIT;

    // create new execution
    ExecutionImpl childExecution = newChildExecution();
    // initialize child execution
    childExecution.setProcessDefinition(getProcessDefinition());
    childExecution.processInstance = this.processInstance;
    childExecution.name = name;

    // composeIds uses the parent so the childExecution has to be added before the ids are composed
    childExecution.setParent(this);
    childExecution.save();

    // make sure that child execution are saved before added to a persistent collection
    // cause of the 'assigned' id strategy, adding the childExecution to the persistent collection
    // before the dbid is assigned will result in identifier of an instance of ExecutionImpl altered from 0 to x
    addExecution(childExecution);

    log.debug("created "+childExecution);

    return childExecution;
  }

  protected ExecutionImpl newChildExecution() {
    return new ExecutionImpl();
  }

  public void addExecution(ExecutionImpl execution) {
    execution.setParent(this);
    executions.add(execution);
    executionsMap = null;
  }

  /** @see Execution#getExecution(String) */
  public ExecutionImpl getExecution(String name) {
    Map<String, Execution> executionsMap = getExecutionsMap();
    return executionsMap != null ? (ExecutionImpl) executionsMap.get(name) : null;
  }

  public void removeExecution(ExecutionImpl child) {
    if (executions.contains(child)) {
      if (executions.remove(child)) {
        child.setParent(null);

        // invalidate the executionsMap cache
        executionsMap = null;
      }
      else {
        throw new JbpmException(child + " is not a child execution of " + this);
      }
    }
  }

  public Map<String, Execution> getExecutionsMap() {
    if (executionsMap == null) {
      // initialize executionsMap cache
      executionsMap = new HashMap<String, Execution>();
      for(ExecutionImpl execution: executions) {
        String executionName = execution.getName();
        // the next test makes sure that the first execution wins
        // in case there are multiple executions with the same name
        if (! executionsMap.containsKey(executionName)) {
          executionsMap.put(executionName, execution);
        }
      }
    }
    return executionsMap;
  }

  public boolean hasExecution(String name) {
    return getExecutionsMap() != null && executionsMap.containsKey(name);
  }

  public boolean isActive(String activityName) {
    return findActiveActivityNames().contains(activityName);
  }

  public Set<String> findActiveActivityNames() {
    return addActiveActivityNames(new HashSet<String>());
  }

  protected Set<String> addActiveActivityNames(Set<String> activityNames) {
    if ((state.equals(STATE_ACTIVE_ROOT) || state.equals(STATE_ACTIVE_CONCURRENT))
      && activityName != null) {
      activityNames.add(activityName);
    }

    for (ExecutionImpl childExecution: executions) {
      childExecution.addActiveActivityNames(activityNames);
    }

    return activityNames;
  }

  public ExecutionImpl findActiveExecutionIn(String activityName) {
    if (activityName.equals(this.activityName) && isActive()) {
      return this;
    }

    for (ExecutionImpl childExecution: executions) {
      ExecutionImpl found = childExecution.findActiveExecutionIn(activityName);
      if (found!=null) {
        return found;
      }
    }

    return null;
  }

  // system variables /////////////////////////////////////////////////////////

  public void createSystemVariable(String key, Object value) {
    createSystemVariable(key, value, null);
  }

  public void createSystemVariable(String key, Object value, String typeName) {
    Variable variable = createVariableObject(key, value, typeName, false);
    systemVariables.put(variable.getKey(), variable);
  }

  public void setSystemVariable(String key, Object value) {
    Variable variable = systemVariables.get(key);
    if (variable!=null) {
      log.debug("setting system variable '"+key+"' in '"+this+"' to value '"+value+"'");
      variable.setValue(value, this);
    }
    else {
      log.debug("creating system variable '"+key+"' in '"+this+"' to value '"+value+"'");
      createSystemVariable(key, value, null);
    }
  }

  public Object getSystemVariable(String key) {
    Variable variable = systemVariables.get(key);
    if (variable!=null) {
      return variable.getValue(this);
    }
    return null;
  }

  public boolean removeSystemVariable(String key) {
    if (systemVariables.containsKey(key)) {
      return systemVariables.remove(key) != null;
    }
    return false;
  }

  // sub process creation /////////////////////////////////////////////////////

  public ClientProcessInstance createSubProcessInstance(ClientProcessDefinition processDefinition) {
    return createSubProcessInstance(processDefinition, null);
  }

  public ClientProcessInstance createSubProcessInstance(ClientProcessDefinition processDefinition, String key) {
    if (subProcessInstance!=null) {
      throw new JbpmException(toString()+" already has a sub process instance: "+subProcessInstance);
    }
    subProcessInstance = (ExecutionImpl) processDefinition.createProcessInstance(key);
    subProcessInstance.setSuperProcessExecution(this);
    return subProcessInstance;
  }

  public ClientProcessInstance startSubProcessInstance(ClientProcessDefinition processDefinition) {
    return startSubProcessInstance(processDefinition, null);
  }

  public ClientProcessInstance startSubProcessInstance(ClientProcessDefinition processDefinition, String key) {
    createSubProcessInstance(processDefinition, key);
    subProcessInstance.start();
    return subProcessInstance;
  }

  // state mgmt ///////////////////////////////////////////////////////////////

  /** @see Execution#suspend() */
  @Override
  public void suspend() {
    super.suspend();
    this.propagation = Propagation.EXPLICIT;
    DbSession dbSession = EnvironmentImpl.getFromCurrent(DbSession.class, false);
    if (dbSession!=null) {
      dbSession.cascadeExecutionSuspend(this);
    }
  }

  /** @see Execution#resume() */
  @Override
  public void resume() {
    super.resume();
    DbSession hibernatePvmDbSession = EnvironmentImpl.getFromCurrent(DbSession.class, false);
    if (hibernatePvmDbSession!=null) {
      hibernatePvmDbSession.cascadeExecutionResume(this);
    }
  }

  protected void checkActive() {
    if (!isActive()) {
      throw new JbpmException(toString()+" is not active: "+state);
    } else if (this.subProcessInstance != null && !Execution.STATE_ENDED.equals(this.subProcessInstance.getState())) {
      throw new JbpmException(toString() + " has running subprocess: "
              + this.subProcessInstance.toString() + " in state " + this.subProcessInstance.getState());
    }
  }

  public boolean isEnded() {
    if (Execution.STATE_ENDED.equals(state)) {
      return true;
    }
    if (Execution.STATE_CREATED.equals(state)) {
      return false;
    }
    if (Execution.STATE_ACTIVE_ROOT.equals(state)) {
      return false;
    }
    if (Execution.STATE_ACTIVE_CONCURRENT.equals(state)) {
      return false;
    }
    if (Execution.STATE_INACTIVE_CONCURRENT_ROOT.equals(state)) {
      return false;
    }
    if (Execution.STATE_INACTIVE_SCOPE.equals(state)) {
      return false;
    }
    if (Execution.STATE_SUSPENDED.equals(state)) {
      return false;
    }
    if (Execution.STATE_ASYNC.equals(state)) {
      return false;
    }
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////////

  // overriding the ScopeInstanceImpl methods /////////////////////////////////

  public ScopeInstanceImpl getParentVariableScope() {
    return parent;
  }

  public ExecutionImpl getTimerExecution() {
    return this;
  }

  // overridable by process languages /////////////////////////////////////////

  /** by default this will use {@link ActivityImpl#findOutgoingTransition(String)} to
   * search for the outgoing transition, which includes a search over the parent chain
   * of the current activity.  This method allows process languages to overwrite this default
   * implementation of the transition lookup by transitionName.*/
  protected TransitionImpl findTransition(String transitionName) {
    return getActivity().findOutgoingTransition(transitionName);
  }

  protected TransitionImpl findDefaultTransition() {
    return getActivity().findDefaultTransition();
  }

  // history //////////////////////////////////////////////////////////////////

  public void historyAutomatic() {
    HistoryEvent.fire(new AutomaticEnd(), this);
  }

  public void historyDecision(String transitionName) {
    HistoryEvent.fire(new DecisionEnd(transitionName), this);
  }

  public void historyActivityStart() {
    HistoryEvent.fire(new ActivityStart(), this);
  }

  public void historyActivityEnd() {
    HistoryEvent.fire(new ActivityEnd(), this);
  }

  public void historyActivityEnd(String transitionName) {
    HistoryEvent.fire(new ActivityEnd(transitionName), this);
  }

  // equals ///////////////////////////////////////////////////////////////////
  // hack to support comparing hibernate proxies against the real objects
  // since this always falls back to ==, we don't need to overwrite the hashcode
  @Override
  public boolean equals(Object o) {
    return EqualsUtil.equals(this, o);
  }

  // process definition getter and setter /////////////////////////////////////
  // this getter and setter is special because persistence is based on the   //
  // process definition id.                                                  //
  /////////////////////////////////////////////////////////////////////////////

  public ProcessDefinitionImpl getProcessDefinition() {
    if (processDefinition == null && processDefinitionId != null) {
      RepositorySession repositorySession = EnvironmentImpl
        .getFromCurrent(RepositorySession.class);
      processDefinition = repositorySession.findProcessDefinitionById(processDefinitionId);
      if (processDefinition==null) {
        throw new JbpmException("couldn't find process definition "+processDefinitionId+" in the repository");
      }
    }
    return processDefinition;
  }
  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
    this.processDefinitionId = processDefinition.getId();
  }

  // activity getter and setter ///////////////////////////////////////////////
  // this getter and setter is special because persistence is based on the   //
  // activity name.                                                          //
  /////////////////////////////////////////////////////////////////////////////

  public ActivityImpl getActivity() {
    if (activity == null && activityName != null) {
      activity = getProcessDefinition().findActivity(activityName);
    }
    return activity;
  }

  public void setActivity(ActivityImpl activity) {
    this.activity = activity;
    if (activity!=null) {
      this.activityName = activity.getName();
    }
    else {
      this.activityName = null;
    }
  }

  public String getActivityName() {
    return activityName;
  }

  // special getters and setters /////////////////////////////////////////////////

  public boolean hasAsyncEndEvent(List<ActivityImpl> leftActivities) {
    for (ActivityImpl leftActivity : leftActivities) {
      EventImpl endEvent = leftActivity.getEvent(Event.END);
      if (endEvent != null && endEvent.isAsync()) {
        return true;
      }
    }
    return false;
  }

  public boolean getIsProcessInstance() {
    return parent==null;
  }

  // getters and setters for scope instance //////////////////////////////////////

  @Override
  public ExecutionImpl getExecution() {
    return this;
  }

  // getters and setters /////////////////////////////////////////////////////////

  public TransitionImpl getTransition() {
    return transition;
  }
  public void setTransition(TransitionImpl transition) {
    this.transition = transition;
  }
  public EventImpl getEvent() {
    return event;
  }
  public ObservableElementImpl getEventSource() {
    return eventSource;
  }
  public Collection<ExecutionImpl> getExecutions() {
    return executions;
  }
  public String getName() {
    return name;
  }
  public ExecutionImpl getParent() {
    return parent;
  }
  public int getPriority() {
    return priority;
  }
  public void setEvent(EventImpl event) {
    this.event = event;
  }
  public void setEventSource(ObservableElementImpl eventSource) {
    this.eventSource = eventSource;
  }
  public void setPriority(int priority) {
    this.priority = priority;
  }
  public ExecutionImpl getProcessInstance() {
    return processInstance;
  }
  public void setProcessInstance(ExecutionImpl processInstance) {
    this.processInstance = processInstance;
  }
  public String getKey() {
    return key;
  }
  public Propagation getPropagation() {
    return propagation;
  }
  public void setPropagation(Propagation propagation) {
    this.propagation = propagation;
  }
  public void setName(String name) {
    this.name = name;
  }
  public void setExecutions(Collection<ExecutionImpl> executions) {
    this.executions = executions;
  }
  public void setParent(ExecutionImpl parent) {
    this.parent = parent;
  }
  public ExecutionImpl getSuperProcessExecution() {
    return superProcessExecution;
  }
  public void setSuperProcessExecution(ExecutionImpl superProcessExecution) {
    this.superProcessExecution = superProcessExecution;
  }
  public ExecutionImpl getSubProcessInstance() {
    return subProcessInstance;
  }
  public void setSubProcessInstance(ExecutionImpl subProcessExecution) {
    this.subProcessInstance = subProcessExecution;
  }
  public void setKey(String key) {
    this.key = key;
  }
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public Long getHistoryActivityInstanceDbid() {
    return historyActivityInstanceDbid;
  }
  public void setHistoryActivityInstanceDbid(Long historyActivityInstanceDbid) {
    this.historyActivityInstanceDbid = historyActivityInstanceDbid;
  }
  public Date getHistoryActivityStart() {
    return historyActivityStart;
  }
  public void setHistoryActivityStart(Date historyActivityStart) {
    this.historyActivityStart = historyActivityStart;
  }
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  public int getEventListenerIndex() {
    return eventListenerIndex;
  }
  public void setEventListenerIndex(int eventListenerIndex) {
    this.eventListenerIndex = eventListenerIndex;
  }
  public AtomicOperation getEventCompletedOperation() {
    return eventCompletedOperation;
  }
  public void setEventCompletedOperation(AtomicOperation eventCompletedOperation) {
    this.eventCompletedOperation = eventCompletedOperation;
  }
}

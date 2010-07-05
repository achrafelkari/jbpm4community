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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.api.model.Activity;
import org.jbpm.api.model.Transition;
import org.jbpm.pvm.internal.util.ReflectUtil;
import org.jbpm.pvm.internal.wire.Descriptor;

/**
 * @author Tom Baeyens
 */
public class ActivityImpl extends CompositeElementImpl implements Activity {

  private static final long serialVersionUID = 1L;
  
  protected ActivityBehaviour activityBehaviour;
  protected boolean isActivityBehaviourStateful = false;
  protected Descriptor activityBehaviourDescriptor;
  
  protected List<TransitionImpl> outgoingTransitions = new ArrayList<TransitionImpl>();
  protected List<TransitionImpl> incomingTransitions = new ArrayList<TransitionImpl>();
  protected TransitionImpl defaultOutgoingTransition;
  protected ActivityImpl parentActivity;

  protected String type;
  protected Continuation continuation = Continuation.SYNCHRONOUS;

  protected ActivityCoordinatesImpl coordinates;
  
  // Do not initialize. Caching is based on the nullity of this map
  transient protected Map<String, TransitionImpl> outgoingTransitionsMap = null;
  
  /**
   * Use {@link ProcessDefinitionImpl#createActivity()} or {@link ActivityImpl#createActivity()} instead.
   */
  public ActivityImpl() {
    super();
  }
  
  // specialized activity containment methods /////////////////////////////////////
  
  public ActivityImpl addActivity(ActivityImpl activity) {
    activity.setParentActivity(this);
    super.addActivity(activity);
    return activity;
  }
  
  public ActivityImpl findActivity(String activityName) {
    if (activityName==null) {
      if (name==null) {
        return this;
      }
    } else if (activityName.equals(name)) {
      return this;
    }
    return super.findActivity(activityName);
  }

  // outgoing transitions //////////////////////////////////////////////////////

  /** creates an outgoing transition from this activity. */
  public TransitionImpl createOutgoingTransition() {
    // create a new transition
    TransitionImpl transition = new TransitionImpl();
    transition.setProcessDefinition(processDefinition);
    
    // wire it between the source and destination
    addOutgoingTransition(transition);

    // if there is no default transition yet
    if (defaultOutgoingTransition==null) {
      // make this the default outgoing transition
      defaultOutgoingTransition = transition;
    }
    
    return transition;
  }
  
  /**
   * adds the given transition as a leaving transition to this activity.
   * Also the source of the transition is set to this activity.
   * Adding a transition that is already contained in the leaving 
   * transitions has no effect. 
   * @return the added transition. 
   * @throws NullPointerException if transition is null.
   */
  public Transition addOutgoingTransition(TransitionImpl transition) {
    if (! outgoingTransitions.contains(transition)) {
      transition.setSource(this);
      transition.setSourceIndex(outgoingTransitions.size());
      outgoingTransitions.add(transition);
      clearOutgoingTransitionsMap();
    }
    return transition;
  }

  /**
   * removes the given transition from the leaving transitions.
   * Also the transition's source will be nulled.
   * This method will do nothing if the transition is null or if 
   * the given transition is not in the list of this activity's leaving 
   * transitions.
   * In case this is the transition that was in the 
   * outgoingTransitionsMap and another transition exists with the same
   * name, that transition (the first) will be put in the 
   * outgoingTransitionsMap as a replacement for the removed transition.
   * If the transition is actually removed from the list of 
   * leaving transitions, the transition's source will be nulled. 
   */
  public boolean removeOutgoingTransition(TransitionImpl transition) {
    if (transition!=null) {
      boolean isRemoved = outgoingTransitions.remove(transition);
      if (isRemoved) {
        transition.setSource(null);
        clearOutgoingTransitionsMap();
      }
      return isRemoved;
    }
    return false;
  }

  /** the first leaving transition with the given name or null of no
   * such leaving transition exists.
   */
  public TransitionImpl getOutgoingTransition(String transitionName) {
    return (getOutgoingTransitionsMap()!=null ? outgoingTransitionsMap.get(transitionName) : null);
  }
  
  /** searches for the given transitionName in this activity and then up the 
   * parent chain. Returns null if no such transition is found. */
  public TransitionImpl findOutgoingTransition(String transitionName) {
    TransitionImpl transition = getOutgoingTransition(transitionName);
    if (transition!=null) {
      return transition;
    }
    if (parentActivity!=null) {
      return parentActivity.findOutgoingTransition(transitionName);
    }
    return null;
  }
  
  /** searches for the default transition in this activity and then up the 
   * parent chain. Returns null if no such transition is found. */
  public TransitionImpl findDefaultTransition() {
    if (defaultOutgoingTransition!=null) {
      return defaultOutgoingTransition;
    }
    if (parentActivity!=null) {
      return parentActivity.findDefaultTransition();
    }
    return null;
  }

  
  /** the list of leaving transitions.
   * Beware: the actual member is returned.  No copy is made. 
   */
  public List<? extends Transition> getOutgoingTransitions() {
    return outgoingTransitions;
  }

  /** indicates if a leaving transition with the given transitionName exists. */
  public boolean hasOutgoingTransition(String transitionName) {
    return (getOutgoingTransition(transitionName)!=null);
  }

  /** indicates if this activity has leaving transitions */
  public boolean hasOutgoingTransitions() {
    return !outgoingTransitions.isEmpty();
  }

  /** sets the outgoingTransitions to the given list of outgoingTransitions.
   * A copy of the collection is made.  Also the outgoingTransitionsMap will 
   * be updated and the source of all the transitions in the given list will 
   * be set to this activity.
   * In case there was a leaving transitions list present, these transition's
   * source will be nulled.
   */
  public void setOutgoingTransitions(List<TransitionImpl> outgoingTransitions) {
    if (!this.outgoingTransitions.isEmpty()) {
      List<TransitionImpl> removedTransitions = new ArrayList<TransitionImpl>(outgoingTransitions);
      for (TransitionImpl removedTransition: removedTransitions) {
        removeOutgoingTransition(removedTransition);
      }
    }
    if (outgoingTransitions!=null) {
      this.outgoingTransitions = new ArrayList<TransitionImpl>();
      for (TransitionImpl addedTransition: outgoingTransitions) {
        addOutgoingTransition(addedTransition);
      }
    } else {
      this.outgoingTransitions = new ArrayList<TransitionImpl>();
    }
    clearOutgoingTransitionsMap();
  }

  // arriving transitions /////////////////////////////////////////////////////
  
  /**
   * adds the given transition as an arriving transition to this activity.
   * Also the source of the transition is set to this activity. 
   * @return the added transition. 
   * @throws NullPointerException if transition is null.
   */
  public Transition addIncomingTransition(TransitionImpl transition) {
    transition.setDestination(this);
    incomingTransitions.add(transition);
    return transition;
  }

  /** removes the given transition if it is contained in the arriving
   * transitions of this activity.  If this transition was actually removed,
   * its destination pointer is nulled.
   * @return true if a transition was removed.
   */
  public boolean removeIncomingTransition(TransitionImpl transition) {
    if ( (transition!=null) && (incomingTransitions.remove(transition))) {
      transition.setDestination(null);
      return true;
    }
    return false;
  }

  /** the list of arriving transitions.
   * Beware: the actual member is returned.  No copy is made.
   */ 
  public List<? extends Transition> getIncomingTransitions() {
    return incomingTransitions;
  }

  /** indicates if this activity has arriving transitions */
  public boolean hasIncomingTransitions() {
    return !incomingTransitions.isEmpty();
  }


  /** sets the incomingTransitions to the given list of incomingTransitions.
   * A copy of the collection is made.  Also the destination of all the transitions 
   * in the given list will be set to this activity.
   * In case there was an arriving transitions list present, these transition's
   * destination will be nulled.
   */
  public void setIncomingTransitions(List<TransitionImpl> incomingTransitions) {
    if (!this.incomingTransitions.isEmpty()) {
      for (TransitionImpl removedTransition: this.incomingTransitions) {
        removedTransition.setDestination(null);
      }
    }
    if (incomingTransitions!=null) {
      this.incomingTransitions = new ArrayList<TransitionImpl>(incomingTransitions);
      for (TransitionImpl addedTransition: incomingTransitions) {
        addedTransition.setDestination(this);
      }
    } else {
      this.incomingTransitions = null;
    }
  }

  /** the leaving transitions, keyed by transition name.  If a transition with 
   * the same name occurs mutltiple times, the first one is returned.
   * Leaving transitions with a null value for their name are not included 
   * in the map.
   * Beware: the actual member is returned.  No copy is made. 
   */
  public Map<String, ? extends Transition> getOutgoingTransitionsMap() {
    if(outgoingTransitionsMap == null){
      outgoingTransitionsMap = new HashMap<String, TransitionImpl>();
      for (TransitionImpl transition: outgoingTransitions) {
        if (!outgoingTransitionsMap.containsKey(transition.getName())) {
          outgoingTransitionsMap.put(transition.getName(), transition);
        }
      }
    }
    return outgoingTransitionsMap;
  }

  void clearOutgoingTransitionsMap() {
    outgoingTransitionsMap = null;
  }

  // various helper methods ///////////////////////////////////////////////////
  

  static Map<String, ActivityImpl> getActivitiesMap(List<ActivityImpl> activities) {
    Map<String, ActivityImpl> map = null;
    if (activities!=null) {
      map = new HashMap<String, ActivityImpl>();
      for (ActivityImpl activity: activities) {
        if (! map.containsKey(activity.getName())) {
          map.put(activity.getName(), activity);
        }
      }
    }
    return map;
  }

  public String toString() { 
    if (name!=null) return "activity("+name+")";
    if (dbid!=0) return "activity("+dbid+")";
    return "activity("+System.identityHashCode(this)+")"; 
  }

  /** collects the full stack of parent in a list.  This activity is the 
   * first element in the chain.  The process definition will be the last element.
   * the chain will never be null. */
  public List<ObservableElementImpl> getParentChain() {
    List<ObservableElementImpl> chain = new ArrayList<ObservableElementImpl>();
    ObservableElementImpl processElement = this;
    while (processElement!=null) {
      chain.add(processElement);
      processElement = processElement.getParent();
    }
    return chain;
  }

  public boolean isAsync() {
    return ! (continuation==Continuation.SYNCHRONOUS);
  }

  public boolean contains(ActivityImpl activity) {
    while (activity!=null) {
      if (activity.getParent()==this) {
        return true;
      }
      activity = activity.getParentActivity();
    }
    return false;
  }

  // customized getters and setters ///////////////////////////////////////////

  public ActivityBehaviour getActivityBehaviour() {
    if (activityBehaviour!=null) {
      return activityBehaviour;
    }
    if (activityBehaviourDescriptor!=null) {
      ActivityBehaviour createdBehaviour = (ActivityBehaviour) ReflectUtil.instantiateUserCode(activityBehaviourDescriptor, processDefinition, null);
      if (!isActivityBehaviourStateful) {
        activityBehaviour = createdBehaviour;
      }
      return createdBehaviour;
    }
    return null;
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public ObservableElementImpl getParent() {
    return (parentActivity!=null ? parentActivity : processDefinition);
  }
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public TransitionImpl getDefaultOutgoingTransition() {
    return defaultOutgoingTransition;
  }
  public void setDefaultOutgoingTransition(TransitionImpl defaultOutgoingTransition) {
    this.defaultOutgoingTransition = defaultOutgoingTransition;
  }
  public ActivityImpl getParentActivity() {
    return parentActivity;
  }
  public void setParentActivity(ActivityImpl parentActivity) {
    this.parentActivity = parentActivity;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public ActivityCoordinatesImpl getCoordinates() {
    return coordinates;
  }
  public void setCoordinates(ActivityCoordinatesImpl coordinates) {
    this.coordinates = coordinates;
  }
  public Continuation getContinuation() {
    return continuation;
  }
  public void setContinuation(Continuation continuation) {
    this.continuation = continuation;
  }
  public void setActivityBehaviour(ActivityBehaviour activityBehaviour) {
    this.activityBehaviour = activityBehaviour;
  }
  public Descriptor getActivityBehaviourDescriptor() {
    return activityBehaviourDescriptor;
  }
  public void setActivityBehaviourDescriptor(Descriptor activityBehaviourDescriptor) {
    this.activityBehaviourDescriptor = activityBehaviourDescriptor;
  }
  public boolean isActivityBehaviourStateful() {
    return isActivityBehaviourStateful;
  }
  public void setActivityBehaviourStateful(boolean isActivityBehaviourStateful) {
    this.isActivityBehaviourStateful = isActivityBehaviourStateful;
  }
}

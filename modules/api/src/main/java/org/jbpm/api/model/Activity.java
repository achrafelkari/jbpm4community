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
package org.jbpm.api.model;

import java.util.List;
import java.util.Map;

/**
 * a activity in a {@link OpenProcessDefinition} graph.
 * 
 * <p>The activity supports both graph based process models as well as 
 * block structured (tree based) process models.  First we describe 
 * tranisions that can be used to formResourceName graph based process structures 
 * and then we'll describe activity composition to formResourceName block structured
 * process models.  Both models can be combined as well.
 * </p>
 * 
 * <p>Activities have {@link #getIncomingTransitions() incoming}
 * and {@link #getOutgoingTransitions() outgoing transitions}.
 * These are lists of transitions.
 * </p>
 * 
 * <p>Optionally, transitions can have names.  In that case the 
 * transition {@link #getOutgoingTransition(String) names are associated 
 * to activity's outgoing transitions}.  The {@link #getOutgoingTransitionsMap() map 
 * of outgoing transitions} provides easy access to the named transitions.
 * </p> 
 *   
 * <p>One of the outgoing transitions can optionally be marked as 
 * {@link #getDefaultOutgoingTransition() the default transition}.
 * </p>
 * 
 * <p>Block structured process languages have composite activities that can be 
 * modeled with the {@link #getParent() parent}-{@link #getActivities() child} 
 * relation.
 * </p> 
 * 
 * @author Tom Baeyens
 */
public interface Activity {

  /** the short display name given to this element. */
  String getName();

  /** the list of outgoing transitions.
   * Caution: the actual member is returned.  No copy is made. */
  List<? extends Transition> getOutgoingTransitions();

  /** the default outgoing transition. */
  Transition getDefaultOutgoingTransition();

  /** the first leaving transition with the given name or null of no
   * such leaving transition exists. If the multiple transitions have 
   * the given transition name, the first (in order of {@link #getOutgoingTransitions()})
   * will be returned.
   *  
   * @param transitionName is the name of the transition to take.  A null value will 
   * match the first unnamed transition. */
  Transition getOutgoingTransition(String transitionName);

  /** indicates if a leaving transition with the given transitionName exists. 
   * A null value matches an unnamed transition. */
  boolean hasOutgoingTransition(String transitionName);

  /** indicates if this activity has leaving transitions */
  boolean hasOutgoingTransitions();

  /** the leaving transitions, keyed by transition name.  If a transition with 
   * the same name occurs mutltiple times, the first one is returned.
   * Leaving transitions with a null value for their name are not included 
   * in the map.
   * Beware: the actual member is returned.  No copy is made.  In fact, the 
   * returned map is maintained as a cache.  So updates to the map will 
   * influence subsequent retrievals of outgoing transitions by name. */
  Map<String, ? extends Transition> getOutgoingTransitionsMap();
  
  /** searches for the given transitionName in this activity and then up the 
   * parent chain. Returns null if no such transition is found. */
  Transition findOutgoingTransition(String transitionName);

  /** the list of arriving transitions.
   * Beware: the actual member is returned.  No copy is made. */
  List<? extends Transition> getIncomingTransitions();

  /** indicates if this activity has arriving transitions */
  boolean hasIncomingTransitions();
  
  /** retrieve the parent activity in the composite activity structure.  This is 
   * different from {@link ObservableElement#getParent()} in that it is restricted 
   * to the parent activities.  It doesn't take into account the process definition. */ 
  Activity getParentActivity();
  
  /** indicates if this processDefinition has activities. */
  boolean hasActivities();

  /** the list of direct composite activities.  Recursively contained 
   * activities are not included in the list. 
   * Beware: the actual member is returned.  No copy is made. */
  List<? extends Activity> getActivities();

  /** indicates if an activity with the given name exists directly in 
   * this element.  Only the direct contained activities are 
   * searched.  No recursive search is made. */
  boolean hasActivity(String activityName);

  /** the first composite activity with the given name or null of no
   * such activity exists. Only the direct contained activities are 
   * searched.  No recursive search is made. */
  Activity getActivity(String activityName);

  /** searches for the given activity in this element recursively, 
   * including this activity and all child activities.  The search 
   * is done depth-first. A null value for activityName matches a activity 
   * without a name. */
  Activity findActivity(String activityName);

  /** the composite activities, keyed by activity name.  If an activity 
   * with the same name occurs mutltiple times, the first in the list
   * is included in the map. Activities with a null value for their name 
   * are not included in the map. 
   * Beware: the actual member is returned.  No copy is made. In fact, the 
   * returned map is maintained as a cache.  So updates to the map will 
   * influence subsequent retrievals of activities by name.*/
  Map<String, ? extends Activity> getActivitiesMap();

  /** the type of this activity which corresponds to the xml tag */
  String getType();
}
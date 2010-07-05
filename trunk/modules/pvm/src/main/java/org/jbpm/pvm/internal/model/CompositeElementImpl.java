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
import java.util.List;
import java.util.Map;

import org.jbpm.api.model.Activity;


/**
 * @author Tom Baeyens
 */
public abstract class CompositeElementImpl extends ScopeElementImpl implements CompositeElement {

  private static final long serialVersionUID = 1L;
  
  protected List<ActivityImpl> activities;
  transient protected Map<String, ActivityImpl> activitiesMap;

  // nested activities /////////////////////////////////////////////////////////////
  
  /**
   * creates a nested activity. Also the nested activity's parent pointer will be set 
   * appropriatly. 
   */
  public ActivityImpl createActivity() {
    return createActivity(null);
  }

  /**
   * creates a nested activity with the given name. Also the nested activity's parent pointer will be set 
   * appropriatly. 
   * @param activityName may be null. 
   */
  public ActivityImpl createActivity(String activityName) {
    ActivityImpl activity = new ActivityImpl();
    activity.setName(activityName);
    addActivity(activity);
    return activity;
  }

  public Activity addActivity(ActivityImpl activity) {
    activity.setProcessDefinition(processDefinition);
    if (activities==null) {
      activities = new ArrayList<ActivityImpl>();
    }
    if (! activities.contains(activity)) {
      activities.add(activity);
    }
    activitiesMap = null;
    return activity;
  }
  
  /** removes the given activity from the nested activities.
   * Also the activity's parent will be nulled.
   * This method will do nothing if the activity is null or if 
   * the activity is not in the list of nested activities.
   * If the activity is actually removed from the list of 
   * activities, the activity's source will be nulled. 
   * In case this is the activity that was in the 
   * activitiesMap and another activity exists with the same
   * name, that activity (the first) will be put in the 
   * activitiesMap as a replacement for the removed activity.
   */
  public boolean removeActivity(ActivityImpl activity) {
    if ( (activity!=null)
         && (activities!=null)
       ) {
      boolean isRemoved = activities.remove(activity);
      if (isRemoved) {
        activity.setParentActivity(null);
        if (activities.isEmpty()) {
          activities = null;
        }
        activitiesMap = null;
      }
      return isRemoved;
    }
    return false;
  }

  /** the first nested activity with the given name or null of no
   * such activity exists.
   */
  public ActivityImpl getActivity(String activityName) {
    return (getActivitiesMap()!=null ? activitiesMap.get(activityName) : null);
  }

  /** is this activity present ? */
  public boolean hasActivity(String activityName) {
    return ((getActivitiesMap()!=null) && (activitiesMap.containsKey(activityName)));
  }

  public ActivityImpl findActivity(String activityName) {
    if (activities!=null) {
      for(ActivityImpl n : activities) {
        ActivityImpl activity = n.findActivity(activityName);
        if (activity!=null) {
          return activity;
        }
      }
    }
    return null;
  }

  /** the list of nested activities.
   * Beware: the actual member is returned.  No copy is made. 
   */
  public List<? extends Activity> getActivities() {
    return activities;
  }

  /** the nested activities, keyed by activity name.  If a activity with 
   * the same name occurs mutltiple times, the first in the list
   * is included in the map.
   * Activities with a null value for their name are not included 
   * in the map.
   * Beware: the actual member is returned.  No copy is made. 
   */
  public Map<String, ? extends Activity> getActivitiesMap() {
    if (activitiesMap == null) {
      this.activitiesMap = ActivityImpl.getActivitiesMap(activities);
    }
    return activitiesMap;
  }
  
  /** indicates if this processDefinition has activities. */
  public boolean hasActivities() {
    return ((activities!=null) && (!activities.isEmpty()));
  }

}

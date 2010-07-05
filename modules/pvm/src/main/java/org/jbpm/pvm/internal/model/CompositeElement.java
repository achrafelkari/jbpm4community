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

import java.util.List;
import java.util.Map;

import org.jbpm.api.model.Activity;



/** activity container base class for {@link OpenProcessDefinition} and {@link Activity}.
 * 
 * @author Tom Baeyens
 */
public interface CompositeElement extends ObservableElement {

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
}

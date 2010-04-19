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

import java.io.Serializable;
import java.util.List;

import org.jbpm.api.model.Activity;



/**
 * @author Tom Baeyens
 */
public class ProcessModificationsImpl implements Serializable {

  private static final long serialVersionUID = 1L;
  
  protected long dbid;
  protected int version;
  
  // TODO: as an optimisation, boolean flag properties could be added 
  // as persistent memberfields to indicate if the collections are 
  // empty or not.  That way, when a process modification object 
  // is consulted, before accessing any of the collections it can 
  // first check if the collections are empty, hence preventing a 
  // database query for the empty collections.
  
  // Removed items just can be referenced in the process definition

  // Adding is harder.  Full information of added items cannot be stored 
  // in the process 
  // definition database part as those tables will be cached and 
  // marked with read-only.  So the added items must be stored in 
  // the runtime (=execution) part of the database schema.
  // That is why subclasses of the real process definition classes 
  // are created.

  protected List<AddedAction> addedActions;
  protected List<EventListenerReference> removedActions;
  protected List<AddedTransition> addedTransitions;
  protected List<TransitionImpl> removedTransitions;
  protected List<AddedActivity> addedActivities;
  protected List<ActivityImpl> removedActivities;
  
  // TODO : convert to top level types
  
  public static class AddedAction extends EventListenerReference {
    private static final long serialVersionUID = 1L;
    EventImpl event;
  }
  
  public static class AddedTransition extends TransitionImpl {
    private static final long serialVersionUID = 1L;
    
  }
  
  public static class AddedActivity extends ActivityImpl {
    private static final long serialVersionUID = 1L;
  }
  
  // next this class would implement methods to merge the 
  // modifications with the process definition information.
  
  // the ExecutionImpl would have to do lookups in 
  // the process definition model conscientious through 
  // these merging methods
  
  public TransitionImpl getOutgoingTransition(ActivityImpl activity, String transitionName) {
    // first check if the requested transition is in the added transition list
    if (addedTransitions!=null) {
      for (AddedTransition addedTransition : addedTransitions) {
        if ( activity.equals(addedTransition.getSource())
             && addedTransition.getName()!=null
             && addedTransition.getName().equals(transitionName)
           ) {
          return addedTransition;
        }
      }
    }
    
    // now, check if it is in the normal transition list in the process definition
    TransitionImpl transition = activity.getOutgoingTransition(transitionName);
    
    if ( (transition!=null) // if the transition exists
         && (removedTransitions!=null) // and if there are removed transitions
         && (removedTransitions.contains(transition)) // and if the transition found in the process definition is present in the removedTransitions collection
       ) {
      // then pretent it wasn't there :-)
      return null;
    }
    
    return transition;
  }

  // furhermore, action, activity and transition creation methods 
  // will have to be exposed through the user interface classes.
  // We have to make sure that the user is able to access and find 
  // all the input parameters for these creation methods from the 
  // org.jbpm.pvm classes, without ever needing org.jbpm.pvm.internal.model 
  // classes.
  // for example:
  
  public void addTransition(Activity source, String name, Activity destination) {
    // TODO
  }
  
}

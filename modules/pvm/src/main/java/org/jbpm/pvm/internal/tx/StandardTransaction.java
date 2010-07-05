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
package org.jbpm.pvm.internal.tx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Synchronization;

import org.jbpm.internal.log.Log;


/** simple 2 phase commit transaction.
 * no logging or recovery.
 * non thread safe (which is ok).
 * @author Tom Baeyens
 */
public class StandardTransaction extends AbstractTransaction implements Serializable {

  private static final long serialVersionUID = 1L; 
  private static Log log = Log.getLog(StandardTransaction.class.getName());

  enum State {
    CREATED,
    ACTIVE,
    ROLLBACKONLY,
    COMMITTED,
    ROLLEDBACK
  }

  protected List<StandardResource> resources;
  protected List<StandardSynchronization> synchronizations;
  protected State state = State.CREATED;

  // methods for interceptor //////////////////////////////////////////////////

  public void begin() {
    if (log.isTraceEnabled()) log.trace("beginning "+this);
    state = State.ACTIVE;
  }

  public void complete() {
    if (state==State.ACTIVE) {
      commit();
    } else if (state==State.ROLLBACKONLY){
      rollback();
    } else {
      throw new TransactionException("complete on transaction in state "+state);
    }
  }

  // public tx methods ////////////////////////////////////////////////////////

  public void setRollbackOnly() {
    if (state!=State.ACTIVE) {
      throw new TransactionException("transaction was not active: "+state);
    }
    state = State.ROLLBACKONLY;
  }

  public boolean isRollbackOnly() {
    return ( (state==State.ROLLBACKONLY)
             || (state==State.ROLLEDBACK)
           );
  }
  
  // commit ///////////////////////////////////////////////////////////////////

  /** implements simplest two phase commit. */
  public void commit() {
    flushDeserializedObjects();
    
    if (state!=State.ACTIVE) {
      throw new TransactionException("commit on transaction in state "+state);
    }
    
    log.trace("committing "+this);

    try {
      beforeCompletion();

      if (resources!=null) {
        // prepare //////////////////////////////////////////////////////////////
        // the prepare loop will be skipped at the first exception
        for (StandardResource standardResource: resources) {
          if (log.isTraceEnabled()) log.trace("preparing resource "+standardResource);
          standardResource.prepare();
        }
      }

    // for any exception in the prepare phase, we'll rollback
    } catch (Exception exception) {
      try {
        if (log.isTraceEnabled()) log.trace("resource threw exception in prepare.  rolling back.");
        rollbackResources();
      } catch (Exception rollbackException) {
        log.error("rollback failed as well", rollbackException);
      }

      // rethrow
      if (exception instanceof RuntimeException) {
        throw (RuntimeException) exception;
      }
      throw new TransactionException("prepare failed", exception);
    }
    
    // here is the point of no return :-)

    // commit ///////////////////////////////////////////////////////////////
    RuntimeException commitException = null;
    if (resources!=null) {
      // The commit loop will try to send the commit to every resource, 
      // No matter what it takes.  If exceptions come out of resource.commit's 
      // they will be suppressed and the first exception will be rethrown after
      // all the resources are commited 
      for (StandardResource standardResource: resources) {
        try {
          if (log.isTraceEnabled()) log.trace("committing resource "+standardResource);
          standardResource.commit();
          
        // Exceptions in the commit phase will not lead to rollback, since some resources
        // might have committed and can't go back.
        } catch (RuntimeException t) {
          // TODO this should go to a special log for sys admin recovery
          log.error("commit failed for resource "+standardResource, t);
          if (commitException==null) {
            commitException = t;
          }
        }
      }
    }
    
    state = State.COMMITTED;
    afterCompletion();
    if (log.isTraceEnabled()) log.trace("committed "+this);
    
    if (commitException!=null) {
      throw commitException;
    }
  }

  // rollback /////////////////////////////////////////////////////////////////

  public void rollback() {
    if ( (state!=State.ACTIVE)
         && (state!=State.ROLLBACKONLY)
       ) {
      throw new TransactionException("rollback on transaction in state "+state);
    }

    if (log.isTraceEnabled()) log.trace("rolling back "+this);

    beforeCompletion();
    rollbackResources();
  }
  

  void rollbackResources() {
    if (resources!=null) {
      for (StandardResource resource: resources) {
        try {
          if (log.isTraceEnabled()) log.trace("rolling back resource "+resource);
          resource.rollback();
        } catch (Exception e) {
          log.error("rollback failed for resource "+resource);
        }
      }
    }

    state = State.ROLLEDBACK;
    
    afterCompletion();

    if (log.isTraceEnabled()) log.trace("rolled back");
  }
  
  // synchronizations /////////////////////////////////////////////////////////

  public void registerSynchronization(Synchronization synchronization) {
    if (synchronizations==null) {
      synchronizations = new ArrayList<StandardSynchronization>(); 
    }
    synchronizations.add(new StandardSynchronization(synchronization));
  }

  public void afterCompletion() {
    if (synchronizations!=null) {
      for (StandardSynchronization synchronization: synchronizations) {
        synchronization.afterCompletion(state);
      }
    }
  }

  public void beforeCompletion() {
    if (synchronizations!=null) {
      for (StandardSynchronization synchronization: synchronizations) {
        synchronization.beforeCompletion();
      }
    }
  }

  // resource enlisting ///////////////////////////////////////////////////////

  public void enlistResource(StandardResource standardResource) {
    if (resources==null) {
      resources = new ArrayList<StandardResource>();
    }
    log.trace("enlisting resource "+standardResource+" to standard transaction");
    resources.add(standardResource);
  }

  List<StandardResource> getResources() {
    return resources;
  }

  // general methods //////////////////////////////////////////////////////////

  public String toString() {
    return "StandardTransaction["+System.identityHashCode(this)+"]";
  }
}

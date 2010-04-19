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
package org.jbpm.pvm.internal.id;

import java.util.Random;

import org.hibernate.StaleStateException;
import org.jbpm.api.JbpmException;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.cmd.CommandService;


/**
 * @author Tom Baeyens
 */
public class DatabaseDbidGenerator extends DbidGenerator {
  
  private static Log log = Log.getLog(DatabaseDbidGenerator.class.getName());
  
  static Random random = new Random();

  // configuration
  CommandService commandService;
  long blocksize = 10000;
  int maxAttempts = 3;

  // runtime state  
  long lastId = -2;
  long nextId = -1;
  
  public synchronized long getNextId() {
    // if no more ids available
    if (lastId<nextId) {
      // acquire a next block of ids

      log.debug("last id "+lastId+" was consumed.  acquiring new block...");

      // reset the id block
      lastId = -2;
      nextId = -1;
      
      // try couple of times 
      try {
        acquireDbidBlock();
      } catch (Exception e) {
        throw new JbpmException("couldn't acquire block of ids", e);
      }
    }

    return nextId++;
  }

  protected void acquireDbidBlock() {
    for ( int attempts = maxAttempts; (attempts>0) && (nextId==-1) ; attempts-- ) {
      try {
        // acquire block 
        nextId = commandService.execute(new AcquireDbidBlockCmd(blocksize));
        lastId = nextId + blocksize - 1;
        
        log.debug("acquired new id block ["+nextId+"-"+lastId+"]");
        
      } catch (StaleStateException e) {
        // optimistic locking exception indicating another thread tried to 
        // acquire a block of ids concurrently  
        attempts--;
        
        // if no attempts left
        if (attempts==0) {
          // fail the surrounding transaction
          throw new JbpmException("couldn't acquire block of ids, tried "+maxAttempts+" times");
        }
        
        // if there are still attempts left, first wait a bit
        int millis = 20 + random.nextInt(200);
        log.debug("optimistic locking failure while trying to acquire id block.  retrying in "+millis+" millis");
        try {
          Thread.sleep(millis);
        } catch (InterruptedException e1) {
          log.debug("waiting after id block locking failure got interrupted");
        }
      }
    }
  }
  
  public void reset() {
    initialize(); // resetting the DatabaseIdGenerator means just reinitializing the id
  }
  
  public void initialize() {
    nextId = commandService.execute(new InitializePropertiesCmd(blocksize));
    lastId = nextId + blocksize - 1;
    log.debug("initial id block ["+nextId+"-"+lastId+"]");
  }
}

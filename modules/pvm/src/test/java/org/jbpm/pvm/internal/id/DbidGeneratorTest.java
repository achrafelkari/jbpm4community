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

import junit.framework.TestCase;

import org.hibernate.Session;
import org.jbpm.api.Configuration;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.cmd.CommandService;


/**
 * @author Tom Baeyens
 */
public class DbidGeneratorTest extends TestCase {
  
  private static Log log = Log.getLog(DbidGeneratorTest.class.getName());
  
  public void testDbidGenerator() {
    ProcessEngine processEngine = new Configuration().buildProcessEngine();
    
    DbidGenerator dbidGenerator = processEngine.get(DbidGenerator.class);
    
    assertEquals(1, dbidGenerator.getNextId());
    
    processEngine.execute(new Command<Void>() {
      private static final long serialVersionUID = 1L;
      public Void execute(Environment environment) throws Exception {
        Session session = environment.get(Session.class);
        assertEquals("10001", session.createQuery(
                "select property.value " +
                "from "+PropertyImpl.class.getName()+" as property " +
                "where property.key = '"+PropertyImpl.NEXT_DBID_KEY+"'").uniqueResult());
        return null;
      }
    });
    
    for (int i=2; i<10020; i++) {
      assertEquals(i, dbidGenerator.getNextId());
      if ((i%1000) == 0) {
        log.debug("just got dbid "+i+"...");
      }
    }

    try {
      processEngine.execute(new Command<Void>() {
        private static final long serialVersionUID = 1L;
        public Void execute(Environment environment) throws Exception {
          DbidGenerator dbidGenerator = environment.get(DbidGenerator.class);
          for (int i=10020; i<20020; i++) {
            assertEquals(i, dbidGenerator.getNextId());
            if ((i%1000) == 0) {
              log.debug("just got dbid "+i+"...");
            }
          }
          // the following 'user' exception will cause the user transaction to 
          // roll back, but the dbid-transaction should have been committed
          throw new RuntimeException("user exception");
        }
      });
    } catch (RuntimeException e) {
      assertEquals("user exception", e.getMessage());
    }

    for (int i=20020; i<30020; i++) {
      assertEquals(i, dbidGenerator.getNextId());
      if ((i%1000) == 0) {
        log.debug("just got dbid "+i+"...");
      }
    }

    CommandService serviceCommandService = (CommandService) processEngine.get(CommandService.NAME_TX_REQUIRED_COMMAND_SERVICE);
    try {
      serviceCommandService.execute(new Command<Void>() {
        private static final long serialVersionUID = 1L;
        public Void execute(Environment environment) throws Exception {
          DbidGenerator dbidGenerator = environment.get(DbidGenerator.class);
          for (int i=30020; i<40020; i++) {
            assertEquals(i, dbidGenerator.getNextId());
            if ((i%1000) == 0) {
              log.debug("just got dbid "+i+"...");
            }
          }
          // the following 'user' exception will cause the user transaction to 
          // roll back, but the dbid-transaction should have been committed
          throw new RuntimeException("user exception");
        }
      });
    } catch (RuntimeException e) {
      assertEquals("user exception", e.getMessage());
    }

    for (int i=40020; i<50020; i++) {
      assertEquals(i, dbidGenerator.getNextId());
      if ((i%1000) == 0) {
        log.debug("just got dbid "+i+"...");
      }
    }
  }
}

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
package org.jbpm.pvm.tx;

import java.util.List;

import javax.transaction.Synchronization;

import org.hibernate.Session;
import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.cmd.CommandService;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.history.model.HistoryCommentImpl;
import org.jbpm.pvm.internal.history.model.HistoryDetailImpl;
import org.jbpm.pvm.internal.tx.Transaction;
import org.jbpm.test.JbpmTestCase;

/**
 * @author Tom Baeyens
 */
public class BasicTransactionTest extends JbpmTestCase {

  protected CommandService commandService;
  
  public void setUp() throws Exception {
    super.setUp();
    commandService = (CommandService) processEngine.get(CommandService.NAME_TX_REQUIRED_COMMAND_SERVICE);
  }

  public void testCommit() {
    commandService.execute(new Command<Object>() {
      public Object execute(Environment environment) {
        Session session = environment.get(Session.class);
        session.save(new HistoryCommentImpl("if i only had the time to write code"));
        return null;
      }
    });

    commandService.execute(new Command<Object>() {
      public Object execute(Environment environment) {
        Session session = environment.get(Session.class);
        List<HistoryCommentImpl> comments = session.createQuery("from " + HistoryCommentImpl.class.getName()).list();
        assertEquals("if i only had the time to write code", comments.get(0).getMessage());
        session.delete(comments.get(0));
        return null;
      }
    });
  }

  public static class MyOwnRuntimeException extends RuntimeException {
  }

  public void testRollbackRuntimeException() {
    try {
      commandService.execute(new Command<Object>() {

        public Object execute(Environment environment) {
          Session session = environment.get(Session.class);
          session.save(new HistoryCommentImpl("if i only had the time to write code"));
          throw new MyOwnRuntimeException();
        }
      });
      fail("expected exception");
    } catch (MyOwnRuntimeException e) {
      // OK
    }

    commandService.execute(new Command<Object>() {

      public Object execute(Environment environment) {
        Session session = environment.get(Session.class);
        List<HistoryDetailImpl> comments = session.createQuery("from " + HistoryDetailImpl.class.getName()).list();
        assertEquals(0, comments.size());
        return null;
      }
    });
  }

  public static class MyOwnCheckedException extends Exception {
  }

  public void testRollbackCheckedException() {
    try {
      commandService.execute(new Command<Object>() {

        public Object execute(Environment environment) throws Exception {
          Session session = environment.get(Session.class);
          session.save(new HistoryCommentImpl("if i only had the time to write code"));
          throw new MyOwnCheckedException();
        }
      });
      fail("expected exception");
    } catch (JbpmException e) {
      // OK
      assertSame(MyOwnCheckedException.class, e.getCause().getClass());
    }

    commandService.execute(new Command<Object>() {

      public Object execute(Environment environment) {
        Session session = environment.get(Session.class);
        List<HistoryDetailImpl> comments = session.createQuery("from " + HistoryDetailImpl.class.getName()).list();
        assertEquals(0, comments.size());
        return null;
      }
    });
  }

  public static class SuccessfulSynchronization implements Synchronization {

    public void beforeCompletion() {
      Session session = EnvironmentImpl.getFromCurrent(Session.class);
      session.save(new HistoryCommentImpl("b) hello from before completion"));
    }

    public void afterCompletion(int arg0) {
    }
  }

  public void testSuccessfulSynchronization() {
    commandService.execute(new Command<Object>() {
      public Object execute(Environment environment) throws Exception {
        Session session = environment.get(Session.class);
        session.save(new HistoryCommentImpl("a) if i only had the time to write code"));
        Transaction transaction = environment.get(Transaction.class);
        SuccessfulSynchronization successfulSynchronization = new SuccessfulSynchronization();
        transaction.registerSynchronization(successfulSynchronization);
        return null;
      }
    });

    commandService.execute(new Command<Object>() {
      public Object execute(Environment environment) {
        Session session = environment.get(Session.class);
        List<HistoryCommentImpl> comments = session.createQuery(
          "from " + HistoryCommentImpl.class.getName()+" as hc " +
          "order by hc.message asc ").list();
        assertEquals("a) if i only had the time to write code", comments.get(0).getMessage());
        assertEquals("b) hello from before completion", comments.get(1).getMessage());
        session.delete(comments.get(0));
        session.delete(comments.get(1));
        return null;
      }
    });
  }

  public static class UnsuccessfulSynchronization implements Synchronization {

    public void beforeCompletion() {
      Session session = EnvironmentImpl.getFromCurrent(Session.class);
      session.save(new HistoryCommentImpl("b) hello from before completion"));
      throw new MyOwnRuntimeException();
    }

    public void afterCompletion(int arg0) {
    }
  }

  public void testUnsuccessfulSynchronization() {
    try {
      commandService.execute(new Command<Object>() {

        public Object execute(Environment environment) throws Exception {
          Session session = environment.get(Session.class);
          session.save(new HistoryCommentImpl("a) if i only had the time to write code"));
          Transaction transaction = environment.get(Transaction.class);
          UnsuccessfulSynchronization unsuccessfulSynchronization = new UnsuccessfulSynchronization();
          transaction.registerSynchronization(unsuccessfulSynchronization);
          return null;
        }
      });
      fail("expected exception");
    } catch (MyOwnRuntimeException e) {
      // OK
    }

    // the exception in the beforeCompletion in the synchronization should have
    // caused
    // the previous transaction to rollback

    commandService.execute(new Command<Object>() {

      public Object execute(Environment environment) {
        Session session = environment.get(Session.class);
        List<HistoryDetailImpl> comments = session.createQuery("from " + HistoryDetailImpl.class.getName()).list();
        assertEquals(0, comments.size());
        return null;
      }
    });
  }

}

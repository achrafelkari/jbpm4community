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
package org.jbpm.pvm.internal.test;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jbpm.api.Execution;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.job.Job;
import org.jbpm.pvm.internal.cmd.CommandService;
import org.jbpm.pvm.internal.cmd.ExecuteJobCmd;
import org.jbpm.pvm.internal.job.MessageImpl;
import org.jbpm.pvm.internal.job.TimerImpl;


/** helper class for testing that executes messages and timers
 * in the test runner thread.  This way, the test can simulate 
 * timers and messages being executed.  The duedate is ignored.
 * 
 * @author Tom Baeyens
 */
public class JobTestHelper {
  
  CommandService commandService;
  
  public Execution executeMessage(final long executionDbid) {
    return commandService.execute(new Command<Execution>() {
      private static final long serialVersionUID = 1L;
      public Execution execute(Environment environment) throws Exception {
        Session session = environment.get(Session.class);
        Query query = session.createQuery(
            "select m.dbid " +
            "from "+MessageImpl.class.getName()+" as m " +
            "where m.execution.dbid = :executionDbid"
        );
        query.setLong("executionDbid", executionDbid);
        query.setMaxResults(1);
        Long messageDbid = (Long) query.uniqueResult();
        ExecuteJobCmd executeJobCommand = new ExecuteJobCmd(messageDbid); 
        Job job = executeJobCommand.execute(environment);
        return job.getExecution();
      }
    });
  }

  public Execution executeTimer(final long timerDbid) {
    return commandService.execute(new Command<Execution>() {
      private static final long serialVersionUID = 1L;
      public Execution execute(Environment environment) throws Exception {
        Session session = environment.get(Session.class);
        Query query = session.createQuery(
            "select t.dbid " +
            "from "+TimerImpl.class.getName()+" as t " +
            "where t.dbid = :timerDbid"
        );
        query.setLong("timerDbid", timerDbid);
        query.setMaxResults(1);
        Long timerDbid = (Long) query.uniqueResult();
        ExecuteJobCmd executeJobCommand = new ExecuteJobCmd(timerDbid); 
        Job job = executeJobCommand.execute(environment);
        return job.getExecution();
      }
    });
  }
}

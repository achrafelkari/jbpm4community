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
package org.jbpm.test.query;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.history.model.HistoryActivityInstanceImpl;
import org.jbpm.pvm.internal.history.model.HistoryDetailImpl;
import org.jbpm.pvm.internal.history.model.HistoryTaskImpl;
import org.jbpm.pvm.internal.history.model.HistoryVariableImpl;
import org.jbpm.pvm.internal.identity.impl.GroupImpl;
import org.jbpm.pvm.internal.identity.impl.MembershipImpl;
import org.jbpm.pvm.internal.identity.impl.UserImpl;
import org.jbpm.pvm.internal.job.JobImpl;
import org.jbpm.pvm.internal.lob.Lob;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.repository.DeploymentImpl;
import org.jbpm.pvm.internal.repository.DeploymentProperty;
import org.jbpm.pvm.internal.task.ParticipationImpl;
import org.jbpm.pvm.internal.task.SwimlaneImpl;
import org.jbpm.pvm.internal.task.TaskImpl;
import org.jbpm.pvm.internal.type.Variable;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class QueryTest extends JbpmTestCase {

  private static Log log = Log.getLog(QueryTest.class.getName());
  
  public void testQueries() {
    
    deployJpdlXmlString(
      "<process name='TaskCommentDetail'>" +
      "  <start>" +
      "    <transition to='t' />" +
      "  </start>" +
      "  <task name='t' assignee='johndoe'/>" +
      "</process>"
    );

    executionService.startProcessInstanceByKey("TaskCommentDetail");
    executionService.startProcessInstanceByKey("TaskCommentDetail");
    executionService.startProcessInstanceByKey("TaskCommentDetail");
    executionService.startProcessInstanceByKey("TaskCommentDetail");

    
    processEngine.execute(new Command<Object>() {
      private static final long serialVersionUID = 1L;
      public Object execute(Environment environment) throws Exception {
        Session session = environment.get(Session.class);
        
        List<String> persistedTypes = new ArrayList<String>();
        persistedTypes.add(DeploymentImpl.class.getName());
        persistedTypes.add(DeploymentProperty.class.getName());
        persistedTypes.add(ExecutionImpl.class.getName());
        persistedTypes.add(GroupImpl.class.getName());
        persistedTypes.add(HistoryActivityInstanceImpl.class.getName());
        persistedTypes.add(HistoryDetailImpl.class.getName());
        persistedTypes.add(HistoryTaskImpl.class.getName());
        persistedTypes.add(HistoryVariableImpl.class.getName());
        persistedTypes.add(JobImpl.class.getName());
        persistedTypes.add(Lob.class.getName());
        persistedTypes.add(MembershipImpl.class.getName());
        persistedTypes.add(ParticipationImpl.class.getName());
        persistedTypes.add(SwimlaneImpl.class.getName());
        persistedTypes.add(TaskImpl.class.getName());
        persistedTypes.add(UserImpl.class.getName());
        persistedTypes.add(Variable.class.getName());

        for (String persistedType: persistedTypes) {
          try {
            Long typeMaxDbid = (Long) session.createQuery(
                    "select max(o.dbid) " +
                    "from "+persistedType+" as o"
                ).uniqueResult();
            
            log.info(persistedType+": "+typeMaxDbid);
            
          } catch (Exception e) {
            log.info("couldn't get max dbid for "+persistedType, e);
            e.printStackTrace();
          }
        }
        return null;
      }
    });

  }
}

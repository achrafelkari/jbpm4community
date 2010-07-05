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
package org.jbpm.pvm.internal.cmd;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.model.Transition;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.session.DbSession;
import org.jbpm.pvm.internal.task.TaskImpl;

/**
 * @author Tom Baeyens
 */
public class GetOutcomes implements Command<Set<String>> {

  private static final long serialVersionUID = 1L;
  
  protected String taskId;
  
  public GetOutcomes(String taskId) {
    if (taskId==null) {
      throw new JbpmException("taskId is null");
    }
    this.taskId = taskId;
  }

  public Set<String> execute(Environment environment) {
    DbSession dbSession = environment.get(DbSession.class);
    TaskImpl task = dbSession.get(TaskImpl.class, Long.parseLong(taskId));
    if (task==null) {
      throw new JbpmException("task "+taskId+" doesn't exist");
    }

    ExecutionImpl execution = task.getExecution();
    if (execution != null) {
      ActivityImpl activity = execution.getActivity();

      if (activity != null) {
        List<? extends Transition> outgoingTransitions = activity.getOutgoingTransitions();

        if (outgoingTransitions!=null && !outgoingTransitions.isEmpty()) {
          Set<String> outcomes = new HashSet<String>();
          for (Transition transition: outgoingTransitions) {
            outcomes.add(transition.getName());
          }
          return outcomes;
        } 
      }
    }

    return Collections.emptySet();
  }
}

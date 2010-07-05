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

import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.model.ActivityCoordinates;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;
import org.jbpm.pvm.internal.session.RepositorySession;


/**
 * @author Tom Baeyens
 */
public class GetActivityCoordinatesCmd implements Command<ActivityCoordinates> {

  private static final long serialVersionUID = 1L;

  String processDefinitionId;
  String activityName;
  
  public GetActivityCoordinatesCmd(String processDefinitionId, String activityName) {
    this.processDefinitionId = processDefinitionId;
    this.activityName = activityName;
  }

  public ActivityCoordinates execute(Environment environment) throws Exception {
    RepositorySession repositorySession = environment.get(RepositorySession.class);
    ProcessDefinitionImpl processDefinition = repositorySession.findProcessDefinitionById(processDefinitionId);
    if (processDefinition==null) {
      throw new JbpmException("process definition "+processDefinitionId+" doesn't exist");
    }
    ActivityImpl activity = processDefinition.findActivity(activityName);
    if (activity==null) {
      throw new JbpmException("activity '"+activityName+"' doesn't exist in process definition "+processDefinitionId);
    }
    return activity.getCoordinates();
  }
}

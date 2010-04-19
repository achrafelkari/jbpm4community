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
package org.jbpm.pvm.internal.svc;

import org.jbpm.api.JobQuery;
import org.jbpm.api.ManagementService;
import org.jbpm.pvm.internal.cmd.CreateJobQueryCmd;
import org.jbpm.pvm.internal.cmd.DeleteJobCmd;
import org.jbpm.pvm.internal.cmd.ExecuteJobCmd;
import org.jbpm.pvm.internal.query.JobQueryImpl;


/**
 * @author Tom Baeyens
 */
public class ManagementServiceImpl extends AbstractServiceImpl implements ManagementService {

  public void executeJob(String jobId) {
    commandService.execute(new ExecuteJobCmd(jobId));
  }

  public JobQuery createJobQuery() {
    JobQueryImpl query = commandService.execute(new CreateJobQueryCmd());
    query.setCommandService(commandService);
    return query;
  }
  
  public boolean deleteJob(long jobId) {
    return commandService.execute(new DeleteJobCmd(jobId));
  }
}

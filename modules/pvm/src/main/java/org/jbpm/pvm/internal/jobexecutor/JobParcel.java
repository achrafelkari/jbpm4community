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
package org.jbpm.pvm.internal.jobexecutor;

import java.util.Collection;

import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.cmd.CommandService;
import org.jbpm.pvm.internal.cmd.ExecuteJobCmd;

/**
 * @author Alejandro Guizar
 */
public class JobParcel implements Runnable {

  private static final Log log = Log.getLog(JobParcel.class.getName());

  private final CommandService commandService;
  private final Collection<Long> jobDbids;

  public JobParcel(CommandService commandService, Collection<Long> jobDbids) {
    this.commandService = commandService;
    this.jobDbids = jobDbids;
  }

  public void run() {
    for (Long jobDbid : jobDbids) {
      try {
        commandService.execute(new ExecuteJobCmd(jobDbid));
      } catch (RuntimeException e) {
        log.error("failed to execute job: " + jobDbid, e);
      }
    }
  }

  @Override
  public String toString() {
    return JobParcel.class.getSimpleName() + jobDbids;
  }
}

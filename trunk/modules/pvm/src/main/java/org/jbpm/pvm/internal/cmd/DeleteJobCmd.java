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
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.job.JobImpl;
import org.jbpm.pvm.internal.session.DbSession;

/**
 * @author Joram Barrez
 */
public class DeleteJobCmd extends AbstractCommand<Boolean>{
  
  private static final long serialVersionUID = 1L;
  
  protected long jobId;
  
  public DeleteJobCmd(long jobId) {
    this.jobId = jobId;
  }
  
  public Boolean execute(Environment environment) throws Exception {
    DbSession dbSession = environment.get(DbSession.class);
    if (dbSession==null) {
      throw new JbpmException("no " + DbSession.class.getName() + " found in environment");
    }
    JobImpl job = dbSession.get(JobImpl.class, jobId);
    if (job != null) {
      dbSession.delete(job);
      return true;
    } 
    return false;
  }

}

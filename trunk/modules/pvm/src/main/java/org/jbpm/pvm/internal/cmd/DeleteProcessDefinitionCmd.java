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
import org.jbpm.pvm.internal.session.DbSession;
import org.jbpm.pvm.internal.wire.binding.DbSessionBinding;


/**
 * @author Tom Baeyens
 */
public class DeleteProcessDefinitionCmd extends AbstractCommand<Void> {

  private static final long serialVersionUID = 1L;
  
  String processDefinitionId;
  boolean deleteProcessInstances;
  boolean deleteHistory;
 
  public DeleteProcessDefinitionCmd(String processDefinitionId) {
    this(processDefinitionId, false, false);
  }

  public DeleteProcessDefinitionCmd(String processDefinitionId, boolean deleteProcessInstances, boolean deleteHistory) {
    this.processDefinitionId = processDefinitionId;
    this.deleteProcessInstances = deleteProcessInstances;
    this.deleteHistory = deleteHistory;
  }

  public Void execute(Environment environment) {
    DbSession dbSession = environment.get(DbSession.class);
    if (dbSession==null) {
      throw new JbpmException("no "+DbSessionBinding.TAG+" configured");
    }
    dbSession.deleteProcessDefinition(processDefinitionId, deleteProcessInstances, deleteHistory);
    return null;
  }
}

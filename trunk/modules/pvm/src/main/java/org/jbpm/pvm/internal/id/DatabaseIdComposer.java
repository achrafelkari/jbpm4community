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

import org.jbpm.api.Execution;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.model.ExecutionImpl;


/**
 * @author Tom Baeyens
 */
public class DatabaseIdComposer extends IdComposer {
  
  private static Log log = Log.getLog(DatabaseIdComposer.class.getName());

  public String createId(ProcessDefinition processDefinition, Execution parent, ExecutionImpl execution) {

    String base = null;
    if (parent!=null) {
      base = parent.getId(); 
    } else if (processDefinition.getKey()!=null){
      base = processDefinition.getKey();
    } else {
      base = processDefinition.getId();
    }

    String executionPart = null;
    if ( (parent==null) 
         && (execution.getKey()!=null) 
       ) {
      executionPart = execution.getKey();
    
    } else if (execution.getName()!=null) {
      executionPart = execution.getName() + '.' + Long.toString(execution.getDbid());
      
    } else {
      executionPart = Long.toString(execution.getDbid());
    }

    String executionId = base+"."+executionPart;
    
    if (log.isDebugEnabled()) log.debug("generated execution id "+executionId);
    
    return executionId;
  }
}

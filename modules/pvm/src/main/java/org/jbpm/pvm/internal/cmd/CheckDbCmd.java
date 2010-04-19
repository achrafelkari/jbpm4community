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

import org.hibernate.Session;
import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.id.PropertyImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.processengine.ProcessEngineImpl;

/**
 * @author Tom Baeyens
 */
public class CheckDbCmd implements Command<Object> {

  private static final long serialVersionUID = 1L;
  
  private static Log log = Log.getLog(CheckDbCmd.class.getName());

  public Object execute(Environment environment) throws Exception {
    Session session = EnvironmentImpl.getFromCurrent(Session.class);

    // if table JBPM4_PROPERTIES doesn't exist, 
    if (!PropertyImpl.propertiesTableExists(session)) {
      if (!executionTableExists(session)) {
        // tell users to run create.schema
        throw new JbpmException("no jBPM DB schema: no JBPM4_EXECUTION table.   Run the create.jbpm.schema target first in the install tool.");
        
      } else {
        // tell users to run upgrade
        throw new JbpmException("jBPM DB schema not in sync with library version: no JBPM4_PROPERTIES table.   Run the upgrade target first in the install tool.");
      }
      
    } else {
      Long nextDbid = PropertyImpl.getNextDbid(session);
      // if there is no next.dbid property specified 
      if (nextDbid==null) {
        // (this only happens in the test suite)
        // initialize the dbid property.  
        PropertyImpl.setNextDbid(session, 1);
      }
    }

    // verify if DB version matches with library version,
    String dbVersion = PropertyImpl.getDbVersion(session);
    log.info("jBPM version info: library["+ProcessEngineImpl.JBPM_LIBRARY_VERSION+"], schema["+dbVersion+"]");

    if ( (dbVersion!=null) 
         && (!dbVersion.equals(ProcessEngineImpl.JBPM_LIBRARY_VERSION))
       ) {
      // tell users to run upgrade
      throw new JbpmException("jBPM DB schema version ("+dbVersion+") differs from jBPM library version ("+ProcessEngineImpl.JBPM_LIBRARY_VERSION+"): run the upgrade tool first.");
    }

    return null;
  }
  
  public static boolean executionTableExists(Session session) {
    try {
      session.createQuery("from "+ExecutionImpl.class.getName())
        .setMaxResults(1)
        .uniqueResult();
      return true;
      
    } catch (Exception e) {
      return false;
    }
  }



}

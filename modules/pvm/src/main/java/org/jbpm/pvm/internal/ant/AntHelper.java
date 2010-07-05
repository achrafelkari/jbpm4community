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
package org.jbpm.pvm.internal.ant;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.api.Configuration;
import org.jbpm.api.ProcessEngine;

/**
 * common strategy for jbpm ant tasks to obtain a hibernate SessionFactory.
 */
public abstract class AntHelper {

  final static Map<String, ProcessEngine> processEngines = new HashMap<String, ProcessEngine>();

  private static final Log log = LogFactory.getLog(AntHelper.class);

  public static ProcessEngine getProcessEngine(String jbpmCfgXml) {
    ProcessEngine processEngine = processEngines.get(jbpmCfgXml);
    if (processEngine == null) {
      Configuration configuration = new Configuration();
      if (jbpmCfgXml!=null) {
        log.debug("using jbpm configuration "+jbpmCfgXml);
        configuration.setResource(jbpmCfgXml);
      } else {
        log.debug("using default jbpm.cfg.xml configuration");
      }
      
      processEngine = configuration.buildProcessEngine();

      processEngines.put(jbpmCfgXml, processEngine);
    }
    return processEngine;
  }
}

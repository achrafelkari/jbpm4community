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
package org.jbpm.test;

import org.jbpm.api.Configuration;
import org.jbpm.api.RepositoryService;



/**
 * @author Tom Baeyens
 */
public class JbpmCustomCfgTestCase extends JbpmTestCase {

  
  protected synchronized void initialize() {
    String cfgResource = getClass().getPackage().getName().replace(".", "/")+"/jbpm.cfg.xml";
    
    // We can't call initialize(String, String) here, since it will do a null
    // check on the statically cached process engine. Since the test-cfg
    // is meant to test different configs (and hence different process engines)
    // this caching is unwanted, which means we create the process engine
    // ourselves here.
    
    if (log.isDebugEnabled()) {
      log.debug("building ProcessEngine from resource " + cfgResource);
    }

    Configuration configuration = new Configuration().setResource(cfgResource);

    processEngine = configuration.buildProcessEngine();
    
    repositoryService = processEngine.get(RepositoryService.class);
    executionService = processEngine.getExecutionService();
    historyService = processEngine.getHistoryService();
    managementService = processEngine.getManagementService();
    taskService = processEngine.getTaskService();
    identityService = processEngine.getIdentityService();
  }
  


}

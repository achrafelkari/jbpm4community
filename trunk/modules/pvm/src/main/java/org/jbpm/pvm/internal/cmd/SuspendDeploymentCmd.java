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
import org.jbpm.pvm.internal.repository.DeploymentImpl;
import org.jbpm.pvm.internal.repository.RepositoryCache;
import org.jbpm.pvm.internal.session.RepositorySession;


/**
 * @author Tom Baeyens
 */
public class SuspendDeploymentCmd implements Command<Object> {
  
  private static final long serialVersionUID = 1L;
  
  String deploymentId;

  public SuspendDeploymentCmd(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public Object execute(Environment environment) throws Exception {
    RepositorySession repositorySession = environment.get(RepositorySession.class);
    
    DeploymentImpl deployment = repositorySession.getDeployment(deploymentId);
    if (deployment==null) {
      throw new JbpmException("deployment "+deploymentId+" doesn't exist");
    }
    
    deployment.suspend();
    
    // removing deployment from the cache
    // next time it's used, it will be redeployed
    // at that time, the suspended property will be propagated to the 
    // process definitions
    RepositoryCache repositoryCache = environment.get(RepositoryCache.class);
    repositoryCache.remove(deploymentId);
    
    return null;
  }

}

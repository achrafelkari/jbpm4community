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
package org.jbpm.pvm.internal.repository;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.api.JbpmException;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.xml.Problem;


/**
 * @author Tom Baeyens
 */
public class DeployerManager {
  
  private static Log LOG = Log.getLog(DeployerManager.class.getName());
  
  List<Deployer> deployers;
  
  public void deploy(DeploymentImpl deployment) {
    deployment.setProblems(new ArrayList<Problem>());

    for (Deployer deployer: deployers) {
      deployer.deploy(deployment);
    }
    
    if (deployment.hasErrors()) {
      JbpmException jbpmException = deployment.getJbpmException();
      LOG.info("errors during deployment of "+deployment+": "+jbpmException.getMessage());
      throw  jbpmException;
    }
    
    RepositoryCache repositoryCache = EnvironmentImpl.getFromCurrent(RepositoryCache.class);
    
    if (repositoryCache != null) {
      
      // If there are no objects after deploying, then there is something wrong
      if (deployment.getObjects() == null || deployment.getObjects().isEmpty()) {
        
        if (LOG.isWarnEnabled()) {
          LOG.warn("WARNING: no objects were deployed! Check if you have configured a correct deployer " 
                  +"in your jbpm.cfg.xml file for the type of deployment you want to do.");
        }
        
      } else {
        
        repositoryCache.set(deployment.getId(), deployment.getObjects());
        
      }
    }
  }

  public void updateResource(DeploymentImpl deployment, String resourceName, byte[] bytes) {
    for (Deployer deployer: deployers) {
      deployer.updateResource(deployment, resourceName, bytes);
    }
    
    RepositoryCache repositoryCache = EnvironmentImpl.getFromCurrent(RepositoryCache.class);
    repositoryCache.remove(deployment.getId());
  }
}

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

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.jbpm.pvm.internal.env.EnvironmentImpl;


/**
 * @author Tom Baeyens
 */
public class RulesDeployer implements Deployer {

  public static final String CACHEKEY_KNOWLEDGEBASE = "KnowledgeBase";

  public void deploy(DeploymentImpl deployment) {
    KnowledgeBuilder knowledgeBuilder = null;

    for (String resourceName: deployment.getResourceNames()) {
      if (resourceName.endsWith(".drl")) {
        if (knowledgeBuilder==null) {
          knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        }
        
        byte[] drlBytes = deployment.getBytes(resourceName);

        knowledgeBuilder.add(ResourceFactory.newByteArrayResource(drlBytes), ResourceType.DRL);
      }
    }
    
    if (knowledgeBuilder!=null) {
      if (knowledgeBuilder.hasErrors()) {
        for (KnowledgeBuilderError error: knowledgeBuilder.getErrors()) {
          deployment.addProblem("drl problem: "+error.getMessage());
        }
      } else {
        KnowledgeBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
        knowledgeBase.addKnowledgePackages(knowledgeBuilder.getKnowledgePackages());
        
        RepositoryCache repositoryCache = EnvironmentImpl.getFromCurrent(RepositoryCache.class);
        repositoryCache.set(deployment.getId(), CACHEKEY_KNOWLEDGEBASE, knowledgeBase);
      }
    }
  }
  
  public static KnowledgeBase getKnowledgeBase(String deploymentId) {
    return getKnowledgeBase(deploymentId, CACHEKEY_KNOWLEDGEBASE);
  }

  public static KnowledgeBase getKnowledgeBase(String deploymentId, String knowledgeBaseName) {
    RepositoryCache repositoryCache = EnvironmentImpl.getFromCurrent(RepositoryCache.class);
    return (KnowledgeBase) repositoryCache.get(deploymentId, knowledgeBaseName);
  }

  public void updateResource(DeploymentImpl deployment, String resourceName, byte[] bytes) {
    if (resourceName.endsWith(".drl")) {
      throw new UnsupportedOperationException("drl resource updates is not implemented yet.  please contribute :-)");
    }
  }
}

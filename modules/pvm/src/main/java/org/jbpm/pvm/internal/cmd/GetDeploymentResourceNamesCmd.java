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

import java.util.Set;

import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.repository.DeploymentImpl;
import org.jbpm.pvm.internal.session.RepositorySession;

/**
 * Command used to retrieve all the resource names of a deployment. The
 * resources itself can be retrieved using other commands (eg
 * {@link GetResourceAsStreamCmd}).
 * 
 * @author jbarrez
 */
public class GetDeploymentResourceNamesCmd implements Command<Set<String>> {

  private static final long serialVersionUID = 1L;

  private String deploymentId;

  public GetDeploymentResourceNamesCmd(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public Set<String> execute(Environment environment) throws Exception {
    RepositorySession repositorySession = environment.get(RepositorySession.class);
    DeploymentImpl depImpl = repositorySession.getDeployment(deploymentId);
    return depImpl.getResourceNames();
  }

}

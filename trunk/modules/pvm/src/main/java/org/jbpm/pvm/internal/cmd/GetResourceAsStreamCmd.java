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

import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.session.RepositorySession;


/**
 * @author Tom Baeyens
 */
public class GetResourceAsStreamCmd extends AbstractCommand<byte[]> {

  private static final long serialVersionUID = 1L;
  
  protected String deploymentId;
  protected String resourceName;

  public GetResourceAsStreamCmd(String deploymentId, String resourceName) {
    this.deploymentId = deploymentId;
    this.resourceName = resourceName;
  }

  public byte[] execute(Environment environment) {
    RepositorySession repositorySession = environment.get(RepositorySession.class);
    return repositorySession.getBytes(deploymentId, resourceName);
  }
}

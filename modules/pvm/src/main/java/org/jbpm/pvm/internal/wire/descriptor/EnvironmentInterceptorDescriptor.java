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
package org.jbpm.pvm.internal.wire.descriptor;

import org.jbpm.pvm.internal.cfg.ConfigurationImpl;
import org.jbpm.pvm.internal.env.EnvironmentFactory;
import org.jbpm.pvm.internal.svc.EnvironmentInterceptor;
import org.jbpm.pvm.internal.svc.Policy;
import org.jbpm.pvm.internal.wire.WireContext;

/**
 * @author Tom Baeyens
 */
public class EnvironmentInterceptorDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;
  
  protected ConfigurationImpl configuration;
  protected Policy policy;
  
  public Object construct(WireContext wireContext) {
    EnvironmentFactory environmentFactory = (EnvironmentFactory) configuration.getProducedProcessEngine();
    EnvironmentInterceptor environmentInterceptor = new EnvironmentInterceptor();
    environmentInterceptor.setEnvironmentFactory(environmentFactory);
    if (policy!=null) {
      environmentInterceptor.setPolicy(policy);
    }
    return environmentInterceptor;
  }

  public void setPolicy(Policy policy) {
    this.policy = policy;
  }
  public void setConfiguration(ConfigurationImpl configuration) {
    this.configuration = configuration;
  }
}

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
package org.jbpm.pvm.test;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.api.Configuration;
import org.jbpm.api.JbpmException;
import org.jbpm.pvm.internal.env.EnvironmentFactory;
import org.jbpm.test.BaseJbpmTestCase;

/**
 * @author Tom Baeyens
 */
public abstract class EnvironmentFactoryTestCase extends BaseJbpmTestCase {

  private String configResource;

  private static Map<String, EnvironmentFactory> environmentFactories = new HashMap<String, EnvironmentFactory>();

  public EnvironmentFactoryTestCase() {
    this("jbpm.cfg.xml");
  }

  public EnvironmentFactoryTestCase(String configResource) {
    this.configResource = configResource;
  }

  public EnvironmentFactory getEnvironmentFactory() {
    EnvironmentFactory environmentFactory = environmentFactories.get(configResource);
    if (environmentFactory == null) {
      environmentFactory = createEnvironmentFactory(configResource);
    }
    return environmentFactory;
  }

  private EnvironmentFactory createEnvironmentFactory(String configResource) {
    try {
      log.debug("creating environment factory for [" + configResource + "]");
      EnvironmentFactory newEnvironmentFactory = (EnvironmentFactory) new Configuration()
        .setResource(configResource)
        .buildProcessEngine();
      environmentFactories.put(configResource, newEnvironmentFactory);
      return newEnvironmentFactory;
    }
    catch (Exception e) {
      throw new JbpmException("Exception during creation of environment factory for "
        + configResource, e);
    }
  }

}

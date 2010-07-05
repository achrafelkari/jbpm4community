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
package org.jbpm.pvm.internal.processengine;

import org.hibernate.cfg.Configuration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

import org.jbpm.api.ProcessEngine;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.cfg.ConfigurationImpl;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.env.PvmEnvironment;
import org.jbpm.pvm.internal.env.SpringContext;
import org.jbpm.pvm.internal.wire.descriptor.ProvidedObjectDescriptor;

/**
 * this environment factory will see only the singleton beans.
 *
 * The created {@link SpringEnvironment}s will see the prototype beans and it
 * will cache them.
 *
 * @author Andries Inze
 */
public class SpringProcessEngine extends ProcessEngineImpl {

  private static final Log log = Log.getLog(SpringProcessEngine.class.getName());

  private static final long serialVersionUID = 1L;

  private ApplicationContext applicationContext;

  public static ProcessEngine create(ConfigurationImpl configuration) {
    SpringProcessEngine springProcessEngine = null;

    ApplicationContext applicationContext = null;
    if (configuration.isInstantiatedFromSpring()) {
      applicationContext = (ApplicationContext) configuration.getApplicationContext();

      springProcessEngine = new SpringProcessEngine();
      springProcessEngine.applicationContext = applicationContext;
      springProcessEngine.initializeProcessEngine(configuration);

      LocalSessionFactoryBean localSessionFactoryBean = springProcessEngine.get(LocalSessionFactoryBean.class);
      Configuration hibernateConfiguration = localSessionFactoryBean.getConfiguration();
      springProcessEngine.processEngineWireContext
          .getWireDefinition()
          .addDescriptor(new ProvidedObjectDescriptor(hibernateConfiguration, true));

      springProcessEngine.checkDb(configuration);

    } else {
      String springCfg = (String) configuration.getProcessEngineWireContext().get("spring.cfg");
      if (springCfg==null) {
        springCfg = "applicationContext.xml";
      }
      applicationContext = new ClassPathXmlApplicationContext(springCfg);
      springProcessEngine = (SpringProcessEngine) applicationContext.getBean("processEngine");
    }

    return springProcessEngine;
  }

  public EnvironmentImpl openEnvironment() {
    PvmEnvironment environment = new PvmEnvironment(this);

    if (log.isTraceEnabled())
      log.trace("opening jbpm-spring" + environment);

    environment.setContext(new SpringContext(applicationContext));

    installAuthenticatedUserId(environment);
    installProcessEngineContext(environment);
    installTransactionContext(environment);

    return environment;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(Class<T> type) {
    T candidateComponent = super.get(type);

    if (candidateComponent != null) {
      return candidateComponent;
    }

    String[] names = applicationContext.getBeanNamesForType(type);

    if (names.length >= 1) {

      if (names.length > 1 && log.isWarnEnabled()) {
        log.warn("Multiple beans for type " + type + " found. Returning the first result.");
      }

      return (T) applicationContext.getBean(names[0]);
    }

    return null;
  }

  @Override
  public Object get(String key) {
    if (applicationContext.containsBean(key)) {
      return applicationContext.getBean(key);
    }

    return super.get(key);
  }
}

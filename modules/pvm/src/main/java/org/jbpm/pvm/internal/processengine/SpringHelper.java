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

import org.jbpm.api.ProcessEngine;
import org.jbpm.pvm.internal.cfg.ConfigurationImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Joram Barrez
 * @author Huisheng Xu
 */
public class SpringHelper implements ApplicationContextAware, DisposableBean {

  protected ApplicationContext applicationContext;
  protected String jbpmCfg = "jbpm.cfg.xml";
  protected ProcessEngine processEngine;

  public void setJbpmCfg(String jbpmCfg) {
    this.jbpmCfg = jbpmCfg;
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public ProcessEngine createProcessEngine() {
    processEngine = new ConfigurationImpl()
      .springInitiated(applicationContext)
      .setResource(jbpmCfg)
      .buildProcessEngine();

    return processEngine;
  }

  /**
   * close process engine when spring close/refresh ctx.
   */
  public void destroy() throws Exception {
    if (processEngine != null) {
      processEngine.close();
      processEngine = null;
    }
  }

}

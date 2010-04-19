/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jbpm.jboss.internal;

import org.jbpm.api.Configuration;
import org.jbpm.api.ProcessEngine;
import org.jbpm.internal.log.Log;

/**
 * @author Tom Baeyens
 */
public class JbpmService {

  private static final Log log = Log.getLog(JbpmService.class.getName());

  private ProcessEngine processEngine;

  public void start() {
    log.debug("JbpmService starting...");
    this.processEngine = Configuration.getProcessEngine();
    log.info("JbpmService started");
  }

  public void stop() {
    this.processEngine.close();
    log.info("JbpmService stopped");
  }

  public ProcessEngine getProcessEngine() {
    return this.processEngine;
  }

  public void setDataSource(Object dataSource) {
  }
}

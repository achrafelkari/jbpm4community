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
package org.jbpm.pvm.internal.env;

import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.processengine.ProcessEngineImpl;
import org.jbpm.pvm.internal.util.Closable;


/**
 * @author Tom Baeyens
 */
public class PvmEnvironment extends BasicEnvironment {

  private static final long serialVersionUID = 1L;
  
  private static final Log log = Log.getLog(PvmEnvironment.class.getName()); 

  protected ProcessEngineImpl processEngineImpl;

  public PvmEnvironment(ProcessEngineImpl processEngineImpl) {
    this.processEngineImpl = processEngineImpl;
  }

  public String toString() {
    return "PvmEnvironment["+System.identityHashCode(this)+"]";
  }
  public void close() {
    Context context = getEnvironmentContext();
    if (context instanceof Closable) {
      ((Closable)context).close();
    }
    super.close();
    if (log.isTraceEnabled()) log.trace("closed "+this);
  }

}

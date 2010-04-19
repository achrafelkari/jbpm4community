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

import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireException;


/**
 * @author Tom Baeyens
 */
public class EnvDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;
  private static final Log log = Log.getLog(EnvDescriptor.class.getName());
  
  protected String objectName;
  protected String typeName;
  protected Class<?> type;

  public EnvDescriptor() {
  }

  public EnvDescriptor(Class<?> type) {
    this.type = type;
  }

  public Object construct(WireContext wireContext) {
    EnvironmentImpl environment = EnvironmentImpl.getCurrent();
    
    if (environment==null) {
      throw new WireException("no environment to get object "+(objectName!=null ? objectName : typeName));
    }
    
    if (objectName!=null) {
      log.trace("looking up "+objectName+" by name in environment");
      return environment.get(objectName);
    }

    log.trace("looking up an object of type "+typeName+" in environment");
    if (type==null) {
      try {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        type = classLoader.loadClass(typeName); 
      } catch (Exception e) {
        throw new WireException("couldn't load class "+typeName, e);
      }
    }
    return environment.get(type);
  }

  public void setObjectName(String objectName) {
    this.objectName = objectName;
  }
  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }
  public void setType(Class< ? > type) {
    this.type = type;
  }
}

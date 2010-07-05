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

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.util.Listener;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireDefinition;
import org.jbpm.pvm.internal.wire.WireException;

/**
 * @author Tom Baeyens
 */
public class HibernateSessionFactoryDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;
  static final Log log = Log.getLog(HibernateSessionFactoryDescriptor.class.getName());
  
  String configurationName;
  Descriptor configurationDescriptor;
  
  public Object construct(WireContext wireContext) {
    Configuration configuration = null;

    if (configurationName!=null) {
      configuration = (Configuration) wireContext.get(configurationName); 

    } else if (configurationDescriptor!=null) {
      configuration = (Configuration) wireContext.create(configurationDescriptor, false);
      
    } else {
      configuration = wireContext.get(Configuration.class);
    }
    
    if (configuration==null) {
      throw new WireException("couldn't find configuration");
    }

    SessionFactory sessionFactory = configuration.buildSessionFactory();
    
    wireContext.addListener(new SessionFactoryCloser(sessionFactory));

    return sessionFactory;
  }
  
  public static class SessionFactoryCloser implements Listener {
    SessionFactory sessionFactory;
    public SessionFactoryCloser(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
    }
    public void event(Object source, String eventName, Object info) {
      if (WireContext.EVENT_CLOSE.equals(eventName)) {
        log.trace("closing hibernate session factory");
        sessionFactory.close();
      }
    }
  }

  
  public Class<?> getType(WireDefinition wireDefinition) {
    return SessionFactory.class;
  }

  public void setConfigurationName(String configurationName) {
    this.configurationName = configurationName;
  }
  public void setConfigurationDescriptor(Descriptor configurationDescriptor) {
    this.configurationDescriptor = configurationDescriptor;
  }
}

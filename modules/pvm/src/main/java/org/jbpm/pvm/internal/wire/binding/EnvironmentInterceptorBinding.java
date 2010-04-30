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
package org.jbpm.pvm.internal.wire.binding;

import org.jbpm.pvm.internal.cfg.ConfigurationImpl;
import org.jbpm.pvm.internal.env.EnvironmentFactory;
import org.jbpm.pvm.internal.processengine.ProcessEngineImpl;
import org.jbpm.pvm.internal.svc.Policy;
import org.jbpm.pvm.internal.wire.descriptor.EnvironmentInterceptorDescriptor;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;
import org.w3c.dom.Element;

/**
 * @author Tom Baeyens
 */
public class EnvironmentInterceptorBinding extends WireInterceptorBinding {
    
  public EnvironmentInterceptorBinding() {
    super("environment-interceptor");
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    EnvironmentInterceptorDescriptor environmentInterceptorDescriptor = new EnvironmentInterceptorDescriptor();

    ConfigurationImpl configuration = (ConfigurationImpl) parse.contextStackFind(ConfigurationImpl.class);
    environmentInterceptorDescriptor.setConfiguration(configuration);
    
    if ( element.hasAttribute("policy")
         && ("requiresNew".equals(element.getAttribute("policy")))
       ) {
      environmentInterceptorDescriptor.setPolicy(Policy.REQUIRES_NEW);
    }
    
    return environmentInterceptorDescriptor;
  }

}
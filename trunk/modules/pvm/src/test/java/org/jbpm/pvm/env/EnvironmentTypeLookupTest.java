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
package org.jbpm.pvm.env;

import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.env.EnvironmentFactory;
import org.jbpm.pvm.internal.processengine.ProcessEngineImpl;
import org.jbpm.test.BaseJbpmTestCase;

/**
 * @author Tom Baeyens
 */
public class EnvironmentTypeLookupTest extends BaseJbpmTestCase {

  public static class A {
  }
  
  public void testApplicationTypeLookup() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<jbpm-configuration>" +
      "  <process-engine-context>" +
      "    <object class='"+A.class.getName()+"' />" +
      "  </process-engine-context>" +
      "  <transaction-context>" +
      "    <string name='a' value='distraction' />" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );
    
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      
      A a = environment.get(A.class);
      assertNotNull(a);
      
    } finally {
      environment.close();
    }
  }


  public void testBlockTypeLookup() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<jbpm-configuration>" +
      "  <process-engine-context>" +
      "    <string name='a' value='distraction' />" +
      "  </process-engine-context>" +
      "  <transaction-context>" +
      "    <object class='"+A.class.getName()+"' />" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );
    
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      
      A a = environment.get(A.class);
      assertNotNull(a);
      
    } finally {
      environment.close();
    }
  }


  public void testNonExistingTypeLookup() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<jbpm-configuration>" +
      "  <process-engine-context>" +
      "    <string name='a' value='A' />" +
      "  </process-engine-context>" +
      "  <transaction-context>" +
      "    <string name='b' value='B' />" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );
    
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      
      assertNull(environment.get(Thread.class));
      
    } finally {
      environment.close();
    }
  }

}

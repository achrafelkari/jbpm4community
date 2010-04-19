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
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireDefinition;
import org.jbpm.pvm.internal.wire.xml.WireParser;
import org.jbpm.test.BaseJbpmTestCase;

/**
 * @author Tom Baeyens
 */
public class EnvironmentSearchOrderTest extends BaseJbpmTestCase
{

  public void testEnvironmentDefaultSearchOrder() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<jbpm-configuration>" +
      "  <process-engine-context>" +
      "    <string name='a' value='process-engine-a' />" +
      "  </process-engine-context>" +
      "  <transaction-context>" +
      "    <string name='a' value='environment-a' />" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );
    
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      
      assertEquals("environment-a", environment.get("a"));
      
    } finally {
      environment.close();
    }
  }

  public void testEnvironmentGivenSearchOrder() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<jbpm-configuration>" +
      "  <process-engine-context>" +
      "    <string name='a' value='process-engine-a' />" +
      "  </process-engine-context>" +
      "  <transaction-context>" +
      "    <string name='a' value='environment-a' />" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );
    
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      
      // only search in context 'process-engine' and then in 'environment'
      String[] searchOrder = new String[]{"process-engine", "environment"};
      assertEquals("process-engine-a", environment.get("a", searchOrder));
      
    } finally {
      environment.close();
    }
  }

  public void testEnvironmentGivenSearchOrderUnexistingObject() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<jbpm-configuration>" +
      "  <process-engine-context>" +
      "    <string name='a' value='process-engine-a' />" +
      "  </process-engine-context>" +
      "  <transaction-context>" +
      "    <string name='a' value='environment-a' />" +
      "    <string name='b' value='environment-b' />" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );
    
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      
      // only search in context 'process-engine'
      String[] searchOrder = new String[]{"process-engine"};
      assertNull(environment.get("b", searchOrder));
      
    } finally {
      environment.close();
    }
  }


  public void testEnvironmentDefaultSearchOrderWithAddedContext() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<jbpm-configuration>" +
      "  <process-engine-context>" +
      "    <string name='a' value='process-engine-a' />" +
      "  </process-engine-context>" +
      "  <transaction-context>" +
      "    <string name='a' value='environment-a' />" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );
    
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {

      // create a new context
      WireDefinition wireDefinition = WireParser.parseXmlString(
        "<objects>" +
        "  <string name='a' value='added-a' />" +
        "</objects>"
      );
      WireContext addedContext = new WireContext(wireDefinition, "added");
      
      // add the new context to the enviromnent
      environment.setContext(addedContext);
      
      // see what you find under key a in the default search order
      assertEquals("added-a", environment.get("a"));
      
    } finally {
      environment.close();
    }
  }

  public void testEnvironmentGivenSearchOrderWithAddedContext() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<jbpm-configuration>" +
      "  <process-engine-context>" +
      "    <string name='a' value='process-engine-a' />" +
      "  </process-engine-context>" +
      "  <transaction-context>" +
      "    <string name='a' value='environment-a' />" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );
    
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {

      // create a new context
      WireDefinition wireDefinition = WireParser.parseXmlString(
        "<objects>" +
        "  <string name='a' value='added-a' />" +
        "</objects>"
      );
      WireContext addedContext = new WireContext(wireDefinition, "added");
      
      // add the new context to the enviromnent
      environment.setContext(addedContext);
      
      // only search in context 'process-engine' and 'environment'
      String[] searchOrder = new String[]{"transaction", "process-engine"};
      assertEquals("environment-a", environment.get("a", searchOrder));
      
    } finally {
      environment.close();
    }
  }
}

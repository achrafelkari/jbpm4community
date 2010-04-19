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

import org.jbpm.pvm.internal.env.Context;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.env.EnvironmentFactory;
import org.jbpm.pvm.internal.processengine.ProcessEngineImpl;
import org.jbpm.test.BaseJbpmTestCase;

/**
 * @author Tom Baeyens
 * @author Guillaume Porcher
 */
public class BasicEnvironmentTest extends BaseJbpmTestCase {
  
  public void testBasicEnvironmentOperation() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<jbpm-configuration>" +
      "  <process-engine-context>" +
      "    <object name='a' class='"+Object.class.getName()+"' />" +
      "  </process-engine-context>" +
      "  <transaction-context>" +
      "    <object name='b' class='"+Object.class.getName()+"' />" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );
    
    Object firstA;
    Object firstB;
    
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      firstA = environment.get("a");
      assertNotNull(firstA);
      firstB = environment.get("b");
      assertNotNull(firstB);
      
      // in the same environment, the same a and b should be returned.
      assertSame(firstA, environment.get("a"));
      assertSame(firstB, environment.get("b"));
      
    } finally {
      environment.close();
    }

    environment = environmentFactory.openEnvironment();
    try {
      // the same a should have been stored in the process-engine cache
      assertSame(firstA, environment.get("a"));
      // a new b should be created because we're in a new environment
      Object secondB = environment.get("b");
      assertNotNull(secondB);
      assertNotSame(firstB, secondB);
    } finally {
      environment.close();
    }
  }

  public void testCurrentEnvironment(){
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
        "<jbpm-configuration>" +
        " <process-engine/>" +
        " <environment/>" +
        "</jbpm-configuration>"
    );
    assertNotNull(environmentFactory);
    
    EnvironmentImpl outerEnvironment = environmentFactory.openEnvironment();
    try {
      assertSame(outerEnvironment, EnvironmentImpl.getCurrent());
      
      EnvironmentImpl innerEnvironment = environmentFactory.openEnvironment();
      try {
        assertSame(innerEnvironment, EnvironmentImpl.getCurrent());
        
      } finally {
        innerEnvironment.close();
      }

      assertSame(outerEnvironment, EnvironmentImpl.getCurrent());

    } finally {
      outerEnvironment.close();
    }
    environmentFactory.close();
  }
  

  public void testUnexistingElement() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<jbpm-configuration>" +
      "  <process-engine-context>" +
      "    <object name='a' class='"+Object.class.getName()+"' />" +
      "  </process-engine-context>" +
      "  <transaction-context>" +
      "    <object name='b' class='"+Object.class.getName()+"' />" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );
    
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      assertNull(environment.get("unexisting element"));
      
    } finally {
      environment.close();
    }
  }
  
  public void testNoBlockEnvironment(){
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
        "<jbpm-configuration>" +
        " <process-engine/>" +
        "</jbpm-configuration>"
    );
    assertNotNull(environmentFactory);
    
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      Context environmentFactoryCtxt = environment.getContext(Context.CONTEXTNAME_PROCESS_ENGINE);
      assertNotNull(environmentFactoryCtxt);
      Context environmentCtxt = environment.getContext(Context.CONTEXTNAME_TRANSACTION);
      assertNotNull(environmentCtxt);
      
    } finally {
      environment.close();
    }
    environmentFactory.close();
  }
  
  public void testNoApplicationEnvironment(){
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
        "<jbpm-configuration>" +
        " <environment/>" +
        "</jbpm-configuration>"
    );
    assertNotNull(environmentFactory);
    
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      assertNotNull(environment);
      Context environmentFactoryCtxt = environment.getContext(Context.CONTEXTNAME_PROCESS_ENGINE);
      assertNotNull(environmentFactoryCtxt);
      Context environmentCtxt = environment.getContext(Context.CONTEXTNAME_TRANSACTION);
      assertNotNull(environmentCtxt);
      
    } finally {
      environment.close();
    }
    environmentFactory.close();
  }
  
  public void testEmptyEnvironment(){
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
        "<environment/>"
    );
    assertNotNull(environmentFactory);
    
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      assertNotNull(environment);
      Context environmentFactoryCtxt = environment.getContext(Context.CONTEXTNAME_PROCESS_ENGINE);
      assertNotNull(environmentFactoryCtxt);
      Context environmentCtxt = environment.getContext(Context.CONTEXTNAME_TRANSACTION);
      assertNotNull(environmentCtxt);
      
    } finally {
      environment.close();
    }
    environmentFactory.close();
  }
  
}

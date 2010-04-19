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
package org.jbpm.test;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.jbpm.api.JbpmException;
import org.jbpm.internal.log.Jdk14LogFactory;
import org.jbpm.internal.log.Log;
import org.jbpm.internal.log.LogFormatter;

/** base class for jbpm test cases.
 * 
 * When this class is loaded (defined), then a search for a logging.properties 
 * file will be done.
 * 
 * If a logging.properties file is present in the root of the classpath, 
 * it will be used to initialize the java.util.logging logging framework.
 * 
 * If this logging.properties contains property 'redirect.commons.logging'
 * then all commons logging will be redirected to java.util.logging during 
 * this logging initialization.
 * 
 * Apart from logging initialization, this class will add some logging 
 * to setUp and tearDown.
 * 
 * If the JbpmLogFormatter is used, it will display multiple threads with 
 * indentation.  
 * 
 * @author Tom Baeyens
 */
public abstract class BaseJbpmTestCase extends TestCase {

  static {
    Jdk14LogFactory.initializeJdk14Logging(); 
  }

  static protected JbpmTestExtensions jbpmTestExtensions = JbpmTestExtensions.getJbpmTestExtensions();
  static protected Log log = Log.getLog(BaseJbpmTestCase.class.getName());
  
  Throwable exception; 

  protected void setUp() throws Exception {
    LogFormatter.resetIndentation();
    log.debug("=== starting "+getName()+" =============================");
  }

  protected void tearDown() throws Exception {
    log.debug("=== ending "+getName()+" =============================\n");
    
    if (jbpmTestExtensions.getExplicitTime()!=null) {
      jbpmTestExtensions.setExplicitTime(null);
      throw new JbpmException("This test forgot to unset the explicit time of the clock.  use JbpmTestExtensions.getJbpmTestExtensions().setExplicitTime(null);");
    }
  }

  public void assertTextPresent(String expected, String value) {
    if ( (value==null)
         || (value.indexOf(expected)==-1)
       ) {
      fail("expected presence of '"+expected+"' but was '"+value+"'");
    }
  }
  
  protected void runTest() throws Throwable {
    try {
      super.runTest();
    } catch (AssertionFailedError e) {
      log.error("");
      log.error("ASSERTION FAILURE: "+e.getMessage(), e);
      log.error("");
      exception = e;
      throw e;
    } catch (Throwable t) {
      log.error("");
      log.error("TEST THROWS EXCEPTION: "+t.getMessage(), t);
      log.error("");
      exception = t;
      throw t;
    }
  }
}

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
package org.jbpm.test.classloading;

import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class DelayedInstantiationTest extends JbpmTestCase {
  
  public static class MyActivity implements ActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) throws Exception {
    }
  }
  
  public static class StrippingClassLoader extends ClassLoader {
    public StrippingClassLoader(ClassLoader parent) {
      super(parent);
    }

    public Class loadClass(String className) throws ClassNotFoundException {
      if ( className.startsWith("==")
           && className.endsWith("==") ) {
        className = className.substring(2, className.length()-2);
      }
      ClassLoader parent = getParent();
      return parent.loadClass(className);
    }
    
  }

  public void testDelayedInstantiation() {
    deployJpdlXmlString(
            "<process name='UserClassNotVisibleDuringprocessParsing'>" +
            "  <start>" +
            "    <transition to='a' />" +
            "  </start>" +
            "  <custom name='a' class='=="+MyActivity.class.getName()+"==' >" +
            "    <transition to='b' />" +
            "  </custom>" + 
            "  <state name='b' />" +
            "</process>"
          );

//    ClassLoader original = Thread.currentThread().getContextClassLoader();
//    StrippingClassLoader strippingClassLoader = new StrippingClassLoader(original);
//    Thread.currentThread().setContextClassLoader(strippingClassLoader);
//    try {
//      executionService.startProcessInstanceByKey("UserClassNotVisibleDuringprocessParsing");
//    } finally {
//      Thread.currentThread().setContextClassLoader(original);
//    }
  }
}

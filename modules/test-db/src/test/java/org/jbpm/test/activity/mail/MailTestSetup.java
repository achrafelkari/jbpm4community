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
package org.jbpm.test.activity.mail;

import org.subethamail.wiser.Wiser;

import junit.extensions.TestSetup;
import junit.framework.TestSuite;

/**
 * @author Alejandro Guizar
 */
public class MailTestSetup extends TestSetup {

  private final Wiser wiser;

  public MailTestSetup(Class<?> testClass, Wiser wiser) {
    super(new TestSuite(testClass));
    this.wiser = wiser;
  }

  public Wiser getWiser() {
    return wiser;
  }

  @Override
  protected void setUp() throws Exception {
    wiser.setPort(2525);
    wiser.start();
  }

  @Override
  protected void tearDown() throws Exception {
    wiser.stop();
  }

}

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

import java.util.List;

import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmTestCase;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

/**
 * @author Tom Baeyens
 */
public class MailTest extends JbpmTestCase {

  private Wiser wiser;

  protected void setUp() throws Exception {
    super.setUp();
    // start mail server
    wiser = new Wiser();
    wiser.setPort(2525);
    wiser.start();
  }

  protected void tearDown() throws Exception {
    // stop mail server
    wiser.stop();
    super.tearDown();
  }

  public void testMailToPlainAddress() {
    deployJpdlXmlString("<process name='plainaddress'>"
      + "  <start>"
      + "    <transition to='mailtestmail' />"
      + "  </start>"
      + "  <mail name='mailtestmail'>"
      + "    <to addresses='jos@rubensstraat' />"
      + "    <subject>mail</subject>"
      + "    <text>youhoooo</text>"
      + "    <transition to='end' />"
      + "  </mail>"
      + "  <end name='end'/>"
      + "</process>");
    ProcessInstance processInstance = executionService
      .startProcessInstanceByKey("plainaddress");
    assertProcessInstanceEnded(processInstance);

    List<WiserMessage> messages = wiser.getMessages();
    assertEquals(1, messages.size());
  }
}

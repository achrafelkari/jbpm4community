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
package org.jbpm.pvm.internal.wire;

import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jbpm.api.JbpmException;
import org.jbpm.pvm.internal.email.impl.AddressFilter;
import org.jbpm.pvm.internal.email.impl.MailServer;
import org.jbpm.pvm.internal.email.impl.MailSessionImpl;

import com.mockrunner.ejb.JNDIUtil;

/**
 * @author Alejandro Guizar
 */
public class MailSessionWireTest extends WireTestCase {

  public void testSessionProperties() {
    WireContext wireContext = createWireContext("<objects>"
      + "  <mail-session>"
      + "    <mail-server>"
      + "      <session-properties>"
      + "        <property name='mail.host' value='localhost' />"
      + "        <property name='mail.user' value='aguizar' />"
      + "        <property name='mail.from' value='noreply@jbpm.org' />"
      + "      </session-properties>"
      + "    </mail-server>"
      + "  </mail-session>"
      + "</objects>");

    MailSessionImpl mailSession = wireContext.get(MailSessionImpl.class);
    List<MailServer> mailServers = mailSession.getMailServers();
    assertEquals(1, mailServers.size());

    MailServer mailServer = mailServers.get(0);
    Properties properties = mailServer.getSessionProperties();
    assertEquals(3, properties.size());
    assertEquals("localhost", properties.getProperty("mail.host"));
    assertEquals("aguizar", properties.getProperty("mail.user"));
    assertEquals("noreply@jbpm.org", properties.getProperty("mail.from"));
  }

  public void testNoSessionProperties() {
    try {
      createWireContext("<objects>"
        + "  <mail-session>"
        + "    <mail-server />"
        + "  </mail-session>"
        + "</objects>");
      fail("expected mail session binding to complain");
    }
    catch (JbpmException e) {
      // session properties are mandatory
    }
  }

  public static class BasicAuthenticator extends Authenticator {

    String userName;
    String password;

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
      return new PasswordAuthentication(userName, password);
    }
  }

  public void testAuthenticator() {
    WireContext wireContext = createWireContext("<objects>"
      + "  <mail-session>"
      + "    <mail-server>"
      + "      <session-properties />"
      + "      <authenticator class='"
      + BasicAuthenticator.class.getName()
      + "'>"
      + "        <field name='userName'><string value='aguizar'/></field>"
      + "        <field name='password'><string value='wontsay'/></field>"
      + "      </authenticator>"
      + "    </mail-server>"
      + "  </mail-session>"
      + "</objects>");

    MailSessionImpl mailSession = wireContext.get(MailSessionImpl.class);
    List<MailServer> mailServers = mailSession.getMailServers();
    assertEquals(1, mailServers.size());

    MailServer mailServer = mailServers.get(0);
    BasicAuthenticator authenticator = (BasicAuthenticator) mailServer.getAuthenticator();
    assertEquals("aguizar", authenticator.userName);
    assertEquals("wontsay", authenticator.password);
  }

  public void testAddressFilter() {
    WireContext wireContext = createWireContext("<objects>"
      + "  <mail-session>"
      + "    <mail-server>"
      + "      <address-filter>"
      + "        <include>.+@jbpm.org</include>"
      + "        <exclude>.+@jboss.com</exclude>"
      + "        <exclude>.+@redhat.com</exclude>"
      + "      </address-filter>"
      + "      <session-properties />"
      + "    </mail-server>"
      + "  </mail-session>"
      + "</objects>");

    MailSessionImpl mailSession = wireContext.get(MailSessionImpl.class);
    List<MailServer> mailServers = mailSession.getMailServers();
    assertEquals(1, mailServers.size());

    MailServer mailServer = mailServers.get(0);
    AddressFilter addressFilter = mailServer.getAddressFilter();

    List<Pattern> includePatterns = addressFilter.getIncludePatterns();
    assertEquals(1, includePatterns.size());
    assertEquals(".+@jbpm.org", includePatterns.get(0).toString());

    List<Pattern> excludePatterns = addressFilter.getExcludePatterns();
    assertEquals(2, excludePatterns.size());
    assertEquals(".+@jboss.com", excludePatterns.get(0).toString());
    assertEquals(".+@redhat.com", excludePatterns.get(1).toString());

    assertEquals(0, mailServer.getSessionProperties().size());
  }

  public void testNoAddressFilter() {
    WireContext wireContext = createWireContext("<objects>"
      + "  <mail-session>"
      + "    <mail-server>"
      + "      <session-properties />"
      + "    </mail-server>"
      + "  </mail-session>"
      + "</objects>");

    MailSessionImpl mailSession = wireContext.get(MailSessionImpl.class);
    List<MailServer> mailServers = mailSession.getMailServers();
    assertEquals(1, mailServers.size());

    MailServer mailServer = mailServers.get(0);
    assertNull("expected no address filter", mailServer.getAddressFilter());
    assertEquals(0, mailServer.getSessionProperties().size());
  }

  public void testMultipleMailServers() {
    WireContext wireContext = createWireContext("<objects>"
      + "  <mail-session>"
      + "    <mail-server>"
      + "      <session-properties />"
      + "    </mail-server>"
      + "    <mail-server>"
      + "      <session-properties />"
      + "    </mail-server>"
      + "    <mail-server>"
      + "      <session-properties />"
      + "    </mail-server>"
      + "  </mail-session>"
      + "</objects>");

    MailSessionImpl mailSession = wireContext.get(MailSessionImpl.class);
    List<MailServer> mailServers = mailSession.getMailServers();
    assertEquals(3, mailServers.size());
  }

  public void testJndiName() throws NamingException {
    JNDIUtil.initMockContextFactory();
    try {
      Session session = Session.getInstance(new Properties());
      new InitialContext().bind("java:comp/env/mail/smtp", session);

      WireContext wireContext = createWireContext("<objects>"
        + "  <mail-session>"
        + "    <mail-server session-jndi='java:comp/env/mail/smtp' />"
        + "  </mail-session>"
        + "</objects>");

      MailSessionImpl mailSession = wireContext.get(MailSessionImpl.class);
      List<MailServer> mailServers = mailSession.getMailServers();
      assertEquals(1, mailServers.size());

      MailServer mailServer = mailServers.get(0);
      assertSame(session, mailServer.getMailSession());
    }
    finally {
      JNDIUtil.resetMockContextFactory();
    }
  }
}

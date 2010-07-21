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
package org.jbpm.test.custom.mail;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Message.RecipientType;

import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import org.jbpm.api.Execution;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.identity.Group;
import org.jbpm.pvm.internal.email.impl.MailProducerImpl;
import org.jbpm.pvm.internal.email.impl.MailTemplateRegistry;
import org.jbpm.pvm.internal.email.spi.AddressResolver;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.identity.spi.IdentitySession;
import org.jbpm.test.JbpmCustomCfgTestCase;

/**
 * @author Alejandro Guizar
 */
public class CustomMailProducerTest extends JbpmCustomCfgTestCase {

  private Wiser wiser = new Wiser();

  protected void setUp() throws Exception {
    super.setUp();
    // create actors
    identityService.createUser("bb", "Big Brother", null, "bb@oceania");
    identityService.createUser("obrien", null, "O'Brien", "obrien@miniluv");
    identityService.createUser("charr", null, "Charrington", "charr@miniluv");
    identityService.createGroup("thinkpol");
    identityService.createGroup("innerparty");
    identityService.createMembership("obrien", "innerparty");
    identityService.createMembership("charr", "thinkpol");
    identityService.createMembership("obrien", "thinkpol");
    // start mail server
    wiser.setPort(2525);
    wiser.start();
  }

  protected void tearDown() throws Exception {
    // stop mail server
    wiser.stop();
    // delete actors
    identityService.deleteGroup("thinkpol");
    identityService.deleteGroup("innerparty");
    identityService.deleteUser("bb");
    identityService.deleteUser("obrien");
    identityService.deleteUser("charr");
    super.tearDown();
  }

  public void testCustomMailProducer() {
    // deploy process
    deployJpdlXmlString("<process name='custommail' xmlns='http://jbpm.org/4.4/jpdl'>"
      + "  <start>"
      + "    <transition to='send mail' />"
      + "  </start>"
      + "  <mail name='send mail' class='"
      + AuditMailProducer.class.getName()
      + "'>"
      + "    <property name='template'>"
      + "      <object method='getTemplate'>"
      + "        <factory><ref type='"
      + MailTemplateRegistry.class.getName()
      + "'/></factory>"
      + "        <arg><string value='rectify-template'/></arg>"
      + "      </object>"
      + "    </property>"
      + "    <property name='auditGroup'><string value='thinkpol'/></property>"
      + "    <transition to='end' />"
      + "  </mail>"
      + "  <end name='end'/>"
      + "</process>");

    // prepare dynamic values
    String addressee = "winston@minitrue";
    String newspaper = "times";
    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.set(1983, Calendar.DECEMBER, 3);
    Date date = calendar.getTime();
    String details = "reporting bb dayorder doubleplusungood refs unpersons rewrite "
      + "fullwise upsub antefiling";
    // assemble variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("addressee", addressee);
    variables.put("newspaper", newspaper);
    variables.put("date", date);
    variables.put("details", details);
    // start process instance
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("custommail", variables);
    assertProcessInstanceEnded(processInstance);

    // examine produced messages
    List<WiserMessage> messages = wiser.getMessages();
    // winston, bb, innerparty(obrien), thinkpol(charr, obrien)
    assertEquals(5, messages.size());
  }

  public static class AuditMailProducer extends MailProducerImpl {

    private String auditGroup;
    private static final long serialVersionUID = 1L;

    public String getAuditGroup() {
      return auditGroup;
    }

    public void setAuditGroup(String auditGroup) {
      this.auditGroup = auditGroup;
    }

    @Override
    protected void fillRecipients(Execution execution, Message email) throws MessagingException {
      // add recipients from template
      super.fillRecipients(execution, email);

      // load audit group from database
      EnvironmentImpl environment = EnvironmentImpl.getCurrent();
      IdentitySession identitySession = environment.get(IdentitySession.class);
      Group group = identitySession.findGroupById(auditGroup);

      // send a blind carbon copy of every message to the audit group
      AddressResolver addressResolver = environment.get(AddressResolver.class);
      email.addRecipients(RecipientType.BCC, addressResolver.resolveAddresses(group));
    }
  }
}

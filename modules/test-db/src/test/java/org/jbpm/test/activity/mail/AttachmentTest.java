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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;

import junit.framework.Test;

import org.jbpm.api.ProcessInstance;
import org.jbpm.pvm.internal.util.IoUtil;
import org.jbpm.test.JbpmTestCase;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

/**
 * @author Alejandro Guizar
 */
public class AttachmentTest extends JbpmTestCase {

  private static Wiser wiser = new Wiser();

  @Override
  protected void tearDown() throws Exception {
    wiser.getMessages().clear();
    super.tearDown();
  }

  public static Test suite() {
    return new MailTestSetup(AttachmentTest.class, wiser);
  }

  public void testVariableAttachment() throws MessagingException, IOException {
    // deploy process definition
    deployJpdlXmlString("<process name='varattachment'>"
      + "  <start>"
      + "    <transition to='send mail' />"
      + "  </start>"
      + "  <mail name='send mail'>"
      + "    <to addresses='dilbert@office' />"
      + "    <subject>review</subject>"
      + "    <text>did you review the document I emailed?</text>"
      + "    <attachments>"
      + "      <attachment name='strip.gif' expression='${strip}' mime-type='image/gif'/>"
      + "    </attachments>"
      + "    <transition to='end' />"
      + "  </mail>"
      + "  <end name='end'/>"
      + "</process>");

    byte[] strip = IoUtil.readBytes(getClass().getResourceAsStream("strip.gif"));

    // start process instance
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("varattachment", Collections.singletonMap("strip", strip));
    assertProcessInstanceEnded(processInstance);

    // examine produced messages
    examineMessages(wiser.getMessages());
  }

  public void testFileAttachment() throws MessagingException, IOException, URISyntaxException {
    File file = new File(getClass().getResource("strip.gif").toURI());

    // deploy process definition
    deployJpdlXmlString("<process name='fileattachment'>"
      + "  <start>"
      + "    <transition to='send mail' />"
      + "  </start>"
      + "  <mail name='send mail'>"
      + "    <to addresses='dilbert@office' />"
      + "    <subject>review</subject>"
      + "    <text>did you review the document I emailed?</text>"
      + "    <attachments>"
      + "      <attachment file='"
      + file
      + "'/>"
      + "    </attachments>"
      + "    <transition to='end' />"
      + "  </mail>"
      + "  <end name='end'/>"
      + "</process>");

    // start process instance
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("fileattachment");
    assertProcessInstanceEnded(processInstance);

    // examine produced messages
    examineMessages(wiser.getMessages());
  }

  public void testUrlAttachment() throws MessagingException, IOException {
    URL url = getClass().getResource("strip.gif");

    // deploy process definition
    deployJpdlXmlString("<process name='urlattachment'>"
      + "  <start>"
      + "    <transition to='send mail' />"
      + "  </start>"
      + "  <mail name='send mail'>"
      + "    <to addresses='dilbert@office' />"
      + "    <subject>review</subject>"
      + "    <text>did you review the document I emailed?</text>"
      + "    <attachments>"
      + "      <attachment url='"
      + url
      + "'/>"
      + "    </attachments>"
      + "    <transition to='end' />"
      + "  </mail>"
      + "  <end name='end'/>"
      + "</process>");

    // start process instance
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("urlattachment");
    assertProcessInstanceEnded(processInstance);

    // examine produced messages
    examineMessages(wiser.getMessages());
  }

  public void testResourceAttachment() throws MessagingException, IOException {
    // deploy process definition
    deployJpdlXmlString("<process name='resattachment'>"
      + "  <start>"
      + "    <transition to='send mail' />"
      + "  </start>"
      + "  <mail name='send mail'>"
      + "    <to addresses='dilbert@office' />"
      + "    <subject>review</subject>"
      + "    <text>did you review the document I emailed?</text>"
      + "    <attachments>"
      + "      <attachment resource='org/jbpm/test/activity/mail/strip.gif'/>"
      + "    </attachments>"
      + "    <transition to='end' />"
      + "  </mail>"
      + "  <end name='end'/>"
      + "</process>");

    // start process instance
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("resattachment");
    assertProcessInstanceEnded(processInstance);

    // examine produced messages
    examineMessages(wiser.getMessages());
  }

  private static void examineMessages(List<WiserMessage> wiserMessages)
    throws MessagingException, IOException {
    assertEquals(1, wiserMessages.size());

    WiserMessage wisMessage = wiserMessages.get(0);
    MimeMessage message = wisMessage.getMimeMessage();

    // multipart content
    Multipart multipart = (Multipart) message.getContent();
    assertEquals(2, multipart.getCount());

    // text part
    BodyPart textPart = multipart.getBodyPart(0);
    assert textPart.getContentType().startsWith("text/plain") : textPart.getContentType();
    assertEquals("did you review the document I emailed?", textPart.getContent());

    // binary part
    BodyPart gifPart = multipart.getBodyPart(1);
    assertEquals("strip.gif", gifPart.getFileName());
    assert gifPart.getContentType().startsWith("image/gif") : gifPart.getContentType();
    // check gif magic numbers
    InputStream gifStream = gifPart.getInputStream();
    try {
      assertEquals('G', gifStream.read());
      assertEquals('I', gifStream.read());
      assertEquals('F', gifStream.read());
    }
    finally {
      gifStream.close();
    }
  }
}

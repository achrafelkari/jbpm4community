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

import org.jbpm.pvm.internal.email.impl.AttachmentTemplate;
import org.jbpm.pvm.internal.email.impl.MailTemplate;
import org.jbpm.pvm.internal.email.impl.MailTemplateRegistry;

/**
 * @author Alejandro Guizar
 */
public class MailTemplateWireTest extends WireTestCase {

  public void testName() {
    WireContext wireContext =
        createWireContext("<objects>"
            + "<mail-template name='memo'>"
            + "  <to addresses='dilbert@office, alice@work, dogbert@house'/>"
            + "  <subject>bureaucracy</subject>"
            + "</mail-template>"
            + "</objects>");

    MailTemplateRegistry templateRegistry = wireContext.get(MailTemplateRegistry.class);
    assertNotNull(templateRegistry);
    assertNotNull(templateRegistry.getTemplate("memo"));
  }

  public void testLanguage() {
    WireContext wireContext =
        createWireContext("<objects>"
            + "<mail-template name='memo' language='juel'>"
            + "  <to addresses='dilbert@office, alice@work, dogbert@house'/>"
            + "  <subject>bureaucracy</subject>"
            + "</mail-template>"
            + "</objects>");

    MailTemplate template = wireContext.get(MailTemplateRegistry.class).getTemplate("memo");
    assertEquals("juel", template.getLanguage());
  }

  public void testFrom() {
    WireContext wireContext =
        createWireContext("<objects>"
            + "<mail-template name='memo'>"
            + "  <from addresses='wacko@jbpm.org' />"
            + "  <to addresses='dilbert@office, alice@work, dogbert@house'/>"
            + "  <subject>bureaucracy</subject>"
            + "</mail-template>"
            + "</objects>");

    MailTemplate template = wireContext.get(MailTemplateRegistry.class).getTemplate("memo");
    assertEquals("wacko@jbpm.org", template.getFrom().getAddresses());
  }

  public void testTo() {
    WireContext wireContext =
        createWireContext("<objects>"
            + "<mail-template name='memo'>"
            + "  <to addresses='dilbert@office, alice@work, dogbert@house'/>"
            + "  <subject>bureaucracy</subject>"
            + "</mail-template>"
            + "</objects>");

    MailTemplate template = wireContext.get(MailTemplateRegistry.class).getTemplate("memo");
    assertEquals("dilbert@office, alice@work, dogbert@house", template.getTo().getAddresses());
  }

  public void testCc() {
    WireContext wireContext =
        createWireContext("<objects>"
            + "<mail-template name='memo'>"
            + "  <to addresses='dilbert@office, alice@work, dogbert@house'/>"
            + "  <cc users='theboss, hrpolicymaker'/>"
            + "  <subject>bureaucracy</subject>"
            + "</mail-template>"
            + "</objects>");

    MailTemplate template = wireContext.get(MailTemplateRegistry.class).getTemplate("memo");
    assertEquals("theboss, hrpolicymaker", template.getCc().getUsers());
  }

  public void testBcc() {
    WireContext wireContext =
        createWireContext("<objects>"
            + "<mail-template name='memo'>"
            + "  <to addresses='dilbert@office, alice@work, dogbert@house'/>"
            + "  <bcc groups='thoughtpolice'/>"
            + "  <subject>bureaucracy</subject>"
            + "</mail-template>"
            + "</objects>");

    MailTemplate template = wireContext.get(MailTemplateRegistry.class).getTemplate("memo");
    assertEquals("thoughtpolice", template.getBcc().getGroups());
  }

  public void testSubject() {
    WireContext wireContext =
        createWireContext("<objects>"
            + "<mail-template name='memo'>"
            + "  <to addresses='dilbert@office, alice@work, dogbert@house'/>"
            + "  <subject>bureaucracy</subject>"
            + "</mail-template>"
            + "</objects>");

    MailTemplate template = wireContext.get(MailTemplateRegistry.class).getTemplate("memo");
    assertEquals("bureaucracy", template.getSubject());
  }

  public void testText() {
    WireContext wireContext =
        createWireContext("<objects>"
            + "<mail-template name='memo'>"
            + "  <to addresses='dilbert@office, alice@work, dogbert@house'/>"
            + "  <subject>bureaucracy</subject>"
            + "  <text>plain text content</text>"
            + "</mail-template>"
            + "</objects>");

    MailTemplate template = wireContext.get(MailTemplateRegistry.class).getTemplate("memo");
    assertEquals("plain text content", template.getText());
  }

  public void testHtml() {
    WireContext wireContext =
        createWireContext("<objects>"
            + "<mail-template name='memo'>"
            + "  <to addresses='dilbert@office, alice@work, dogbert@house'/>"
            + "  <subject>bureaucracy</subject>"
            + "  <html><strong>rich</strong> content</html>"
            + "</mail-template>"
            + "</objects>");

    MailTemplate template = wireContext.get(MailTemplateRegistry.class).getTemplate("memo");
    assertEquals("<strong>rich</strong> content", template.getHtml());
  }

  public void testAttachments() {
    WireContext wireContext =
        createWireContext("<objects>"
            + "<mail-template name='memo'>"
            + "  <to addresses='dilbert@office, alice@work, dogbert@house'/>"
            + "  <subject>bureaucracy</subject>"
            + "  <attachments>"
            + "    <attachment url='http://en.wikipedia.org/wiki/File:Dilbert-20050910.png'/>"
            + "    <attachment resource='org/example/pic.jpg'/>"
            + "    <attachment file='${user.home}/.face'/>"
            + "  </attachments>"
            + "</mail-template>"
            + "</objects>");

    MailTemplate template = wireContext.get(MailTemplateRegistry.class).getTemplate("memo");
    List<AttachmentTemplate> attachmentTemplates = template.getAttachmentTemplates();
    assertEquals(3, attachmentTemplates.size());
    // url
    AttachmentTemplate attachmentTemplate = attachmentTemplates.get(0);
    assertEquals("http://en.wikipedia.org/wiki/File:Dilbert-20050910.png",
        attachmentTemplate.getUrl());
    // resource
    attachmentTemplate = attachmentTemplates.get(1);
    assertEquals("org/example/pic.jpg", attachmentTemplate.getResource());
    // file
    attachmentTemplate = attachmentTemplates.get(2);
    assertEquals("${user.home}/.face", attachmentTemplate.getFile());
  }
}

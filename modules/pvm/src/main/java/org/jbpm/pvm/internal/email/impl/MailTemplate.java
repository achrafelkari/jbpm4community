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
package org.jbpm.pvm.internal.email.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Message.RecipientType;

/**
 * Template for electronic mail, complete with recipients, subject, content and attachments.
 * 
 * @author Alejandro Guizar
 */
public class MailTemplate implements Serializable {

  private static final long serialVersionUID = 1L;

  private String language;
  private AddressTemplate from;
  private Map<RecipientType, AddressTemplate> recipientTemplates =
      new HashMap<RecipientType, AddressTemplate>();
  private String subject;
  private String text;
  private String html;
  private List<AttachmentTemplate> attachmentTemplates = new ArrayList<AttachmentTemplate>();

  /**
   * Templating engine meant to produce dynamic content.
   */
  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public AddressTemplate getFrom() {
    return from;
  }

  public void setFrom(AddressTemplate from) {
    this.from = from;
  }

  public AddressTemplate getRecipientTemplate(RecipientType recipientType) {
    return recipientTemplates.get(recipientType);
  }

  public void setRecipientTemplate(RecipientType recipientType, AddressTemplate recipientTemplate) {
    recipientTemplates.put(recipientType, recipientTemplate);
  }

  public AddressTemplate getTo() {
    return getRecipientTemplate(RecipientType.TO);
  }

  public void setTo(AddressTemplate to) {
    setRecipientTemplate(RecipientType.TO, to);
  }

  public AddressTemplate getCc() {
    return getRecipientTemplate(RecipientType.CC);
  }

  public void setCc(AddressTemplate cc) {
    setRecipientTemplate(RecipientType.CC, cc);
  }

  public AddressTemplate getBcc() {
    return getRecipientTemplate(RecipientType.BCC);
  }

  public void setBcc(AddressTemplate bcc) {
    setRecipientTemplate(RecipientType.BCC, bcc);
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getHtml() {
    return html;
  }

  public void setHtml(String html) {
    this.html = html;
  }

  public List<AttachmentTemplate> getAttachmentTemplates() {
    return attachmentTemplates;
  }

  public void addAttachmentTemplate(AttachmentTemplate attachmentTemplate) {
    attachmentTemplates.add(attachmentTemplate);
  }
}

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

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.api.identity.Group;
import org.jbpm.api.identity.User;
import org.jbpm.pvm.internal.email.spi.AddressResolver;
import org.jbpm.pvm.internal.email.spi.MailProducer;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.identity.spi.IdentitySession;
import org.jbpm.pvm.internal.script.ScriptManager;

/**
 * Default mail producer.
 * 
 * @author Alejandro Guizar
 */
public class MailProducerImpl implements MailProducer, Serializable {

  private static final long serialVersionUID = 1L;

  private MailTemplate template;

  public MailTemplate getTemplate() {
    return template;
  }

  public void setTemplate(MailTemplate template) {
    this.template = template;
  }

  public Collection<Message> produce(Execution execution) {
    Message email = instantiateEmail();
    fillFrom(execution, email);
    fillRecipients(execution, email);
    fillSubject(execution, email);
    fillContent(execution, email);
    return Collections.singleton(email);
  }

  protected Message instantiateEmail() {
    return new MimeMessage((Session) null);
  }

  /**
   * Fills the <code>from</code> attribute of the given email. The sender addresses are an optional
   * element in the mail template. If absent, each mail server supplies the current user's email
   * address.
   * 
   * @see {@link InternetAddress#getLocalAddress(Session)}
   */
  protected void fillFrom(Execution execution, Message email) {
    AddressTemplate fromTemplate = template.getFrom();
    // "from" attribute is optional
    if (fromTemplate == null) return;

    // resolve and parse addresses
    String addresses = fromTemplate.getAddresses();
    if (addresses != null) {
      addresses = evaluateExpression(addresses, execution);
      try {
        email.addFrom(InternetAddress.parse(addresses));
      }
      catch (MessagingException e) {
        throw new JbpmException("failed to add " + addresses + " to senders", e);
      }
    }

    EnvironmentImpl environment = EnvironmentImpl.getCurrent();
    IdentitySession identitySession = environment.get(IdentitySession.class);
    AddressResolver addressResolver = environment.get(AddressResolver.class);

    // resolve and tokenize users
    String userList = fromTemplate.getUsers();
    if (userList != null) {
      String[] userIds = tokenizeActors(userList, execution);
      List<User> users = identitySession.findUsersById(userIds);
      addSenders(resolveAddresses(users, addressResolver), email);
    }

    // resolve and tokenize groups
    String groupList = fromTemplate.getGroups();
    if (groupList != null) {
      for (String groupId : tokenizeActors(groupList, execution)) {
        Group group = identitySession.findGroupById(groupId);
        addSenders(addressResolver.resolveAddresses(group), email);
      }
    }
  }

  private String evaluateExpression(String expression, Execution execution) {
    ScriptManager scriptManager = EnvironmentImpl.getFromCurrent(ScriptManager.class);
    Object value = scriptManager.evaluateExpression(expression, template.getLanguage());
    if (!(value instanceof String)) {
      throw new JbpmException("expected expression '"
          + expression
          + "' to return string, but was: "
          + value);
    }
    return (String) value;
  }

  private String[] tokenizeActors(String recipients, Execution execution) {
    String[] actors = evaluateExpression(recipients, execution).split("[,;|\\s]+");
    if (actors.length == 0) throw new JbpmException("recipient list is empty: " + recipients);
    return actors;
  }

  /** construct recipient addresses from user entities */
  private Address[] resolveAddresses(List<User> users, AddressResolver addressResolver) {
    int userCount = users.size();
    Address[] addresses = new Address[userCount];
    for (int i = 0; i < userCount; i++) {
      addresses[i] = addressResolver.resolveAddress(users.get(i));
    }
    return addresses;
  }

  /** add senders to message */
  private void addSenders(Address[] addresses, Message email) {
    try {
      email.addFrom(addresses);
    }
    catch (MessagingException e) {
      throw new JbpmException("failed to add " + Arrays.toString(addresses) + " to senders", e);
    }
  }

  protected void fillRecipients(Execution execution, Message email) {
    // to
    AddressTemplate to = template.getTo();
    if (to != null) fillRecipients(to, execution, email, RecipientType.TO);

    // cc
    AddressTemplate cc = template.getCc();
    if (cc != null) fillRecipients(cc, execution, email, RecipientType.CC);

    // bcc
    AddressTemplate bcc = template.getBcc();
    if (bcc != null) fillRecipients(bcc, execution, email, RecipientType.BCC);
  }

  private void fillRecipients(AddressTemplate addressTemplate, Execution execution, Message email,
      RecipientType recipientType) {
    // resolve and parse addresses
    String addresses = addressTemplate.getAddresses();
    if (addresses != null) {
      addresses = evaluateExpression(addresses, execution);
      try {
        email.addRecipients(recipientType, InternetAddress.parse(addresses));
      }
      catch (MessagingException e) {
        throw new JbpmException("failed to add "
            + addresses
            + " to recipients of type "
            + recipientType, e);
      }
    }

    EnvironmentImpl environment = EnvironmentImpl.getCurrent();
    IdentitySession identitySession = environment.get(IdentitySession.class);
    AddressResolver addressResolver = environment.get(AddressResolver.class);

    // resolve and tokenize users
    String userList = addressTemplate.getUsers();
    if (userList != null) {
      String[] userIds = tokenizeActors(userList, execution);
      List<User> users = identitySession.findUsersById(userIds);
      addRecipients(resolveAddresses(users, addressResolver), email, recipientType);
    }

    // resolve and tokenize groups
    String groupList = addressTemplate.getGroups();
    if (groupList != null) {
      for (String groupId : tokenizeActors(groupList, execution)) {
        Group group = identitySession.findGroupById(groupId);
        addRecipients(addressResolver.resolveAddresses(group), email, recipientType);
      }
    }
  }

  /** add recipient addresses to message */
  private void addRecipients(Address[] addresses, Message email, RecipientType recipientType) {
    try {
      email.addRecipients(recipientType, addresses);
    }
    catch (MessagingException e) {
      throw new JbpmException("failed to add "
          + Arrays.toString(addresses)
          + " to recipients of type "
          + recipientType, e);
    }
  }

  protected void fillSubject(Execution execution, Message email) {
    String subject = template.getSubject();
    if (subject != null) {
      subject = evaluateExpression(subject, execution);
      try {
        email.setSubject(subject);
      }
      catch (MessagingException e) {
        throw new JbpmException("failed to set subject to " + subject, e);
      }
    }
  }

  protected void fillContent(Execution execution, Message email) {
    String text = template.getText();
    String html = template.getHtml();
    List<AttachmentTemplate> attachmentTemplates = template.getAttachmentTemplates();

    if (html != null || !attachmentTemplates.isEmpty()) {
      // multipart
      Multipart multipart = new MimeMultipart("related");

      // text
      if (text != null) {
        BodyPart textPart = new MimeBodyPart();
        text = evaluateExpression(text, execution);
        try {
          textPart.setText(text);
          multipart.addBodyPart(textPart);
        }
        catch (MessagingException e) {
          throw new JbpmException("failed to add text content: " + text, e);
        }
      }

      // html
      if (html != null) {
        BodyPart htmlPart = new MimeBodyPart();
        html = evaluateExpression(html, execution);
        try {
          htmlPart.setContent(html, "text/html");
          multipart.addBodyPart(htmlPart);
        }
        catch (MessagingException e) {
          throw new JbpmException("failed to add html content: " + html, e);
        }
      }

      // attachments
      if (!attachmentTemplates.isEmpty()) {
        addAttachments(execution, multipart);
      }

      try {
        email.setContent(multipart);
      }
      catch (MessagingException e) {
        throw new JbpmException("failed to set multipart content: " + multipart, e);
      }
    }
    else if (text != null) {
      // unipart
      text = evaluateExpression(text, execution);
      try {
        email.setText(text);
      }
      catch (MessagingException e) {
        throw new JbpmException("failed to add text content: " + text, e);
      }
    }
  }

  protected void addAttachments(Execution execution, Multipart multipart) {
    for (AttachmentTemplate attachmentTemplate : template.getAttachmentTemplates()) {
      BodyPart attachmentPart = new MimeBodyPart();

      // resolve and set description
      String description = attachmentTemplate.getDescription();
      if (description != null) {
        description = evaluateExpression(description, execution);
        try {
          attachmentPart.setDescription(description);
        }
        catch (MessagingException e) {
          throw new JbpmException("failed to set attachment description: " + description, e);
        }
      }

      // resolve name; if absent, it will be taken from file or url
      String name = attachmentTemplate.getName();
      if (name != null) {
        name = evaluateExpression(name, execution);
      }

      // resolve and read file
      String file = attachmentTemplate.getFile();
      if (file != null) {
        File targetFile = new File(evaluateExpression(file, execution));
        if (!targetFile.isFile()) {
          throw new JbpmException("could not read attachment content, file not found: "
              + targetFile);
        }
        // set content from target file
        try {
          attachmentPart.setDataHandler(new DataHandler(new FileDataSource(targetFile)));
          // extract attachment name from file
          if (name == null) {
            name = targetFile.getName();
          }
        }
        catch (MessagingException e) {
          throw new JbpmException("failed to add attachment content: " + targetFile, e);
        }
      }
      else {
        URL targetUrl;
        // resolve and read external url
        String url = attachmentTemplate.getUrl();
        if (url != null) {
          try {
            url = evaluateExpression(url, execution);
            targetUrl = new URL(url);
          }
          catch (MalformedURLException e) {
            throw new JbpmException("could not read attachment content, malformed url: " + url, e);
          }
        }
        // resolve and read classpath resource
        else {
          String resource = evaluateExpression(attachmentTemplate.getResource(), execution);
          targetUrl = EnvironmentImpl.getCurrent().getClassLoader().getResource(resource);
          if (targetUrl == null) {
            throw new JbpmException("could not read attachment content, resource not found: "
                + resource);
          }
        }
        // set content from url
        try {
          attachmentPart.setDataHandler(new DataHandler(targetUrl));
          // extract attachment name from target url
          if (name == null) {
            name = extractResourceName(targetUrl);
          }
        }
        catch (MessagingException e) {
          throw new JbpmException("failed to add attachment content: " + targetUrl, e);
        }
      }

      // set name, must be resolved at this point
      try {
        attachmentPart.setFileName(name);
      }
      catch (MessagingException e) {
        throw new JbpmException("failed to set attachment name: " + name, e);
      }

      try {
        multipart.addBodyPart(attachmentPart);
      }
      catch (MessagingException e) {
        throw new JbpmException("failed to add attachment part: " + attachmentPart, e);
      }
    }
  }

  private static String extractResourceName(URL url) {
    String path = url.getPath();
    if (path == null || path.length() == 0) return null;

    int sepIndex = path.lastIndexOf('/');
    return sepIndex != -1 ? path.substring(sepIndex) : null;
  }
}

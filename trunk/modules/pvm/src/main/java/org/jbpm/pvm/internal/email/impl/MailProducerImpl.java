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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
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
import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.email.spi.AddressResolver;
import org.jbpm.pvm.internal.email.spi.MailProducer;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.identity.spi.IdentitySession;

/**
 * Default mail producer.
 *
 * @author Alejandro Guizar
 * @author Huisheng Xu
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
    try {
      Message email = instantiateEmail();
      fillFrom(execution, email);
      fillRecipients(execution, email);
      fillSubject(execution, email);
      fillContent(execution, email);
      return Collections.singleton(email);
    }
    catch (MessagingException e) {
      throw new JbpmException("failed to produce email message", e);
    }
  }

  protected Message instantiateEmail() {
    return new MimeMessage((Session) null);
  }

  /**
   * Fills the <code>from</code> attribute of the given email. The sender addresses are an
   * optional element in the mail template. If absent, each mail server supplies the current
   * user's email address.
   *
   * @see {@link InternetAddress#getLocalAddress(Session)}
   */
  protected void fillFrom(Execution execution, Message email) throws MessagingException {
    AddressTemplate fromTemplate = template.getFrom();
    // "from" attribute is optional
    if (fromTemplate == null) return;

    // resolve and parse addresses
    String addresses = fromTemplate.getAddresses();
    if (addresses != null) {
      addresses = evaluateExpression(addresses);
      // non-strict parsing applies to a list of mail addresses entered by a human
      email.addFrom(InternetAddress.parse(addresses, false));
    }

    EnvironmentImpl environment = EnvironmentImpl.getCurrent();
    IdentitySession identitySession = environment.get(IdentitySession.class);
    AddressResolver addressResolver = environment.get(AddressResolver.class);

    // resolve and tokenize users
    String userList = fromTemplate.getUsers();
    if (userList != null) {
      String[] userIds = tokenizeActors(userList, execution);
      List<User> users = identitySession.findUsersById(userIds);
      email.addFrom(resolveAddresses(users, addressResolver));
    }

    // resolve and tokenize groups
    String groupList = fromTemplate.getGroups();
    if (groupList != null) {
      for (String groupId : tokenizeActors(groupList, execution)) {
        Group group = identitySession.findGroupById(groupId);
        email.addFrom(addressResolver.resolveAddresses(group));
      }
    }
  }

  private String evaluateExpression(String expression) {
    return evaluateExpression(expression, String.class);
  }

  private <T> T evaluateExpression(String expression, Class<T> type) {
    Object value = Expression.create(expression, template.getLanguage()).evaluate();
    return type.cast(value);
  }

  private String[] tokenizeActors(String recipients, Execution execution) {
    String[] actors = evaluateExpression(recipients).split("[,;\\s]+");
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

  protected void fillRecipients(Execution execution, Message email) throws MessagingException {
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

  private void fillRecipients(AddressTemplate addressTemplate, Execution execution,
    Message email, RecipientType recipientType) throws MessagingException {
    // resolve and parse addresses
    String addresses = addressTemplate.getAddresses();
    if (addresses != null) {
      addresses = evaluateExpression(addresses);
      // non-strict parsing applies to a list of mail addresses entered by a human
      email.addRecipients(recipientType, InternetAddress.parse(addresses, false));
    }

    EnvironmentImpl environment = EnvironmentImpl.getCurrent();
    IdentitySession identitySession = environment.get(IdentitySession.class);
    AddressResolver addressResolver = environment.get(AddressResolver.class);

    // resolve and tokenize users
    String userList = addressTemplate.getUsers();
    if (userList != null) {
      String[] userIds = tokenizeActors(userList, execution);
      List<User> users = identitySession.findUsersById(userIds);
      email.addRecipients(recipientType, resolveAddresses(users, addressResolver));
    }

    // resolve and tokenize groups
    String groupList = addressTemplate.getGroups();
    if (groupList != null) {
      for (String groupId : tokenizeActors(groupList, execution)) {
        Group group = identitySession.findGroupById(groupId);
        email.addRecipients(recipientType, addressResolver.resolveAddresses(group));
      }
    }
  }

  protected void fillSubject(Execution execution, Message email) throws MessagingException {
    String subject = template.getSubject();
    if (subject != null) {
      subject = evaluateExpression(subject);
      email.setSubject(subject);
    }
  }

  protected void fillContent(Execution execution, Message email) throws MessagingException {
    String text = template.getText();
    String html = template.getHtml();
    List<AttachmentTemplate> attachmentTemplates = template.getAttachmentTemplates();

    if (html != null || !attachmentTemplates.isEmpty()) {
      // multipart
      Multipart multipart = new MimeMultipart("related");

      // text
      if (text != null) {
        BodyPart textPart = new MimeBodyPart();
        text = evaluateExpression(text);
        textPart.setText(text);
        multipart.addBodyPart(textPart);
      }

      // html
      if (html != null) {
        BodyPart htmlPart = new MimeBodyPart();
        html = evaluateExpression(html);
        htmlPart.setContent(html, "text/html");
        multipart.addBodyPart(htmlPart);
      }

      // attachments
      if (!attachmentTemplates.isEmpty()) {
        addAttachments(execution, multipart);
      }

      email.setContent(multipart);
    }
    else if (text != null) {
      // unipart
      text = evaluateExpression(text);
      email.setText(text);
    }
  }

  protected void addAttachments(Execution execution, Multipart multipart)
    throws MessagingException {
    for (AttachmentTemplate attachmentTemplate : template.getAttachmentTemplates()) {
      BodyPart attachmentPart = new MimeBodyPart();

      // resolve description
      String description = attachmentTemplate.getDescription();
      if (description != null) {
        attachmentPart.setDescription(evaluateExpression(description));
      }

      // obtain interface to data
      DataHandler dataHandler = createDataHandler(attachmentTemplate);
      attachmentPart.setDataHandler(dataHandler);

      // resolve file name
      String name = attachmentTemplate.getName();
      if (name != null) {
        attachmentPart.setFileName(evaluateExpression(name));
      }
      else {
        // fall back on data source
        DataSource dataSource = dataHandler.getDataSource();
        if (dataSource instanceof URLDataSource) {
          name = extractResourceName(((URLDataSource) dataSource).getURL());
        }
        else {
          name = dataSource.getName();
        }
        if (name != null) {
          attachmentPart.setFileName(name);
        }
      }

      multipart.addBodyPart(attachmentPart);
    }
  }

  private DataHandler createDataHandler(AttachmentTemplate attachmentTemplate) {
    // evaluate expression
    String expression = attachmentTemplate.getExpression();
    if (expression != null) {
      Object object = evaluateExpression(expression, Object.class);
      return new DataHandler(object, attachmentTemplate.getMimeType());
    }

    // resolve local file
    String file = attachmentTemplate.getFile();
    if (file != null) {
      File targetFile = new File(evaluateExpression(file));
      if (!targetFile.isFile()) {
        throw new JbpmException("could not read attachment content, file not found: "
          + targetFile);
      }
      // set content from file
      return new DataHandler(new FileDataSource(targetFile));
    }

    // resolve external url
    URL targetUrl;
    String url = attachmentTemplate.getUrl();
    if (url != null) {
      url = evaluateExpression(url);
      try {
        targetUrl = new URL(url);
      }
      catch (MalformedURLException e) {
        throw new JbpmException("could not read attachment content, malformed url: " + url, e);
      }
    }
    // resolve classpath resource
    else {
      String resource = evaluateExpression(attachmentTemplate.getResource());
      targetUrl = Thread.currentThread().getContextClassLoader().getResource(resource);
      if (targetUrl == null) {
        throw new JbpmException("could not read attachment content, resource not found: "
          + resource);
      }
    }
    // set content from url
    return new DataHandler(targetUrl);
  }

  private static String extractResourceName(URL url) {
    String path = url.getPath();
    if (path == null || path.length() == 0) return null;

    int sepIndex = path.lastIndexOf('/');
    return sepIndex != -1 ? path.substring(sepIndex + 1) : null;
  }
}

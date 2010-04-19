package org.jbpm.pvm.internal.email.impl;

import java.util.Collection;
import java.util.List;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;

import org.jbpm.api.JbpmException;
import org.jbpm.pvm.internal.email.spi.MailSession;

public class MailSessionImpl implements MailSession {

  private List<MailServer> mailServers;

  public void send(Collection<Message> emails) {
    // Emails need to have the sessions populated.
    for (Message email : emails) {
      try {
        Address[] to = email.getRecipients(RecipientType.TO);
        Address[] cc = email.getRecipients(RecipientType.CC);
        Address[] bcc = email.getRecipients(RecipientType.BCC);

        for (MailServer mailServer : mailServers) {
          // Need to apply filter.
          AddressFilter addressFilter = mailServer.getAddressFilter();
          if (addressFilter != null) {
            // Set the email with the new filtered addresses.
            email.setRecipients(RecipientType.TO, addressFilter.filter(to));
            email.setRecipients(RecipientType.CC, addressFilter.filter(cc));
            email.setRecipients(RecipientType.BCC, addressFilter.filter(bcc));
          }

          // if sender is not present, use local address
          Session mailSession = mailServer.getMailSession();
          if (email.getFrom() == null) {
            email.setFrom(InternetAddress.getLocalAddress(mailSession));
          }

          // If there is someone to send it to, then send it.
          Address[] recipients = email.getAllRecipients();
          if (recipients.length > 0) {
            Transport transport = mailSession.getTransport(recipients[0]);
            try {
              transport.connect();
              transport.sendMessage(email, recipients);
            }
            finally {
              transport.close();
            }
          }
        }
      }
      catch (MessagingException e) {
        throw new JbpmException("could not send email: " + email, e);
      }
    }
  }

  public List<MailServer> getMailServers() {
    return mailServers;
  }

  protected void setMailServers(List<MailServer> mailServers) {
    this.mailServers = mailServers;
  }

}

package org.jbpm.pvm.internal.email.spi;

import java.util.Collection;

import javax.mail.Message;

/**
 * Pluggable control object for sending emails.
 * 
 * @author Brad Davis
 */
public interface MailSession {

  void send(Collection<Message> emails);

}

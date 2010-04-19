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

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import org.jbpm.api.JbpmException;
import org.jbpm.api.identity.Group;
import org.jbpm.api.identity.User;
import org.jbpm.pvm.internal.email.spi.AddressResolver;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.identity.spi.IdentitySession;

/**
 * @author Alejandro Guizar
 */
public class DefaultAddressResolver implements AddressResolver {

  public Address resolveAddress(User user) {
    String givenName = user.getGivenName();
    String familyName = user.getFamilyName();
    String personal = givenName != null ? familyName != null ? givenName + ' ' + familyName
        : givenName : familyName;
    try {
      return new InternetAddress(user.getBusinessEmail(), personal);
    }
    catch (UnsupportedEncodingException e) {
      throw new JbpmException("invalid recipient name: " + personal);
    }
  }

  public Address[] resolveAddresses(Group group) {
    List<User> users = EnvironmentImpl.getFromCurrent(IdentitySession.class)
        .findUsersByGroup(group.getId());
    int userCount = users.size();
    Address[] addresses = new Address[userCount];
    for (int i = 0; i < userCount; i++) {
      addresses[i] = resolveAddress(users.get(i));
    }
    return addresses;
  }

}

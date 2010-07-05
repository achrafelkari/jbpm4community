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
package org.jbpm.pvm.internal.wire.binding;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import org.jbpm.pvm.internal.email.impl.AddressFilter;
import org.jbpm.pvm.internal.email.impl.MailServer;
import org.jbpm.pvm.internal.email.impl.MailSessionImpl;
import org.jbpm.pvm.internal.email.spi.MailSession;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.descriptor.JndiDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.ListDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.PatternDescriptor;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;

/**
 * Parses a descriptor for creating a {@link MailSession}.
 * 
 * @author Alejandro Guizar
 */
public class MailSessionBinding extends WireDescriptorBinding {

  public MailSessionBinding() {
    super("mail-session");
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    // mail servers
    List<Descriptor> serverDescriptors = new ArrayList<Descriptor>();
    for (Element serverElement : XmlUtil.elements(element, "mail-server")) {
      // mail server
      ObjectDescriptor serverDescriptor = new ObjectDescriptor(MailServer.class);
      serverDescriptors.add(serverDescriptor);

      // address filter
      Element filterElement = XmlUtil.element(serverElement, "address-filter");
      if (filterElement != null) {
        // includes
        List<Descriptor> includeDescriptors = new ArrayList<Descriptor>();
        for (Element includeElement : XmlUtil.elements(filterElement, "include")) {
          includeDescriptors.add(parsePattern(includeElement, parse, parser));
        }
        ListDescriptor includesDescriptor = new ListDescriptor();
        includesDescriptor.setValueDescriptors(includeDescriptors);

        // excludes
        List<Descriptor> excludeDescriptors = new ArrayList<Descriptor>();
        for (Element excludeElement : XmlUtil.elements(filterElement, "exclude")) {
          excludeDescriptors.add(parsePattern(excludeElement, parse, parser));
        }
        ListDescriptor excludesDescriptor = new ListDescriptor();
        excludesDescriptor.setValueDescriptors(excludeDescriptors);

        // address filter
        ObjectDescriptor filterDescriptor = new ObjectDescriptor(AddressFilter.class);
        filterDescriptor.addInjection("includePatterns", includesDescriptor);
        filterDescriptor.addInjection("excludePatterns", excludesDescriptor);

        serverDescriptor.addInjection("addressFilter", filterDescriptor);
      }

      if (serverElement.hasAttribute("session-jndi")) {
        // session jndi name
        String jndiName = serverElement.getAttribute("session-jndi");
        Descriptor jndiDescriptor = new JndiDescriptor(jndiName);
        serverDescriptor.addInjection("mailSession", jndiDescriptor);
      }
      else {
        // session properties
        Element propertiesElement = XmlUtil.element(serverElement, "session-properties");
        if (propertiesElement != null) {
          Descriptor propertiesDescriptor = PropertiesBinding.parseDescriptor(propertiesElement, parse, parser);
          serverDescriptor.addInjection("sessionProperties", propertiesDescriptor);
        }
        else {
          parse.addProblem("missing mail session properties or jndi name", serverElement);
        }

        // authenticator
        Element authenticatorElement = XmlUtil.element(serverElement, "authenticator");
        if (authenticatorElement != null) {
          Descriptor authenticatorDescriptor = ObjectBinding.parseObjectDescriptor(authenticatorElement, parse, parser);
          serverDescriptor.addInjection("authenticator", authenticatorDescriptor);
        }
      }
    }

    // mail servers
    ListDescriptor serversDescriptor = new ListDescriptor();
    serversDescriptor.setValueDescriptors(serverDescriptors);

    // mail session
    ObjectDescriptor sessionDescriptor = new ObjectDescriptor(MailSessionImpl.class);
    sessionDescriptor.addInjection("mailServers", serversDescriptor);
    return sessionDescriptor;
  }

  public static PatternDescriptor parsePattern(Element element, Parse parse, Parser parser) {
    // content
    String regex = XmlUtil.getContentText(element);
    PatternDescriptor patternDescriptor = new PatternDescriptor(regex);
    // literal
    String literalAttr = XmlUtil.attribute(element, "literal");
    if (literalAttr != null) {
      Boolean literal = XmlUtil.parseBooleanValue(literalAttr);
      if (literal != null)
        patternDescriptor.setLiteral(literal);
    }
    // canonEq
    String canonEqAttr = XmlUtil.attribute(element, "canonEq");
    if (canonEqAttr != null) {
      Boolean canonEq = XmlUtil.parseBooleanValue(canonEqAttr);
      if (canonEq != null)
        patternDescriptor.setCanonEq(canonEq);
    }
    return patternDescriptor;
  }

}

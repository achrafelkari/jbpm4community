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

import org.w3c.dom.Attr;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.jbpm.pvm.internal.email.impl.AddressTemplate;
import org.jbpm.pvm.internal.email.impl.AttachmentTemplate;
import org.jbpm.pvm.internal.email.impl.MailTemplate;
import org.jbpm.pvm.internal.email.impl.MailTemplateRegistry;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.WireDefinition;
import org.jbpm.pvm.internal.wire.descriptor.ProvidedObjectDescriptor;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;

/**
 * @author Alejandro Guizar
 */
public class MailTemplateBinding extends WireDescriptorBinding {

  public MailTemplateBinding() {
    super("mail-template");
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    // MailTemplateRegistry is added to the WireDescriptor with a ProvidedObjectDescriptor
    // The MailTemplateRegistry descriptor is lazy initialized by this binding
    // mail-template will add a MailTemplate to the MailTemplateRegistry
    ProvidedObjectDescriptor templateRegistryDescriptor;
    MailTemplateRegistry templateRegistry;

    WireDefinition wireDefinition = parse.contextStackFind(WireDefinition.class);
    String templateRegistryName;

    if (wireDefinition != null
      && (templateRegistryName = wireDefinition.getDescriptorName(MailTemplateRegistry.class)) != null) {
      templateRegistryDescriptor = (ProvidedObjectDescriptor) wireDefinition.getDescriptor(templateRegistryName);
      templateRegistry = (MailTemplateRegistry) templateRegistryDescriptor.getProvidedObject();
    }
    else {
      templateRegistry = new MailTemplateRegistry();
      templateRegistryDescriptor = new ProvidedObjectDescriptor(templateRegistry, true);
    }

    // create the mail template and add it to the registry
    MailTemplate mailTemplate = parseMailTemplate(element, parse);
    String templateName = XmlUtil.attribute(element, "name", parse);
    templateRegistry.addTemplate(templateName, mailTemplate);

    return templateRegistryDescriptor;
  }

  public static MailTemplate parseMailTemplate(Element element, Parse parse) {
    MailTemplate mailTemplate = new MailTemplate();
    mailTemplate.setLanguage(XmlUtil.attribute(element, "language"));

    mailTemplate.setFrom(parseRecipientTemplate(element, "from", parse));
    mailTemplate.setTo(parseRecipientTemplate(element, "to", parse));
    mailTemplate.setCc(parseRecipientTemplate(element, "cc", parse));
    mailTemplate.setBcc(parseRecipientTemplate(element, "bcc", parse));

    Element subjectElement = XmlUtil.element(element, "subject");
    if (subjectElement != null) {
      mailTemplate.setSubject(XmlUtil.getContentText(subjectElement));
    }

    Element textElement = XmlUtil.element(element, "text");
    if (textElement != null) {
      mailTemplate.setText(XmlUtil.getContentText(textElement));
    }

    Element htmlElement = XmlUtil.element(element, "html");
    if (htmlElement != null) {
      // extract child nodes from html element
      DocumentFragment fragment = htmlElement.getOwnerDocument().createDocumentFragment();
      for (Node child = htmlElement.getFirstChild(), next; child != null; child = next) {
        next = child.getNextSibling();
        fragment.appendChild(child);
      }
      mailTemplate.setHtml(XmlUtil.toString(fragment));
    }

    // attachments
    Element attachmentsElement = XmlUtil.element(element, "attachments");
    if (attachmentsElement != null) {
      for (Element attachmentElement : XmlUtil.elements(attachmentsElement, "attachment")) {
        AttachmentTemplate attachmentTemplate = new AttachmentTemplate();
        mailTemplate.addAttachmentTemplate(attachmentTemplate);

        attachmentTemplate.setName(XmlUtil.attribute(attachmentElement, "name"));
        attachmentTemplate.setDescription(XmlUtil.attribute(attachmentElement, "description"));

        // expression attribute
        Attr expressionAttr = attachmentElement.getAttributeNode("expression");
        if (expressionAttr != null) {
          attachmentTemplate.setExpression(expressionAttr.getValue());
          attachmentTemplate.setMimeType(XmlUtil.attribute(attachmentElement, "mime-type"));
        }
        else {
          // expression element
          Element expressionElem = XmlUtil.element(attachmentElement, "expression");
          if (expressionElem != null) {
            attachmentTemplate.setExpression(XmlUtil.getContentText(expressionElem));
            attachmentTemplate.setMimeType(XmlUtil.attribute(attachmentElement, "mime-type"));
          }
          else {
            // file
            Attr file = attachmentElement.getAttributeNode("file");
            if (file != null) {
              attachmentTemplate.setFile(file.getValue());
            }
            else {
              // url
              Attr url = attachmentElement.getAttributeNode("url");
              if (url != null) {
                attachmentTemplate.setUrl(url.getValue());
              }
              else {
                // resource
                Attr resource = attachmentElement.getAttributeNode("resource");
                if (resource != null) attachmentTemplate.setResource(resource.getValue());
              }
            }
          }
        }
      }
    }
    return mailTemplate;
  }

  private static AddressTemplate parseRecipientTemplate(Element element, String tagName,
    Parse parse) {
    Element recipientElement = XmlUtil.element(element, tagName);
    if (recipientElement == null) return null;

    String addresses = XmlUtil.attribute(recipientElement, "addresses");
    String users = XmlUtil.attribute(recipientElement, "users");
    String groups = XmlUtil.attribute(recipientElement, "groups");

    if (addresses == null && users == null && groups == null) {
      parse.addProblem(tagName + " does not specify any recipient", element);
    }

    AddressTemplate addressTemplate = new AddressTemplate();
    addressTemplate.setAddresses(addresses);
    addressTemplate.setUsers(users);
    addressTemplate.setGroups(groups);
    return addressTemplate;
  }
}

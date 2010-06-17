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
package org.jbpm.pvm.internal.util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.jbpm.api.JbpmException;
import org.jbpm.pvm.internal.xml.Parse;

/**
 * convenience methods to make reading org.w3c.dom models easier.
 * @author Tom Baeyens
 */
public class XmlUtil {

  private XmlUtil() {
    // hide default constructor to prevent instantiation
  }

  public static List<Element> elements(Element element, String tagName) {
    if (element == null || !element.hasChildNodes()) {
      return Collections.emptyList();
    }

    List<Element> elements = new ArrayList<Element>();
    for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        Element childElement = (Element) child;
        String childTagName = childElement.getLocalName();

        if (tagName.equals(childTagName)) elements.add(childElement);
      }
    }
    return elements;
  }

  public static List<Element> elements(Element element, Set<String> allowedTagNames) {
    if (element == null || !element.hasChildNodes()) {
      return Collections.emptyList();
    }

    List<Element> elements = new ArrayList<Element>();
    for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        Element childElement = (Element) child;
        if (allowedTagNames.contains(child.getLocalName())) {
          elements.add(childElement);
        }
      }
    }
    return elements;
  }

  public static Element element(Element element, String tagName) {
    return element(element, tagName, null);
  }

  public static Element element(Element element, String tagName, Parse parse) {
    if (element != null && element.hasChildNodes()) {
      for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
        if (child.getNodeType() == Node.ELEMENT_NODE && tagName.equals(child.getLocalName())) {
          return (Element) child;
        }
      }
    }

    if (parse != null) {
      parse.addProblem("missing element " + tagName, element);
    }
    return null;
  }

  public static List<Element> elements(Element element) {
    if (element == null || !element.hasChildNodes()) {
      return Collections.emptyList();
    }

    List<Element> elements = new ArrayList<Element>();
    for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        elements.add((Element) child);
      }
    }
    return elements;
  }

  public static List<Element> elements(Element element, String namespace, String localName) {
    if (element == null || !element.hasChildNodes()) {
      return Collections.emptyList();
    }

    List<Element> elements = new ArrayList<Element>();
    for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
      String childNamespace = element.getNamespaceURI();
      if (child.getNodeType() == Node.ELEMENT_NODE
        && (namespace != null ? namespace.equals(childNamespace) : childNamespace == null)
        && localName.equals(element.getLocalName())) {
        elements.add((Element) child);
      }
    }
    return elements;
  }

  public static List<Element> elementsQName(Element element, Set<QName> allowedTagNames) {
    if (element == null || !element.hasChildNodes()) {
      return Collections.emptyList();
    }

    List<Element> elements = new ArrayList<Element>();
    for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        Element childElement = (Element) child;
        QName childQName = new QName(childElement.getNamespaceURI(),
          childElement.getLocalName());
        if (allowedTagNames.contains(childQName)) {
          elements.add(childElement);
        }
      }
    }
    return elements;
  }

  public static Element element(Element element) {
    if (element != null && element.hasChildNodes()) {
      for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
        if (child.getNodeType() == Node.ELEMENT_NODE) {
          return (Element) child;
        }
      }
    }
    return null;
  }

  public static String toString(Node node) {
    if (node == null) return "null";

    try {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

      StringWriter stringWriter = new StringWriter();
      transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
      return stringWriter.toString();
    }
    catch (TransformerException e) {
      throw new JbpmException("could not transform dom node to string", e);
    }
  }

  public static String getContentText(Element element) {
    return element.getTextContent();
  }

  public static boolean isTextOnly(Element element) {
    for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child.getNodeType() == Node.ELEMENT_NODE) return false;
    }
    return true;
  }

  public static List<Attr> attributes(Element element) {
    NamedNodeMap attributeMap = element.getAttributes();
    if ((attributeMap == null) || (attributeMap.getLength() == 0)) {
      return Collections.emptyList();
    }

    List<Attr> attributes = new ArrayList<Attr>();
    for (int i = 0; i < attributeMap.getLength(); i++) {
      attributes.add((Attr) attributeMap.item(i));
    }

    return attributes;
  }

  public static List<Node> contents(Element element) {
    if (element == null || !element.hasChildNodes()) {
      return Collections.emptyList();
    }

    List<Node> contents = new ArrayList<Node>();
    for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
      contents.add(child);
    }
    return contents;
  }

  /**
   * retrieves an attribute value by name.
   * @return the attribute value or <code>null</code> if there is no such attribute
   */
  public static String attribute(Element element, String attributeName) {
    Attr attribute = element.getAttributeNode(attributeName);
    return attribute != null ? attribute.getValue() : null;
  }

  /**
   * retrieves an attribute value by name.
   * @return the attribute value or <code>defaultValue</code> if there is no such attribute
   */
  public static String attribute(Element element, String attributeName, String defaultValue) {
    return attribute(element, attributeName, null, defaultValue);
  }

  /**
   * retrieves an attribute value by name. if the attribute is not present, a problem will be
   * added to the parse.
   * @return the attribute value or <code>null</code> if there is no such attribute
   */
  public static String attribute(Element element, String attributeName, Parse parse) {
    return attribute(element, attributeName, parse, null);
  }

  /**
   * retrieves an attribute value by name. if the attribute is not present, a problem will be
   * added to the parse.
   * @return the attribute value or <code>defaultValue</code> if there is no such attribute
   */
  public static String attribute(Element element, String attributeName, Parse parse,
    String defaultValue) {
    Attr attribute = element.getAttributeNode(attributeName);
    if (attribute != null) {
      String value = attribute.getValue();
      if (value.length() == 0) {
        parse.addProblem("attribute <"+element.getLocalName()+" "+attributeName+"=\"\" is empty", element);
      }
      return value;
    }

    if (parse != null) {
      parse.addProblem("attribute <"+element.getLocalName()+" "+attributeName+"=\"...\" is required", element);
    }
    return defaultValue;
  }

  /** retrieves an attribute value by name. */
  public static Integer attributeInteger(Element element, String attributeName, Parse parse) {
    Attr attribute = element.getAttributeNode(attributeName);
    if (attribute != null) {
      String attributeValue = attribute.getValue();
      try {
        return Integer.parseInt(attributeValue);
      }
      catch (NumberFormatException e) {
        parse.addProblem(errorMessageAttribute(element, attributeName, attributeValue, e.getMessage()), element);
      }
    }
    return null;
  }

  /** parse an attribute as an boolean. */
  public static Boolean attributeBoolean(Element element, String attributeName, Parse parse) {
    Attr attribute = element.getAttributeNode(attributeName);
    if (attribute != null) {
      String attributeValue = attribute.getValue();
      Boolean value = parseBooleanValue(attributeValue);
      if (value != null) return value;

      String message = errorMessageAttribute(element, attributeName, attributeValue,
        "value not in {true, enabled, on, false, disabled, off}");
      parse.addProblem(message, element);
    }
    return null;
  }

  public static Boolean parseBooleanValue(String valueText) {
    if (valueText != null) {
      // if we have to check for value true
      if ("true".equals(valueText) || "enabled".equals(valueText) || "on".equals(valueText)) {
        return Boolean.TRUE;
      }
      if ("false".equals(valueText) || "disabled".equals(valueText) || "off".equals(valueText)) {
        return Boolean.FALSE;
      }
    }
    return null;
  }

  public static String errorMessageAttribute(Element element, String attributeName,
    String attributeValue, String message) {
    return "attribute <" + element.getLocalName() + " " + attributeName + "=\""
      + attributeValue + "\" " + message;
  }

  public static List<String> parseList(Element element, String singularTagName) {
    // a null value for text represents a wildcard
    String text = XmlUtil.attribute(element, singularTagName + "s");
    // convert '*' into the text null value, which indicates a wildcard
    if ("*".equals(text)) {
      text = null;
    }
    if (element.hasAttribute(singularTagName)) {
      String eventText = element.getAttribute(singularTagName);
      text = (text == null ? eventText : text + "," + eventText);
    }
    List<String> eventNames = parseCommaSeparatedList(text);
    return eventNames;
  }

  /**
   * parses comma or space separated list. A null return value means a wildcard.
   * @return List of tokens or null if the commaSeparatedListText is null, '*', or empty
   */
  public static List<String> parseCommaSeparatedList(String commaSeparatedListText) {
    List<String> entries = null;
    if (commaSeparatedListText != null) {
      if (!"*".equals(commaSeparatedListText)) {
        StringTokenizer tokenizer = new StringTokenizer(commaSeparatedListText, ", ");
        while (tokenizer.hasMoreTokens()) {
          if (entries == null) {
            entries = new ArrayList<String>();
          }
          entries.add(tokenizer.nextToken());
        }
      }
    }
    return entries;
  }

  public static class NamespaceValue {

    public String prefix;
    public String localPart;

    public NamespaceValue(String prefix, String localPart) {
      this.prefix = prefix;
      this.localPart = localPart;
    }
  }

  public static NamespaceValue attributeNamespaceValue(Element element, String attributeName) {
    NamespaceValue namespaceValue = null;
    String text = attribute(element, attributeName);
    if (text != null) {
      int colonIndex = text.indexOf(':');
      if (colonIndex == -1) {
        namespaceValue = new NamespaceValue(null, text);
      }
      else {
        String prefix = text.substring(0, colonIndex);
        String localPart = null;
        if (text.length() > colonIndex + 1) {
          localPart = text.substring(colonIndex + 1);
        }
        namespaceValue = new NamespaceValue(prefix, localPart);
      }
    }
    return namespaceValue;
  }

  public static QName attributeQName(Element element, String attributeName) {
    QName qname = null;

    NamespaceValue namespaceValue = attributeNamespaceValue(element, attributeName);
    String text = attribute(element, attributeName);
    if (namespaceValue != null) {
      if (namespaceValue.prefix == null) {
        qname = new QName(text);
      }
      else {
        String uri = element.lookupNamespaceURI(namespaceValue.prefix);
        if (uri == null) {
          throw new JbpmException("unknown prefix in qname " + text);
        }
        else if (namespaceValue.localPart == null) {
          throw new JbpmException("no local part in qname " + text);
        }
        else {
          qname = new QName(uri, namespaceValue.localPart, namespaceValue.prefix);
        }
      }
    }
    return qname;
  }

  public static QName getQNameFromString(Element element, String qnameAsString) {
    if (qnameAsString == null || element == null) {
      return null;
    }
    int colonIndex = qnameAsString.indexOf(":");
    String prefix = qnameAsString.substring(0, colonIndex);
    String localName = qnameAsString.substring(colonIndex + 1);
    String ns = getNamespaceURI(element, prefix);
    return new QName(ns, localName, prefix);
  }

  public static String getNamespaceURI(final org.w3c.dom.Node n, final String prefix) {
    Node prefixDeclaration = n.getAttributes().getNamedItem("xmlns:" + prefix);
    if (prefixDeclaration != null) {
      // we have found the good NameSpace
      return prefixDeclaration.getNodeValue();
    }
    // we have found the good NameSpace
    // we look for the NameSpace in the parent Node
    return getNamespaceURI(n.getParentNode(), prefix);
  }
}
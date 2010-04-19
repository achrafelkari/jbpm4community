/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: DebugDomBuilder.java 1434 2008-07-01 10:32:10Z heiko.braun@jboss.com $
 */
package org.jbpm.pvm.internal.xml;

import java.util.Stack;
import java.util.Vector;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/** builds the dom model from SAX events, optionally adding the line and 
 * column number as attributes to every element. */
public class DomBuilder extends DefaultHandler implements ContentHandler, LexicalHandler {  /** Root document */

  public Document document;
  
  protected String lineAttributeName = "line";
  protected String columnAttributeName = "column";

  /** Current activity */
  protected Node currentNode = null;

  /** The root activity */
  protected Node root = null;

  /** The next sibling activity */
  protected Node nextSibling = null;

  /** First activity of document fragment or null if not a DocumentFragment */
  public DocumentFragment docFrag = null;

  /** Vector of element activities */
  protected Stack elemStack = new Stack();

  /** Namespace support */
  protected Vector prefixMappings = new Vector();

  /** to obtain the line number information */
  protected Locator locator = null;

  /**
   * Get the root document or DocumentFragment of the DOM being created.
   * 
   * @return The root document or document fragment if not null
   */
  public Node getRootDocument() {
    return (null != this.docFrag) ? (Node) this.docFrag : (Node) this.document;
  }

  /**
   * Get the root activity of the DOM tree.
   */
  public Node getRootNode() {
    return this.root;
  }

  /**
   * Get the activity currently being processed.
   * 
   * @return the current activity being processed
   */
  public Node getCurrentNode() {
    return this.currentNode;
  }

  /**
   * Set the next sibling activity, which is where the result activities should be
   * inserted before.
   * 
   * @param nextSibling
   *          the next sibling activity.
   */
  public void setNextSibling(Node nextSibling) {
    this.nextSibling = nextSibling;
  }

  /**
   * Return the next sibling activity.
   * 
   * @return the next sibling activity.
   */
  public Node getNextSibling() {
    return this.nextSibling;
  }

  /**
   * Return null since there is no Writer for this class.
   * 
   * @return null
   */
  public java.io.Writer getWriter() {
    return null;
  }

  /**
   * Append a activity to the current container.
   * 
   * @param newNode
   *          New activity to append
   */
  protected void append(Node newNode) throws org.xml.sax.SAXException {

    Node currentNode = this.currentNode;

    if (null != currentNode) {
      if (currentNode == this.root && this.nextSibling != null)
        currentNode.insertBefore(newNode, this.nextSibling);
      else
        currentNode.appendChild(newNode);

      // System.out.println(newNode.getNodeName());
    } else if (null != this.docFrag) {
      if (this.nextSibling != null)
        this.docFrag.insertBefore(newNode, this.nextSibling);
      else
        this.docFrag.appendChild(newNode);
    } else {
      boolean ok = true;
      short type = newNode.getNodeType();

      if (type == Node.TEXT_NODE) {
        String data = newNode.getNodeValue();

        if ((null != data) && (data.trim().length() > 0)) {
          throw new org.xml.sax.SAXException("Warning: can't output text before document element!  Ignoring...");
        }

        ok = false;
      } else if (type == Node.ELEMENT_NODE) {
        if (this.document.getDocumentElement() != null) {
          ok = false;

          throw new org.xml.sax.SAXException("Can't have more than one root on a DOM!");
        }
      }

      if (ok) {
        if (this.nextSibling != null)
          this.document.insertBefore(newNode, this.nextSibling);
        else
          this.document.appendChild(newNode);
      }
    }
  }

  /**
   * Receive an object for locating the origin of SAX document events.
   * 
   * <p>
   * SAX parsers are strongly encouraged (though not absolutely required) to
   * supply a locator: if it does so, it must supply the locator to the
   * application by invoking this method before invoking any of the other
   * methods in the ContentHandler interface.
   * </p>
   * 
   * <p>
   * The locator allows the application to determine the end position of any
   * document-related event, even if the parser is not reporting an error.
   * Typically, the application will use this information for reporting its own
   * errors (such as character content that does not match an application's
   * business rules). The information returned by the locator is probably not
   * sufficient for use with a search engine.
   * </p>
   * 
   * <p>
   * Note that the locator will return correct information only during the
   * invocation of the events in this interface. The application should not
   * attempt to use it at any other time.
   * </p>
   * 
   * @param locator
   *          An object that can return the location of any SAX document event.
   * @see org.xml.sax.Locator
   */
  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
    // No action for the moment.
  }

  /**
   * Receive notification of the beginning of a document.
   * 
   * <p>
   * The SAX parser will invoke this method only once, before any other methods
   * in this interface or in DTDHandler (except for setDocumentLocator).
   * </p>
   */
  public void startDocument() throws org.xml.sax.SAXException {

    // No action for the moment.
  }

  /**
   * Receive notification of the end of a document.
   * 
   * <p>
   * The SAX parser will invoke this method only once, and it will be the last
   * method invoked during the parse. The parser shall not invoke this method
   * until it has either abandoned parsing (because of an unrecoverable error)
   * or reached the end of input.
   * </p>
   */
  public void endDocument() throws org.xml.sax.SAXException {

    // No action for the moment.
  }

  /**
   * Receive notification of the beginning of an element.
   * 
   * <p>
   * The Parser will invoke this method at the beginning of every element in the
   * XML document; there will be a corresponding endElement() event for every
   * startElement() event (even when the element is empty). All of the element's
   * content will be reported, in order, before the corresponding endElement()
   * event.
   * </p>
   * 
   * <p>
   * If the element name has a namespace prefix, the prefix will still be
   * attached. Note that the attribute list provided will contain only
   * attributes with explicit values (specified or defaulted): #IMPLIED
   * attributes will be omitted.
   * </p>
   * 
   * 
   * @param ns
   *          The namespace of the activity
   * @param localName
   *          The local part of the qualified name
   * @param name
   *          The element name.
   * @param atts
   *          The attributes attached to the element, if any.
   * @see #endElement
   * @see org.xml.sax.Attributes
   */
  public void startElement(String ns, String localName, String name, Attributes atts) throws org.xml.sax.SAXException {

    Element elem;

    // Note that the namespace-aware call must be used to correctly
    // construct a Level 2 DOM, even for non-namespaced activities.
    if ((null == ns) || (ns.length() == 0))
      elem = this.document.createElementNS(null, name);
    else
      elem = this.document.createElementNS(ns, name);

    append(elem);

    try {
      int nAtts = atts.getLength();

      if (0 != nAtts) {
        for (int i = 0; i < nAtts; i++) {

          // System.out.println("type " + atts.getType(i) + " name " +
          // atts.getLocalName(i) );
          // First handle a possible ID attribute
          if (atts.getType(i).equalsIgnoreCase("ID"))
            setIDAttribute(atts.getValue(i), elem);

          String attrNS = atts.getURI(i);

          if ("".equals(attrNS))
            attrNS = null; // DOM represents no-namespace as null

          // System.out.println("attrNS: "+attrNS+", localName:
          // "+atts.getQName(i)
          // +", qname: "+atts.getQName(i)+", value: "+atts.getValue(i));
          // Crimson won't let us set an xmlns: attribute on the DOM.
          String attrQName = atts.getQName(i);

          // In SAX, xmlns[:] attributes have an empty namespace, while in DOM
          // they
          // should have the xmlns namespace
          if (attrQName.startsWith("xmlns:") || attrQName.equals("xmlns")) {
            attrNS = "http://www.w3.org/2000/xmlns/";
          }

          // ALWAYS use the DOM Level 2 call!
          elem.setAttributeNS(attrNS, attrQName, atts.getValue(i));
        }
      }

      if (locator!=null) {
        int lineNumber = locator.getLineNumber();
        int columnNumber = locator.getColumnNumber();

        if (lineAttributeName!=null) {
          elem.setUserData(lineAttributeName, lineNumber, null);
        }
        if (columnAttributeName!=null) {
          elem.setUserData(columnAttributeName, columnNumber, null);
        }
      }


      /*
       * Adding namespace activities to the DOM tree;
       */
      int nDecls = this.prefixMappings.size();

      String prefix, declURL;

      for (int i = 0; i < nDecls; i += 2) {
        prefix = (String) this.prefixMappings.elementAt(i);

        if (prefix == null)
          continue;

        declURL = (String) this.prefixMappings.elementAt(i + 1);

        elem.setAttributeNS("http://www.w3.org/2000/xmlns/", prefix, declURL);
      }

      this.prefixMappings.clear();

      // append(elem);

      this.elemStack.push(elem);

      this.currentNode = elem;

      // append(elem);
    } catch (java.lang.Exception de) {
      // de.printStackTrace();
      throw new org.xml.sax.SAXException(de);
    }

  }

  /**
   * 
   * 
   * 
   * Receive notification of the end of an element.
   * 
   * <p>
   * The SAX parser will invoke this method at the end of every element in the
   * XML document; there will be a corresponding startElement() event for every
   * endElement() event (even when the element is empty).
   * </p>
   * 
   * <p>
   * If the element name has a namespace prefix, the prefix will still be
   * attached to the name.
   * </p>
   * 
   * 
   * @param ns
   *          the namespace of the element
   * @param localName
   *          The local part of the qualified name of the element
   * @param name
   *          The element name
   */
  public void endElement(String ns, String localName, String name) throws org.xml.sax.SAXException {
    this.elemStack.pop();
    this.currentNode = this.elemStack.isEmpty() ? null : (Node) this.elemStack.peek();
  }

  /**
   * Set an ID string to activity association in the ID table.
   * 
   * @param id
   *          The ID string.
   * @param elem
   *          The associated ID.
   */
  public void setIDAttribute(String id, Element elem) {

    // Do nothing. This method is meant to be overiden.
  }

  /**
   * Receive notification of character data.
   * 
   * <p>
   * The Parser will call this method to report each chunk of character data.
   * SAX parsers may return all contiguous character data in a single chunk, or
   * they may split it into several chunks; however, all of the characters in
   * any single event must come from the same external entity, so that the
   * Locator provides useful information.
   * </p>
   * 
   * <p>
   * The application must not attempt to read from the array outside of the
   * specified range.
   * </p>
   * 
   * <p>
   * Note that some parsers will report whitespace using the
   * ignorableWhitespace() method rather than this one (validating parsers must
   * do so).
   * </p>
   * 
   * @param ch
   *          The characters from the XML document.
   * @param start
   *          The start position in the array.
   * @param length
   *          The number of characters to read from the array.
   * @see #ignorableWhitespace
   * @see org.xml.sax.Locator
   */
  public void characters(char ch[], int start, int length) throws org.xml.sax.SAXException {
    if (isOutsideDocElem() && isWhiteSpace(ch, start, length))
      return; // avoid DOM006 Hierarchy request error

    if (this.inCData) {
      cdata(ch, start, length);

      return;
    }

    String s = new String(ch, start, length);
    Node childNode;
    childNode = this.currentNode != null ? this.currentNode.getLastChild() : null;
    if (childNode != null && childNode.getNodeType() == Node.TEXT_NODE) {
      ((Text) childNode).appendData(s);
    } else {
      Text text = this.document.createTextNode(s);
      append(text);
    }
  }

  /**
   * If available, when the disable-output-escaping attribute is used, output
   * raw text without escaping. A PI will be inserted in front of the activity with
   * the name "lotusxsl-next-is-raw" and a value of "formatter-to-dom".
   * 
   * @param ch
   *          Array containing the characters
   * @param start
   *          Index to start of characters in the array
   * @param length
   *          Number of characters in the array
   */
  public void charactersRaw(char ch[], int start, int length) throws org.xml.sax.SAXException {
    if (isOutsideDocElem() && isWhiteSpace(ch, start, length))
      return; // avoid DOM006 Hierarchy request error

    String s = new String(ch, start, length);

    append(this.document.createProcessingInstruction("xslt-next-is-raw", "formatter-to-dom"));
    append(this.document.createTextNode(s));
  }

  /**
   * Report the beginning of an entity.
   * 
   * The start and end of the document entity are not reported. The start and
   * end of the external DTD subset are reported using the pseudo-name "[dtd]".
   * All other events must be properly nested within start/end entity events.
   * 
   * @param name
   *          The name of the entity. If it is a parameter entity, the name will
   *          begin with '%'.
   * @see #endEntity
   * @see org.xml.sax.ext.DeclHandler#internalEntityDecl
   * @see org.xml.sax.ext.DeclHandler#externalEntityDecl
   */
  public void startEntity(String name) throws org.xml.sax.SAXException {

    // Almost certainly the wrong behavior...
    // entityReference(name);
  }

  /**
   * Report the end of an entity.
   * 
   * @param name
   *          The name of the entity that is ending.
   * @see #startEntity
   */
  public void endEntity(String name) throws org.xml.sax.SAXException {
  }

  /**
   * Receive notivication of a entityReference.
   * 
   * @param name
   *          name of the entity reference
   */
  public void entityReference(String name) throws org.xml.sax.SAXException {
    append(this.document.createEntityReference(name));
  }

  /**
   * Receive notification of ignorable whitespace in element content.
   * 
   * <p>
   * Validating Parsers must use this method to report each chunk of ignorable
   * whitespace (see the W3C XML 1.0 recommendation, section 2.10):
   * non-validating parsers may also use this method if they are capable of
   * parsing and using content models.
   * </p>
   * 
   * <p>
   * SAX parsers may return all contiguous whitespace in a single chunk, or they
   * may split it into several chunks; however, all of the characters in any
   * single event must come from the same external entity, so that the Locator
   * provides useful information.
   * </p>
   * 
   * <p>
   * The application must not attempt to read from the array outside of the
   * specified range.
   * </p>
   * 
   * @param ch
   *          The characters from the XML document.
   * @param start
   *          The start position in the array.
   * @param length
   *          The number of characters to read from the array.
   * @see #characters
   */
  public void ignorableWhitespace(char ch[], int start, int length) throws org.xml.sax.SAXException {
    if (isOutsideDocElem())
      return; // avoid DOM006 Hierarchy request error

    String s = new String(ch, start, length);

    append(this.document.createTextNode(s));
  }

  /**
   * Tell if the current activity is outside the document element.
   * 
   * @return true if the current activity is outside the document element.
   */
  private boolean isOutsideDocElem() {
    return (null == this.docFrag) && this.elemStack.size() == 0 && (null == this.currentNode || this.currentNode.getNodeType() == Node.DOCUMENT_NODE);
  }

  /**
   * Receive notification of a processing instruction.
   * 
   * <p>
   * The Parser will invoke this method once for each processing instruction
   * found: note that processing instructions may occur before or after the main
   * document element.
   * </p>
   * 
   * <p>
   * A SAX parser should never report an XML declaration (XML 1.0, section 2.8)
   * or a text declaration (XML 1.0, section 4.3.1) using this method.
   * </p>
   * 
   * @param target
   *          The processing instruction target.
   * @param data
   *          The processing instruction data, or null if none was supplied.
   */
  public void processingInstruction(String target, String data) throws org.xml.sax.SAXException {
    append(this.document.createProcessingInstruction(target, data));
  }

  /**
   * Report an XML comment anywhere in the document.
   * 
   * This callback will be used for comments inside or outside the document
   * element, including comments in the external DTD subset (if read).
   * 
   * @param ch
   *          An array holding the characters in the comment.
   * @param start
   *          The starting position in the array.
   * @param length
   *          The number of characters to use from the array.
   */
  public void comment(char ch[], int start, int length) throws org.xml.sax.SAXException {
    append(this.document.createComment(new String(ch, start, length)));
  }

  /** Flag indicating that we are processing a CData section */
  protected boolean inCData = false;

  /**
   * Report the start of a CDATA section.
   * 
   * @see #endCDATA
   */
  public void startCDATA() throws org.xml.sax.SAXException {
    this.inCData = true;
    append(this.document.createCDATASection(""));
  }

  /**
   * Report the end of a CDATA section.
   * 
   * @see #startCDATA
   */
  public void endCDATA() throws org.xml.sax.SAXException {
    this.inCData = false;
  }

  /**
   * Receive notification of cdata.
   * 
   * <p>
   * The Parser will call this method to report each chunk of character data.
   * SAX parsers may return all contiguous character data in a single chunk, or
   * they may split it into several chunks; however, all of the characters in
   * any single event must come from the same external entity, so that the
   * Locator provides useful information.
   * </p>
   * 
   * <p>
   * The application must not attempt to read from the array outside of the
   * specified range.
   * </p>
   * 
   * <p>
   * Note that some parsers will report whitespace using the
   * ignorableWhitespace() method rather than this one (validating parsers must
   * do so).
   * </p>
   * 
   * @param ch
   *          The characters from the XML document.
   * @param start
   *          The start position in the array.
   * @param length
   *          The number of characters to read from the array.
   * @see #ignorableWhitespace
   * @see org.xml.sax.Locator
   */
  public void cdata(char ch[], int start, int length) throws org.xml.sax.SAXException {
    if (isOutsideDocElem() && isWhiteSpace(ch, start, length))
      return; // avoid DOM006 Hierarchy request error

    String s = new String(ch, start, length);

    CDATASection section = (CDATASection) this.currentNode.getLastChild();
    section.appendData(s);
  }

  /**
   * Report the start of DTD declarations, if any.
   * 
   * Any declarations are assumed to be in the internal subset unless otherwise
   * indicated.
   * 
   * @param name
   *          The document type name.
   * @param publicId
   *          The declared public identifier for the external DTD subset, or
   *          null if none was declared.
   * @param systemId
   *          The declared system identifier for the external DTD subset, or
   *          null if none was declared.
   * @see #endDTD
   * @see #startEntity
   */
  public void startDTD(String name, String publicId, String systemId) throws org.xml.sax.SAXException {

    // Do nothing for now.
  }

  /**
   * Report the end of DTD declarations.
   * 
   * @see #startDTD
   */
  public void endDTD() throws org.xml.sax.SAXException {

    // Do nothing for now.
  }

  /**
   * Begin the scope of a prefix-URI Namespace mapping.
   * 
   * <p>
   * The information from this event is not necessary for normal Namespace
   * processing: the SAX XML reader will automatically replace prefixes for
   * element and attribute names when the http://xml.org/sax/features/namespaces
   * feature is true (the default).
   * </p>
   * 
   * <p>
   * There are cases, however, when applications need to use prefixes in
   * character data or in attribute values, where they cannot safely be expanded
   * automatically; the start/endPrefixMapping event supplies the information to
   * the application to expand prefixes in those contexts itself, if necessary.
   * </p>
   * 
   * <p>
   * Note that start/endPrefixMapping events are not guaranteed to be properly
   * nested relative to each-other: all startPrefixMapping events will occur
   * before the corresponding startElement event, and all endPrefixMapping
   * events will occur after the corresponding endElement event, but their order
   * is not guaranteed.
   * </p>
   * 
   * @param prefix
   *          The Namespace prefix being declared.
   * @param uri
   *          The Namespace URI the prefix is mapped to.
   * @see #endPrefixMapping
   * @see #startElement
   */
  public void startPrefixMapping(String prefix, String uri) throws org.xml.sax.SAXException {
    if (null == prefix || prefix.equals(""))
      prefix = "xmlns";
    else
      prefix = "xmlns:" + prefix;
    this.prefixMappings.addElement(prefix);
    this.prefixMappings.addElement(uri);
  }

  /**
   * End the scope of a prefix-URI mapping.
   * 
   * <p>
   * See startPrefixMapping for details. This event will always occur after the
   * corresponding endElement event, but the order of endPrefixMapping events is
   * not otherwise guaranteed.
   * </p>
   * 
   * @param prefix
   *          The prefix that was being mapping.
   * @see #startPrefixMapping
   * @see #endElement
   */
  public void endPrefixMapping(String prefix) throws org.xml.sax.SAXException {
  }

  /**
   * Receive notification of a skipped entity.
   * 
   * <p>
   * The Parser will invoke this method once for each entity skipped.
   * Non-validating processors may skip entities if they have not seen the
   * declarations (because, for example, the entity was declared in an external
   * DTD subset). All processors may skip external entities, depending on the
   * values of the http://xml.org/sax/features/external-general-entities and the
   * http://xml.org/sax/features/external-parameter-entities properties.
   * </p>
   * 
   * @param name
   *          The name of the skipped entity. If it is a parameter entity, the
   *          name will begin with '%'.
   */
  public void skippedEntity(String name) throws org.xml.sax.SAXException {
  }

  /**
   * Returns whether the specified <var>ch</var> conforms to the XML 1.0
   * definition of whitespace. Refer to <A
   * href="http://www.w3.org/TR/1998/REC-xml-19980210#NT-S"> the definition of
   * <CODE>S</CODE></A> for details.
   * 
   * @param ch
   *          Character to check as XML whitespace.
   * @return =true if <var>ch</var> is XML whitespace; otherwise =false.
   */
  public static boolean isWhiteSpace(char ch) {
    return (ch == 0x20) || (ch == 0x09) || (ch == 0xD) || (ch == 0xA);
  }

  /**
   * Tell if the string is whitespace.
   * 
   * @param ch
   *          Character array to check as XML whitespace.
   * @param start
   *          Start index of characters in the array
   * @param length
   *          Number of characters in the array
   * @return True if the characters in the array are XML whitespace; otherwise,
   *         false.
   */
  public static boolean isWhiteSpace(char ch[], int start, int length) {

    int end = start + length;

    for (int s = start; s < end; s++) {
      if (!isWhiteSpace(ch[s]))
        return false;
    }

    return true;
  }

  public void setLineAttributeName(String lineAttributeName) {
    this.lineAttributeName = lineAttributeName;
  }
  public void setColumnAttributeName(String columnAttributeName) {
    this.columnAttributeName = columnAttributeName;
  }
  public String getLineAttributeName() {
    return lineAttributeName;
  }
  public String getColumnAttributeName() {
    return columnAttributeName;
  }
  public Document getDocument() {
    return document;
  }
  public void setDocument(Document document) {
    this.document = document;
  }
}

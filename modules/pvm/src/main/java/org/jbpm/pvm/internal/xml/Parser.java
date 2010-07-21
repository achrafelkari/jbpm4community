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
package org.jbpm.pvm.internal.xml;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.stream.StreamInput;
import org.jbpm.pvm.internal.util.UrlEntity;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.descriptor.ArgDescriptor;
import org.jbpm.pvm.internal.wire.xml.WireParser;

/** makes typical usage of JAXP more convenient, adds a binding framework, 
 * entity resolution and error handling.
 * 
 * <h2>Purpose</h2>
 * <p>This is a base parser for the common pattern where first JAXP is used
 * to parse xml into a Document Object Model (DOM), and then, this DOM is
 * examined to build a domain model object. The main purpose of this parser 
 * is to serve as a base class for implementing such parsers and to provide 
 * a more convenient API for working with JAXP.
 * </p>
 * 
 * <p>A {@link Parser} is a thread safe object.  For each parse operation, a 
 * new {@link Parse} object is created with method {@link #createParse()}.  
 * Then the parse object is used to specify the input source, execute the 
 * parse operation and extract the results. 
 * </p>
 * 
 * <p>{@link Binding}s capture parsing of a certain element type. This way,
 * the parser becomes more modular and customizable.
 * </p>
 * 
 * <p>{@link Entity Entities} are schema's that specify the grammar of the 
 * XML files that are parsed by the parser. 
 * </p>
 * 
 * <h2>API Usage</h2>
 * <p>Parsers can be customized by inheritance (that will be covered below), 
 * but a parser can also be used as is:
 * </p>
 *
 * <pre><i> 1 </i>|   static Parser parser = new Parser();
 *<i> 2 </i>| 
 *<i> 3 </i>|   void someMethod() {
 *<i> 4 </i>|     MyDomainObject mdo = (MyDomainObject) parser
 *<i> 5 </i>|             .createParse()
 *<i> 6 </i>|             .setString(myXmlString)
 *<i> 7 </i>|             .execute()
 *<i> 8 </i>|             .checkProblems()
 *<i> 9 </i>|             .getDocumentObject();
 *<i>10 </i>|   }
 * </pre>
 * 
 * <p><b>line 1</b> shows that a single parser can be used for all threads as 
 * the parser is maintained in a static member field.
 * </p>
 *
 * <p><b>line 5</b> shows that a new parse operation is always started with 
 * the {@link #createParse()} operation.  The {@link Parse} object that is 
 * returned will maintain all data that is related to that single parse 
 * operation. 
 * </p>
 *
 * <p><b>line 6</b> shows how a simple XML string can be provided as the input 
 * source for the parse operation.  Alternative methods to specify the input 
 * source are {@link Parse#setFile(java.io.File)}, 
 * {@link Parse#setInputStream(java.io.InputStream)}, 
 * {@link Parse#setInputSource(InputSource)},
 * {@link Parse#setUrl(java.net.URL)} and
 * {@link Parse#setStreamSource(StreamInput)}. 
 * </p>
 *
 * <p><b>line 7</b> shows how the execution of the parse is performed.  The 
 * input source will be read, the resulting Document Object Model (DOM) will 
 * be walked and potentially problems are produced in the parse.
 * </p>
 *
 * <p><b>line 8</b> shows how an exception can be thrown in case of an error.
 * The parse execution itself tries to keep parsing as much as possible to 
 * provide the developer with as much feedback as possible in one parse cycle.
 * The {@link Parse#getProblems() problems} are silently captured in the parse
 * object.  If an exception is thrown by 
 * {@link Parse#checkErrors(String)}, it will contain a report of 
 * all the parsing problems.  Alternatively, the {@link Parse#getProblems() problems
 * in the parse object} could be examined directly without the need for an exception. 
 * </p>
 *
 * <p><b>line 9</b> shows how the result of the parse operation is extracted 
 * from the parse object.  
 * </p>
 * 
 * <h2 id="binding">Binding</h2>
 * <p>Bindings are the link between a certain type of element in your XML document
 * and the corresponding java object in your domain model.</p>
 *
 * <p>A parser can be configured with a set of {@link Binding}s.  Each {@link Binding}
 * knows how to transform a dom element of a given tagName to the corresponding Java
 * object.  {@link Bindings} has a notion of binding categories.  For example, activities
 * and actions can be seen as different categories in jPDL.
 * </p>
 *
 * <p>The purpose of bindings is to make certain elements in the parsing configurable.
 * E.g. in jPDL, the main structure of the document is fixed.  But activity types can be
 * added dynamically.
 * </p>
 *
 * <p>The current {@link Bindings} implementation only supports matching of an
 * element with a {@link Binding} based on tagName.  If you want to take other things
 * into account (e.g. when you want to differentiate between elements of the same
 * tagName with a different attribute value), you can create a specialized
 * {@link Bindings} class.</p>
 *
 * <p>Bindings are added by tagName, but they have to be looked up by element.  That is
 * to support more specialized bindings implementations that match an element with a
 * binding on more information then just the tagName.  In that case, a specialized subclass of
 * {@link Binding} should be created and the method {@link #getBinding(Element, String)} and
 * constructor {@link Bindings#Bindings(Bindings)} should be provided
 * with the more specialized matching behaviour.
 * </p>
 *
 * <h2 id="objectstack">Object stack</h2>
 * <p>When implementing {@link Binding}s, you might want to make use of the
 * contextual object stack that is provided on the {@link Parse}.  The
 * {@link Binding} implementations can maintain Java objects on that stack
 * that are being created.
 * </p>
 *
 * <p>E.g. you could push the ProcessDefinition element onto the object stack while it
 * is being parsed like this:
 * </p>
 *
 * <pre>public class MyProcessBinding implements Binding {
 *
 *   public Object parse(Element element, Parse parse, Parser parser) {
 *     <i>// instantiate the object for this binding</i>
 *     MyProcess myProcess = new MyProcess();
 *
 *     <i>// collect all the child elements of element</i>
 *     List<Element> elements = XmlUtil.elements(element);
 *
 *     <i>// push my processDefinition onto the object stack</i>
 *     parse.pushObject(myProcess);
 *     try {
 *
 *       for (Element activityElement: elements) {
 *         // parse the child elements with the bindings in category "activity"
 *         parseElement(activityElement, parse, "activity");
 *       }
 *     } finally {
 *       // make sure my processDefinition is popped.
 *       parse.popObject();
 *     }
 *     return myProcess;
 *   }
 * }
 * </pre>
 *
 * <p>Then, activity bindings might access the processDefinition like this:
 * </p>
 *
 * <pre>public class MyNodeBinding implements Binding {
 *
 *   public Object parse(Element element, Parse parse, Parser parser) {
 *     <i>// instantiate the object for this binding</i>
 *     MyNode myNode = new MyNode();
 *
 *     <i>// add the activity to the processDefinition</i>
 *     MyProcess myProcess = parse.findObject(MyProcess.class);
 *     myProcess.addNode(myNode);
 *     myNode.setMyProcess(myProcess);
 *
 *     return myNode;
 *   }
 * }
 * </pre>
 *
 * <p>A parser implementation will typically have a static Bindings object that
 * is leveraged in all parser objects.   To customize bindings for a such a parser
 * be sure to make a deep copy with {@link Bindings#Bindings(Bindings)} before
 * you start adding more bindings to the specialized parser.  Otherwise the
 * base parser's bindings will be updated as well.
 * </p>
 * 
 * <h2 id="buildingcustomparsers">Building custom parsers</h2>
 * 
 * <p>This parser is build for inheritance.   
 * Overriding method {@link #parseDocumentElement(Element, Parse)} can be an easy 
 * way to start writing your own logic on walking the Document Object Model (DOM).
 * Such customizations can still be combined with the usage of 
 * <a href="#binding">bindings</a>.
 * </p>
 * 
 * <h2 id="entityresolving">Entity resolving</h2>
 * <p>A parser can be configured with a set of entities with the
 * {@link #addEntity(String, Entity)} method.  The {@link UrlEntity} has
 * a convenience method to build entities from resources
 * {@link UrlEntity#UrlEntity(String, ClassLoader)}.
 * </p>
 *
 * <p>When a document builder is created, the default implementation of the
 * {@link #setEntityResolver(DocumentBuilder)} will set this parser as the entity resolver.
 * The implementation method of {@link EntityResolver} ({@link #resolveEntity(String, String)}
 * will use the added {@link Entity}s to try and find a match based on the
 * publicId.  If one is found, the {@link Entity} inputSource is returned, otherwise
 * the systemId is used.
 * </p>
 *
 * <p>This class is intended to be used with aggregation as well as inheritence.
 * </p>
 *
 * @author Tom Baeyens
 */
public class Parser {

  private static final Log log = Log.getLog(Parser.class.getName());

  protected Bindings bindings;
  protected final DocumentBuilderFactory documentBuilderFactory =
    createDocumentBuilderFactory();

  /** the default parser */
  public Parser() {
  }

  /** creates a new Parser with bindings that can be maintained statically in
   * specialized subclasses of Parser. */
  public Parser(Bindings bindings) {
    this.bindings = bindings;
  }

  /** creates a new Parser with bindings and entities that can be maintained statically
   * in specialized subclasses of Parser.
   * @deprecated entities should be replaced by {@link #setSchemaResources(List)} */
  @Deprecated
  public Parser(Bindings bindings, Map<String, Entity> entities) {
    this.bindings = bindings;
  }

  // initialization ///////////////////////////////////////////////////////////

  protected DocumentBuilderFactory createDocumentBuilderFactory() {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setCoalescing(true);
    factory.setIgnoringComments(true);
    return factory;
  }

  // document builder methods /////////////////////////////////////////////////

  /** customizable creation of a new document builder.  Used by 
   * {@link #buildDocument(Parse)}. */
  protected DocumentBuilder createDocumentBuilder(Parse parse) {
    try {
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      documentBuilder.setErrorHandler(parse);
      parse.documentBuilder = documentBuilder;
      return documentBuilder;
    }
    catch (ParserConfigurationException e) {
      parse.addProblem("could not create document builder", e);
      return null;
    }
  }

  // schema validation ////////////////////////////////////////////////////////

  public void setSchemaResources(String... schemaResources) {
    // load resources from classpath
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    String[] schemaSources = new String[schemaResources.length];

    for (int i = 0; i < schemaResources.length; i++) {
      String schemaResource = schemaResources[i];
      URL schemaLocation = classLoader.getResource(schemaResource);
      if (schemaLocation != null) {
        log.info("loading schema resource: " + schemaResource);
        schemaSources[i] = schemaLocation.toString();
      }
      else {
        log.warn("skipping unavailable schema resource: " + schemaResource);
      }
    }

    documentBuilderFactory.setValidating(true);
    try {
      // set xml schema as the schema language
      documentBuilderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
        XMLConstants.W3C_XML_SCHEMA_NS_URI);
      // set schema sources
      documentBuilderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource",
        schemaSources);
    }
    catch (IllegalArgumentException e) {
      log.warn("JAXP implementation does not support XML Schema, "
        + "XML documents will not be checked for grammar errors", e);
    }

    try {
      // validate the document only if a grammar is specified
      documentBuilderFactory.setAttribute("http://apache.org/xml/features/validation/dynamic",
        Boolean.TRUE);
    }
    catch (IllegalArgumentException e) {
      log.warn("JAXP implementation is not Xerces, cannot enable dynamic validation, "
        + "XML documents without schema location will not parse", e);
    }

    /*
    try {
      // parse schema sources
      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      Schema schema = schemaFactory.newSchema(sources.toArray(new Source[sources.size()]));
      // register schema
      documentBuilderFactory.setSchema(schema);
      try {
        // validate the document only if a grammar is specified
        documentBuilderFactory.setAttribute("http://apache.org/xml/features/validation/dynamic",
          Boolean.TRUE);
      }
      catch (IllegalArgumentException e) {
        log.info("could not enable dynamic validation feature", e);
      }
    }
    catch (SAXException e) {
      log.info("could not set schema", e);
    }
    */
  }

  // bindings /////////////////////////////////////////////////////////////////

  /** the handlers for specific element types */
  public Bindings getBindings() {
    return bindings;
  }

  /** set the handlers for specific element types */
  public void setBindings(Bindings bindings) {
    this.bindings = bindings;
  }

  /** the handler for the given element */
  public Binding getBinding(Element element) {
    return getBinding(element, null);
  }

  /** the handler for the given element limited to a given category */
  public Binding getBinding(Element element, String category) {
    return (bindings != null ? bindings.getBinding(element, category) : null);
  }

  // runtime parsing methods //////////////////////////////////////////////////
  
  /** main method to start a new parse, check {@link Parse} for specifying 
   * input, executing the parse and extracting the results. */
  public Parse createParse() {
    return new Parse(this);
  }

  /** builds a dom from the importedStreamSource and appends the child elements 
   * of the document element to the destination element.  Problems are reported 
   * in the importingParse. */
  public void importStream(StreamInput importedStreamInput, Element destination, Parse importingParse) {
    try {
      // build the dom of the imported document
      Parse importedParse = createParse();
      importedParse.setStreamSource(importedStreamInput);
      Document importedDocument = buildDocument(importedParse);

      // loop over all the imported document elements
      Element importedDocumentElement = importedDocument.getDocumentElement();
      for (Element e : XmlUtil.elements(importedDocumentElement)) {
        // import the element into the destination element
        destination.appendChild(destination.getOwnerDocument().importNode(e, true));
      }
      importedParse.checkErrors(destination.getTagName());
    }
    catch (Exception e) {
      importingParse.addProblem("could not import " + importedStreamInput, e);
    }
  }

  /** customizable parse execution */
  protected void execute(Parse parse) {
    try {
      if (parse.document == null) {
        parse.document = buildDocument(parse);
      }

      // walk the dom tree
      if (parse.document != null) {
        try {
          // walk the dom tree
          parseDocument(parse.document, parse);
        }
        catch (Exception e) {
          parse.addProblem("parsing exception: " + e.getMessage(), e);
        }
      }
    }
    finally {
      if (parse.inputStream != null) {
        try {
          parse.inputStream.close();
        }
        catch (Exception e) {
          parse.addProblem("couldn't close input stream", e);
        }
      }
    }
  }

  protected Document buildDocument(Parse parse) {
    DocumentBuilder documentBuilder = createDocumentBuilder(parse);
    InputSource inputSource = parse.getInputSource();
    try {
      return documentBuilder.parse(inputSource);
    }
    catch (IOException e) {
      parse.addProblem("could not read input", e);
    }
    catch (SAXException e) {
      parse.addProblem("failed to parse xml", e);
    }
    return null;
  }

  // Document Object Model walking ////////////////////////////////////////////

  /** start of the DOM walk.
   * 
   * This method is used as part of 
   * {@link #execute(Parse) the parse execution}.
   * 
   * This default implementation behaviour extracts the document element and 
   * delegates to {@link #parseDocumentElement(Element, Parse)}.
   *
   * This method can be overridden for customized behaviour.
   * 
   * @return the object that is the result from parsing this document. */
  public Object parseDocument(Document document, Parse parse) {
    Object object = parseDocumentElement(document.getDocumentElement(), parse);
    parse.documentObject = object;
    return object;
  }

  /** parses the top level element in the document and produces the object that 
   * is the result from the parsing. 
   *
   * @return the object that is the result from parsing this document element. */
  public Object parseDocumentElement(Element documentElement, Parse parse) {
    return parseElement(documentElement, parse);
  }

  /** parses an arbitrary element in the document with the first matching 
   * binding found using any of the categories.
   * 
   * @return the object that is the result from parsing this element. */
  public Object parseElement(Element element, Parse parse) {
    return parseElement(element, parse, null);
  }

  /** parses an arbitrary element in the document based on the bindings in the 
   * given category.
   * 
   * @param category is the category in which the tagName should be resolved to 
   *   a {@link Binding}.  If category is null, all the categories will be 
   *   scanned for an appropriate binding in random order.
   * 
   * @return the object that is the result from parsing this element. */
  public Object parseElement(Element element, Parse parse, String category) {
    Object object = null;
    String tagName = element.getLocalName();

    Binding binding = getBinding(element, category);

    if (binding != null) {
      object = binding.parse(element, parse, this);
    }
    else if (log.isDebugEnabled()) {
      log.debug("no element parser for tag " + tagName
        + (category != null ? " in category " + category : " in the default category"));
    }

    return object;
  }

  public List<ArgDescriptor> parseArgs(List<Element> argElements, Parse parse) {
    return parseArgs(argElements, parse, WireParser.CATEGORY_DESCRIPTOR);
  }

  public List<ArgDescriptor> parseArgs(List<Element> argElements, Parse parse, String category) {
    List<ArgDescriptor> args = null;
    if (argElements != null) {
      if (argElements.size() > 0) {
        args = new ArrayList<ArgDescriptor>(argElements.size());
      }
      for (Element argElement : argElements) {
        ArgDescriptor argDescriptor = new ArgDescriptor();
        argDescriptor.setTypeName(XmlUtil.attribute(argElement, "type"));
        Element descriptorElement = XmlUtil.element(argElement);
        if (descriptorElement == null) {
          parse.addProblem("arg must contain exactly one descriptor element out of "
            + bindings.getTagNames(category) + " as contents:"
            + XmlUtil.toString(argElement.getParentNode()), argElement);
        }
        else {
          Descriptor descriptor = (Descriptor) parseElement(descriptorElement, parse, category);
          argDescriptor.setDescriptor(descriptor);
        }
        args.add(argDescriptor);
      }
    }
    return args;
  }
}

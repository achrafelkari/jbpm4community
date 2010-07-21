/*
 * JBoss, Home of Professional Open Source
 * 
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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;

import org.jbpm.pvm.internal.stream.FileStreamInput;
import org.jbpm.pvm.internal.stream.InputStreamInput;
import org.jbpm.pvm.internal.stream.ResourceStreamInput;
import org.jbpm.pvm.internal.stream.StreamInput;
import org.jbpm.pvm.internal.stream.StringStreamInput;
import org.jbpm.pvm.internal.stream.UrlStreamInput;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

/** information related to one single parse operation, for instructions 
 * see {@link Parser}.
 * 
 * @author Tom Baeyens
 */
public class Parse extends ProblemList implements ErrorHandler {

  private static final long serialVersionUID = 1L;

  public static final String CONTEXT_KEY_DEPLOYMENT = "deployment";
  public static final String CONTEXT_KEY_PROCESS_LANGUAGE_ID = "proclangid";
  public static final String CONTEXT_KEY_BINDINGS = "bindings";
  public static final String CONTEXT_KEY_MIGRATIONS = "migrations";
  
  protected Parser parser;
  
  protected StreamInput streamInput;
  protected InputStream inputStream;
  protected InputSource inputSource;
  
  protected DocumentBuilder documentBuilder = null;
  protected Document document = null;

  protected Map<String, Object> contextMap;
  protected Stack<Object> contextStack;
  protected Object documentObject;
  
  protected Parse(Parser parser) {
    this.parser = parser;
  }
  
  // specifying the input source //////////////////////////////////////////////

  /** specify an input stream as the source for this parse */
  public Parse setInputStream(InputStream inputStream) {
    this.streamInput = new InputStreamInput(inputStream);
    return this;
  }

  /** specify a URL as the source for this parse */
  public Parse setUrl(URL url) {
    this.streamInput = new UrlStreamInput(url);
    return this;
  }

  /** specify a file as the source for this parse */
  public Parse setFile(File file) {
    this.streamInput = new FileStreamInput(file);
    return this;
  }

  /** specify a resource as the source for this parse */
  public Parse setResource(String resource) {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    this.streamInput = new ResourceStreamInput(resource, classLoader);
    return this;
  }

  /** specify an XML string as the source for this parse */
  public Parse setString(String xmlString) {
    this.streamInput = new StringStreamInput(xmlString);
    return this;
  }

  /** specify a {@link StreamInput} as the source for this parse */
  public Parse setStreamSource(StreamInput streamInput) {
    this.streamInput = streamInput;
    return this;
  }

  /** specify an InputStream as the source for this parse */
  public Parse setInputSource(InputSource inputSource) {
    this.inputSource = inputSource;
    return this;
  }

  /** normally the Document Object Model is created during the 
   * parse execution, but providing a document can be convenient when 
   * the DOM is already available and only the walking of the 
   * DOM needs to be done by the parser.  If the document 
   * is provide, building the DOM from a source is skipped. */
  public Parse setDocument(Document document) {
    this.document = document;
    return this;
  }

  /** provides the result of this parse operation. */
  public Parse setDocumentObject(Object object) {
    this.documentObject = object;
    return this;
  }

  // retrieving input source //////////////////////////////////////////////////
  
  protected InputSource getInputSource() {
    if (inputSource!=null) {
      return inputSource;
    }

    if (streamInput!=null) {
      inputStream = streamInput.openStream();
      return new InputSource(inputStream);
    }
    
    addProblem("no source specified to parse");
    return null;
  }

  // parse execution //////////////////////////////////////////////////////////
  
  /** perform the actual parse operation with the specified input source. */
  public Parse execute() {
    parser.execute(this);
    return this;
  }

  // problems /////////////////////////////////////////////////////////////////

  /** part of {@link ErrorHandler} to capture XML parsing problems. */
  public void error(SAXParseException e) {
    addXmlValidationProblem(e, ProblemImpl.TYPE_XML_VALIDATION_ERROR);
  }

  /** part of {@link ErrorHandler} to capture XML parsing problems. */
  public void fatalError(SAXParseException e) {
    addXmlValidationProblem(e, ProblemImpl.TYPE_XML_VALIDATION_ERROR);
  }
  /** part of {@link ErrorHandler} to capture XML parsing problems. */
  public void warning(SAXParseException e) {
    addXmlValidationProblem(e, ProblemImpl.TYPE_XML_VALIDATION_WARNING);
  }

  protected void addXmlValidationProblem(SAXParseException e, String type) {
    ProblemImpl problem = new ProblemImpl(e.getMessage(), e, type);
    problem.setLine(e.getLineNumber());
    problem.setColumn(e.getColumnNumber());
    addProblem(problem);
  }

  /** throws an exception with appropriate message in case the parse contains 
   * errors or fatal errors.  This method also logs the problems with severity
   * 'warning'. */
  public Parse checkErrors(String description) {
    if (hasErrors()) {
      throw getJbpmException(description);
    }
    return this;
  }

  // context map //////////////////////////////////////////////////////////////

  public Parse contextMapPut(String key, Object value) {
    if (contextMap==null) {
      contextMap = new HashMap<String, Object>();
    }
    contextMap.put(key, value);
    return this;
  }

  public Object contextMapGet(String key) {
    if (contextMap!=null) {
      return contextMap.get(key);
    }
    return null;
  }

  public Parse contextMapRemove(String key) {
    if (contextMap!=null) {    
      contextMap.remove(key);
    }
    return this;
  }
  
  public Parse propagateContexMap(Parse parse) {
    if (parse.contextMap==null) {
      parse.contextMap = new HashMap<String, Object>();
    }
    if (this.contextMap!=null) {
      parse.contextMap.putAll(this.contextMap);
    }
    this.contextMap = parse.contextMap;
    return this;
  }


  // contex stack /////////////////////////////////////////////////////////////

  /** push a contextual object on the stack of this parse. */
  public Parse contextStackPush(Object object) {
    if (contextStack==null) {
      contextStack = new Stack<Object>();
    }
    contextStack.push(object);
    return this;
  }

  /** remove a contextual object from the stack. */
  public Object contextStackPop() {
    if (contextStack!=null) {
      return contextStack.pop();
    }
    return null;
  }

  /** look up the top contextual object from the stack. */
  public Object contextStackPeek() {
    if (contextStack!=null) {
      return contextStack.peek();
    }
    return null;
  }
  
  /** search a contextual object in the stack by type. */
  public <T> T contextStackFind(Class<T> clazz) {
    if ( (contextStack!=null)
         && (! contextStack.isEmpty())
       ) {
      ListIterator<Object> listIter = contextStack.listIterator(contextStack.size());
      while (listIter.hasPrevious()) {
        Object object = listIter.previous();
        if (clazz.isInstance(object)) {
          return clazz.cast(object);
        }
      }
    }
    return null;
  }
  
  // getters //////////////////////////////////////////////////////////////////

  /** the result of this parse operation. */
  public Object getDocumentObject() {
    return documentObject;
  }
  /** the Document Object Model (DOM). */
  public Document getDocument() {
    return document;
  }
}

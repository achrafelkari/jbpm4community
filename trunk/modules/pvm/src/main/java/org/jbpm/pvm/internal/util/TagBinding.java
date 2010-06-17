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

import org.jbpm.api.JbpmException;
import org.jbpm.pvm.internal.xml.Binding;
import org.jbpm.pvm.internal.xml.Bindings;
import org.w3c.dom.Element;


/**
 * @author Tom Baeyens
 */
public abstract class TagBinding implements Binding {
  
  protected String category;
  protected String tagName;
  protected String namespaceUri;
  
  /** @param tagName is required and represents the local part of the tag name.
   * @param namespaceUri is optional (null is allowed) and represents the full 
   *   namespace URI of the element
   * @param category is optional (null is allowed) and represents the category of 
   *   the binding. 
   * @see Bindings */
  public TagBinding(String tagName, String namespaceUri, String category) {
    if (tagName==null) {
      throw new JbpmException("tagName is null");
    }
    this.tagName = tagName;
    this.namespaceUri = namespaceUri;
    this.category = category;
  }

  public boolean matches(Element element) {
    String elementNamespaceUri = element.getNamespaceURI();
    if ( (elementNamespaceUri!=null)
         && (namespaceUri!=null)
         && (!namespaceUri.equals(elementNamespaceUri))
       ) {
      return false;
    }
    String elementTagLocalName = element.getLocalName();
    if (!tagName.equals(elementTagLocalName)) {
      return false;
    }
    return true;
  }

  public String toString() {
    // xml parsing error messages depend on this implementation of the toString to be meaningfull 
    return tagName;
  }
  public String getCategory() {
    return category;
  }
  public String getTagName() {
    return tagName;
  }
  public String getNamespaceUri() {
    return namespaceUri;
  }
  public void setCategory(String category) {
    this.category = category;
  }
  public void setTagName(String tagName) {
    this.tagName = tagName;
  }
  public void setNamespaceUri(String namespaceUri) {
    this.namespaceUri = namespaceUri;
  }
}

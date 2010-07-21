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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.descriptor.ArgDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;
import org.jbpm.pvm.internal.wire.operation.Operation;
import org.jbpm.pvm.internal.wire.xml.WireParser;
import org.jbpm.pvm.internal.xml.Bindings;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;
import org.w3c.dom.Element;

/** parses a descriptor for creating a java object through reflection.
 *
 * See schema docs for more details.
 *
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 */
public class ObjectBinding extends WireDescriptorBinding {

  public static final String TAG = "object";

  public ObjectBinding() {
    super(TAG);
  }

  public static boolean isObjectDescriptor(Element element) {
    if (XmlUtil.attribute(element, "class") != null) {
      return true;
    }
    if (XmlUtil.attribute(element, "expr") != null) {
      return true;
    }
    if (XmlUtil.attribute(element, "factory") != null) {
      return true;
    }
    if (XmlUtil.element(element, "factory") != null) {
      return true;
    }
    return false;
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    return parseObjectDescriptor(element, parse, parser);
  }

  public static ObjectDescriptor parseObjectDescriptor(Element element, Parse parse, Parser parser) {
    ObjectDescriptor descriptor = new ObjectDescriptor();

    String className = XmlUtil.attribute(element, "class");
    String expr = XmlUtil.attribute(element, "expr");
    String factoryObjectName = XmlUtil.attribute(element, "factory");
    Element factoryElement = XmlUtil.element(element, "factory");

    if (className != null) {
      descriptor.setClassName(className);
      if (factoryObjectName != null) {
        parse.addProblem("attribute 'factory' is specified together with attribute 'class' in element 'object': "
          + XmlUtil.toString(element), element);
      }
      if (factoryElement != null) {
        parse.addProblem("element 'factory' is specified together with attribute 'class' in element 'object': "
          + XmlUtil.toString(element), element);
      }

      Element constructorElement = XmlUtil.element(element, "constructor");
      if (constructorElement != null) {
        List<Element> argElements = XmlUtil.elements(constructorElement, "arg");
        List<ArgDescriptor> argDescriptors = parser.parseArgs(argElements, parse);
        descriptor.setArgDescriptors(argDescriptors);

        if (element.hasAttribute("method")) {
          parse.addProblem("attributes 'class' and 'method' indicate static method and also a 'constructor' element is specified for element 'object': "
            + XmlUtil.toString(element), element);
        }
      }

    } else if (factoryObjectName != null) {
      descriptor.setFactoryObjectName(factoryObjectName);
      if (factoryElement!=null) {
        parse.addProblem("element 'factory' is specified together with attribute 'factory' in element 'object': "
          + XmlUtil.toString(element), element);
      }

    } else if (factoryElement != null) {
      Element factoryDescriptorElement = XmlUtil.element(factoryElement);
      Descriptor factoryDescriptor = (Descriptor) parser.parseElement(factoryDescriptorElement,
        parse, WireParser.CATEGORY_DESCRIPTOR);
      descriptor.setFactoryDescriptor(factoryDescriptor);

    } else if (expr != null) {
      String exprType = XmlUtil.attribute(element, "expr-type");
      descriptor.setExpression(Expression.create(expr, exprType));

    } else {
      parse.addProblem("element 'object' must have one of {attribute 'class', attribute 'expr', attribute 'factory' or element 'factory'}: "
        + XmlUtil.toString(element), element);
    }

    // method
    if (element.hasAttribute("method")) {
      descriptor.setMethodName(element.getAttribute("method"));

      List<Element> argElements = XmlUtil.elements(element, "arg");
      List<ArgDescriptor> argDescriptors = parser.parseArgs(argElements, parse);
      descriptor.setArgDescriptors(argDescriptors);
    } else if ( (factoryObjectName != null)
                || (factoryElement != null)
              ) {
      parse.addProblem("element 'object' with a element 'factory' or a attribute 'factory' must have a attribute 'method': "
        + XmlUtil.toString(element), element);
    }

    if( (className == null)
        && (XmlUtil.element(element, "constructor") != null)
      ) {
      parse.addProblem("element 'object' with a 'constructor' element must have 'class' attribute: "
        + XmlUtil.toString(element), element);
    }

    // read the operations elements
    List<Operation> operations = null;
    List<Element> elements = XmlUtil.elements(element);

    Set<String> operationTagNames = null;
    Bindings bindings = parser.getBindings();
    if (bindings!=null) {
      operationTagNames = bindings.getTagNames(WireParser.CATEGORY_OPERATION);
    } else {
      operationTagNames = Collections.emptySet();
    }

    for (Element childElement : elements) {
      if (operationTagNames.contains(childElement.getTagName())) {
        Operation operation = (Operation) parser.parseElement(childElement, parse, WireParser.CATEGORY_OPERATION);
        if (operations == null) {
          operations = new ArrayList<Operation>();
        }
        operations.add(operation);
      }
    }
    descriptor.setOperations(operations);

    // autowiring
    Boolean isAutoWireEnabled = XmlUtil.attributeBoolean(element, "auto-wire", parse);
    if (isAutoWireEnabled != null) {
      descriptor.setAutoWireEnabled(isAutoWireEnabled);
    }
    return descriptor;
  }
}
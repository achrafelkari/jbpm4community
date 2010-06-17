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
package org.jbpm.jpdl.internal.activity;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.jpdl.internal.xml.JpdlParser;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.descriptor.ListDescriptor;
import org.jbpm.pvm.internal.wire.xml.WireParser;
import org.jbpm.pvm.internal.xml.Parse;
import org.w3c.dom.Element;


/**
 * @author Tom Baeyens
 */
public class HqlBinding extends JpdlBinding {

  public static final String TAG = "hql";

  public HqlBinding() {
    super(TAG);
  }
  
  protected HqlBinding(String tagName) {
    super(tagName);
  }

  public Object parseJpdl(Element element, Parse parse, JpdlParser parser) {
    HqlActivity hqlActivity = createHqlActivity();
    
    Element queryElement = XmlUtil.element(element, "query", parse);
    if (queryElement!=null) {
      String query = XmlUtil.getContentText(queryElement);
      hqlActivity.setQuery(query);
    }
    
    Boolean resultUnique = XmlUtil.attributeBoolean(element, "unique", parse);
    if (resultUnique!=null) {
      hqlActivity.setResultUnique(resultUnique);
    }
    
    String variableName = XmlUtil.attribute(element, "var", parse);
    hqlActivity.setResultVariableName(variableName);
    
    Element parametersElement = XmlUtil.element(element, "parameters");
    List<Element> paramElements = XmlUtil.elements(parametersElement);
    if (!paramElements.isEmpty()) {
      List<Descriptor> parametersDescriptor = new ArrayList<Descriptor>();
      for (Element paramElement: paramElements) {
        WireParser wireParser = WireParser.getInstance();
        Descriptor paramDescriptor = (Descriptor) wireParser.parseElement(paramElement, parse, WireParser.CATEGORY_DESCRIPTOR);
        parametersDescriptor.add(paramDescriptor);
      }

      ListDescriptor parametersListDescriptor = new ListDescriptor();
      parametersListDescriptor.setValueDescriptors(parametersDescriptor);
      hqlActivity.setParametersDescriptor(parametersListDescriptor);
    }
      
    return hqlActivity;
  }

  protected HqlActivity createHqlActivity() {
    return new HqlActivity();
  }
}

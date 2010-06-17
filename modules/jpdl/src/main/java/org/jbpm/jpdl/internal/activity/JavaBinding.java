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

import java.util.List;

import org.jbpm.jpdl.internal.xml.JpdlParser;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.descriptor.ArgDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;
import org.jbpm.pvm.internal.wire.usercode.UserCodeReference;
import org.jbpm.pvm.internal.wire.xml.WireParser;
import org.jbpm.pvm.internal.xml.Parse;
import org.w3c.dom.Element;


/**
 * @author Tom Baeyens
 * @author Koen Aers
 */
public class JavaBinding extends JpdlBinding {

  public static final String TAG = "java";
  
  public JavaBinding() {
    super(TAG);
  }

  public Object parseJpdl(Element element, Parse parse, JpdlParser parser) {
    JavaActivity javaActivity = new JavaActivity();
    if (XmlUtil.attribute(element, "method", parse)!=null) {      
      String jndiName = XmlUtil.attribute(element, "ejb-jndi-name"); 
      if (jndiName != null) {
        parseEjbInvocation(javaActivity, element, parse, parser);
      } else {
        parseJavaInvocation(javaActivity, element, parse, parser);
      }      
    }
    String variableName = XmlUtil.attribute(element, "var");
    javaActivity.setVariableName(variableName);    
    return javaActivity;
  }
  
  private void parseEjbInvocation(JavaActivity javaActivity, Element element, Parse parse, JpdlParser parser) {
    javaActivity.setJndiName(XmlUtil.attribute(element, "ejb-jndi-name"));
    javaActivity.setMethodName(XmlUtil.attribute(element, "method"));
    List<Element> argElements = XmlUtil.elements(element, "arg");
    List<ArgDescriptor> argDescriptors = new WireParser().parseArgs(argElements, parse);
    javaActivity.setArgDescriptors(argDescriptors);
  }
  
  private void parseJavaInvocation(JavaActivity javaActivity, Element element, Parse parse, JpdlParser parser) {
    UserCodeReference invocationReference = parser.parseUserCodeReference(element, parse);
    javaActivity.setInvocationReference(invocationReference);    
    ObjectDescriptor objectDescriptor = (ObjectDescriptor) invocationReference.getDescriptor();
    javaActivity.setArgDescriptors(objectDescriptor.getArgDescriptors());
    objectDescriptor.setArgDescriptors(null);
    javaActivity.setMethodName(objectDescriptor.getMethodName());
    objectDescriptor.setMethodName(null);
  }
  
}

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

import javax.jms.Session;

import org.jbpm.jpdl.internal.xml.JpdlParser;
import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.descriptor.MapDescriptor;
import org.jbpm.pvm.internal.xml.Parse;
import org.w3c.dom.Element;

/**
 * @author Koen Aers
 */
public class JmsBinding extends JpdlBinding {

  public static final String TAG = "jms";

  public JmsBinding() {
    super(TAG);
  }

  protected JmsBinding(String tagName) {
    super(tagName);
  }

  public Object parseJpdl(Element element, Parse parse, JpdlParser parser) {
    JmsActivity jmsActivity = createJmsActivity();

    // attributes /////////////////////////////////////////////////////////////

    String connectionFactoryName = XmlUtil.attribute(element, "connection-factory", parse);
    jmsActivity.setConnectionFactoryName(connectionFactoryName);

    String destinationName = XmlUtil.attribute(element, "destination", parse);
    jmsActivity.setDestinationName(destinationName);

    Boolean transacted = XmlUtil.attributeBoolean(element, "transacted", parse);
    if (transacted!=null) {
      jmsActivity.setTransacted(transacted);
    }

    String acknowledge = XmlUtil.attribute(element, "acknowledge");
    if (acknowledge != null) {
      if (acknowledge.equalsIgnoreCase("auto")) {
        jmsActivity.setAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
      } else if (acknowledge.equalsIgnoreCase("client")) {
        jmsActivity.setAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
      } else if (acknowledge.equalsIgnoreCase("dups-ok")) {
        jmsActivity.setAcknowledgeMode(Session.DUPS_OK_ACKNOWLEDGE);
      } else {
        parse.addProblem("unknown jms acknowledge: '"+acknowledge+"'", element);
      }
    }

    // elements ///////////////////////////////////////////////////////////////

    Element textElement = XmlUtil.element(element, "text");
    if (textElement != null) {
      String expressionText = XmlUtil.getContentText(textElement);
      jmsActivity.setType("text");
      Expression expression = Expression.create(expressionText, Expression.LANGUAGE_UEL_VALUE);
      jmsActivity.setTextExpression(expression);
    }

    Element objectElement = XmlUtil.element(element, "object");
    if (objectElement != null) {
      jmsActivity.setType("object");
      String expressionText = XmlUtil.attribute(objectElement, "expr");
      Expression expression = Expression.create(expressionText, Expression.LANGUAGE_UEL_VALUE);
      jmsActivity.setObjectExpression(expression);
    }

    Element mapElement = XmlUtil.element(element, "map");
    if (mapElement != null) {
      jmsActivity.setType("map");
      Descriptor descriptor = parser.parseDescriptor(mapElement, parse);
      if (descriptor instanceof MapDescriptor) {
        jmsActivity.setMapDescriptor((MapDescriptor) descriptor);
      } else {
        parse.addProblem("the parser did not return a descriptor of type MapDescriptor");
      }
    }

    return jmsActivity;
  }

  protected JmsActivity createJmsActivity() {
    return new JmsActivity();
  }
}

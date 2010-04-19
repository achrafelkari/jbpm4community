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
package org.jbpm.jpdl.internal.convert.node;

import org.dom4j.Element;
import org.jbpm.jpdl.internal.convert.Jpdl3Converter;

public class MailNode extends Node {
	public Element createConvertedElement(Element jpdl4Doc) {
		convertedElement = jpdl4Doc.addElement("mail");
		return convertedElement;
	}

	public void read(Jpdl3Converter reader) {
		String template = nodeElement.attributeValue("template");
		String actors = nodeElement.attributeValue("actors");
		String to = nodeElement.attributeValue("to");
		String subject = reader.getProperty("subject", nodeElement);
		String text = reader.getProperty("text", nodeElement);
        if (template != null) {
        	convertedElement.addAttribute("template", template);
        }
        
        if (actors != null) {
        	Element toElement = convertedElement.addElement("to");
        	toElement.addAttribute("users", actors);
        }
        if (to != null) {
        	Element toElement = convertedElement.addElement("to");
        	toElement.addAttribute("addresses", to);
        }
        if (subject != null) {
        	Element subjectElement = convertedElement.addElement("subject");
        	subjectElement.addText(subject);
        }
        if (text != null) {
        	Element textElement = convertedElement.addElement("text");
        	textElement.addText(text);
        }        
	}
}
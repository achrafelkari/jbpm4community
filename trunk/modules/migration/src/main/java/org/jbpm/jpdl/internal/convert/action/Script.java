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
package org.jbpm.jpdl.internal.convert.action;

import org.dom4j.Element;
import org.jbpm.jpdl.internal.convert.Jpdl3Converter;
/**
 * Create the converted script element and map the specific attributes
 * @author<a href="mailto:ema@redhat.com>Jim Ma</a> 
 *
 */
public class Script extends Action {
	
	public Element createConvertedElement(Element actionElement, Element jpdl4Doc) {
	    convertedElement = jpdl4Doc.addElement("script");
        return convertedElement;
	}
	
	public void read(Element actionElement, Jpdl3Converter jpdlReader) {
		String expression = null;
		if (actionElement.isTextOnly()) {
			expression = actionElement.getText();
		} else {
			//TODO:Unsupported variable conversion
			//List<VariableAccess> vias = jpdlReader.readVariableAccesses(actionElement);
			expression = actionElement.element("expression").getText();
		}
		convertedElement.addAttribute("expr", expression);
		convertedElement.addAttribute("lang", "juel");
	}	
}

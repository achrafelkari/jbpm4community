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
 * The Action class to create the converted action or script elements and map the specific attributes
 * @author<a href="mailto:ema@redhat.com>Jim Ma</a> 
 *
 */
public class Action {

	protected Element convertedElement = null;
	protected boolean isPropagationAllowed = true;
	protected boolean isAsync = false;
	protected boolean isAsyncExclusive = false;
	
	public Action() {
		
	}
	
	public Element createConvertedElement(Element actionElement, Element jpdl4Doc) {
		String expression = actionElement.attributeValue("expression");
	
		if (expression != null) {
			convertedElement = jpdl4Doc.addElement("script");
			
		} else if (actionElement.attributeValue("class")!=null) {
			convertedElement = jpdl4Doc.addElement("java");			
		} else {
			//both expression and class are null
			convertedElement = jpdl4Doc.addElement("custom");
		}
		
        return convertedElement;
	}
	
	public void read(Element actionElement, Jpdl3Converter jpdlReader) {
	    String expression = actionElement.attributeValue("expression");
	    if (expression!=null) {
	    	convertedElement.addAttribute("expr", expression);
			convertedElement.addAttribute("lang", "juel");

	    } else if (actionElement.attribute("ref-name")!=null) {
	      //TODO: Unsupported ref-name
	      //jpdlReader.addUnresolvedActionReference(actionElement, this);
	      jpdlReader.addWarning("Unsupported ref-name attribute conversion in element " + actionElement.asXML());
	    } else if (actionElement.attribute("class")!=null) {
	    	convertedElement.addAttribute("class", actionElement.attributeValue("class"));
			convertedElement.addAttribute("method", "execute");
	      
	    } else {
	      jpdlReader.addWarning("action does not have class nor ref-name attribute, generated the default node <custom>  "+actionElement.asXML());
	    }
	    //TODO: propagate...
	    String acceptPropagatedEvents = actionElement.attributeValue("accept-propagated-events");
	    if ("false".equalsIgnoreCase(acceptPropagatedEvents)
	        || "no".equalsIgnoreCase(acceptPropagatedEvents) 
	        || "off".equalsIgnoreCase(acceptPropagatedEvents)) {
	      isPropagationAllowed = false;
	    }
        
	    String asyncText = actionElement.attributeValue("async");
	    if ("true".equalsIgnoreCase(asyncText)) {
	       convertedElement.addAttribute("continue", "async");
	    } else if ("exclusive".equalsIgnoreCase(asyncText)) {
	       convertedElement.addAttribute("continue", "exclusive");
	    }
	  }

}

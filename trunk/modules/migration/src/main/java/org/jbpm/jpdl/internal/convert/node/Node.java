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
import org.jbpm.jpdl.internal.convert.action.Action;
/**
 * create the converted action element and map the specific attributes
 * @author<a href="mailto:ema@redhat.com>Jim Ma</a> 
 *
 */
public class Node {
  
	protected Element convertedElement = null;
	
	protected Element nodeElement = null;
	
	protected Action action = new Action();
	
	public Element createConvertedElement(Element jpdl4Doc) {			
		Element actionElement = nodeElement.element("action");
		convertedElement= action.createConvertedElement(actionElement, jpdl4Doc);
	    return convertedElement;		
	}
	
	public void read(Jpdl3Converter reader) {
		Element actionElement = nodeElement.element("action");
		action.read(actionElement, reader);
		
	}
	
	public Element getConvertedElement()  {
		return convertedElement;
	}
	
	//set the jpdl3 element
	public void setNodeElement(Element ele) {
		this.nodeElement = ele;
	}
}

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
import org.jbpm.jpdl.internal.convert.problem.Problem;

/**
 * Create the converted timer element and map the specific attributes
 * @author<a href="mailto:ema@redhat.com>Jim Ma</a> 
 *
 */

public class CreateTimerAction extends Action {
	@Override
	public Element createConvertedElement(Element actionElement,
			Element jpdl4Doc) {
		this.convertedElement = jpdl4Doc.addElement("timer");
		return convertedElement;
	}

	@Override
	public void read(Element actionElement, Jpdl3Converter jpdlReader) {

		Element timerAction = jpdlReader.convertSingleAction(actionElement, this.convertedElement);

		String dueDate = actionElement.attributeValue("duedate");
		if (dueDate == null) {
			jpdlReader
					.addWarning("no duedate specified in create timer action '"
							+ actionElement + "'");
		} else {
			convertedElement.addAttribute("duedate", dueDate);
		}
		String repeat = actionElement.attributeValue("repeat");
		if ("true".equalsIgnoreCase(repeat) || "yes".equalsIgnoreCase(repeat)) {
			repeat = dueDate;
		}
		
		if (repeat != null) {
			convertedElement.addAttribute("repeat", repeat);
		}
		
		String transitionName = actionElement.attributeValue("transition");

		if ((transitionName != null) && (repeat != null)) {
			repeat = null;
			jpdlReader.addProblem(new Problem(Problem.LEVEL_WARNING,
					"ignoring repeat on timer with transition "
							+ actionElement.asXML()));
		}
		//TODO: Investigate how to convert transition attribute		
	}
}

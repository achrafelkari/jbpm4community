package org.jbpm.jpdl.internal.convert.node;

import java.util.List;

import org.dom4j.Element;
import org.jbpm.jpdl.internal.convert.Jpdl3Converter;

public class ProcessState extends Node {
	public Element createConvertedElement(Element jpdl4Doc) {
		convertedElement = jpdl4Doc.addElement("sub-process");
		return convertedElement;
	}
	
	public void read(Jpdl3Converter reader) {
		
		String name = nodeElement.attributeValue("name");
		
		convertedElement.attributeValue("name", name);
		
		Element subProcessElement = nodeElement.element("sub-process");
	    if (subProcessElement!=null) {
	    	String subProcessName = subProcessElement.attributeValue("name");
	    	//String subProcessVersion = subProcessElement.attributeValue("version");
	    	convertedElement.addAttribute("sub-process-key", subProcessName);
	    }
	    
	    List<VariableAccess> readVariableAccesses = reader.convertVariableAccesses(nodeElement);
	    for (VariableAccess via : readVariableAccesses) {
	    	Element para = null;
	    	if (via.isReadable()) {
	    		 para = convertedElement.addElement("parameter-in");
	    	} else {
	    		para = convertedElement.addElement("parameter-out");	    		
	    	}
	    	para.addAttribute("var", via.getVariableName());
    		para.addAttribute("subvar", via.getMappedName());
	    }
	    
	}
}

package org.jbpm.pvm.internal.wire.binding;

import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.descriptor.AbstractDescriptor;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;
import org.w3c.dom.Element;

public abstract class BasicTypeBinding extends WireDescriptorBinding {
  
  public BasicTypeBinding(String tagName) {
    super(tagName);
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    AbstractDescriptor descriptor = null;
    

    if (element.hasAttribute("value")) {
      String value = element.getAttribute("value");
      descriptor = createDescriptor(value, element, parse);
    } else {
      parse.addProblem("attribute 'value' is required in element '"+element.getLocalName()+"': "+XmlUtil.toString(element), element);
    }
    
    return descriptor;
  }
  
  public String createValueExceptionMessage(String message, Element element) {
    return element.getLocalName()+" has invalid formatted value attribute: "+(message!=null ? message+": " : "")+XmlUtil.toString(element);
  }

  /** subclasses can be sure that the value is not null.
   * subclasses should use {@link #createValueExceptionMessage(String, Element) for 
   * reporting format problems in the parse.  */
  protected abstract AbstractDescriptor createDescriptor(String value, Element element, Parse parse);
}
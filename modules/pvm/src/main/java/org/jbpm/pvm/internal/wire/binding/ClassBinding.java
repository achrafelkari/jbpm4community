package org.jbpm.pvm.internal.wire.binding;

import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.descriptor.ClassDescriptor;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;
import org.w3c.dom.Element;

/** parses a descriptor for creating a {@link Class}.
 * 
 * See schema docs for more details.
 *
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 */
public class ClassBinding extends WireDescriptorBinding {

  public ClassBinding() {
    super("class");
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    ClassDescriptor classDescriptor = null;
    String className = XmlUtil.attribute(element, "class-name");
    if (className!=null) {
      classDescriptor = new ClassDescriptor();
      classDescriptor.setClassName(className);
    } else {
      parse.addProblem("class must have classname attribute: "+XmlUtil.toString(element), element);
    }
    return classDescriptor;
  }

}

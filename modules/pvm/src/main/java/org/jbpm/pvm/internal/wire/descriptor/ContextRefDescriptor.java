package org.jbpm.pvm.internal.wire.descriptor;

import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireException;

public class ContextRefDescriptor extends AbstractDescriptor implements Descriptor {

  private static final long serialVersionUID = 1L;
  
  String contextName;

  public Object construct(WireContext wireContext) {
    if (contextName==null) {
      return wireContext;
    }
    EnvironmentImpl environment = EnvironmentImpl.getCurrent();
    if (environment==null) {
      throw new WireException("can't get context '"+contextName+"': no current environment");
    }
    return environment.getContext(contextName);
  }

  public String getContextName() {
    return contextName;
  }
  public void setContextName(String contextName) {
    this.contextName = contextName;
  }
}

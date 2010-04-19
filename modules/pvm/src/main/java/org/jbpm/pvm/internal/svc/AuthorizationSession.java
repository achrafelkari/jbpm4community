package org.jbpm.pvm.internal.svc;

import org.jbpm.api.cmd.Command;
import org.jbpm.pvm.internal.env.EnvironmentImpl;

public interface AuthorizationSession {

  void checkPermission(Command<?> command, EnvironmentImpl environment);
}

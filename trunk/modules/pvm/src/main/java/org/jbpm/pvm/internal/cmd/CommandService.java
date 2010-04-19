package org.jbpm.pvm.internal.cmd;

import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Command;

/**
 * abstract extensible session facade.  Developers can use this directly or 
 * extend one of the implementations with custom methods.
 * Developers should be encouraged to use this interface as it will 
 * be kept more stable then direct usage of the API (which is still 
 * allowed).
 * All the method implementations should be based on commands.
 * Each of the method implementations will have a environment block.
 * Then the command is executed and the environment is passed into the 
 * command.
 */
public interface CommandService {
  
  String NAME_TX_REQUIRED_COMMAND_SERVICE = "txRequiredCommandService";
  String NAME_NEW_TX_REQUIRED_COMMAND_SERVICE = "newTxRequiredCommandService";

  /**
   * @throws JbpmException if command throws an exception.
   */
  <T> T execute(Command<T> command);
}

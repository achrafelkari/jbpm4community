package org.jbpm.api.cmd;

import java.io.Serializable;

import org.jbpm.api.ProcessEngine;

/** commands that can be {@link ProcessEngine#execute(Command) executed by the process engine}.
 * 
 * @author Tom Baeyens
 */
public interface Command<T> extends Serializable {
  
  T execute(Environment environment) throws Exception;
}

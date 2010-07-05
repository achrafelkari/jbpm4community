package org.jbpm.integration.console;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jbpm.api.Configuration;
import org.jbpm.api.ProcessEngine;

/**
 * Utility class for retrieving the {@link ProcessEngine}.
 * 
 * @author jbarrez
 */
public final class ProcessEngineUtil {

  private static ProcessEngine processEngine;

  private static final String PROCESS_ENGINE_JNDI_NAME = "java:ProcessEngine";

  public static ProcessEngine retrieveProcessEngine() {
    if (processEngine == null) {
      synchronized (ProcessEngine.class) {
        if (processEngine == null) {
          try {
            InitialContext ctx = new InitialContext();
            try {
              processEngine = (ProcessEngine) ctx.lookup(PROCESS_ENGINE_JNDI_NAME);
            }
            finally {
              ctx.close();
            }
          }
          catch (NamingException e) {
            // build a process engine from a default jbpm.cfg.xml
            processEngine = Configuration.getProcessEngine();
          }
        }
      }
    }
    return processEngine;
  }

}

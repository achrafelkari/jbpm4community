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
package org.jbpm.enterprise.internal.ejb;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.jbpm.api.Configuration;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.cmd.Command;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.cmd.CommandService;
import org.jbpm.pvm.internal.env.EnvironmentFactory;

/**
 * Stateless session command executor.
 *
 * <h3>Configuration</h3>
 *
 * <p>
 * In order to provide commands an environment in which to run, the command
 * executor builds an environment factory from a configuration file loaded as a
 * classpath resource. The environment entry <code>ConfigurationResource</code>
 * specifies the name of the resource to access; the default is
 * <code>jbpm.cfg.xml</code>.
 * </p>
 *
 * <h3>JNDI-bound environment factory</h3>
 *
 * <p>
 * To avoid parsing the configuration file multiple times, the command executor
 * attempts to bind the environment factory to the name specified in the
 * <code>EnvironmentFactoryName</code> environment entry, defaulting to
 * <code>java:jbpm/EnvironmentFactory</code>. If the binding fails, the
 * command executor will still work; however, each instance will build its own
 * environment factory from the configuration.
 * </p>
 *
 * @author Jim Rigsbee
 * @author Tom Baeyens
 * @author Alejandro Guizar
 */
public class CommandExecutorSLSB implements SessionBean {

  private static final long serialVersionUID = 1L;

  private static final Log log = Log.getLog(CommandExecutorSLSB.class.getName());

  protected ProcessEngine processEngine = null;
  protected SessionContext sessionContext;

  /*public <T> T execute(Command<T> command) {
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      log.debug("executing command " + command);
      return command.execute(environment);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new EJBException("failed to execute command " + command, e);
    }
    finally {
      environment.close();
    }
  } */

  public <T> T execute(Command<T> command) {
    log.info("Execute " + command);
    CommandService cmd = (CommandService) processEngine.get(CommandService.NAME_TX_REQUIRED_COMMAND_SERVICE);
    return cmd.execute(command);
  }

  public void setSessionContext(SessionContext sessionContext) {
    this.sessionContext = sessionContext;
  }

  /**
   * Creates a command executor that will be used to execute the commands that
   * are passed in the execute method.
   */
  public void ejbCreate() throws CreateException {

    try
    {
      InitialContext ctx = new InitialContext();
      this.processEngine = (ProcessEngine)ctx.lookup("java:ProcessEngine");
    }
    catch (Exception e)
    {
      throw new RuntimeException("Failed to lookup process engine", e);
    }

    /*String envFactoryName = "java:jbpm/EnvironmentFactory";
    try {
      Context initial = new InitialContext();

      // determine environment factory name
      try {
        envFactoryName = (String) initial.lookup("java:comp/env/EnvironmentFactoryName");
      }
      catch (NameNotFoundException e) {
        log.debug("environment factory name not set, using default: " + envFactoryName);
      }

      try {
        // retrieve environment factory
        Object namedObject = initial.lookup(envFactoryName);
        if (namedObject instanceof EnvironmentFactory) {
          log.debug("using environment factory at " + envFactoryName);
          environmentFactory = (EnvironmentFactory) namedObject;
        }
        else {

          if (namedObject == null || isInstance(EnvironmentFactory.class.getName(), namedObject)) {
            log.debug("object bound to "
                + envFactoryName
                + " is a stale object factory, or null; unbinding it");
            initial.unbind(envFactoryName);

            environmentFactory = parseConfig(getConfigResource(initial));
            bind(initial, environmentFactory, envFactoryName);
          }
          else {
            log.debug("object bound to "
                + envFactoryName
                + " is not an environment factory, building one");
            environmentFactory = parseConfig(getConfigResource(initial));
            // no bind attempt
          }
        }
      }
      catch (NameNotFoundException noEnv) {
        log.debug("environment factory not found at " + envFactoryName + ", building it");
        environmentFactory = parseConfig(getConfigResource(initial));
        bind(initial, environmentFactory, envFactoryName);
      }
    }
    catch (NamingException e) {
      log.error("could not create command executor", e);
      throw new CreateException("jndi access failed");
    }

    */

  }

  private static boolean isInstance(String className, Object object) {
    for (Class<?> cl = object.getClass(); cl != Object.class; cl = cl.getSuperclass()) {
      if (cl.getName().equals(className))
        return true;
    }
    return false;
  }

  private static String getConfigResource(Context context) throws NamingException {
    String resource = "jbpm.cfg.xml";
    try {
      resource = (String) context.lookup("java:comp/env/ConfigurationResource");
    }
    catch (NameNotFoundException e) {
      log.debug("configuration resource not set, using default: " + resource);
    }
    return resource;
  }

  private static EnvironmentFactory parseConfig(String resource) {
    log.debug("parsing configuration from " + resource);
    return (EnvironmentFactory) new Configuration().setResource(resource).buildProcessEngine();
  }

  private static void bind(Context context, EnvironmentFactory environmentFactory, String name) {
    try {
      context.bind(name, environmentFactory);
      log.info("bound " + environmentFactory + " to " + name);
    }
    catch (NamingException e) {
      log.info("WARNING: environment factory binding failed", e);
    }
  }

  public void ejbRemove() {
    processEngine = null;
    sessionContext = null;
  }

  public void ejbActivate() {
  }

  public void ejbPassivate() {
  }
}

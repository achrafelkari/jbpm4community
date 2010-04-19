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

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Command;
import org.jbpm.enterprise.internal.wire.binding.EjbRemoteCommandServiceBinding;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.cmd.CommandService;


/**
 * @author Tom Baeyens
 */
public class EjbRemoteCommandService implements CommandService {
  
  protected String initialContextFactory; 
  protected String providerUrl; 
  protected String urlPkgPrefixes;
  
  protected String jndiName;
  
  public EjbRemoteCommandService(String initialContextFactory, String providerUrl, String urlPkgPrefixes, String jndiName) {
    this.initialContextFactory = initialContextFactory;
    this.providerUrl = providerUrl;
    this.urlPkgPrefixes = urlPkgPrefixes;
    this.jndiName = jndiName;
  }

  public <T> T execute(Command<T> command) {    
    try {
      Hashtable<String, String> env = new Hashtable<String, String>();
      env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
      env.put(Context.PROVIDER_URL, providerUrl);
      env.put(Context.URL_PKG_PREFIXES, urlPkgPrefixes);

      InitialContext initialContext = new InitialContext(env);
      
      RemoteCommandExecutorHome remoteCommandExecutorHome = (RemoteCommandExecutorHome) initialContext.lookup(jndiName);
      RemoteCommandExecutor remoteCommandExecutor = remoteCommandExecutorHome.create();
      Object result = remoteCommandExecutor.execute(command);
      remoteCommandExecutor.remove();
      return (T) result;

    } catch (Exception e) {
      throw new JbpmException("couldn't execute remote command: "+e.getMessage(), e);
    }
  }
}

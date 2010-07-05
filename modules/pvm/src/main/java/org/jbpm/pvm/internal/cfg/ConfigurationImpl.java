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
package org.jbpm.pvm.internal.cfg;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jbpm.api.Configuration;
import org.jbpm.api.ProcessEngine;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.env.Context;
import org.jbpm.pvm.internal.processengine.ProcessEngineImpl;
import org.jbpm.pvm.internal.processengine.SpringProcessEngine;
import org.jbpm.pvm.internal.stream.FileStreamInput;
import org.jbpm.pvm.internal.stream.InputStreamInput;
import org.jbpm.pvm.internal.stream.ResourceStreamInput;
import org.jbpm.pvm.internal.stream.StreamInput;
import org.jbpm.pvm.internal.stream.StringStreamInput;
import org.jbpm.pvm.internal.stream.UrlStreamInput;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireDefinition;
import org.jbpm.pvm.internal.wire.descriptor.ProvidedObjectDescriptor;

/**
 * @author Tom Baeyens
 */
public class ConfigurationImpl extends Configuration {
  
  public static final String DEFAULT_CONFIG_RESOURCENAME = "jbpm.cfg.xml";

  private static Log log = Log.getLog(ConfigurationImpl.class.getName());

  transient protected boolean isConfigured = false;
  transient String jndiName;
  transient boolean checkDb = true;
  transient boolean isSpringEnabled = false;
  // type is Object because we don't want a dependency on spring in this class
  transient Object applicationContext = null;
  transient WireContext processEngineWireContext = new WireContext(new WireDefinition(), Context.CONTEXTNAME_PROCESS_ENGINE, true);
  transient WireDefinition transactionWireDefinition = new WireDefinition();
  transient ProcessEngine producedProcessEngine;
  
  public ConfigurationImpl() {
    // to prevent a loop in the constructors, we need to delegate to a non-default constructor in Configuration
    super(null);
  }
  
  @Override
  public ProcessEngine buildProcessEngine() {
    if (!isConfigured) {
      setResource(DEFAULT_CONFIG_RESOURCENAME);
    }
    if (jndiName!=null) {
      try {
        InitialContext initialContext = new InitialContext();
        ProcessEngineImpl existing = (ProcessEngineImpl) initialContext.lookup(jndiName);
        if (existing!=null) {
          log.debug("found existing process engine under "+jndiName);
          return existing;
        }
      } catch (NamingException e) {
        log.debug("jndi name "+jndiName+" is not bound");
      }
    }
    
    if (isSpringEnabled) {
      return SpringProcessEngine.create(this);
    }
    return instantiateProcessEngine();
  }
  
  /**
   * This method is called at the end of the buildProcessEngine() operation.
   * Subclasses should override this method for custom ProcessEngine instantiation.
   */
  protected ProcessEngine instantiateProcessEngine() {
    return new ProcessEngineImpl(this);
  }

  public ConfigurationImpl setHibernateSessionFactory(Object hibernateSessionFactory) {
    processEngineWireContext
        .getWireDefinition()
        .addDescriptor(new ProvidedObjectDescriptor(hibernateSessionFactory, true));
    return this;
  }

  public ConfigurationImpl setInputStream(InputStream inputStream) {
    parse(new InputStreamInput(inputStream));
    return this;
  }

  public ConfigurationImpl setResource(String resource) {
    parse(new ResourceStreamInput(resource, getClassLoader()));
    return this;
  }

  public ConfigurationImpl setUrl(URL url) {
    parse(new UrlStreamInput(url));
    return this;
  }

  public ConfigurationImpl setFile(File file) {
    parse(new FileStreamInput(file));
    return this;
  }

  public ConfigurationImpl setXmlString(String xmlString) {
    parse(new StringStreamInput(xmlString));
    return this;
  }

  protected void parse(StreamInput streamSource) {
    isConfigured = true;
    ConfigurationParser.getInstance()
      .createParse()
      .contextStackPush(this)
      .setStreamSource(streamSource)
      .execute()
      .checkErrors("jbpm configuration " + streamSource);
  }
  
  // fluent setters ///////////////////////////////////////////////////////////

  public ConfigurationImpl skipDbCheck() {
    checkDb = false;
    return this;
  }

  public ConfigurationImpl jndiName(String jndiName) {
    this.jndiName = jndiName;
    return this;
  }

  public ConfigurationImpl springInitiated(Object applicationContext) {
    this.isSpringEnabled = true;
    this.applicationContext = applicationContext;
    return this;
  }

  public ConfigurationImpl springEnabled() {
    this.isSpringEnabled = true;
    return this;
  }

  public ConfigurationImpl jndi(String jndiName) {
    this.jndiName = jndiName;
    return this;
  }

  public boolean isInstantiatedFromSpring() {
    return (applicationContext!=null);
  }

  // getters and setters //////////////////////////////////////////////////////

  public WireContext getProcessEngineWireContext() {
    return processEngineWireContext;
  }
  public WireDefinition getTransactionWireDefinition() {
    return transactionWireDefinition;
  }
  public String getJndiName() {
    return jndiName;
  }
  public boolean isCheckDb() {
    return checkDb;
  }
  public ProcessEngine getProducedProcessEngine() {
    return producedProcessEngine;
  }
  public void setProducedProcessEngine(ProcessEngineImpl processEngine) {
    this.producedProcessEngine = processEngine;
  }
  public Object getApplicationContext() {
    return applicationContext;
  }
}

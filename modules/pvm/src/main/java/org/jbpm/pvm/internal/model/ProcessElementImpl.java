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
package org.jbpm.pvm.internal.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jbpm.pvm.internal.wire.Descriptor;

/** 
 * superclass for {@link ActivityImpl}, {@link TransitionImpl} and {@link ProcessDefinitionImpl}.
 * 
 * @author Tom Baeyens
 */
 
public class ProcessElementImpl implements Serializable {

  private static final long serialVersionUID = 1L;

  protected long dbid;
  protected int dbversion;
  protected ProcessDefinitionImpl processDefinition;
  protected List<ExceptionHandlerImpl> exceptionHandlers;
  protected WireProperties properties;

  // exception handlers ///////////////////////////////////////////////////////

  public ExceptionHandlerImpl createExceptionHandler() {
    ExceptionHandlerImpl exceptionHandler = new ExceptionHandlerImpl();
    addExceptionHandler(exceptionHandler);
    return exceptionHandler;
  }

  public void addExceptionHandler(ExceptionHandlerImpl exceptionHandler) {
    if (exceptionHandlers==null) {
      exceptionHandlers = new ArrayList<ExceptionHandlerImpl>();
    }
    exceptionHandler.setProcessDefinition(processDefinition);
    exceptionHandlers.add(exceptionHandler);
  }
  
  // properties ///////////////////////////////////////////////////////////////

  public void addProperty(Descriptor descriptor) {
    if (properties==null) {
      properties = new WireProperties();
    }
    properties.add(descriptor);
  }

  public Object getProperty(String key) {
    if (properties==null) {
      return null;
    }
    return properties.get(key);
  }

  public Set<String> getPropertyKeys() {
    if (properties==null) {
      return Collections.emptySet();
    }
    return properties.keys();
  }

  // getters and setters //////////////////////////////////////////////////////

  /** the database id. */
  public long getDbid() {
    return dbid;
  }
  public ProcessDefinitionImpl getProcessDefinition() {
    return processDefinition;
  }
  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
  }
  public WireProperties getProperties() {
    return properties;
  }
  public void setProperties(WireProperties properties) {
    this.properties = properties;
  }
  public List<ExceptionHandlerImpl> getExceptionHandlers() {
    return exceptionHandlers;
  }
  public void setExceptionHandlers(List<ExceptionHandlerImpl> exceptionHandlers) {
    this.exceptionHandlers = exceptionHandlers;
  }
}

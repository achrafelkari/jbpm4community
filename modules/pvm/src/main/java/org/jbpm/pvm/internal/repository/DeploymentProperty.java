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
package org.jbpm.pvm.internal.repository;

import java.io.Serializable;

import org.hibernate.Session;
import org.jbpm.api.JbpmException;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.id.DbidGenerator;


/**
 * @author Tom Baeyens
 */
public class DeploymentProperty implements Serializable {
  
  private static final long serialVersionUID = 1L;

  long dbid;
  protected DeploymentImpl deployment;
  protected String objectName;
  protected String key;
  protected String stringValue;
  protected Long longValue;
  
  public DeploymentProperty() {
  }
  
  public DeploymentProperty(DeploymentImpl deployment, String objectName, String key) {
    this.dbid = DbidGenerator.getDbidGenerator().getNextId();
    EnvironmentImpl.getFromCurrent(Session.class).save(this);
    this.deployment = deployment;
    this.objectName = objectName;
    this.key = key;
  }
  
  public String toString() {
    return "{"+objectName+","+key+","+(stringValue!=null?stringValue:longValue)+"}";
  }

  public String getObjectName() {
    return objectName;
  }
  public String getKey() {
    return key;
  }
  public DeploymentImpl getDeployment() {
    return deployment;
  }
  public String getStringValue() {
    return stringValue;
  }
  public Long getLongValue() {
    return longValue;
  }
  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
  }
  public void setLongValue(Long longValue) {
    this.longValue = longValue;
  }

  public void setValue(Object value) {
    if (value==null) {
      stringValue = null;
      longValue = null;
    } else if (value instanceof String) {
      stringValue = (String)value;
    } else if (value instanceof Long) {
      longValue = (Long)value;
    } else {
      throw new JbpmException("value type "+value.getClass().getName()+" not supported (only string and long)");
    }
  }
  
  public Object getValue() {
    if (stringValue!=null) {
      return stringValue;
    }
    return longValue;
  }
}

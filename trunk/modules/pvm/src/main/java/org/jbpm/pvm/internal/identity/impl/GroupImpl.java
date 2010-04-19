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
package org.jbpm.pvm.internal.identity.impl;

import java.io.Serializable;

import org.jbpm.api.identity.Group;


/**
 * @author Tom Baeyens
 */
public class GroupImpl implements Group, Serializable {

  private static final long serialVersionUID = 1L;
  
  protected long dbid;
  protected int dbversion;

  protected GroupImpl parent;

  protected String id;
  protected String name;
  protected String type;

  public GroupImpl() {
  }

  public GroupImpl(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public GroupImpl getParent() {
    return parent;
  }
  public void setParent(GroupImpl parent) {
    this.parent = parent;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public void setDbid(long dbid) {
    this.dbid = dbid;
  }
}
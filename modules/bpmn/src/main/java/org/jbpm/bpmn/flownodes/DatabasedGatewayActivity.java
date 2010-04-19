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
package org.jbpm.bpmn.flownodes;

/**
 * Superclass for gateway activities which use process data to synchronize (ie merge or split)
 * sequence flow. A common feature of those gateway types is the possibility to define
 * a default outgoing sequence flow, that will be taken when no condition evaluates to true.
 * 
 * @author Joram Barrez
 */
public abstract class DatabasedGatewayActivity extends AbstractGatewayActivity {
  
  protected String default_;
  
  public String getDefault() {
    return default_;
  }

  public void setDefault(String default_) {
    this.default_ = default_;
  }

}

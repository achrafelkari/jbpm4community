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

import org.jbpm.api.identity.User;

/**
 * @author Tom Baeyens
 */
public class UserImpl implements User, Serializable {

  private static final long serialVersionUID = 1L;

  protected long dbid;
  protected int dbversion;

  protected String id;
  protected String password;
  protected String givenName;
  protected String familyName;
  protected String businessEmail;

  public UserImpl() {
  }

  public UserImpl(String id, String givenName, String familyName) {
    this.id = id;
    this.givenName = givenName;
    this.familyName = familyName;
  }

  public String getId() {
    return id;
  }

  public String getGivenName() {
    return givenName;
  }

  public String getFamilyName() {
    return familyName;
  }

  public String getBusinessEmail() {
    return businessEmail;
  }

  public String toString() {
    return givenName != null ? familyName != null ? givenName + ' ' + familyName : givenName
        : familyName != null ? familyName : id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }

  public void setFamilyName(String familyName) {
    this.familyName = familyName;
  }

  public void setBusinessEmail(String emailAddress) {
    this.businessEmail = emailAddress;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setDbid(long dbid) {
    this.dbid = dbid;
  }
}

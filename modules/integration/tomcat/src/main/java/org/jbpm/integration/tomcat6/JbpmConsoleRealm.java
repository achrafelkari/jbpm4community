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
/**
 * 
 */
package org.jbpm.integration.tomcat6;

import java.security.Principal;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.JDBCRealm;
import org.apache.catalina.realm.RealmBase;


/**
 * Basic implementation of a Realm authenticator for the jBPM identity tables
 * using plain JDBC queries.
 * 
 * The default provided JDBCRealm cannot be used, since the tables need
 * to follow a schema which does not map to the jBPM identity tables.
 * See http://tomcat.apache.org/tomcat-6.0-doc/realm-howto.html#JDBCRealm.
 * 
 * This code is based on the {@link JDBCRealm} code.
 * 
 * @author Joram Barrez
 */
public class JbpmConsoleRealm extends RealmBase {
  
  private String driverName;
  
  private String connectionUrl;
  
  private String connectionName;
  
  private String connectionPassword;
  
  private Driver driver;
   
  public Principal authenticate(String user, String credentials) {
    Connection conn = null;
    try {
      
      conn = openConnection();
      Long userId = retrieveUserId(conn, user, credentials);
      
      if (userId != null) {
        List<String> roles = retrieveRoles(conn, userId);
        return new GenericPrincipal(this, user, credentials, roles);
      }
      
    } catch (Exception e) {
      containerLog.error(e);
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException e) {
          containerLog.error(e);
        }
      }
    }
    return null;
  }
  
  private Long retrieveUserId(Connection conn, String username, String password) throws SQLException {
    Long result = null;
    PreparedStatement stm = null;
    try {
      stm = conn.prepareStatement("SELECT DBID_ FROM JBPM4_ID_USER WHERE ID_=? AND PASSWORD_=?");
      stm.setString(1, username);
      stm.setString(2, password);
      ResultSet rs = stm.executeQuery();
      while (rs.next()) {
        return rs.getLong(1);
      }
    } finally {
      stm.close();
    }
    return result;
  }
  
  private List<String> retrieveRoles(Connection conn, Long userDbId) throws SQLException {
    List<String> roles = new ArrayList<String>();
    PreparedStatement stm = null;
    try {
      stm = conn.prepareStatement("SELECT JBPM4_ID_GROUP.NAME_ FROM JBPM4_ID_GROUP " +
                "INNER JOIN JBPM4_ID_MEMBERSHIP ON JBPM4_ID_MEMBERSHIP.GROUP_=JBPM4_ID_GROUP.DBID_ " +
                "INNER JOIN JBPM4_ID_USER ON JBPM4_ID_MEMBERSHIP.USER_=JBPM4_ID_USER.DBID_ " +
                "WHERE JBPM4_ID_USER.DBID_=?");
      stm.setLong(1, userDbId);
      ResultSet rs = stm.executeQuery();
      while (rs.next()) {
        roles.add(rs.getString(1));
      }
    } finally {
      stm.close();
    }
    return roles;
  }
  
  public Principal authenticate(String user, byte[] credentials) {
    return authenticate(user, new String(credentials));
  }
  
  public Principal authenticate(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, String arg6, String arg7) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * Return the password associated with the principal user name.
   */
  @Override
  protected String getPassword(String username) {
    throw new UnsupportedOperationException();
  }
  

  /**
   * Return the principal associated with the given username.
   */
  @Override
  protected Principal getPrincipal(String userName) {
    throw new UnsupportedOperationException();
  }
  

  protected String getName() {
    return this.getClass().getName();
  }

  public String getInfo() {
    return "JbpmConsoleRealm";
  }
  
  
  private Connection openConnection() throws SQLException {
    
    if (driver == null) {
      try {
        driver = (Driver) Class.forName(driverName).newInstance();
        DriverManager.setLoginTimeout(10);
      } catch (Exception e) {
        throw new RuntimeException("Could not instantiate driver " + driverName);
      }
    }
    
    
    Properties props = new Properties();
    props.put("user", connectionName);
    props.put("password", connectionPassword);
    
    Connection conn = driver.connect(connectionUrl, props);
    conn.setReadOnly(true);
    return conn;
  }
  
  /**
   * @return the driverName
   */
  public String getDriverName() {
    return driverName;
  }


  
  /**
   * @param driverName the driverName to set
   */
  public void setDriverName(String driverName) {
    this.driverName = driverName;
  }


  
  /**
   * @return the connectionUrl
   */
  public String getConnectionUrl() {
    return connectionUrl;
  }


  
  /**
   * @param connectionUrl the connectionUrl to set
   */
  public void setConnectionUrl(String connectionUrl) {
    this.connectionUrl = connectionUrl;
  }


  
  /**
   * @return the connectionName
   */
  public String getConnectionName() {
    return connectionName;
  }


  
  /**
   * @param connectionName the connectionName to set
   */
  public void setConnectionName(String connectionName) {
    this.connectionName = connectionName;
  }


  
  /**
   * @return the connectionPassword
   */
  public String getConnectionPassword() {
    return connectionPassword;
  }


  
  /**
   * @param connectionPassword the connectionPassword to set
   */
  public void setConnectionPassword(String connectionPassword) {
    this.connectionPassword = connectionPassword;
  }

}

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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.catalina.ServerFactory;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.JDBCRealm;
import org.apache.catalina.realm.RealmBase;
import org.apache.naming.ContextBindings;

/**
 * <p>
 * Basic implementation of a Realm authenticator for the jBPM identity tables using plain JDBC
 * queries.
 * </p>
 * <p>
 * The default provided JDBCRealm cannot be used, since the tables need to follow a schema which
 * does not map to the jBPM identity tables.
 * </p>
 * <p>
 * This code is based on the {@link JDBCRealm} code.
 * </p>
 * 
 * @see <a href="http://tomcat.apache.org/tomcat-6.0-doc/realm-howto.html">Realm How-To</a>
 * @author Joram Barrez
 * @author Alejandro Guizar
 */
public class JbpmConsoleRealm extends RealmBase {

  private String driverName;
  private String connectionUrl;
  private String connectionName;
  private String connectionPassword;

  private String dataSourceName;
  private boolean localDataSource;
  private DataSource dataSource;

  public Principal authenticate(String user, String credentials) {
    try {
      Connection conn = openConnection();
      try {
        long userId = retrieveUserId(conn, user, credentials);
        if (userId != -1) {
          List<String> roles = retrieveRoles(conn, userId);
          return new GenericPrincipal(this, user, credentials, roles);
        }
      }
      finally {
        conn.close();
      }
    }
    catch (SQLException e) {
      containerLog.error(e);
    }
    return null;
  }

  private long retrieveUserId(Connection conn, String username, String password)
    throws SQLException {
    PreparedStatement stm = conn.prepareStatement("SELECT DBID_\n"
      + "FROM JBPM4_ID_USER\n"
      + "WHERE ID_=? AND PASSWORD_=?");
    try {
      stm.setString(1, username);
      stm.setString(2, password);
      ResultSet rs = stm.executeQuery();
      if (rs.next()) {
        return rs.getLong(1);
      }
    }
    finally {
      stm.close();
    }
    return -1;
  }

  private List<String> retrieveRoles(Connection conn, Long userDbId) throws SQLException {
    PreparedStatement stm = conn.prepareStatement("SELECT JBPM4_ID_GROUP.NAME_\n"
      + "FROM JBPM4_ID_GROUP\n"
      + "INNER JOIN JBPM4_ID_MEMBERSHIP ON JBPM4_ID_MEMBERSHIP.GROUP_=JBPM4_ID_GROUP.DBID_\n"
      + "INNER JOIN JBPM4_ID_USER ON JBPM4_ID_MEMBERSHIP.USER_=JBPM4_ID_USER.DBID_\n"
      + "WHERE JBPM4_ID_USER.DBID_=?");
    try {
      stm.setLong(1, userDbId);
      ResultSet rs = stm.executeQuery();
      List<String> roles = new ArrayList<String>();
      while (rs.next()) {
        roles.add(rs.getString(1));
      }
      return roles;
    }
    finally {
      stm.close();
    }
  }

  public Principal authenticate(String user, byte[] credentials) {
    return authenticate(user, new String(credentials));
  }

  public Principal authenticate(String arg0, String arg1, String arg2, String arg3,
    String arg4, String arg5, String arg6, String arg7) {
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
    return getClass().getName();
  }

  public String getInfo() {
    return "JbpmConsoleRealm";
  }

  private Connection openConnection() throws SQLException {
    if (dataSourceName != null) {
      if (dataSource == null) {
        try {
          Context context;
          if (localDataSource) {
            context = (Context) ContextBindings.getClassLoader().lookup("comp/env");
          }
          else {
            StandardServer server = (StandardServer) ServerFactory.getServer();
            context = server.getGlobalNamingContext();
          }
          dataSource = (DataSource) context.lookup(dataSourceName);
        }
        catch (NamingException e) {
          SQLException sqlException = new SQLException("failed to retrieve " + dataSourceName);
          sqlException.initCause(e);
          throw sqlException;
        }
      }
      return dataSource.getConnection();
    }
    else {
      try {
        Class.forName(driverName);
        Connection connection = DriverManager.getConnection(connectionUrl,
          connectionName,
          connectionPassword);
        connection.setReadOnly(true);
        return connection;
      }
      catch (ClassNotFoundException e) {
        SQLException sqlException = new SQLException("could not find " + driverName);
        sqlException.initCause(e);
        throw sqlException;
      }
    }
  }

  public String getDataSourceName() {
    return dataSourceName;
  }

  public void setDataSourceName(String dataSourceName) {
    this.dataSourceName = dataSourceName;
  }

  public String getDriverName() {
    return driverName;
  }

  public void setDriverName(String driverName) {
    this.driverName = driverName;
  }

  public String getConnectionUrl() {
    return connectionUrl;
  }

  public void setConnectionUrl(String connectionUrl) {
    this.connectionUrl = connectionUrl;
  }

  public String getConnectionName() {
    return connectionName;
  }

  public void setConnectionName(String connectionName) {
    this.connectionName = connectionName;
  }

  public String getConnectionPassword() {
    return connectionPassword;
  }

  public void setConnectionPassword(String connectionPassword) {
    this.connectionPassword = connectionPassword;
  }

}

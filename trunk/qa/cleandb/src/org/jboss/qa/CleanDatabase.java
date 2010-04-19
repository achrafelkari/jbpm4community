package org.jboss.qa;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class CleanDatabase extends Task {
  
  static String ORACLE = "oracle";
  static String MYSQL = "mysql";
  static String POSTGRESQL = "postgtresql";
  static String DB2 = "db2";
  static String HSQLDB = "hsqldb";
  static String OTHER = "other";

  private String url;
  private String driver;
  private String username;
  private String password;
  private String schema;
  
  private String dbType;
  private String quote; 
  private String dropTableString = null;
  private String dropForeignKeyString = null;
  private boolean dropConstraintsFirst = true;

  public void execute() throws BuildException {

    Connection connection = null;
    try {
      connection = getConnection();
      initialize(connection);

      List<String> tableNames = getTableNames(connection);

      if (dbType==MYSQL) {
        for (String tableName : tableNames) {
          Set<String> foreignKeyNames = getForeignKeyNames(connection, tableName);
          for (String foreignKeyName : foreignKeyNames) {
            String stmt = dropForeignKeyString
                    .replaceAll("@TABLE@", Matcher.quoteReplacement(tableName))
                    .replaceAll("@FK@", Matcher.quoteReplacement(foreignKeyName));
            log("dropping foreign key: " + stmt);
            connection.createStatement().executeUpdate(stmt);
          }
        }
      }

      for (String tableName: tableNames) {
        String stmt = dropTableString
                    .replaceAll("@TABLE@", Matcher.quoteReplacement(tableName));
        log("dropping table: " + stmt);
        connection.createStatement().executeUpdate(stmt);
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException(e);

    } finally {
      if (connection != null) {
        try {
          if (!connection.isClosed()) {
            connection.close();
          }
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private List<String> getTableNames(Connection connection) throws Exception {
    List<String> tableNames = new ArrayList<String>();
    
    String[] types = { "TABLE" };
    ResultSet resultSet = connection.getMetaData().getTables(null, schema, null, types);
    while (resultSet.next()) {
      String tableName = resultSet.getString("TABLE_NAME");
      if ( tableName.startsWith("JBPM") 
           || tableName.startsWith("jbpm") 
         ) {
        log("adding table "+tableName);
        tableNames.add(tableName);
      } else {
        log("--- skipping table "+tableName+" ---");
      }
    }
    
    return tableNames;
  }

  private Set<String> getForeignKeyNames(Connection connection, String table) throws Exception {
    ResultSet resultSet = connection.getMetaData().getImportedKeys(null, schema, table);
    // we are interested in the FK's only, not in the column they
    // represent...
    // the above method returns one record for each column which is part
    // of a FK
    // eg:
    // FK1 represents CUST_ID and ORDR_NUM from EMBD_KM2O_ORDR table
    // it returns two rows, but we want to drop the constraint (one
    // record).
    // So, we use another list, to get rid of "duplicates"
    Set<String> foreignKeyNames = new HashSet<String>();
    while (resultSet.next()) {
      String foreignKeyName = resultSet.getString("FK_NAME");
      if (foreignKeyName != null) {
        foreignKeyNames.add(foreignKeyName);
      }
    }
    return foreignKeyNames;
  }

  private Connection getConnection() throws Exception {
    // initialize the JDBC driver
    log("initializing JDBC driver "+getDriver());
    Class.forName(getDriver());
    
    // create the connection
    log("creating JDBC connection "+getUrl());
    return DriverManager.getConnection(getUrl(), getUsername(), getPassword());
  }

  private void initialize(Connection connection) throws Exception {
    quote = connection.getMetaData().getIdentifierQuoteString();

    // schema is optional, but let's make it null if it is empty
    if ("".equals(schema)) {
      schema = null;
    }

    if (getUrl().startsWith("jdbc:mysql")) {
      dbType = MYSQL;
      dropForeignKeyString = "ALTER TABLE " + quote + "@TABLE@" + quote + " DROP FOREIGN KEY " + quote + "@FK@" + quote;
      
    } else if (getUrl().startsWith("jdbc:postgresql")) {
      dbType = POSTGRESQL;
      dropTableString = getDropTableString() + " CASCADE";
      
    } else if (getUrl().startsWith("jdbc:oracle")) {
      dbType = ORACLE;
      dropTableString = "DROP TABLE "+(schema!=null ? quote+schema+quote+"." : "")+quote+"@TABLE@"+quote+" CASCADE CONSTRAINTS";
      
    } else if (getUrl().startsWith("jdbc:db2")) {
      dbType = DB2;
      // db2 drops the constraints by default, when dropping a table
      
    } else {
      dbType = OTHER;
      dropTableString = "DROP TABLE " + (schema!=null ? quote+schema+"." : "") + quote + "@TABLE@" + quote;
      dropForeignKeyString = "ALTER TABLE " + quote + "@TABLE@" + quote + " DROP CONSTRAINT " + quote + "@FK@" + quote;
    }
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getDriver() {
    return driver;
  }

  public void setDriver(String driver) {
    this.driver = driver;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getDropTableString() {
    return dropTableString;
  }

  public void setDropTableString(String dropTableString) {
    this.dropTableString = dropTableString;
  }

  public String getDropForeignKeyString() {
    return dropForeignKeyString;
  }

  public void setDropForeignKeyString(String dropForeignKeyString) {
    this.dropForeignKeyString = dropForeignKeyString;
  }

  public boolean isDropConstraintsFirst() {
    return dropConstraintsFirst;
  }

  public void setDropConstraintsFirst(boolean dropConstraintsFirst) {
    this.dropConstraintsFirst = dropConstraintsFirst;
  }

}
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
package org.jbpm.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Table;
import org.jbpm.api.ProcessEngine;

/**
 * @author Tom Baeyens
 */
public class Db {
  
  private static final Map<ProcessEngine, String[]> cleanSqlCache = new HashMap<ProcessEngine, String[]>();
  private static final Map<ProcessEngine, String[]> tableNamesCache = new HashMap<ProcessEngine, String[]>();
  
  public static void clean(ProcessEngine processEngine) {
    SessionFactory sessionFactory = processEngine.get(SessionFactory.class);
    // when running this with a remote ejb invocation configuration, there is no
    // session factory and no cleanup needs to be done
    if (sessionFactory==null) {
      return;
    }
    
    String[] cleanSql = cleanSqlCache.get(processEngine);

    if (cleanSql == null) {
      Configuration configuration = processEngine.get(Configuration.class);
      
      SessionFactoryImplementor sessionFactoryImplementor = (SessionFactoryImplementor) sessionFactory;
      Dialect dialect = sessionFactoryImplementor.getDialect();

      // loop over all foreign key constraints
      List<String> dropForeignKeysSql = new ArrayList<String>();
      List<String> createForeignKeysSql = new ArrayList<String>();
      
      //if no session-factory is build, the configuration is not fully initialized.
      //Hence, the ForeignKey's won't have a referenced table. This is calculated on 
      //second pass.
      configuration.buildMappings();
      
      for (Iterator<?> iter = configuration.getTableMappings(); iter.hasNext();) {
        Table table = (Table) iter.next();
        if (table.isPhysicalTable()) {
          String catalog = table.getCatalog();
          String schema = table.getSchema();

          for (Iterator<?> subIter = table.getForeignKeyIterator(); subIter.hasNext();) {
            ForeignKey fk = (ForeignKey) subIter.next();
            if (fk.isPhysicalConstraint()) {
              // collect the drop foreign key constraint sql
              dropForeignKeysSql.add(fk.sqlDropString(dialect, catalog, schema));
              // MySQLDialect creates an index for each foreign key.
              // see http://opensource.atlassian.com/projects/hibernate/browse/HHH-2155
              // This index should be dropped or an error will be thrown during
              // the creation phase
              if (dialect instanceof MySQLDialect) {
                dropForeignKeysSql.add("alter table " + table.getName() + " drop key " + fk.getName());
              }
              // and collect the create foreign key constraint sql
              createForeignKeysSql.add(fk.sqlCreateString(dialect, sessionFactoryImplementor, catalog, schema));
            }
          }
        }
      }

      List<String> deleteSql = new ArrayList<String>();
      for (Iterator<?> iter = configuration.getTableMappings(); iter.hasNext();) {
        Table table = (Table) iter.next();
        if (table.isPhysicalTable()) {
          deleteSql.add("delete from " + table.getName());
        }
      }

      // glue
      // - drop foreign key constraints
      // - delete contents of all tables
      // - create foreign key constraints
      // together to form the clean script
      List<String> cleanSqlList = new ArrayList<String>();
      cleanSqlList.addAll(dropForeignKeysSql);
      cleanSqlList.addAll(deleteSql);
      cleanSqlList.addAll(createForeignKeysSql);

      cleanSql = cleanSqlList.toArray(new String[cleanSqlList.size()]);
      
      cleanSqlCache.put(processEngine, cleanSql);
    }

    Session session = sessionFactory.openSession();
    try {
      for (String query : cleanSql) {
        // log.trace(query);
        session.createSQLQuery(query).executeUpdate();
      }
    } finally {
      session.close();
    }
  }

  public static String verifyClean(ProcessEngine processEngine) {
    SessionFactory sessionFactory = processEngine.get(SessionFactory.class);
    // when running this with a remote ejb invocation configuration, there is no
    // session factory and no cleanup needs to be done
    if (sessionFactory==null) {
      return null;
    }
    
    String[] tableNames = tableNamesCache.get(processEngine);

    if (tableNames == null) {
      Configuration configuration = processEngine.get(Configuration.class);
      
      // loop over all foreign key constraints
      List<String> tableNamesList = new ArrayList<String>();
      for (Iterator<?> iter = configuration.getTableMappings(); iter.hasNext();) {
        Table table = (Table) iter.next();
        if (table.isPhysicalTable()) {
          tableNamesList.add(table.getName());
        }
      }

      tableNames = tableNamesList.toArray(new String[tableNamesList.size()]);
      
      tableNamesCache.put(processEngine, tableNames);
    }

    String recordsLeftMsg = "";
    Session session = sessionFactory.openSession();
    try {
      for (String tableName : tableNames) {
        if (!"JBPM4_PROPERTY".equals(tableName)) {
          String countSql = "select count(*) as RECORD_COUNT_ from "+tableName;
          SQLQuery sqlQuery = session.createSQLQuery(countSql);
          sqlQuery.addScalar("RECORD_COUNT_", Hibernate.LONG);
          Long recordCount = (Long) sqlQuery.uniqueResult();
          if (recordCount>0L) {
            recordsLeftMsg += tableName+":"+recordCount+", ";
          }
        }
      }
    } finally {
      session.close();
    }
    
    if (recordsLeftMsg.length()>0) {
      clean(processEngine);
    }
    
    return recordsLeftMsg;
  }
}

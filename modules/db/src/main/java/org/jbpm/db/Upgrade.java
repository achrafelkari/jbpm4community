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
package org.jbpm.db;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.cmd.VoidCommand;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.cfg.ConfigurationImpl;
import org.jbpm.pvm.internal.id.PropertyImpl;
import org.jbpm.pvm.internal.processengine.ProcessEngineImpl;
import org.jbpm.pvm.internal.repository.DeploymentImpl;
import org.jbpm.pvm.internal.repository.DeploymentProperty;
import org.jbpm.pvm.internal.util.CollectionUtil;

/**
 * @author Tom Baeyens
 * @author Alejandro Guizar
 */
public class Upgrade {

  private static final long serialVersionUID = 1L;

  static final Log log = Log.getLog(Upgrade.class.getName());

  private Upgrade() {
    // hide default constructor to prevent instantiation
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      DbHelper.printSyntax(Upgrade.class);
      return;
    }

    String database = args[0];
    ProcessEngine processEngine = new ConfigurationImpl().skipDbCheck().buildProcessEngine();

    try {
      boolean propertiesTableExists = processEngine.execute(PropertiesTableExists.INSTANCE);
      final JbpmVersion databaseVersion = processEngine.execute(new FindDatabaseVersion(propertiesTableExists));
      JbpmVersion libraryVersion = JbpmVersion.getJbpmVersion(ProcessEngineImpl.JBPM_LIBRARY_VERSION);

      if (databaseVersion == libraryVersion) {
        log.info("database schema is already up to date");
      }
      else {
        processEngine.execute(new RunUpgradeScripts(database, databaseVersion, propertiesTableExists));
        processEngine.execute(new UpdateDatabaseVersion(databaseVersion));
      }

      log.info("database schema upgraded successfully");
    }
    catch (Exception e) {
      log.error("database schema upgrade failed", e);
    }
    finally {
      processEngine.close();
    }
  }

  private static class PropertiesTableExists implements Command<Boolean> {

    private static final long serialVersionUID = 1L;

    static final PropertiesTableExists INSTANCE = new PropertiesTableExists();

    public Boolean execute(Environment environment) throws Exception {
      Session session = environment.get(Session.class);
      return PropertyImpl.propertiesTableExists(session);
    }
  }

  private static class FindDatabaseVersion implements Command<JbpmVersion> {

    private final boolean propertiesTableExists;

    private static final long serialVersionUID = 1L;

    FindDatabaseVersion(boolean propertiesTableExists) {
      this.propertiesTableExists = propertiesTableExists;
    }

    public JbpmVersion execute(Environment environment) throws Exception {
      Session session = environment.get(Session.class);
      if (!propertiesTableExists) {
        try {
          session.createSQLQuery("select CLASSNAME_ from JBPM4_VARIABLE").list();
          return JbpmVersion.V_4_1;
        }
        catch (HibernateException e) {
          return JbpmVersion.V_4_0;
        }
      }
      else {
        String dbVersion = PropertyImpl.getDbVersion(session);
        if (dbVersion == null) {
          throw new JbpmException("property table exists, but no db version property is present");
        }
        return JbpmVersion.getJbpmVersion(dbVersion);
      }
    }
  }

  private static class RunUpgradeScripts extends VoidCommand {

    private final JbpmVersion databaseVersion;
    private final String database;
    private final boolean propertiesTableExists;

    private static final long serialVersionUID = 1L;

    RunUpgradeScripts(String database, JbpmVersion databaseVersion,
      boolean propertiesTableExists) {
      this.database = database;
      this.databaseVersion = databaseVersion;
      this.propertiesTableExists = propertiesTableExists;
    }

    protected void executeVoid(Environment environment) throws Exception {
      Session session = environment.get(Session.class);

      log.info("upgrading from " + databaseVersion + " to "
        + ProcessEngineImpl.JBPM_LIBRARY_VERSION);

      if (databaseVersion.compareTo(JbpmVersion.V_4_1) < 0) {
        DbHelper.executeSqlResource("upgrade-4.0-to-4.1/jbpm." + database + ".upgrade.sql", session);
      }

      if (databaseVersion.compareTo(JbpmVersion.V_4_2) < 0) {
        // the first part of the upgrade to 4.2 might already be done before as that
        // happens in the next transaction (and that next transaction might have failed in
        // a previous run of upgrade)
        // in that case, the next part is skipped
        if (!propertiesTableExists) {
          DbHelper.executeSqlResource("upgrade-4.1-to-4.2/jbpm." + database + ".upgrade.sql", session);
          PropertyImpl.initializeNextDbid(session);
          // we set the version to 4.1 as the next transaction might fail
          PropertyImpl.setDbVersionTo41(session);
        }
      }
      // transaction is now committed as the next transaction requires the NextDbid
      // property to be initialized and committed.
    }
  }

  private static final class UpdateDatabaseVersion extends VoidCommand {

    private final JbpmVersion databaseVersion;

    private static final long serialVersionUID = 1L;

    UpdateDatabaseVersion(JbpmVersion databaseVersion) {
      this.databaseVersion = databaseVersion;
    }

    protected void executeVoid(Environment environment) throws Exception {
      Session session = environment.get(Session.class);

      if (databaseVersion.compareTo(JbpmVersion.V_4_2) < 0) {
        // find deployments without a langid property
        List<?> deploymentProperties = session.createCriteria(DeploymentProperty.class)
          .add(Restrictions.eq("key", DeploymentImpl.KEY_PROCESS_DEFINITION_ID))
          .list();

        for (DeploymentProperty deploymentProperty : CollectionUtil.checkList(deploymentProperties, DeploymentProperty.class)) {
          DeploymentImpl deployment = deploymentProperty.getDeployment();
          deployment.setProcessLanguageId(deploymentProperty.getObjectName(), "jpdl-4.0");
        }
      }

      PropertyImpl.setDbVersionToLibraryVersion(session);
    }
  }
}

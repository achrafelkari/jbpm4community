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
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.cfg.ConfigurationImpl;
import org.jbpm.pvm.internal.id.PropertyImpl;
import org.jbpm.pvm.internal.processengine.ProcessEngineImpl;
import org.jbpm.pvm.internal.repository.DeploymentImpl;
import org.jbpm.pvm.internal.repository.DeploymentProperty;

/**
 * @author Tom Baeyens
 * @author Alejandro Guizar
 */
public class Upgrade {

  private static final long serialVersionUID = 1L;
  
  private static Log log = Log.getLog(Upgrade.class.getName());
  
  static String database;
  static JbpmVersion jbpmVersion;
  static boolean propertiesTableExists;

  public static void main(String[] args) {
    if ( (args==null)
         || (args.length!=1)
       ) {
      DbHelper.printSyntax(Upgrade.class);
      return;
    }
    
    database = args[0];
    
    ProcessEngine processEngine = new ConfigurationImpl()
      .skipDbCheck()
      .buildProcessEngine();
  
    try {
      processEngine.execute(new Command<Object>(){
        private static final long serialVersionUID = 1L;
        public Object execute(Environment environment) throws Exception {
          Session session = environment.get(Session.class);
          propertiesTableExists = PropertyImpl.propertiesTableExists(session);
          return null;
        }
      });
      processEngine.execute(new Command<Object>(){
        private static final long serialVersionUID = 1L;
        public Object execute(Environment environment) throws Exception {
          Session session = environment.get(Session.class);
          if (!propertiesTableExists) {
            try {
              session.createSQLQuery("select CLASSNAME_ from JBPM4_VARIABLE").list();
              jbpmVersion = JbpmVersion.V_4_1;

            } catch (HibernateException e) {
              jbpmVersion = JbpmVersion.V_4_0;
            }
          } else {
            String dbVersion = PropertyImpl.getDbVersion(session);
            if (dbVersion == null) {
              throw new JbpmException("property table exists, but no db version property is present");
            }

            jbpmVersion = JbpmVersion.getJbpmVersion(dbVersion);
          }
          return null;
        }
      });

      JbpmVersion currentJbpmVersion = JbpmVersion.getJbpmVersion(ProcessEngineImpl.JBPM_LIBRARY_VERSION);
      if (jbpmVersion == currentJbpmVersion) {
        log.info("jBPM schema is already up to date");
        
      } else {
        processEngine.execute(new Command<Object>(){
          private static final long serialVersionUID = 1L;
          public Object execute(Environment environment) throws Exception {
            Session session = environment.get(Session.class);
            
            log.info("upgrading from "+jbpmVersion+" to "+ProcessEngineImpl.JBPM_LIBRARY_VERSION);
            
            if (jbpmVersion.isEarlier(JbpmVersion.V_4_1)) {
              DbHelper.executeSqlResource("upgrade-4.0-to-4.1/jbpm." + database + ".upgrade.sql", session);
            }

            if (jbpmVersion.isEarlier(JbpmVersion.V_4_2)) {
              // the first part of the upgrade to 4.2 might already be done before as that happens in the next transaction (and that next transaction might have failed in a previous run of upgrade)
              // in that case, the next part is skipped
              if (!propertiesTableExists) {
                DbHelper.executeSqlResource("upgrade-4.1-to-4.2/jbpm." + database + ".upgrade.sql", session);
                PropertyImpl.initializeNextDbid(session);
                // we set the version to 4.1 as the next transaction might fail
                PropertyImpl.setDbVersionTo41(session);
              }
            }
            // transaction is now committed as the next transaction requires the NextDbid property to be initialized and committed.
            return null;
          }
        });

        processEngine.execute(new Command<Object>(){
          private static final long serialVersionUID = 1L;
          public Object execute(Environment environment) throws Exception {
            Session session = environment.get(Session.class);
            
            if (jbpmVersion.isEarlier(JbpmVersion.V_4_2)) {
              // find deployments without a langid property
              List<DeploymentProperty> deploymentProperties = session.createCriteria(DeploymentProperty.class)
                  .add(Restrictions.eq("key", DeploymentImpl.KEY_PROCESS_DEFINITION_ID))
                  .list();
        
              for (DeploymentProperty deploymentProperty : deploymentProperties) {
                String objectName = deploymentProperty.getObjectName();
                DeploymentImpl deployment = deploymentProperty.getDeployment();
                deployment.setProcessLanguageId(objectName, "jpdl-4.0");
              }
            }
            
            PropertyImpl.setDbVersionToLibraryVersion(session);
            return null;
          }
        });
      }      

      log.info("jBPM DB upgrade completed successfully.");

    } catch (Exception e) {
      log.error("ERROR: jBPM DB upgrade FAILED", e);

    } finally {
      processEngine.close();
    }
  }
}

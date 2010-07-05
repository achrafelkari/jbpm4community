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

import org.hibernate.classic.Session;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.cfg.ConfigurationImpl;
import org.jbpm.pvm.internal.id.PropertyImpl;

/**
 * @author Alejandro Guizar
 */
public class Create {

  private static final long serialVersionUID = 1L;

  private static final Log log = Log.getLog(Create.class.getName());

  public static void main(String[] args) {
    if (args.length != 1) {
      DbHelper.printSyntax(Upgrade.class);
      return;
    }

    final String database = args[0];
    ProcessEngine processEngine = new ConfigurationImpl().skipDbCheck().buildProcessEngine();

    try {
      processEngine.execute(new Command<Void>() {
        private static final long serialVersionUID = 1L;

        public Void execute(Environment environment) throws Exception {
          Session session = environment.get(Session.class);
          DbHelper.executeSqlResource("create/jbpm." + database + ".create.sql", session);
          PropertyImpl.createProperties(session);
          return null;
        }
      });
      log.info("database schema created successfully");
    }
    catch (Exception e) {
      log.error("database schema creation failed", e);
    }
    finally {
      processEngine.close();
    }
  }
}

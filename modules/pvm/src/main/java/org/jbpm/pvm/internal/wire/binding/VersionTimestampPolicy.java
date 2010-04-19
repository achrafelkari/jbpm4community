/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jbpm.pvm.internal.wire.binding;

import java.io.Serializable;

import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.repository.DeploymentImpl;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class VersionTimestampPolicy implements SavePolicy, Serializable {

  private static final long serialVersionUID = 1L;

  private static Log log = Log.getLog(VersionTimestampPolicy.class.getName());
  
  private static final String DEPLOYER_TIMESTAMP =  "jbpmdeployer.deploymentArtifact.timestamp";
  private static final String DEPLOYER_LOCATION =   "jbpmdeployer.deploymentArtifact.location";

  public PolicyEvaluation evaluate(DeploymentImpl deployment)
  {
    log.debug("Apply " + this.getClass().getName());

    PolicyEvaluation evaluation = new PolicyEvaluation();
    
    /*
    TODO

    for (String fileName: deployment.getFileNamesForType("jpdl"))
    {
      Document doc = deployment.getDocument(fileName);
      if(null==doc)
        throw new IllegalArgumentException("Policy expects JPDL document");

      Element processEl = doc.getDocumentElement();
      String nameAttribute = XmlUtil.attribute(processEl, "name");
      String keyAttribute = XmlUtil.attribute(processEl, "key");
      String versionAttribute = XmlUtil.attribute(processEl, "version");

      // @see DeploymentAdaptor, it provides the timestamp
      long currentTimestamp = deployment.getTimestamp();
      String deloymentLocation = deployment.getName();

      // provide evaluation properties
      evaluation.getResults().put(DEPLOYER_TIMESTAMP, String.valueOf(currentTimestamp));
      evaluation.getResults().put(DEPLOYER_LOCATION, deloymentLocation);

      // can either be key or name given
      String identifier = nameAttribute!=null ? nameAttribute : keyAttribute;

      // query exisisting processes
      ProcessService processService = getEnvironment().get(ProcessService.class);
      ProcessDefinitionQuery query = processService.createProcessDefinitionQuery();
      if(nameAttribute!=null)
        query.nameLike(nameAttribute);
      else
        query.keyLike(keyAttribute);
      List<ProcessDefinition> deployedProcesses = query.execute();

      // apply either version or timestamp policy
      if(versionAttribute!=null)
      {
        log.info("Version driven policy evaluation: process '"+identifier+"', version " +versionAttribute);

        long processVersion = Long.valueOf(versionAttribute);
        for(ProcessDefinition pd : deployedProcesses)
        {
          if(processVersion == pd.getVersion())
          {
            // explicit version version match
            // the process will not be saved
            evaluation.setDoesApply(false);

            log.info("A process definition '"+identifier+"' with version "+versionAttribute+
                " does already exist. The process will not be deployed: " + deloymentLocation);
            break;
          }
        }
      }
      else
      {
        log.info("Timestamp driven policy evaluation: process '"+identifier+"', timestamp " +currentTimestamp);

        for(ProcessDefinition pd : deployedProcesses)
        {

          byte[] bytes = ((ClientProcessDefinition)pd).getAttachment(DEPLOYER_TIMESTAMP);
          if(null==bytes)
          {
            log.error("Failed to retrieve 'jbpmdeployer.deploymentArtifact.timestamp' from process attachments");
            continue;
          }

          String s = new String(bytes);
          long processTimestamp = Long.valueOf(s);

          if(currentTimestamp <= processTimestamp)
          {
            // the timestamp of a given deployment artifact
            // is less or equal to the already deployed process definition
            // this prevents redeployments upon AS boot
            evaluation.setDoesApply(false);

            log.info("The deployment artifact doesn't seem to have changed: '"+deloymentLocation+"'."
                +" Process "+identifier+" will not be deployed");
            break;
          }

        }
      }

    }
    */


    return evaluation;
  }

  private EnvironmentImpl getEnvironment()
  {
    EnvironmentImpl environment = EnvironmentImpl.getCurrent();
    if (environment==null)
      throw new RuntimeException("Failed to access current environment");
    return environment;

  }
}

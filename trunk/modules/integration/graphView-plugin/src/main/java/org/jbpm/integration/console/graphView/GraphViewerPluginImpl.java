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
package org.jbpm.integration.console.graphView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.naming.InitialContext;

import org.jboss.bpm.console.client.model.ActiveNodeInfo;
import org.jboss.bpm.console.client.model.DiagramInfo;
import org.jboss.bpm.console.client.model.DiagramNodeInfo;
import org.jboss.bpm.console.server.plugin.GraphViewerPlugin;
import org.jbpm.api.Configuration;
import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.RepositoryService;
import org.jbpm.api.model.ActivityCoordinates;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class GraphViewerPluginImpl implements GraphViewerPlugin {

  protected final static String WEB_CONTEXT = "/gwt-console-server/rs";

  protected ProcessEngine processEngine;
  protected String webServiceHost = null;
  protected String webServicePort = null;

  public GraphViewerPluginImpl() {
    initializeProcessEngine();
  }

  protected void initializeProcessEngine() {
    try {
      InitialContext ctx = new InitialContext();
      this.processEngine = (ProcessEngine) ctx.lookup("java:/ProcessEngine");
      
    } catch (Exception e) {
      // Fall back to default mechanism
      this.processEngine = Configuration.getProcessEngine();
    }
    
    this.webServiceHost = (String) processEngine.get("jbpm.console.server.host");
    this.webServicePort = (String) processEngine.get("jbpm.console.server.port");
    
    if ( (webServiceHost==null)
         || (webServicePort==null)
       ) {
      throw new JbpmException("make sure that strings 'jbpm.console.server.host' and 'jbpm.console.server.port' are properly configured in the process-engine-context of jbpm.cfg.xml");
    }
  }

  protected StringBuilder getBaseUrl() {
    StringBuilder spec = new StringBuilder();
    spec.append("http://");
    spec.append(webServiceHost);
    spec.append(":");
    spec.append(webServicePort);
    spec.append(WEB_CONTEXT);
    return spec;
  }

  public byte[] getProcessImage(String processId) {
    RepositoryService repositoryService = this.processEngine.getRepositoryService();
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processId).uniqueResult();

    String imgRes = processDefinition.getImageResourceName();
    InputStream in = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), imgRes);

    if (null == in)
      throw new RuntimeException("Failed to retrieve image resource: " + imgRes);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    final int BUF_SIZE = 1 << 8; // 1KiB buffer
    byte[] buffer = new byte[BUF_SIZE];
    int bytesRead = -1;
    try {
      while ((bytesRead = in.read(buffer)) > -1) {
        out.write(buffer, 0, bytesRead);
      }
      in.close();
    } catch (IOException e) {
      throw new RuntimeException("Failed to read image resource: " + imgRes, e);
    }

    byte[] imageBytes = out.toByteArray();

    return imageBytes;
  }

  public DiagramInfo getDiagramInfo(String processId) {
    throw new RuntimeException("Not implemented");
  }

  public List<ActiveNodeInfo> getActiveNodeInfo(String processInstanceId) {
    List<ActiveNodeInfo> results = new ArrayList<ActiveNodeInfo>();

    ProcessInstance pi = processEngine.getExecutionService().findProcessInstanceById(processInstanceId);
    Set<String> currentActivities = pi.findActiveActivityNames();

    RepositoryService repoService = this.processEngine.getRepositoryService();    
    for (String activityName : currentActivities) {   
      ActivityCoordinates coords = repoService.getActivityCoordinates(pi.getProcessDefinitionId(), activityName);
      results.add(new ActiveNodeInfo(coords.getWidth(), coords.getHeight(), 
                  new DiagramNodeInfo(activityName, coords.getX(), 
                 coords.getY(), coords.getWidth(), coords.getHeight())));
    }

    return results;
  }

  public URL getDiagramURL(String id) {
    URL result = null;

    // check resource availability
    boolean hasImageResource = false;
    RepositoryService repositoryService = this.processEngine.getRepositoryService();
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).uniqueResult();

    InputStream inputStream = null;
    if (processDefinition != null) {
      String imgRes = processDefinition.getImageResourceName();
      inputStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), imgRes);
    }

    if (inputStream != null) {
      hasImageResource = true;
      try {
        inputStream.close();
      } catch (IOException e) {
        throw new RuntimeException("Failed to close stream", e);
      }
    }

    if (hasImageResource) {
      StringBuilder sb = getBaseUrl().append("/process/definition/");
      sb.append(id);
      sb.append("/image");

      try {
        result = new URL(sb.toString());
      } catch (MalformedURLException e) {
        throw new RuntimeException("Failed to create url", e);
      }
    }

    return result;
  }
  
  public List<ActiveNodeInfo> getNodeInfoForActivities(String processDefinitionId, List<String> activities) {
    List<ActiveNodeInfo> results = new ArrayList<ActiveNodeInfo>();

    RepositoryService repoService = this.processEngine.getRepositoryService(); 
    
    for (String activityName : activities) {   
      ActivityCoordinates coords = repoService.getActivityCoordinates(processDefinitionId, activityName);
      results.add(new ActiveNodeInfo(coords.getWidth(), coords.getHeight(), 
                  new DiagramNodeInfo(activityName, coords.getX(), 
                 coords.getY(), coords.getWidth(), coords.getHeight())));
    }

    return results;
  }
}

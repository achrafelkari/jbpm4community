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
package org.jbpm.integration.console.forms;

import org.jboss.bpm.console.server.plugin.FormAuthorityRef;
import org.jboss.bpm.console.server.plugin.FormDispatcherPlugin;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.RepositoryService;

import javax.activation.DataHandler;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Processes form data to start processes.
 * 
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class ProcessFormDispatcher extends AbstractFormDispatcher implements FormDispatcherPlugin {

  public URL getDispatchUrl(FormAuthorityRef ref) {
    if (!processHasForm(ref.getReferenceId())) {
      return null;
    }

    StringBuilder baseUrl = getBaseUrl();
    baseUrl.append("/form/process/");
    baseUrl.append(ref.getReferenceId());
    baseUrl.append("/render");

    try {
      return new URL(baseUrl.toString());
    } catch (MalformedURLException e) {
      throw new RuntimeException("Failed to resolve task dispatch url", e);
    }
  }

  private boolean processHasForm(String id) {
    return getStartFormName(id) != null;
  }

  private String getStartFormName(String procDefId) {
    RepositoryService repoService = processEngine.getRepositoryService();
    List<String> startActivityNames = repoService.getStartActivityNames(procDefId);

    if (null == startActivityNames) 
      throw new RuntimeException("Unable to resolve start activity names for process: " + procDefId);

    String defaultActitvity = startActivityNames.get(0);
    if (startActivityNames.size() > 1) {
      System.out.println("WARN: More then 1 start activity found. Default to " + defaultActitvity + " to resolve the form name.");
    }    
    
    return repoService.getStartFormResourceName(procDefId, defaultActitvity);
  }

  public DataHandler provideForm(FormAuthorityRef ref) {
    DataHandler result = null;

    RepositoryService repoService = processEngine.getRepositoryService();

    // check if a template exists
    String startFormResourceName = getStartFormName(ref.getReferenceId());
    if (null == startFormResourceName)
      throw new IllegalArgumentException("Process " + ref.getReferenceId() + " doesn't provide a start form");

    ProcessDefinition procDef = repoService.createProcessDefinitionQuery().processDefinitionId(ref.getReferenceId()).uniqueResult();
    InputStream template = repoService.getResourceAsStream(procDef.getDeploymentId(), startFormResourceName);

    // merge template with process variables
    if (template != null) {
      // plugin context
      StringBuilder action = getBaseUrl();
      action.append("/form/process/");
      action.append(ref.getReferenceId());
      action.append("/complete");

      Map<String, Object> renderContext = new HashMap<String, Object>();

      // form directive
      FormDirective formDirective = new FormDirective();
      formDirective.setAction(action.toString());
      renderContext.put(FORM_DIRECTIVE_KEY, formDirective);

      // outcome directive
      renderContext.put(OUTCOME_DIRECTIVE_NAME, new OutcomeDirective());

      // global css
      InputStream css = loadCSS(procDef.getDeploymentId());
      if(css!=null)
        renderContext.put("CSS", streamToString(css));
      
      result = processTemplate(startFormResourceName, template, renderContext);
    }

    return result;
  }

  private InputStream loadCSS(String deploymentId)
  {
    RepositoryService repoService = processEngine.getRepositoryService();

    InputStream in = repoService.getResourceAsStream(
        deploymentId, PROCESSFORMS_CSS
    );
    return in;
  }
}

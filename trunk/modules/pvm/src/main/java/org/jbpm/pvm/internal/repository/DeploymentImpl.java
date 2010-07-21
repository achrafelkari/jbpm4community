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
package org.jbpm.pvm.internal.repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jbpm.api.Deployment;
import org.jbpm.api.NewDeployment;
import org.jbpm.api.JbpmException;
import org.jbpm.pvm.internal.cmd.CommandService;
import org.jbpm.pvm.internal.cmd.DeployCmd;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.id.DbidGenerator;
import org.jbpm.pvm.internal.lob.Lob;
import org.jbpm.pvm.internal.stream.ByteArrayStreamInput;
import org.jbpm.pvm.internal.stream.FileStreamInput;
import org.jbpm.pvm.internal.stream.InputStreamInput;
import org.jbpm.pvm.internal.stream.ResourceStreamInput;
import org.jbpm.pvm.internal.stream.StreamInput;
import org.jbpm.pvm.internal.stream.StringStreamInput;
import org.jbpm.pvm.internal.stream.UrlStreamInput;
import org.jbpm.pvm.internal.util.IoUtil;
import org.jbpm.pvm.internal.xml.ProblemList;

/**
 * @author Tom Baeyens
 */
public class DeploymentImpl extends ProblemList implements NewDeployment {

  private static final long serialVersionUID = 1L;

  public static final String KEY_PROCESS_LANGUAGE_ID = "langid";
  public static final String KEY_PROCESS_DEFINITION_ID = "pdid";
  public static final String KEY_PROCESS_DEFINITION_KEY = "pdkey";
  public static final String KEY_PROCESS_DEFINITION_VERSION = "pdversion";

  protected long dbid;
  protected String name;
  protected long timestamp;
  protected String state = Deployment.STATE_ACTIVE;
  protected Map<String, Lob> resources;

  protected CommandService commandService;
  protected Map<String, Object> objects;
  protected Set<DeploymentProperty> objectProperties;

  public DeploymentImpl() {
  }

  public DeploymentImpl(CommandService commandService) {
    this.commandService = commandService;
  }

  @Override
  public String toString() {
    return "deployment(" + dbid + ")";
  }

  public String deploy() {
    return commandService.execute(new DeployCmd(this));
  }

  public NewDeployment addResourceFromClasspath(String resourceName) {
    addResourceFromStreamInput(resourceName, new ResourceStreamInput(resourceName));
    return this;
  }

  public NewDeployment addResourceFromString(String resourceName, String text) {
    addResourceFromStreamInput(resourceName, new StringStreamInput(text));
    return this;
  }

  public NewDeployment addResourcesFromZipInputStream(ZipInputStream zipInputStream) {
    try {
      ZipEntry zipEntry = zipInputStream.getNextEntry();
      while (zipEntry != null) {
        String entryName = zipEntry.getName();
        byte[] bytes = IoUtil.readBytes(zipInputStream);
        if (bytes != null) {
          addResourceFromStreamInput(entryName, new ByteArrayStreamInput(bytes));
        }
        zipEntry = zipInputStream.getNextEntry();
      }
    }
    catch (IOException e) {
      throw new JbpmException("couldn't read zip archive", e);
    }
    finally {
      IoUtil.close(zipInputStream);
    }
    return this;
  }

  public NewDeployment addResourceFromInputStream(String resourceName, InputStream inputStream) {
    addResourceFromStreamInput(resourceName, new InputStreamInput(inputStream));
    return this;
  }

  public NewDeployment addResourceFromUrl(URL url) {
    addResourceFromStreamInput(url.toString(), new UrlStreamInput(url));
    return this;
  }

  public NewDeployment addResourceFromFile(File file) {
    addResourceFromStreamInput(file.getPath(), new FileStreamInput(file));
    return this;
  }

  public NewDeployment addResourceFromStreamInput(String name, StreamInput streamInput) {
    if (resources == null) {
      resources = new HashMap<String, Lob>();
    }
    InputStream inputStream = streamInput.openStream();
    try {
      byte[] bytes = IoUtil.readBytes(inputStream);

      // Since this method is probably called outside an environment block, we
      // need to generate the dbid of the Lob later (during the actual deployment).
      Lob lob = new Lob(bytes, false);
      resources.put(name, lob);
    }
    catch (IOException e) {
      throw new JbpmException("couldn't read from " + name, e);
    }
    finally {
      IoUtil.close(inputStream);
    }

    return this;
  }

  public Set<String> getResourceNames() {
    if (resources == null) {
      return Collections.emptySet();
    }
    return resources.keySet();
  }

  /**
   * This method should be called before saving the deployment. It will assign a generated dbid
   * to the resource Lobs.
   * 
   * Note: when using a database, this method must be called within an environment block!
   */
  public void initResourceLobDbids() {
    if (resources != null) {
      for (Lob resource : resources.values()) {
        long resourceDbid = DbidGenerator.getDbidGenerator().getNextId();
        resource.setDbid(resourceDbid);
      }
    }
  }

  public byte[] getBytes(String resourceName) {
    if (resources != null) {
      Lob lob = resources.get(resourceName);
      if (lob != null) {
        return lob.extractBytes();
      }
    }
    return null;
  }

  public void addObject(String objectName, Object object) {
    if (objects == null) {
      objects = new HashMap<String, Object>();
    }
    objects.put(objectName, object);
  }

  // object properties ////////////////////////////////////////////////////////

  public void setProcessDefinitionId(String processDefinitionName, String processDefinitionId) {
    setObjectProperty(processDefinitionName, KEY_PROCESS_DEFINITION_ID, processDefinitionId);
  }

  public String getProcessDefinitionId(String processDefinitionName) {
    return (String) getObjectProperty(processDefinitionName, KEY_PROCESS_DEFINITION_ID);
  }

  public void setProcessDefinitionKey(String processDefinitionName, String processDefinitionKey) {
    setObjectProperty(processDefinitionName, KEY_PROCESS_DEFINITION_KEY, processDefinitionKey);
  }

  public String getProcessDefinitionKey(String processDefinitionName) {
    return (String) getObjectProperty(processDefinitionName, KEY_PROCESS_DEFINITION_KEY);
  }

  public void setProcessDefinitionVersion(String processDefinitionName,
    Long processDefinitionVersion) {
    setObjectProperty(processDefinitionName, KEY_PROCESS_DEFINITION_VERSION, processDefinitionVersion);
  }

  public Long getProcessDefinitionVersion(String processDefinitionName) {
    return (Long) getObjectProperty(processDefinitionName, KEY_PROCESS_DEFINITION_VERSION);
  }

  public String getProcessLanguageId(String processDefinitionName) {
    return (String) getObjectProperty(processDefinitionName, KEY_PROCESS_LANGUAGE_ID);
  }

  public void setProcessLanguageId(String processDefinitionName, String processLanguageId) {
    setObjectProperty(processDefinitionName, KEY_PROCESS_LANGUAGE_ID, processLanguageId);
  }

  public void setObjectProperty(String objectName, String key, Object value) {
    if (objectProperties == null) {
      objectProperties = new HashSet<DeploymentProperty>();
    }
    DeploymentProperty deploymentProperty = new DeploymentProperty(this, objectName, key);
    deploymentProperty.setValue(value);
    objectProperties.add(deploymentProperty);
  }

  public Object removeObjectProperty(String objectName, String key) {
    if (objectProperties != null) {
      for (DeploymentProperty deploymentProperty : objectProperties) {
        if (deploymentProperty.getObjectName().equals(objectName)
          && deploymentProperty.getKey().equals(key)) {
          Object value = deploymentProperty.getValue();
          objectProperties.remove(deploymentProperty);
          return value;
        }
      }
    }
    return null;
  }

  public Object getObjectProperty(String objectName, String key) {
    if (objectProperties != null) {
      for (DeploymentProperty deploymentProperty : objectProperties) {
        if (deploymentProperty.getObjectName().equals(objectName)
          && deploymentProperty.getKey().equals(key)) {
          return deploymentProperty.getValue();
        }
      }
    }
    return null;
  }

  public Set<String> getProcessDefinitionIds() {
    Set<String> processDefinitionIds = new HashSet<String>();
    if (objectProperties != null) {
      for (DeploymentProperty deploymentProperty : objectProperties) {
        if (KEY_PROCESS_DEFINITION_ID.equals(deploymentProperty.getKey())) {
          String processDefinitionId = deploymentProperty.getStringValue();
          processDefinitionIds.add(processDefinitionId);
        }
      }
    }
    return processDefinitionIds;
  }

  public boolean hasObjectProperties(String objectName) {
    if (objectProperties != null) {
      for (DeploymentProperty deploymentProperty : objectProperties) {
        if (deploymentProperty.getObjectName().equals(objectName)
          && KEY_PROCESS_DEFINITION_ID.equals(deploymentProperty.getKey())) {
          return true;
        }
      }
    }
    return false;
  }

  public void suspend() {
    if (isSuspended()) {
      throw new JbpmException("deployment is already suspended");
    }

    state = Deployment.STATE_SUSPENDED;

    RepositorySessionImpl repositorySession = EnvironmentImpl
      .getFromCurrent(RepositorySessionImpl.class);
    repositorySession.cascadeDeploymentSuspend(this);

    RepositoryCache repositoryCache = EnvironmentImpl.getFromCurrent(RepositoryCache.class);
    repositoryCache.remove(Long.toString(dbid));
  }

  public void resume() {
    if (!isSuspended()) {
      throw new JbpmException("deployment is not suspended");
    }

    state = Deployment.STATE_ACTIVE;

    RepositorySessionImpl repositorySession = EnvironmentImpl
      .getFromCurrent(RepositorySessionImpl.class);
    repositorySession.cascadeDeploymentResume(this);

    RepositoryCache repositoryCache = EnvironmentImpl.getFromCurrent(RepositoryCache.class);
    repositoryCache.remove(Long.toString(dbid));
  }

  public boolean isSuspended() {
    return Deployment.STATE_SUSPENDED.equals(state);
  }

  protected Object writeReplace() throws ObjectStreamException {
    commandService = null;
    return this;
  }

  // customized getters and setters ///////////////////////////////////////////

  public String getId() {
    return Long.toString(dbid);
  }

  // getters and setters //////////////////////////////////////////////////////

  public long getDbid() {
    return dbid;
  }
  public Map<String, Object> getObjects() {
    return objects;
  }
  public Set<DeploymentProperty> getObjectProperties() {
    return objectProperties;
  }
  public String getName() {
    return name;
  }
  public DeploymentImpl setName(String name) {
    this.name = name;
    return this;
  }
  public long getTimestamp() {
    return timestamp;
  }
  public DeploymentImpl setTimestamp(long timestamp) {
    this.timestamp = timestamp;
    return this;
  }
  public String getState() {
    return state;
  }
  public void setDbid(long dbid) {
    this.dbid = dbid;
  }
}

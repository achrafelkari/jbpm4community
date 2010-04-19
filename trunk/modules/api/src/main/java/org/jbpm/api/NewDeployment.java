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
package org.jbpm.api;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipInputStream;

/** extends a {@link Deployment} with method for creating a new 
 * deployment.
 * 
 * @see RepositoryService#createDeployment()
 * 
 * @author Tom Baeyens
 */
public interface NewDeployment extends Deployment {

  /** typically correspond to the file name or url or some other 
   * form of identifying the source archive file for this deployment. */
  NewDeployment setName(String name);
  
  /** the timestamp can optionally be given and represents the last updated timestamp 
   * of the archive file that is being deployed. 
   * JBoss deployer makes use of this to remember if a file is already deployed or not.*/
  NewDeployment setTimestamp(long timestamp);
  
  /** adds a resource as a string */
  NewDeployment addResourceFromString(String resourceName, String string);

  /** adds a resource as a string */
  NewDeployment addResourceFromInputStream(String resourceName, InputStream inputStream);
  
  /** adds a resource as a resource from the classpath */
  NewDeployment addResourceFromClasspath(String resourceName);
  
  /** adds a resource as a url */
  NewDeployment addResourceFromUrl(URL url);
  
  /** adds a resource as a zip stream */
  NewDeployment addResourcesFromZipInputStream(ZipInputStream zipInputStream);

  /** adds a resource from a file */
  NewDeployment addResourceFromFile(File file);
  
  /** after adding resources, this will perform the actual deployment.
   * @return the generated deploymentId identification for this deployment
   * in the {@link RepositoryService repository}. */
  String deploy();

}

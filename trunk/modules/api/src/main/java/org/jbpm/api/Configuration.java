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

import org.xml.sax.InputSource;

/**
 * process engine configuration.
 *
 * @author Tom Baeyens
 */
public class Configuration {

  /** singletone instance */
  private static ProcessEngine singleton;

  transient Configuration impl;

  /** default constructor */
  public Configuration() {
    impl = instantiate("org.jbpm.pvm.internal.cfg.ConfigurationImpl");
  }

  // constructor for ConfigurationImpl to prevent construction loop
  protected Configuration(Object o) {
  }

  protected Configuration instantiate(String className) {
    Configuration implementation;
    try {
      Class<?> implementationClass = null;
      try {
        implementationClass = Class.forName(className, true, getClassLoader());
      } catch(ClassNotFoundException ex) {
        implementationClass = Class.forName(className);
      }
      implementation = (Configuration) implementationClass.newInstance();
    } catch (Exception e) {
      throw new JbpmException("couldn't instantiate configuration of type "+className, e);
    }
    return implementation;
  }

  protected ClassLoader getClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }

  /** provide an xml string as the configuration resource */
  public Configuration setXmlString(String xmlString) {
    impl.setXmlString(xmlString);
    return impl;
  }

  /** provide an classpath resource as the configuration file */
  public Configuration setResource(String resource) {
    impl.setResource(resource);
    return impl;
  }

  /** provide an input stream as the configuration resource */
  public Configuration setInputStream(InputStream inputStream) {
    impl.setInputStream(inputStream);
    return impl;
  }

  /** provide a sax input source as the configuration resource */
  public Configuration setInputSource(InputSource inputSource) {
    impl.setInputSource(inputSource);
    return impl;
  }

  /** point with a url to the configuration file */
  public Configuration setUrl(URL url) {
    impl.setUrl(url);
    return impl;
  }

  /** provide a File as the configuration file */
  public Configuration setFile(File file) {
    impl.setFile(file);
    return impl;
  }

  /** after specifying the configuration resources with the other methods, a
   * process engine can be created. */
  public ProcessEngine buildProcessEngine() {
    return impl.buildProcessEngine();
  }

  /** provides the hibernate session factory programmatically.
   * The hibernateSessionFactory parameter is of type Object to
   * prevent a dependency of the API on hibernate directly.*/
  public Configuration setHibernateSessionFactory(Object hibernateSessionFactory){
    return impl.setHibernateSessionFactory(hibernateSessionFactory);
  }

  /** get the singleton ProcessEngine that is created from the default
   * configuration file 'jbpm.cfg.xml'. */
  public static ProcessEngine getProcessEngine() {
    if (singleton == null) {
      synchronized (Configuration.class) {
        if (singleton == null) {
          singleton = new Configuration().setResource("jbpm.cfg.xml").buildProcessEngine();
        }
      }
    }
    return Configuration.singleton;
  }
}

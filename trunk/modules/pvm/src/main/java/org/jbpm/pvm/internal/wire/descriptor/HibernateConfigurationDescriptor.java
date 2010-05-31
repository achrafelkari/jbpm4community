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
package org.jbpm.pvm.internal.wire.descriptor;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.hibernate.cfg.Configuration;
import org.jbpm.api.JbpmException;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.util.ReflectUtil;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireDefinition;
import org.jbpm.pvm.internal.wire.WireException;
import org.jbpm.pvm.internal.wire.operation.Operation;

/**
 * @author Tom Baeyens
 */
public class HibernateConfigurationDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;

  private static final Log log = Log.getLog(HibernateConfigurationDescriptor.class.getName());

  String className;
  String namingStrategyClassName;
  List<Operation> cfgOperations = new ArrayList<Operation>();
  List<Operation> cfgCacheOperations = new ArrayList<Operation>();
  PropertiesDescriptor propertiesDescriptor = null;

  public Object construct(WireContext wireContext) {
    // instantiation of the configuration
    Configuration configuration = null;
    if (className!=null) {
      try {
        log.trace("instantiating hibernate configuration class "+className);
        Class<?> configurationClass = ReflectUtil.classForName(className);
        configuration = (Configuration) ReflectUtil.newInstance(configurationClass);
      } catch (Exception e) {
        throw new JbpmException("couldn't instantiate hibernate configuration class "+className, e);
      }
    } else {
      log.trace("instantiating default hibernate configuration");
      configuration = new Configuration();
    }
    return configuration;
  }

  public void initialize(Object object, WireContext wireContext) {
    Configuration configuration = (Configuration) object;

    apply(cfgOperations, configuration, wireContext);
    apply(cfgCacheOperations, configuration, wireContext);

    if (propertiesDescriptor!=null) {
      Properties properties = (Properties) wireContext.create(propertiesDescriptor, false);
      if (log.isDebugEnabled()) log.debug("adding properties to hibernate configuration: "+properties);
      configuration.addProperties(properties);
    }
  }

  private void apply(List<Operation> operations, Configuration configuration, WireContext wireContext) {
    if (operations!=null) {
      for (Operation operation: operations) {
        log.trace(operation.toString());
        operation.apply(configuration, wireContext);
      }
    }
  }

  public Class<?> getType(WireDefinition wireDefinition) {
    if (className!=null) {
      try {
        return ReflectUtil.classForName(className);
      } catch (Exception e) {
        throw new WireException("couldn't create hibernate configuration '"+className+"': "+e.getMessage(), e.getCause());
      }
    }
    return Configuration.class;
  }

  public void addCfgResource(String cfgResource) {
    cfgOperations.add(new AddCfgResource(cfgResource));
  }

  public void addCfgFile(String cfgFile) {
    cfgOperations.add(new AddCfgFile(cfgFile));
  }

  public void addCfgUrl(String cfgUrl) {
    cfgOperations.add(new AddCfgUrl(cfgUrl));
  }

  public void addMappingResource(String mappingResource) {
    cfgOperations.add(new AddMappingResource(mappingResource));
  }

  public void addMappingFile(String mappingFileName) {
    cfgOperations.add(new AddMappingFile(mappingFileName));
  }

  public void addMappingClass(String mappingClassName) {
    cfgOperations.add(new AddMappingClass(mappingClassName));
  }

  public void addMappingUrl(String mappingUrl) {
    cfgOperations.add(new AddMappingUrl(mappingUrl));
  }

  public void addClassToCache(String className, String concurrencyStrategy) {
    cfgCacheOperations.add(new SetCacheConcurrencyStrategy(className, concurrencyStrategy));
  }

  public void addCollectionToCache(String collectionName, String concurrencyStrategy) {
    cfgCacheOperations.add(new SetCollectionCacheConcurrencyStrategy(collectionName, concurrencyStrategy));
  }

  public static class AddCfgResource implements Operation {
    private static final long serialVersionUID = 1L;
    String cfgResource;
    public AddCfgResource(String cfgResource) {
      this.cfgResource = cfgResource;
    }
    public void apply(Object target, WireContext wireContext) {
      Configuration configuration = (Configuration) target;
      configuration.configure(cfgResource);
    }
    public String toString() {
      return "adding hibernate cfg resource "+cfgResource;
    }
  }

  public static class AddCfgFile implements Operation {
    private static final long serialVersionUID = 1L;
    String cfgFileName;
    public AddCfgFile(String cfgFileName) {
      this.cfgFileName = cfgFileName;
    }
    public void apply(Object target, WireContext wireContext) {
      Configuration configuration = (Configuration) target;
      File file = new File(cfgFileName);
      configuration.configure(file);
    }
    public String toString() {
      return "adding hibernate cfg file "+cfgFileName;
    }
  }

  public static class AddCfgUrl implements Operation {
    private static final long serialVersionUID = 1L;
    String cfgUrl;
    public AddCfgUrl(String cfgUrl) {
      this.cfgUrl = cfgUrl;
    }
    public void apply(Object target, WireContext wireContext) {
      Configuration configuration = (Configuration) target;
      try {
        URL url = new URL(cfgUrl);
        configuration.configure(url);
      } catch (Exception e) {
        throw new JbpmException("couldn't configure hibernate with url "+cfgUrl, e);
      }
    }
    public String toString() {
      return "adding hibernate cfg url "+cfgUrl;
    }
  }

  public static class AddMappingResource implements Operation {
    private static final long serialVersionUID = 1L;
    String resource;
    public AddMappingResource(String resource) {
      this.resource = resource;
    }
    public void apply(Object target, WireContext wireContext) {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      Configuration configuration = (Configuration) target;
      configuration.addResource(resource, classLoader);
    }
    public String toString() {
      return "adding mapping resource "+resource+" to hibernate configuration";
    }
  }

  public static class AddMappingFile implements Operation {
    private static final long serialVersionUID = 1L;
    String fileName;
    public AddMappingFile(String fileName) {
      this.fileName = fileName;
    }
    public void apply(Object target, WireContext wireContext) {
      Configuration configuration = (Configuration) target;
      configuration.addFile(fileName);
    }
    public String toString() {
      return "adding hibernate mapping file "+fileName+" to configuration";
    }
  }

  public static class AddMappingClass implements Operation {
    private static final long serialVersionUID = 1L;
    String className;
    public AddMappingClass(String className) {
      this.className = className;
    }
    public void apply(Object target, WireContext wireContext) {
      Configuration configuration = (Configuration) target;
      try {
        Class<?> persistentClass = ReflectUtil.classForName(className);
        configuration.addClass(persistentClass);
      } catch (Exception e) {
        throw new JbpmException("couldn't add mapping for class "+className, e);
      }
    }
    public String toString() {
      return "adding persistent class "+className+" to hibernate configuration";
    }
  }

  public static class AddMappingUrl implements Operation {
    private static final long serialVersionUID = 1L;
    String url;
    public AddMappingUrl(String url) {
      this.url = url;
    }
    public void apply(Object target, WireContext wireContext) {
      Configuration configuration = (Configuration) target;
      try {
        configuration.addURL(new URL(url));
      } catch (Exception e) {
        throw new JbpmException("couldn't add hibernate mapping from URL "+url, e);
      }
    }
  }

  public static class SetCacheConcurrencyStrategy implements Operation {
    private static final long serialVersionUID = 1L;
    String className;
    String concurrencyStrategy;
    public SetCacheConcurrencyStrategy(String className, String concurrencyStrategy) {
      this.className = className;
      this.concurrencyStrategy = concurrencyStrategy;
    }
    public void apply(Object target, WireContext wireContext) {
      Configuration configuration = (Configuration) target;
      configuration.setCacheConcurrencyStrategy(className, concurrencyStrategy);
    }
    public String toString() {
      return "setting cache concurrency strategy on class "+className+" to "+concurrencyStrategy+" on hibernate configuration";
    }
  }

  public static class SetCollectionCacheConcurrencyStrategy implements Operation {
    private static final long serialVersionUID = 1L;
    String collection;
    String concurrencyStrategy;
    public SetCollectionCacheConcurrencyStrategy(String collection, String concurrencyStrategy) {
      this.collection = collection;
      this.concurrencyStrategy = concurrencyStrategy;
    }
    public void apply(Object target, WireContext wireContext) {
      Configuration configuration = (Configuration) target;
      configuration.setCollectionCacheConcurrencyStrategy(collection, concurrencyStrategy);
    }
    public String toString() {
      return "setting cache concurrency strategy on collection "+collection+" to "+concurrencyStrategy+" on hibernate configuration";
    }
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getClassName() {
    return className;
  }
  public void setClassName(String className) {
    this.className = className;
  }
  public PropertiesDescriptor getPropertiesDescriptor() {
    return propertiesDescriptor;
  }
  public void setPropertiesDescriptor(PropertiesDescriptor propertiesDescriptor) {
    this.propertiesDescriptor = propertiesDescriptor;
  }
  public String getNamingStrategyClassName() {
    return namingStrategyClassName;
  }
  public void setNamingStrategyClassName(String namingStrategyClassName) {
    this.namingStrategyClassName = namingStrategyClassName;
  }
}

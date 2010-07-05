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
package org.jbpm.pvm.internal.wire.binding;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.hibernate.cfg.Configuration;
import org.w3c.dom.Element;

import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.stream.FileStreamInput;
import org.jbpm.pvm.internal.stream.ResourceStreamInput;
import org.jbpm.pvm.internal.stream.StreamInput;
import org.jbpm.pvm.internal.stream.UrlStreamInput;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.descriptor.HibernateConfigurationDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.PropertiesDescriptor;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;

/** parses a descriptor for creating a hibernate Configuration.
 * 
 * See schema docs for more details.
 * 
 * @author Tom Baeyens
 */
public class HibernateConfigurationBinding extends WireDescriptorBinding {

  private static final Log log = Log.getLog(HibernateConfigurationBinding.class.getName());
  
  public HibernateConfigurationBinding() {
    super("hibernate-configuration");
  }

  protected HibernateConfigurationBinding(String tagName) {
    super(tagName);
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    HibernateConfigurationDescriptor descriptor = new HibernateConfigurationDescriptor();
    
    String configurationClassName = null;
    if ( element.hasAttribute("annotations")
         && element.getAttribute("annotations").equalsIgnoreCase("enabled")   
       ) {
      // don't replace the string with the reflective classname as that will 
      // introduce a hard dependency on the hibernate annotations library
      configurationClassName = "org.hibernate.cfg.AnnotationConfiguration";

    } else {
      configurationClassName = Configuration.class.getName();
    }

    descriptor.setClassName(configurationClassName);

    parseConfiguration(element, parse, descriptor, parser);
    
    return descriptor;
  }

  private void parseConfiguration(Element element, Parse parse, HibernateConfigurationDescriptor descriptor, Parser parser)
  {
    List<Element> configElements = XmlUtil.elements(element);
    for (Element configElement: configElements) {

      if ("cfg".equals(configElement.getLocalName())) {
        if (configElement.hasAttribute("resource")) {
          String resource = configElement.getAttribute("resource");
          log.trace("adding hibernate configuration resource "+resource);
          descriptor.addCfgResource(resource);

        } else if (configElement.hasAttribute("file")) {
          String fileName = configElement.getAttribute("file");
          log.trace("adding hibernate configuration file "+fileName);
          descriptor.addCfgFile(fileName);

        } else if (configElement.hasAttribute("url")) {
          String urlText = configElement.getAttribute("url");
          log.trace("adding hibernate configuration url "+urlText);
          descriptor.addCfgUrl(urlText);
          
        } else {
          parse.addProblem("exactly 1 attribute in {resource, file, url} was expected in cfg: "+XmlUtil.toString(configElement), element);
        }

      } else if ("mapping".equals(configElement.getLocalName())) {
        if (configElement.hasAttribute("resource")) {
          String resource = configElement.getAttribute("resource");
          log.trace("adding hibernate mapping resource "+resource);
          descriptor.addMappingResource(resource);

        } else if (configElement.hasAttribute("file")) {
          String fileName = configElement.getAttribute("file");
          log.trace("adding hibernate mapping file "+fileName);
          descriptor.addMappingFile(fileName);

        } else if (configElement.hasAttribute("class")) {
          String className = configElement.getAttribute("class");
          log.trace("adding hibernate mapping class "+className);
          descriptor.addMappingClass(className);
          
        } else if (configElement.hasAttribute("url")) {
          String urlText = configElement.getAttribute("url");
          log.trace("adding hibernate mapping url "+urlText);
          descriptor.addMappingUrl(urlText);
          
        } else {
          parse.addProblem("exactly 1 attribute in {resource, file, class, url} was expected in mapping: "+XmlUtil.toString(element));
        }

      } else if ("properties".equals(configElement.getLocalName())) {
        PropertiesDescriptor propertiesDescriptor = PropertiesBinding.parseDescriptor(configElement, parse, parser);
        descriptor.setPropertiesDescriptor(propertiesDescriptor);

      } else if ("cache-configuration".equals(configElement.getLocalName())) {
        StreamInput streamSource = null;

        String cacheUsage = configElement.getAttribute("usage");
        if (! ( ("read-only".equals(cacheUsage))
                || ("nonstrict-read-write".equals(cacheUsage))
                || ("read-write".equals(cacheUsage))
                || ("transactional".equals(cacheUsage))
              )
           ){
          parse.addProblem("problem in cache-configuration: no usage attribute or illegal value: "+cacheUsage+" Possible values are {read-only, nonstrict-read-write, read-write, transactional}");
        } else {

          if (configElement.hasAttribute("file")) {
            String fileName = configElement.getAttribute("file");
            File file = new File(fileName);
            if (file.exists() && file.isFile()) {
              streamSource = new FileStreamInput(file);
            } else {
              parse.addProblem("file "+fileName+" isn't a file");
            }
          }

          if (configElement.hasAttribute("resource")) {
            String resource = configElement.getAttribute("resource");
            
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            streamSource = new ResourceStreamInput(resource, classLoader);
          }

          if (configElement.hasAttribute("url")) {
            String urlText = configElement.getAttribute("url");
            try {
              URL url = new URL(urlText);
              streamSource = new UrlStreamInput(url);
            } catch (Exception e) {
              parse.addProblem("couldn't open url "+urlText, e);
            }
          }

          if (streamSource != null) {
            parser.importStream(streamSource, configElement, parse);
          }

          // parse the cache configurations in the same way as the hibernate cfg schema
          // translate the contents of the file into invoke operations for methods
          // Configuration.setCacheConcurrencyStrategy(String clazz, String concurrencyStrategy, String region)
          // Configuration.setCollectionCacheConcurrencyStrategy(String collectionRole, String concurrencyStrategy)
          //         <class-cache class="org.hibernate.auction.Item" usage="read-write"/>
          //         <class-cache class="org.hibernate.auction.Bid" usage="read-only"/>
          //         <collection-cache collection="org.hibernate.auction.Item.bids" usage="read-write"/>
          List<Element> cacheElements = XmlUtil.elements(configElement);
          if (cacheElements!=null) {
            for (Element cacheElement : cacheElements) {

              if ("class-cache".equals(cacheElement.getLocalName())) {
                String className = cacheElement.getAttribute("class");
                descriptor.addClassToCache(className, cacheUsage);

              } else if ("collection-cache".equals(cacheElement.getLocalName())) {
                String collection = cacheElement.getAttribute("collection");
                descriptor.addCollectionToCache(collection, cacheUsage);

              } else {
                parse.addProblem("unknown hibernate cache configuration element "+XmlUtil.toString(configElement));
              }
            }
          }
        }

      } else {
        parse.addProblem("unknown hibernate configuration element "+XmlUtil.toString(configElement));
      }
    }
  }

}

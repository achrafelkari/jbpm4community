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

import java.io.*;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.naming.InitialContext;

import org.jbpm.api.*;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

/**
 * Base class for freemarker based form dispatcher implementations that should
 * run on JBoss. Uses {@link org.jbpm.integration.spi.mgmt.ServerConfig} to
 * resolve the HTTP host and port.
 * 
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class AbstractFormDispatcher {

  // private static final Log log =
  // LogFactory.getLog(AbstractFormDispatcher.class);

  public static final String PROCESSFORMS_CSS = "process_forms.css";
  
  protected final static String WEB_CONTEXT = "/gwt-console-server/rs";

  protected ProcessEngine processEngine;
  protected String webServiceHost = null;
  protected String webServicePort = null;

  protected static final String FORM_DIRECTIVE_KEY = "form";
  
  protected static final String OUTCOME_DIRECTIVE_NAME = "outcome";

  public AbstractFormDispatcher() {
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

  protected DataHandler processTemplate(final String name, InputStream src, Map<String, Object> renderContext) {
    DataHandler merged = null;

    try {
      freemarker.template.Configuration cfg = new freemarker.template.Configuration();
      cfg.setObjectWrapper(new DefaultObjectWrapper());
      cfg.setTemplateUpdateDelay(0);

      Template temp = new Template(name, new InputStreamReader(src), cfg);

      // dump template
      /*
       * if(log.isDebugEnabled()) { ByteArrayOutputStream bout = new
       * ByteArrayOutputStream(); temp.dump(new PrintWriter(bout));
       * log.debug(new String(bout.toByteArray())); }
       */

      final ByteArrayOutputStream bout = new ByteArrayOutputStream();
      Writer out = new OutputStreamWriter(bout);
      temp.process(renderContext, out);
      out.flush();

      merged = new DataHandler(

      new DataSource() {

        public InputStream getInputStream() throws IOException {
          return new ByteArrayInputStream(bout.toByteArray());
        }

        public OutputStream getOutputStream() throws IOException {
          return bout;
        }

        public String getContentType() {
          return "application/octet-stream";
        }

        public String getName() {
          return name + "_DataSource";
        }
      });

    } catch (Exception e) {
      throw new RuntimeException("Failed to process task template", e);
    }

    return merged;
  }

  protected String streamToString(InputStream is)
  {
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    StringBuilder sb = new StringBuilder();
    String line = null;

    try
    {
      while ((line = reader.readLine()) != null)
      {
        sb.append(line + "\n");
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException("Failed to read " + PROCESSFORMS_CSS, e);
    }
    finally
    {
      try
      {
        is.close();
      }
      catch (IOException e)
      {
       throw new RuntimeException("Failed to close stream " + PROCESSFORMS_CSS, e);
      }
    }
    return sb.toString();
  }
}

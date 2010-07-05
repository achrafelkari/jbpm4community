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
package org.jbpm.pvm.internal.jobexecutor;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbpm.api.Configuration;
import org.jbpm.api.JbpmException;
import org.jbpm.pvm.internal.env.EnvironmentFactory;

/**
 * starts the job executor on init and closes the 
 * jbpm configuration upon destroy.  The closing of the
 * jbpm configuration will also shut down the job executor 
 * thread pool. 
 * <h1>Config parameters</h1>
 * <table>
 *   <tr>
 *     <th>Name</th> 
 *     <th>Description</th> 
 *     <th>Type</th>
 *     <th>Default value</th>
 *   </tr> 
 *   <tr>
 *     <td>jbpm.configuration.resource</td>
 *     <td>resource location of the jbpm.cfg.xml</td>
 *     <td>String</td>
 *     <td>jbpm.cfg.xml</td>
 *   </tr> 
 * </table>
 * 
 * <p>Configuration example:
 * <pre>
 * &lt;web-app&gt;
 *   ...
 *   &lt;servlet &gt;
 *     &lt;servlet-name>JobExecutorServlet&lt;/servlet-name>
 *     &lt;servlet-class>org.jbpm.job.executor.JobExecutorServlet&lt;/servlet-class>
 *     &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 *   &lt;/servlet&gt;
 *   &lt;servlet-mapping &gt;
 *     &lt;servlet-name&gt;JobExecutorServlet&lt;/servlet-name&gt;
 *     &lt;url-pattern&gt;/jobexecutor&lt;/url-pattern&gt;
 *   &lt;/servlet-mapping&gt;
 *   ...
 * &lt;/web-app&gt;
 * </pre>
 * </p>
 * 
 * @author Tom Baeyens
 */
public class JobExecutorServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  
  private JobExecutor jobExecutor;
  
  @Override
  public void init() throws ServletException {
    String configurationResource = getInitParameter("jbpm.configuration.resource", "jbpm.cfg.xml");
    EnvironmentFactory environmentFactory = (EnvironmentFactory) new Configuration().setResource(configurationResource).buildProcessEngine();
    jobExecutor = environmentFactory.get(JobExecutor.class);
    if (jobExecutor==null) {
      throw new JbpmException("no job executor configured in resource "+configurationResource);
    }
    jobExecutor.start();
  }
  
  @Override
  public void destroy() {
    if (jobExecutor != null) {
      jobExecutor.stop(true);
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    PrintWriter out = response.getWriter();
    out.println("<html>");
    out.println("<body>");
    out.println("<h2>JBoss jBPM job Executor Servlet</h2><hr />");
    /* TODO
    Iterator<Thread> iter = jobExecutor.getJobExecutorThreads().values().iterator();
    while (iter.hasNext()) {
      Thread thread = iter.next();
      out.println("<h4>"+thread.getName()+"</h4>");
      out.println("<b>isAlive</b>:"+thread.isAlive());
    }
    */
    out.println("</body>");
    out.println("</html>");
  }

  String getInitParameter(String name, String defaultValue) {
    String value = getInitParameter(name);
    if (value!=null) {
      return value;
    }
    return defaultValue;
  }
}

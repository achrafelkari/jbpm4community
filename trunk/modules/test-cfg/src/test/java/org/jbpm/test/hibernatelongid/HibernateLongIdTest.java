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
package org.jbpm.test.hibernatelongid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.cmd.ParamCommand;
import org.jbpm.test.JbpmCustomCfgTestCase;


/**
 * @authorX Tom Baeyens
 */
public class HibernateLongIdTest extends JbpmCustomCfgTestCase {
  
  protected void deleteRegisteredDeployments() {
    processEngine.execute(new Command<Void>() {
      public Void execute(Environment environment) {
        Session session = environment.get(Session.class);
        for (String deploymentId : registeredDeployments) {
          repositoryService.deleteDeploymentCascade(deploymentId);
        }

        List<Order> orders = session.createQuery("from "+Order.class.getName()).list();
        for (Order order: orders) {
          session.delete(order);
        }
        
        return null;
      }
    });
  }

  public void testHibernateLongId() {
    processEngine.execute(new Command<Void>() {
      public Void execute(Environment environment) {
        deployJpdlXmlString(
          "<process name='HibernateLongId'>" +
          "  <start>" +
          "    <transition to='a' />" +
          "  </start>" +
          "  <state name='a' />" +
          "</process>"
        );
        return null;
      }
    });

    String processInstanceId = (String) processEngine.execute(new Command<String>() {
      public String execute(Environment environment) {
        Session session = environment.get(Session.class);
        Order order = new Order();
        order.setClient("Contador");
        order.setProduct("Shampoo");
    
        session.save(order);
        session.flush();
        
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("order", order);
    
        ProcessInstance processInstance = executionService.startProcessInstanceByKey("HibernateLongId", variables);
        return processInstance.getId();
      }
    });

    processEngine.execute(new ParamCommand<Void>() {
      public Void execute(Environment environment) {
        Session session = environment.get(Session.class);
        String processInstanceId = (String) params.get("processInstanceId");
        Order order = (Order) executionService.getVariable(processInstanceId, "order");
        assertNotNull(order);
        assertEquals("Contador", order.getClient());
        assertEquals("Shampoo", order.getProduct());
        return null;
      }
    }.setParam("processInstanceId", processInstanceId)
    );
  }
}

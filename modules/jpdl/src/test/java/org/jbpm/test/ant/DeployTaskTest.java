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
package org.jbpm.test.ant;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.apache.tools.ant.Main;

import org.jbpm.api.Deployment;
import org.jbpm.test.JbpmTestCase;

/**
 * @author Alejandro Guizar
 */
public class DeployTaskTest extends JbpmTestCase {

  public void testDeployXml() {
    runTarget("deploy-xml");
    checkDeployment("valid.jpdl.xml");
  }

  public void testDeployZip() {
    runTarget("deploy-zip");
    checkDeployment("valid.zip");
  }

  public void testDeployBar() {
    runTarget("deploy-bar");
    checkDeployment("valid.bar");
  }

  private void runTarget(String target) {
    PrintStream stdout = System.out;
    PrintStream stderr = System.err;

    ByteArrayOutputStream memout = new ByteArrayOutputStream();
    PrintStream prnout = new PrintStream(memout);

    try {
      System.setOut(prnout);
      System.setErr(prnout);

      Main antMain = new Main() {

        protected void exit(int exitCode) {
          // prevent ant from terminating the VM
        }
      };
      String[] args = { "-f", getClass().getResource("build.xml").getPath(), target };
      antMain.startAnt(args, System.getProperties(), getClass().getClassLoader());
    }
    finally {
      System.setOut(stdout);
      System.setErr(stderr);
    }

    log.info(memout.toString());
  }

  private void checkDeployment(String deploymentName) {
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    assertEquals(1, deployments.size());

    Deployment deployment = deployments.get(0);
    assertEquals(deploymentName, deployment.getName());
    repositoryService.deleteDeploymentCascade(deployment.getId());
  }
}

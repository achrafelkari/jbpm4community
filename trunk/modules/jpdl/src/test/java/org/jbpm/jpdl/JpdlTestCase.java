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
package org.jbpm.jpdl;

import org.jbpm.internal.log.Log;
import org.jbpm.jpdl.internal.xml.JpdlParser;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.test.BaseJbpmTestCase;

/**
 * @author Tom Baeyens
 */
public abstract class JpdlTestCase extends BaseJbpmTestCase {

  static protected Log log = Log.getLog(JpdlTestCase.class.getName());

  public ClientProcessDefinition parseJpdl(String xmlString) {
    JpdlParser jpdlParser = new JpdlParser();
    Parse result = jpdlParser.createParse().setString(xmlString).execute();
    return (ClientProcessDefinition)result.checkErrors("process definition xml string").getDocumentObject();
  }

}

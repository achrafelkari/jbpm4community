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
package org.jbpm.jpdl.parsing;

import java.util.List;

import org.jbpm.pvm.internal.xml.Problem;

/**
 * @author Alejandro Guizar
 */
public class VariableParsingTest extends JpdlParseTestCase {

  public void testVariableParse() {
    List<Problem> problems = jpdlParser.createParse()
      .setString("<process name='var' xmlns='http://jbpm.org/4.4/jpdl'>"
        + "  <variable name='s' type='string' init-expr='static' history='true'/>"
        + "  <variable name='i' type='integer' init-expr='#{param * 2}' history='false'/>"
        + "  <variable name='d' type='serializable'>"
        + "    <object class='java.util.Date'>"
        + "      <constructor><arg><long value='1276086573250'/></arg></constructor>"
        + "    </object>"
        + "  </variable>"
        + "  <start/>"
        + "</process>")
      .execute()
      .getProblems();
    assertEquals(problems.toString(), 0, problems.size());
  }
}

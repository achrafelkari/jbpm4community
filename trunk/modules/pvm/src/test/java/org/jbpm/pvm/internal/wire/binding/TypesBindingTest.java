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

import java.util.List;
import org.jbpm.test.BaseJbpmTestCase;
import org.jbpm.pvm.internal.xml.Parser;
import org.jbpm.pvm.internal.xml.Bindings;
import org.jbpm.pvm.internal.xml.Problem;

/**
 * @author Huisheng Xu
 */
public class TypesBindingTest extends BaseJbpmTestCase {

  public void testParse() {
    Parser parser = new Parser();
    Bindings bindings = new Bindings();
    parser.setBindings(bindings);
    parser.getBindings().addBinding(new TypesBinding());

    String xml = "<types resource='org/jbpm/pvm/internal/wire/binding/invalid.jbpm.variable.types.xml' />";

    List<Problem> problems = parser.createParse().setString(xml).execute().getProblems();

    assertTextPresent("org/jbpm/pvm/internal/wire/binding/invalid.jbpm.variable.types.xml",
      problems.get(0).getMsg());
  }
}

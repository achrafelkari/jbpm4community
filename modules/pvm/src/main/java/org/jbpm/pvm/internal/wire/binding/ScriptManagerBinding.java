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

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.jbpm.pvm.internal.script.ScriptManager;
import org.jbpm.pvm.internal.util.ReflectUtil;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.ProvidedObjectDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.StringDescriptor;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;
import org.w3c.dom.Element;


/**
 * @author Tom Baeyens
 */
public class ScriptManagerBinding extends WireDescriptorBinding {

  public ScriptManagerBinding() {
    super("script-manager");
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    ObjectDescriptor descriptor = new ObjectDescriptor(ScriptManager.class);

    if (element.hasAttribute("default-expression-language")) {
      String defaultLanguage = element.getAttribute("default-expression-language");
      descriptor.addInjection("defaultExpressionLanguage", new StringDescriptor(defaultLanguage));
    }

    if (element.hasAttribute("default-script-language")) {
      String defaultLanguage = element.getAttribute("default-script-language");
      descriptor.addInjection("defaultScriptLanguage", new StringDescriptor(defaultLanguage));
    }

    ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    List<Element> scriptElements = XmlUtil.elements(element, "script-language");
    for (Element scriptElement : scriptElements) {
      String languageName = null;
      if (scriptElement.hasAttribute("name")) {
        languageName = scriptElement.getAttribute("name");
      } else {
        parse.addProblem("'name' is a required attribute in element <script-language />", element);
      }
      String factoryClassName = null;
      if (scriptElement.hasAttribute("factory")) {
        factoryClassName = scriptElement.getAttribute("factory");
      } else {
        parse.addProblem("'name' is a required attribute in element <script-language />", element);
      }

      if ( (languageName!=null)
           && (factoryClassName!=null)
         ) {
        try {
          Class<?> factoryClass = ReflectUtil.classForName(factoryClassName);
          ScriptEngineFactory scriptEngineFactory = (ScriptEngineFactory) factoryClass.newInstance();
          scriptEngineManager.registerEngineName(languageName, scriptEngineFactory);
        } catch (Exception e) {
          parse.addProblem("couldn't instantiate ScriptEngineFactory "+factoryClassName, e);
        }
      }
    }

    descriptor.addInjection("scriptEngineManager", new ProvidedObjectDescriptor(scriptEngineManager));

    return descriptor;
  }
}

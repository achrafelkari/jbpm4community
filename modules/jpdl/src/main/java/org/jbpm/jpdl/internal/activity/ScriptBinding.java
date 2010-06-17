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
package org.jbpm.jpdl.internal.activity;

import org.jbpm.jpdl.internal.xml.JpdlParser;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.script.ScriptManager;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.xml.Parse;
import org.w3c.dom.Element;


/**
 * @author Tom Baeyens
 */
public class ScriptBinding extends JpdlBinding {

  private static final String TAG = "script";

  public ScriptBinding() {
    super(TAG);
  }

  public Object parseJpdl(Element element, Parse parse, JpdlParser parser) {
    String language = null;

    String script = XmlUtil.attribute(element, "expr");
    Element textElement = XmlUtil.element(element, "text");
    if(script!=null) {
      ScriptManager scriptManager = EnvironmentImpl.getFromCurrent(ScriptManager.class);
      language = scriptManager.getDefaultExpressionLanguage();
      if (textElement!=null) {
        parse.addProblem("in <script ...> attribute expr can't be combined with a nested text element", element);
      }
    } else if(textElement != null) {
      language = XmlUtil.attribute(element, "lang");
      script = XmlUtil.getContentText(textElement);
    } else {
      parse.addProblem("<script...> element must have either expr attribute or nested text element", element);
    }

    String variableName = XmlUtil.attribute(element, "var");
    
    ScriptActivity scriptActivity = new ScriptActivity();
    scriptActivity.setScript(script);
    scriptActivity.setLanguage(language);
    scriptActivity.setVariableName(variableName);

    return scriptActivity;
  }

}

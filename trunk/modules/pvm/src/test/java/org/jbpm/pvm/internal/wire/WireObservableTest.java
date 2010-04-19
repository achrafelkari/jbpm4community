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
package org.jbpm.pvm.internal.wire;

import java.util.List;

import org.jbpm.pvm.internal.env.Context;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.env.EnvironmentFactory;
import org.jbpm.pvm.internal.processengine.ProcessEngineImpl;
import org.jbpm.pvm.internal.util.Listener;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.test.BaseJbpmTestCase;

/**
 * @author Tom Baeyens
 */
public class WireObservableTest extends BaseJbpmTestCase
{
  
  public static class Recorder implements Listener {
    public void event(Object source, String eventName, Object info) {
    }
  }

  public void testSubscription() {
    // <subscribe /> will use the scope as the observable
    // In this test, there is no eager initialisation

    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<environment-scopes>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"'>" +
      "      <subscribe />" +
      "    </object>" +
      "  </transaction-context>" +
      "</environment-scopes>"
    );

    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      WireContext environmentContext = (WireContext) environment.getContext(Context.CONTEXTNAME_TRANSACTION);

      // this test also checks non-eager-initialized subscription
      // subscription should only be done when the object is created the first time
      List<Listener> listeners = environmentContext.getListeners();
      int beforeListenersSize = (listeners!=null ? listeners.size() : 0);
      
      assertNotNull(environment.get("recorder"));
      
      listeners = environmentContext.getListeners();
      int afterListenersSize = (listeners!=null ? listeners.size() : 0);
      int addedListeners = afterListenersSize - beforeListenersSize;
      assertEquals(1, addedListeners);
      

    } finally {
      environment.close();
    }
    environmentFactory.close();
  }
}

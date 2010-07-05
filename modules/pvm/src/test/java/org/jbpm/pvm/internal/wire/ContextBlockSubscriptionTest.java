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

import org.jbpm.pvm.internal.env.Context;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.env.EnvironmentFactory;
import org.jbpm.pvm.internal.processengine.ProcessEngineImpl;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireException;
import org.jbpm.pvm.internal.wire.WireObjectEventInfo;
import org.jbpm.pvm.internal.wire.xml.WireParser;

/**
 * @author Tom Baeyens
 * @author Guillaume Porcher
 */
public class ContextBlockSubscriptionTest extends SubscriptionTestCase {

  // these are the different ways of specifying observables:
  // scope is always optional.  if the scope is not mentioned, the current scope is intended.

  // <subscribe [context='contextName'] [event(s)='...']... /> will use the scope as the observable
  // <subscribe [context='contextName'] object(s)='objectName' [event(s)='...'] ... /> will use the object(s) with the given name in the specified scope
  // <subscribe [context='contextName'] wire-events='...' object='...' ... /> will listen to wire events of a specific object in the specified scope
  // <subscribe [context='contextName'] object='objectName' [events='...']... /> will use the object with the given name in the same scope as the target object

  // wireXmlParseResult.addProblem("observable in subscribe must be 'environment' or 'scope': "+XmlUtil.toString(element));

  // wireXmlParseResult.addProblem("subscriber to wire-events must have one or more objects specified: "+XmlUtil.toString(element));
  
  public void testSubscriptionNoEnvironment() {
    // <subscribe /> will use the scope as the observable
    // In this test, there is no eager initialization

    WireContext context = new WireContext(WireParser.parseXmlString(
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"'>" +
      "      <subscribe context='foo'/>" +
      "    </object>" +
      "  </transaction-context>"
    ));

    try{
      context.get("recorder");
      fail("expected exception");
    } catch (WireException e) {
      assertTextPresent("couldn't get context foo for subscribe because no environment available in context ", e.getMessage());
    }
  }

  public void testEventReception() {
    // <subscribe /> will use the scope as the observable
    // In this test, there is no eager initialization

    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<jbpm-configuration>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"'>" +
      "      <subscribe />" +
      "    </object>" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );

    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      WireContext contextBlockContext = (WireContext) environment.getContext(Context.CONTEXTNAME_TRANSACTION);
      assertNotNull(environment.get("recorder"));

      // The recorder object will be subscribed to the environment WireScope
      // during initialization.  So the wiring events 'initializing' and 'constructing'
      // will not be recorded.  But events events 'constructed' and 'set'
      // will be received.

      // Object wiring events are fired on the descriptors *and* on the WireScope.  Here
      // we subscribe to the environment WireScope, so we receive also the wire events.
      assertEquals(events.toString(), 2, events.size());
      assertEquals("constructed", events.get(0).eventName);
      assertEquals("set", events.get(1).eventName);

      contextBlockContext.fire("interestingevent", null);
      assertEquals(events.toString(), 3, events.size());
      assertEquals("interestingevent", events.get(2).eventName);

      Descriptor descriptor = contextBlockContext.getWireDefinition().getDescriptor("recorder");
      // this event is fired directly on the descriptor and should therefor not be received
      descriptor.fire("descriptorEvent", null);
      assertEquals(events.toString(), 3, events.size());

    } finally {
      environment.close();
    }

    assertEquals(events.toString(), 4, events.size());
    assertEquals("close", events.get(3).eventName);

    environmentFactory.close();
  }

  public void testEventReceptionWithEagerInit() {
    // <subscribe /> will use the scope as the observable
    // In this test, there is eager initialisation

    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<jbpm-configuration>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"' init='eager'>" +
      "      <subscribe />" +
      "    </object>" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );

    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      assertEquals(events.toString(), 3, events.size());
      int index=0;
      assertEquals("constructed", events.get(index).eventName);
      assertEquals("recorder", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      index++;
      assertEquals("set", events.get(index).eventName);
      assertEquals("recorder", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      index++;
      assertEquals("open", events.get(index).eventName);
      assertNull(events.get(index).info);

    } finally {
      environment.close();
    }
    environmentFactory.close();
  }

  public void testEventReceptionFromOtherObject() {
    // <subscribe /> will use the scope as the observable
    // In this test, there is eager initialisation

    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<jbpm-configuration>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"' init='eager'>" +
      "      <subscribe />" +
      "    </object>" +
      "    <object name='o' class='"+Object.class.getName()+"' />" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );

    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      assertNotNull(environment.get("o"));

      assertEquals(events.toString(), 7, events.size());
      // ignoring the first 3 events: constructed(recorder), set(recorder), open(environment)
      int index=3;
      assertEquals("constructing", events.get(index).eventName);
      assertEquals("o", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      index++;
      assertEquals("initializing", events.get(index).eventName);
      assertEquals("o", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      index++;
      assertEquals("constructed", events.get(index).eventName);
      assertEquals("o", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      index++;
      assertEquals("set", events.get(index).eventName);
      assertEquals("o", ((WireObjectEventInfo)events.get(index).info).getObjectName());

    } finally {
      environment.close();
    }
    environmentFactory.close();
  }

  public void testEventFiltering() {
    // <subscribe event='...' /> will use the scope as the observable and only notify on the specified event
    // In this test, there is eager initialisation

    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<jbpm-configuration>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"' init='eager'>" +
      "      <subscribe event='interestingevent'/>" +
      "    </object>" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );

    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      assertEquals(events.toString(), 0, events.size());

      WireContext contextBlockContext = (WireContext) environment.getContext(Context.CONTEXTNAME_TRANSACTION);
      contextBlockContext.fire("interestingevent", null);

      assertEquals(events.toString(), 1, events.size());

      int index=0;
      assertEquals("interestingevent", events.get(index).eventName);
      assertNull(events.get(index).info);

    } finally {
      environment.close();
    }
    assertEquals(events.toString(), 1, events.size());

    environmentFactory.close();
  }

  public void testEventsFiltering() {
    // <subscribe event='...' /> will use the scope as the observable and only notify on the specified event
    // In this test, there is eager initialisation

    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<jbpm-configuration>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"' init='eager'>" +
      "      <subscribe events='constructing, open, constructed, interestingevent, close'/>" +
      "    </object>" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );

    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      WireContext contextBlockContext = (WireContext) environment.getContext(Context.CONTEXTNAME_TRANSACTION);

      assertEquals(events.toString(), 2, events.size());
      int index=0;
      assertEquals("constructed", events.get(index).eventName);
      assertEquals("recorder", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      index++;
      assertEquals("open", events.get(index).eventName);
      assertNull(events.get(index).info);

      contextBlockContext.fire("interestingevent", null);

      assertEquals(events.toString(), 3, events.size());
      index++;
      assertEquals("interestingevent", events.get(index).eventName);
      assertNull(events.get(index).info);

      contextBlockContext.fire("boringevent", null);

      assertEquals(events.toString(), 3, events.size());

    } finally {
      environment.close();
    }
    assertEquals(events.toString(), 4, events.size());
    assertEquals("close", events.get(3).eventName);

    environmentFactory.close();
  }

  public void testEventsFilteringOnOtherObject() {
    // <subscribe event='...' /> will use the scope as the observable and only notify on the specified event
    // In this test, there is eager initialisation

    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<jbpm-configuration>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"' init='eager'>" +
      "      <subscribe events='constructing, open, constructed, interestingevent, close'/>" +
      "    </object>" +
      "    <object name='o' class='"+Object.class.getName()+"' />" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );

    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      assertNotNull(environment.get("o"));

      assertEquals(events.toString(), 4, events.size());
      // ignoring the first 2 events: constructed(recorder), open(environment)
      int index=2;
      assertEquals("constructing", events.get(index).eventName);
      assertEquals("o", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      index++;
      assertEquals("constructed", events.get(index).eventName);
      assertEquals("o", ((WireObjectEventInfo)events.get(index).info).getObjectName());


    } finally {
      environment.close();
    }
    assertEquals(events.toString(), 5, events.size());
    assertEquals("close", events.get(4).eventName);

    environmentFactory.close();
  }

  public void testOtherWireScope() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<jbpm-configuration>" +
      "  <process-engine-context>" +
      "    <object name='factory' class='"+Object.class.getName()+"' />" +
      "  </process-engine-context>" +
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"' init='eager'>" +
      "      <subscribe context='process-engine'/>" +
      "    </object>" +
      "    <object name='product' factory='factory' method='getClass' />" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );

    int index=0;

    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      assertEquals(events.toString(), 0, events.size());

      assertNotNull(environment.get("product"));

      assertEquals("constructing", events.get(index).eventName);
      assertEquals("factory", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      index++;
      assertEquals("initializing", events.get(index).eventName);
      assertEquals("factory", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      index++;
      assertEquals("constructed", events.get(index).eventName);
      assertEquals("factory", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      index++;
      assertEquals("set", events.get(index).eventName);
      assertEquals("factory", ((WireObjectEventInfo)events.get(index).info).getObjectName());


    } finally {
      environment.close();
    }

    environmentFactory.close();

    index++;
    assertEquals("close", events.get(index).eventName);
    ProcessEngineImpl processEngineImpl = (ProcessEngineImpl) environmentFactory;
    WireContext applicationWireContext = processEngineImpl.getProcessEngineWireContext();
    assertEquals(applicationWireContext, events.get(index).source);
    assertNull(events.get(index).info);
  }

  public void testOtherWireScopeWithEventFiltering() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<jbpm-configuration>" +
      "  <process-engine-context>" +
      "    <object name='factory' class='"+Object.class.getName()+"' />" +
      "  </process-engine-context>" +
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"' init='eager'>" +
      "      <subscribe context='process-engine' events='initializing' />" +
      "    </object>" +
      "    <object name='product' factory='factory' method='getClass' />" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );

    int index=0;

    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      assertEquals(events.toString(), 0, events.size());

      assertNotNull(environment.get("product"));

      assertEquals("initializing", events.get(index).eventName);
      assertEquals("factory", ((WireObjectEventInfo)events.get(index).info).getObjectName());


    } finally {
      environment.close();
    }

    assertEquals(events.toString(), 1, events.size());

    environmentFactory.close();

    assertEquals(events.toString(), 1, events.size());
  }

  public void testUnexistingScope() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<jbpm-configuration>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"' init='eager'>" +
      "      <subscribe context='unexistingcontext' />" +
      "    </object>" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );

    assertNull(EnvironmentImpl.getCurrent());
    try {
      environmentFactory.openEnvironment();
      fail("expected exception");
    } catch (WireException e) {
      assertNull(EnvironmentImpl.getCurrent());
      assertTextPresent("couldn't initialize object 'recorder': couldn't subscribe because context unexistingcontext doesn't exist", e.getMessage());
    }
  }


  public static class A {
    Object o;
  }
  public void testEventReceptionNested() {
    // <subscribe /> will use the scope as the observable
    // In this test, there is no eager initialization

    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<jbpm-configuration>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='a' class='"+A.class.getName()+"'>" +
      "      <field name='o'>" +
      "         <object name='recorder' class='"+Recorder.class.getName()+"'>" +
      "           <subscribe />" +
      "         </object>" +
      "      </field>" +
      "     </object>"+
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );

    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      WireContext transactionContext = (WireContext) environment.getContext(Context.CONTEXTNAME_TRANSACTION);
      assertNotNull(environment.get("a"));

      // The recorder object will be subscribed to the environment WireContext
      // during initialization.  So the wiring events 'initializing' and 'constructing'
      // will not be recorded.  But events events 'constructed' and 'set'
      // will be received.

      // Object wiring events are fired on the descriptors *and* on the WireContext.  Here
      // we subscribe to the environment WireContext, so we receive also the wire events.
      assertEquals(events.toString(), 4, events.size());
      assertEquals("constructed", events.get(0).eventName);
      assertEquals("recorder", ((WireObjectEventInfo)events.get(0).info).getObjectName());
      assertEquals("set", events.get(1).eventName);
      assertEquals("recorder", ((WireObjectEventInfo)events.get(1).info).getObjectName());
      assertEquals("constructed", events.get(2).eventName);
      assertEquals("a", ((WireObjectEventInfo)events.get(2).info).getObjectName());
      assertEquals("set", events.get(3).eventName);
      assertEquals("a", ((WireObjectEventInfo)events.get(3).info).getObjectName());
      transactionContext.fire("interestingevent", null);
      assertEquals(events.toString(), 5, events.size());
      assertEquals("interestingevent", events.get(4).eventName);

    } finally {
      environment.close();
    }

    assertEquals(events.toString(), 6, events.size());
    assertEquals("close", events.get(5).eventName);

    environmentFactory.close();
  }

  public void testEventReceptionNestedTwoTimes() {
    // <subscribe /> will use the scope as the observable
    // In this test, there is no eager initialization

    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<jbpm-configuration>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='a' class='"+A.class.getName()+"'>" +
      "      <field name='o'>" +
      "        <object name='b' class='"+A.class.getName()+"'>" +
      "          <field name='o'>" +
      "            <object name='recorder' class='"+Recorder.class.getName()+"'>" +
      "              <subscribe />" +
      "            </object>" +
      "          </field>" +
      "        </object>"+
      "      </field>" +
      "     </object>"+
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );

    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      WireContext contextBlockContext = (WireContext) environment.getContext(Context.CONTEXTNAME_TRANSACTION);
      assertNotNull(environment.get("a"));

      // The recorder object will be subscribed to the environment WireContext
      // during initialization.  So the wiring events 'initializing' and 'constructing'
      // will not be recorded.  But events events 'constructed' and 'set'
      // will be received.

      // Object wiring events are fired on the descriptors *and* on the WireContext.  Here
      // we subscribe to the environment WireContext, so we receive also the wire events.
      assertEquals(events.toString(), 6, events.size());
      
      assertEquals("constructed", events.get(0).eventName);
      assertEquals("recorder", ((WireObjectEventInfo)events.get(0).info).getObjectName());
      assertEquals("set", events.get(1).eventName);
      assertEquals("recorder", ((WireObjectEventInfo)events.get(1).info).getObjectName());
      
      assertEquals("constructed", events.get(2).eventName);
      assertEquals("b", ((WireObjectEventInfo)events.get(2).info).getObjectName());
      assertEquals("set", events.get(3).eventName);
      assertEquals("b", ((WireObjectEventInfo)events.get(3).info).getObjectName());

      assertEquals("constructed", events.get(4).eventName);
      assertEquals("a", ((WireObjectEventInfo)events.get(4).info).getObjectName());
      assertEquals("set", events.get(5).eventName);
      assertEquals("a", ((WireObjectEventInfo)events.get(5).info).getObjectName());

      contextBlockContext.fire("interestingevent", null);
      assertEquals(events.toString(), 7, events.size());
      assertEquals("interestingevent", events.get(6).eventName);

    } finally {
      environment.close();
    }

    assertEquals(events.toString(), 8, events.size());
    assertEquals("close", events.get(7).eventName);

    environmentFactory.close();
  }
}

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

import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.env.EnvironmentFactory;
import org.jbpm.pvm.internal.processengine.ProcessEngineImpl;
import org.jbpm.pvm.internal.util.DefaultObservable;
import org.jbpm.pvm.internal.util.Observable;
import org.jbpm.pvm.internal.wire.WireObjectEventInfo;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;

/**
 * @author Tom Baeyens
 */
public class WireEventsSubscriptionTest extends SubscriptionTestCase {

  // <subscribe to='wire-events' ... /> will use the descriptors in the 
  // WireDefinition as observables.  They will notify the subscriber
  // of the wire events as defined in the static contants found in 
  // class Descriptor

  public void testRegisterToAllDescriptors() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<environment-scopes>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"'>" +
      // If no object(s) are specified, the subscriber will be informed of 
      // events on *all* descriptors.
      "      <subscribe to='wire-events' />" +
      "    </object>" +
      "    <object name='o' class='"+DefaultObservable.class.getName()+"' />" +
      "  </transaction-context>" +
      "</environment-scopes>"
    );

    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      Observable observable = (Observable) environment.get("o");
      assertNotNull(observable);

      assertEquals(events.toString(), 0, events.size());

      environment.get("recorder");

      assertEquals(events.toString(), 2, events.size());

      int index=0;
      assertEquals("constructed", events.get(index).eventName);
      assertEquals("recorder", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      
      index++;
      assertEquals("set", events.get(index).eventName);
      assertEquals("recorder", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      
    } finally {
      environment.close();
    }
    environmentFactory.close();

    assertEquals(events.toString(), 2, events.size());
  }

  public void testOneObjectDescriptor() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<environment-scopes>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"' init='eager'>" +
      // only subscribe to wire-events of object o, which means that recorder will 
      // be notified of any event produced by the descriptor of object o
      "      <subscribe to='wire-events' object='o' />" +
      "    </object>" +
      "    <object name='o' class='"+DefaultObservable.class.getName()+"' />" +
      "    <object name='p' class='"+Object.class.getName()+"' />" +
      "  </transaction-context>" +
      "</environment-scopes>"
    );

    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {

      assertEquals(events.toString(), 0, events.size());

      assertNotNull(environment.get("o"));

      assertEquals(events.toString(), 4, events.size());

      int index=0;
      assertEquals("constructing", events.get(index).eventName);
      assertEquals("o", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      assertEquals(ObjectDescriptor.class, events.get(index).source.getClass());
      
      index++;
      assertEquals("initializing", events.get(index).eventName);
      assertEquals("o", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      assertEquals(ObjectDescriptor.class, events.get(index).source.getClass());
      
      index++;
      assertEquals("constructed", events.get(index).eventName);
      assertEquals("o", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      assertEquals(ObjectDescriptor.class, events.get(index).source.getClass());
      
      index++;
      assertEquals("set", events.get(index).eventName);
      assertEquals("o", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      assertEquals(ObjectDescriptor.class, events.get(index).source.getClass());
      
      assertNotNull(environment.get("p"));
      
      assertEquals(events.toString(), 4, events.size());
      
    } finally {
      environment.close();
    }
    environmentFactory.close();

    assertEquals(events.toString(), 4, events.size());
  }

  public void testMultipleObjectDescriptors() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<environment-scopes>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"' init='eager'>" +
      // record all wire events coming from the descriptors of objects o and p  
      "      <subscribe to='wire-events' objects='o, p' />" +
      "    </object>" +
      "    <object name='o' class='"+DefaultObservable.class.getName()+"' />" +
      "    <object name='p' class='"+Object.class.getName()+"' />" +
      "  </transaction-context>" +
      "</environment-scopes>"
    );

    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {

      assertEquals(events.toString(), 0, events.size());

      assertNotNull(environment.get("o"));

      assertEquals(events.toString(), 4, events.size());

      int index=0;
      assertEquals("constructing", events.get(index).eventName);
      assertEquals("o", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      assertEquals(ObjectDescriptor.class, events.get(index).source.getClass());
      
      index++;
      assertEquals("initializing", events.get(index).eventName);
      assertEquals("o", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      assertEquals(ObjectDescriptor.class, events.get(index).source.getClass());
      
      index++;
      assertEquals("constructed", events.get(index).eventName);
      assertEquals("o", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      assertEquals(ObjectDescriptor.class, events.get(index).source.getClass());
      
      index++;
      assertEquals("set", events.get(index).eventName);
      assertEquals("o", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      assertEquals(ObjectDescriptor.class, events.get(index).source.getClass());
      
      assertNotNull(environment.get("p"));
      
      assertEquals(events.toString(), 8, events.size());

      index++;
      assertEquals("constructing", events.get(index).eventName);
      assertEquals("p", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      assertEquals(ObjectDescriptor.class, events.get(index).source.getClass());
      
      index++;
      assertEquals("initializing", events.get(index).eventName);
      assertEquals("p", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      assertEquals(ObjectDescriptor.class, events.get(index).source.getClass());
      
      index++;
      assertEquals("constructed", events.get(index).eventName);
      assertEquals("p", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      assertEquals(ObjectDescriptor.class, events.get(index).source.getClass());
      
      index++;
      assertEquals("set", events.get(index).eventName);
      assertEquals("p", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      assertEquals(ObjectDescriptor.class, events.get(index).source.getClass());
      
    } finally {
      environment.close();
    }
    environmentFactory.close();

    assertEquals(events.toString(), 8, events.size());
  }

  public void testOneObjectDescriptorWithEventFilter() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<environment-scopes>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"' init='eager'>" +
      // record only 'initializing' wire events produced by the descriptor of object o. 
      "      <subscribe to='wire-events' object='o' event='initializing' />" +
      "    </object>" +
      "    <object name='o' class='"+DefaultObservable.class.getName()+"' />" +
      "    <object name='p' class='"+Object.class.getName()+"' />" +
      "  </transaction-context>" +
      "</environment-scopes>"
    );

    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {

      assertEquals(events.toString(), 0, events.size());

      assertNotNull(environment.get("o"));

      assertEquals(events.toString(), 1, events.size());

      int index=0;
      assertEquals("initializing", events.get(index).eventName);
      assertEquals("o", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      assertEquals(ObjectDescriptor.class, events.get(index).source.getClass());
      
      assertNotNull(environment.get("p"));
      
      assertEquals(events.toString(), 1, events.size());
      
    } finally {
      environment.close();
    }
    environmentFactory.close();

    assertEquals(events.toString(), 1, events.size());
  }

  public void testMultipleObjectDescriptorsWithEventFilter() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<environment-scopes>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"' init='eager'>" +
      // record 'constructing' and 'constructed' events fired by descriptors for objects o and p 
      "      <subscribe to='wire-events' objects='o, p' events='constructing, constructed' />" +
      "    </object>" +
      "    <object name='o' class='"+DefaultObservable.class.getName()+"' />" +
      "    <object name='p' class='"+Object.class.getName()+"' />" +
      "  </transaction-context>" +
      "</environment-scopes>"
    );

    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {

      assertEquals(events.toString(), 0, events.size());

      assertNotNull(environment.get("o"));

      assertEquals(events.toString(), 2, events.size());

      int index=0;
      assertEquals("constructing", events.get(index).eventName);
      assertEquals("o", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      assertEquals(ObjectDescriptor.class, events.get(index).source.getClass());
      
      index++;
      assertEquals("constructed", events.get(index).eventName);
      assertEquals("o", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      assertEquals(ObjectDescriptor.class, events.get(index).source.getClass());
      
      assertNotNull(environment.get("p"));
      
      assertEquals(events.toString(), 4, events.size());

      index++;
      assertEquals("constructing", events.get(index).eventName);
      assertEquals("p", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      assertEquals(ObjectDescriptor.class, events.get(index).source.getClass());
      
      index++;
      assertEquals("constructed", events.get(index).eventName);
      assertEquals("p", ((WireObjectEventInfo)events.get(index).info).getObjectName());
      assertEquals(ObjectDescriptor.class, events.get(index).source.getClass());
      
    } finally {
      environment.close();
    }
    environmentFactory.close();

    assertEquals(events.toString(), 4, events.size());
  }
}

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
import org.jbpm.pvm.internal.wire.WireException;

/**
 * @author Tom Baeyens
 * @author Guillaume Porcher
 */
public class ObjectSubscriptionTest extends SubscriptionTestCase {

  public void testObjectSubscriptionLazyInit() {
    // <subscribe object='...' /> will use the object as the observable
    // In this test, there is no eager initialisation

    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<environment-scopes>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"'>" +
      "      <subscribe object='o' />" +
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

      observable.fire("gun", null);
      assertEquals(events.toString(), 0, events.size());

      // initialize the recorder and subscribe to the observer
      environment.get("recorder");

      observable.fire("bazooka", null);
      assertEquals(events.toString(), 1, events.size());

      int index=0;
      assertEquals("bazooka", events.get(index).eventName);
      assertNull(events.get(index).info);

    } finally {
      environment.close();
    }
    environmentFactory.close();
  }

  public void testObjectSubscriptionEagerInit() {
    // <subscribe object='...' /> will use the object as the observable
    // In this test, there is eager initialisation

    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<environment-scopes>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"' init='eager'>" +
      "      <subscribe object='o' />" +
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

      observable.fire("gun", null);
      assertEquals(events.toString(), 1, events.size());

      int index=0;
      assertEquals("gun", events.get(index).eventName);
      assertNull(events.get(index).info);

      // initialize the recorder and subscribe to the observer
      environment.get("recorder");

      observable.fire("bazooka", null);
      assertEquals(events.toString(), 2, events.size());

      index++;
      assertEquals("bazooka", events.get(index).eventName);
      assertNull(events.get(index).info);

    } finally {
      environment.close();
    }
    environmentFactory.close();
  }

  public void testMultipleObjects() {
    // <subscribe objects='...' /> will use the object as the observable
    // In this test, there is eager initialisation

    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<environment-scopes>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"' init='eager'>" +
      "      <subscribe objects='newsletter, blog, newspaper' />" +
      "    </object>" +
      "    <object name='newsletter' class='"+DefaultObservable.class.getName()+"' />" +
      "    <object name='blog' class='"+DefaultObservable.class.getName()+"' />" +
      "    <object name='newspaper' class='"+DefaultObservable.class.getName()+"' />" +
      "  </transaction-context>" +
      "</environment-scopes>"
    );

    int index=0;
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      // fire 2 events on newsletter: "look at our website" and "buy our product"
      Observable newsletter = (Observable) environment.get("newsletter");
      assertNotNull(newsletter);

      assertEquals(events.toString(), 0, events.size());

      newsletter.fire("look at our website", null);

      assertEquals(events.toString(), index+1, events.size());
      assertEquals("look at our website", events.get(index).eventName);
      assertNull(events.get(index).info);
      assertSame(newsletter, events.get(index).source);

      newsletter.fire("buy our product", null);

      index++;
      assertEquals(events.toString(), index+1, events.size());
      assertEquals("buy our product", events.get(index).eventName);
      assertNull(events.get(index).info);
      assertSame(newsletter, events.get(index).source);

      // fire 1 event on blog: 'the most stupid test'
      Observable blog = (Observable) environment.get("blog");
      blog.fire("The most stupid test", "Today, I saw the most stupid test ever...");

      index++;
      assertEquals(events.toString(), index+1, events.size());
      assertEquals("The most stupid test", events.get(index).eventName);
      assertEquals("Today, I saw the most stupid test ever...", events.get(index).info);
      assertSame(blog, events.get(index).source);

      // fire 1 event on newspaper: 'peace in the middle east'
      Observable newspaper = (Observable) environment.get("newspaper");
      newspaper.fire("peace in the middle east", null);

      index++;
      assertEquals(events.toString(), index+1, events.size());
      assertEquals("peace in the middle east", events.get(index).eventName);
      assertNull(events.get(index).info);
      assertSame(newspaper, events.get(index).source);

    } finally {
      environment.close();
      assertEquals(events.toString(), index+1, events.size());
    }
    environmentFactory.close();
    assertEquals(events.toString(), index+1, events.size());
  }

  public void testUnexistingObject() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<environment-scopes>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"' init='eager'>" +
      "      <subscribe object='unexistingobject' />" +
      "    </object>" +
      "  </transaction-context>" +
      "</environment-scopes>"
    );

    try {
      environmentFactory.openEnvironment();
      fail("expected exception");
    } catch (WireException e) {
      assertTextPresent("couldn't initialize object 'recorder': couldn't subscribe to object in context transaction: object unexistingobject unavailable", e.getMessage());
    }
  }

  public void testEventFilter() {
    // <subscribe object='...' /> will use the object as the observable
    // In this test, there is eager initialization

    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<environment-scopes>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"' init='eager'>" +
      "      <subscribe object='o' events='ping, pong' />" +
      "    </object>" +
      "    <object name='o' class='"+DefaultObservable.class.getName()+"' />" +
      "  </transaction-context>" +
      "</environment-scopes>"
    );

    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      Observable observable = (Observable) environment.get("o");
      assertNotNull(observable);

      observable.fire("ping", null);
      observable.fire("en", null);
      observable.fire("pong", null);
      observable.fire("gingen", null);
      observable.fire("samen", null);
      observable.fire("pingpong", null);
      observable.fire("spelen.", null);
      observable.fire("Pang", null);
      observable.fire("zei", null);
      observable.fire("ping", null);
      observable.fire("en", null);
      observable.fire("pong", null);
      observable.fire("stond", null);
      observable.fire("paf", null);

      assertEquals(events.toString(), 4, events.size());

      int index=0;
      assertEquals("ping", events.get(index).eventName);
      index++;
      assertEquals("pong", events.get(index).eventName);
      index++;
      assertEquals("ping", events.get(index).eventName);
      index++;
      assertEquals("pong", events.get(index).eventName);

    } finally {
      environment.close();
    }
    environmentFactory.close();
  }

  public void testNotObservableObject() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<environment-scopes>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='recorder' class='"+Recorder.class.getName()+"' init='eager'>" +
      "      <subscribe object='o' />" +
      "    </object>" +
      "    <object name='o' class='java.lang.Object' />" +
      "  </transaction-context>" +
      "</environment-scopes>"
    );

    try {
      environmentFactory.openEnvironment();
      fail("expected exception");
    } catch (WireException e) {
      assertTextPresent("couldn't initialize object 'recorder': couldn't subscribe to object in context transaction: object o (java.lang.Object) isn't "+Observable.class.getName(), e.getMessage());
    }
    environmentFactory.close();
  }

  public void testNotListener() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<environment-scopes>" +
      "  <process-engine-context />" +
      "  <transaction-context>" +
      "    <object name='recorder' class='java.lang.Object' init='eager'>" +
      "      <subscribe object='o' />" +
      "    </object>" +
      "    <object name='o' class='" + DefaultObservable.class.getName() +"' />" +
      "  </transaction-context>" +
      "</environment-scopes>"
    );

    try {
      environmentFactory.openEnvironment();
      fail("expected exception");
    } catch (WireException e) {
      assertTextPresent("couldn't initialize object 'recorder': couldn't subscribe object", e.getMessage());
      assertTextPresent("because it is not a Listener", e.getMessage());
    }
    environmentFactory.close();
  }
}

package org.jbpm.pvm.internal.wire;

import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.env.EnvironmentFactory;
import org.jbpm.pvm.internal.processengine.ProcessEngineImpl;
import org.jbpm.pvm.internal.util.DefaultObservable;
import org.jbpm.pvm.internal.util.Observable;
import org.jbpm.pvm.internal.wire.WireException;

/**
 * Test subscription using a specified method.
 * @author Guillaume Porcher
 *
 */
public class MethodSubscriptionTest extends SubscriptionTestCase {

  /**
   * Records calls to logEvent methods.
   */
  public static class NonListenerRecorder {
    public void logEvent(Object source, String eventName, Object info) {
      events.add(new Event(source, eventName, info));
    }

    public void logEvent(Object source, String eventName) {
      logEvent(source, eventName, null);
    }

    public void logEvent(String eventName) {
      logEvent(null, eventName, null);
    }

    public void logEvent() {
      logEvent(null,null,null);
    }

    public static int getNumberOfEvents() {
      return events.size();
    }
  }

  /**
   * Test subscription using a specified method with no argument.
   * The recorder is eagerly initialized.
   */
  public void testMethodWithNoArg() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
        "<jbpm-configuration>" +
        "  <process-engine-context />" +
        "  <transaction-context>" +
        "  <object name='l' class='"+NonListenerRecorder.class.getName()+"' init='eager'>" +
        "    <subscribe object='a' method='logEvent' />" +
        "  </object>" +
        "  <object name='a' class='"+DefaultObservable.class.getName()+"' />" +
        "  </transaction-context>" +
        "</jbpm-configuration>"
    );
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      Observable a = (Observable) environment.get("a");
      assertNotNull(a);

      assertEquals(events.toString(), 0, events.size());

      a.fire("ping", null);
      a.fire("pong", null);

      assertEquals(events.toString(), 2, events.size());

      int index=0;
      assertNull(events.get(index).eventName);
      assertNull(events.get(index).info);
      assertNull(events.get(index).source);
      index++;
      assertNull(events.get(index).eventName);
      assertNull(events.get(index).info);
      assertNull(events.get(index).source);

    } finally {
      environment.close();
    }
    environmentFactory.close();
  }
  /**
   * Test subscription using a non existing method.
   * The recorder is eagerly initialized.
   */
  public void testMethodWithBadName() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
        "<jbpm-configuration>" +
        "  <process-engine-context />" +
        "  <transaction-context>" +
        "  <object name='l' class='"+NonListenerRecorder.class.getName()+"' init='eager'>" +
        "    <subscribe object='a' method='i-am-not-a-method' />" +
        "  </object>" +
        "  <object name='a' class='"+DefaultObservable.class.getName()+"' />" +
        "  </transaction-context>" +
        "</jbpm-configuration>"
    );
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      Observable a = (Observable) environment.get("a");
      assertNotNull(a);

      assertEquals(events.toString(), 0, events.size());

      try {
        a.fire("ping", null);
        fail("expected exception");
      } catch (WireException e) {
        assertTextPresent("method i-am-not-a-method() unavailable", e.getMessage());
      }


    } finally {
      environment.close();
    }
    environmentFactory.close();
  }

  /**
   * Test subscription using a specified method with one argument.
   * The recorder is eagerly initialized.
   */
  public void testMethodWithOneArg() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
        "<jbpm-configuration>" +
        "  <process-engine-context />" +
        "  <transaction-context>" +
        "  <object name='l' class='"+NonListenerRecorder.class.getName()+"' init='eager'>" +
        "    <subscribe object='a' method='logEvent' >" +
        "       <arg>" +
        "         <string value='a'/>" +
        "       </arg>" +
        "    </subscribe>" +
        "  </object>" +
        "  <object name='a' class='"+DefaultObservable.class.getName()+"' />" +
        "  </transaction-context>" +
        "</jbpm-configuration>"
    );
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      Observable a = (Observable) environment.get("a");
      assertNotNull(a);

      assertEquals(events.toString(), 0, events.size());

      a.fire("ping", null);
      a.fire("pong", null);

      assertEquals(events.toString(), 2, events.size());

      int index=0;
      assertEquals("a", events.get(index).eventName);
      assertNull(events.get(index).info);
      assertNull(events.get(index).source);
      index++;
      assertEquals("a", events.get(index).eventName);
      assertNull(events.get(index).info);
      assertNull(events.get(index).source);

    } finally {
      environment.close();
    }
    environmentFactory.close();
  }

  /**
   * Test subscription using a specified method with two arguments.
   * The recorder is eagerly initialized.
   */
  public void testMethodWithTwoArgs() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
        "<jbpm-configuration>" +
        "  <process-engine-context /> "+
        "  <transaction-context>" +
        "  <object name='l' class='"+NonListenerRecorder.class.getName()+"' init='eager'>" +
        "    <subscribe object='a' method='logEvent' event='ping'>" +
        "       <arg type='java.lang.Object'>" +
        "         <ref object='a'/>" +
        "       </arg>" +
        "       <arg>" +
        "         <string value='hello'/>" +
        "       </arg>" +
        "    </subscribe>" +
        "    <subscribe object='a' method='logEvent' event='pong'>" +
        "       <arg type='java.lang.Object'>" +
        "         <ref object='a'/>" +
        "       </arg>" +
        "       <arg>" +
        "         <string value='world'/>" +
        "       </arg>" +
        "    </subscribe>" +
        "  </object>" +
        "  <object name='a' class='"+DefaultObservable.class.getName()+"' />" +
        "  </transaction-context>" +
        "</jbpm-configuration>"
    );
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      Observable a = (Observable) environment.get("a");
      assertNotNull(a);

      assertEquals(events.toString(), 0, events.size());

      a.fire("ping", null);
      a.fire("pong", null);

      assertEquals(events.toString(), 2, events.size());

      int index=0;
      assertEquals("hello", events.get(index).eventName);
      assertNull(events.get(index).info);
      assertEquals(a, events.get(index).source);
      index++;
      assertEquals("world", events.get(index).eventName);
      assertNull(events.get(index).info);
      assertEquals(a, events.get(index).source);

    } finally {
      environment.close();
    }
    environmentFactory.close();
  }


  /**
   * Test subscription using a specified method with 3 arguments.
   * The info argument is taken from a method invocation.
   * The recorder is eagerly initialized.
   */
  public void testMethodWithThreeArgs() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
        "<jbpm-configuration>" +
        "  <process-engine-context />" +
        "  <transaction-context>" +
        "  <object name='l' class='"+NonListenerRecorder.class.getName()+"' init='eager'>" +
        "    <subscribe object='a' method='logEvent'>" +
        "       <arg type='java.lang.Object'>" +
        "         <ref object='a'/>" +
        "       </arg>" +
        "       <arg>" +
        "         <string value='hello'/>" +
        "       </arg>" +
        "       <arg type='java.lang.Object'>" +
        "         <object class='"+NonListenerRecorder.class.getName()+"' method='getNumberOfEvents' />" +
        "       </arg>" +
        "    </subscribe>" +
        "  </object>" +
        "  <object name='a' class='"+DefaultObservable.class.getName()+"' />" +
        "  </transaction-context>" +
        "</jbpm-configuration>"
    );
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      Observable a = (Observable) environment.get("a");
      assertNotNull(a);

      assertEquals(events.toString(), 0, events.size());

      a.fire("ping", null);
      a.fire("pong", null);

      assertEquals(events.toString(), 2, events.size());

      int index=0;
      assertEquals("hello", events.get(index).eventName);
      assertEquals(0, events.get(index).info);
      assertEquals(a, events.get(index).source);
      index++;
      assertEquals("hello", events.get(index).eventName);
      assertEquals(1, events.get(index).info);
      assertEquals(a, events.get(index).source);

    } finally {
      environment.close();
    }
    environmentFactory.close();
  }

  /**
   * Test subscription using a specified method and two objects.
   */
  public void testMethodWithArgsAndMultipleObjectsSubcription() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
        "<environment-scopes>" +
        "  <process-engine-context />" +
        "  <transaction-context>" +
        "  <object name='l' class='"+NonListenerRecorder.class.getName()+"' init='eager'>" +
        "    <subscribe object='a' method='logEvent' >" +
        "       <arg>" +
        "        <string value='a'/>" +
        "      </arg>" +
        "   </subscribe>" +
        "   <subscribe object='b' method='logEvent' >" +
        "      <arg>" +
        "       <string value='b'/>" +
        "     </arg>" +
        "    </subscribe>" +
        "  </object>" +
        "  <object name='a' class='"+DefaultObservable.class.getName()+"' />" +
        "  <object name='b' class='"+DefaultObservable.class.getName()+"' />" +
        "  </transaction-context>" +
        "</environment-scopes>"
    );
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      Observable a = (Observable) environment.get("a");
      assertNotNull(a);
      Observable b = (Observable) environment.get("b");
      assertNotNull(b);

      a.fire("ping", null);
      b.fire("ping", null);
      a.fire("ping", null);
      b.fire("ping", null);

      assertEquals(events.toString(), 4, events.size());

      int index=0;
      assertEquals("a", events.get(index).eventName);
      assertNull(events.get(index).info);
      assertNull(events.get(index).source);
      index++;
      assertEquals("b", events.get(index).eventName);
      assertNull(events.get(index).info);
      assertNull(events.get(index).source);
      index++;
      assertEquals("a", events.get(index).eventName);
      assertNull(events.get(index).info);
      assertNull(events.get(index).source);
      index++;
      assertEquals("b", events.get(index).eventName);
      assertNull(events.get(index).info);
      assertNull(events.get(index).source);
    } finally {
      environment.close();
    }
    environmentFactory.close();
  }


  /**
   * Test subscription using a specified method.
   */
  public static class OverloadingTestClass extends DefaultObservable{
    Object o;
    public Object getO() {
      return o;
    }
    public void setO(Object o) {
      this.o = o;
    }
  }

  /**
   * Records calls to logEvent methods.
   */
  public static class OverloadingRecorder {

    public void logEvent(Object o) {
      events.add(new Event(null, "Event(Object) : " + o, null));
    }

    public void logEvent(String name) {
      events.add(new Event(null, "Event(String) : " + name, null));
    }

    public void logEvent(Integer num) {
      events.add(new Event(null, "Event(Integer) : " + num, null));
    }

  }

  public void testMethodAndOverLoading() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
        "<environment-scopes>" +
        "  <process-engine-context />" +
        "  <transaction-context>" +
        "    <object name='l' class='"+OverloadingRecorder.class.getName()+"' init='eager'>" +
        "      <subscribe object='a' method='logEvent' >" +
        "        <arg type='java.lang.Object'>" +
        "          <object factory='a' method='getO' />" +
        "        </arg>" +
        "      </subscribe>" +
        "    </object>" +
        "    <object name='a' class='"+OverloadingTestClass.class.getName()+"' />" +
        "  </transaction-context>" +
        "</environment-scopes>"
    );
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      OverloadingRecorder r = (OverloadingRecorder) environment.get("l");
      assertNotNull(r);
      OverloadingTestClass a = (OverloadingTestClass) environment.get("a");
      assertNotNull(a);

      r.logEvent(a.getO());
      a.fire("ping", null);

      a.setO("toto");
      r.logEvent(a.getO());
      a.fire("ping", null);

      a.setO(Integer.valueOf(42));
      r.logEvent(a.getO());
      a.fire("ping", null);

      a.setO(Boolean.TRUE);
      r.logEvent(a.getO());
      a.fire("ping", null);

      assertEquals(events.toString(), 8, events.size());

      int index=0;
      assertEquals(events.get(index).eventName, events.get(index+1).eventName, events.get(index).eventName);
      index+=2;
      assertEquals(events.get(index).eventName, events.get(index+1).eventName, events.get(index).eventName);
      index+=2;
      assertEquals(events.get(index).eventName, events.get(index+1).eventName, events.get(index).eventName);
      index+=2;
      assertEquals(events.get(index).eventName, events.get(index+1).eventName, events.get(index).eventName);

    } finally {
      environment.close();
    }
    environmentFactory.close();
  }

  public void testMethodAndBadArg() {
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
        "<environment-scopes>" +
        "  <process-engine-context />" +
        "  <transaction-context>" +
        "    <object name='l' class='"+OverloadingRecorder.class.getName()+"' init='eager'>" +
        "      <subscribe object='a' method='logEvent' >" +
        "        <arg type='java.lang.Integer'>" +
        "          <string value='test' />" +
        "        </arg>" +
        "      </subscribe>" +
        "    </object>" +
        "    <object name='a' class='"+OverloadingTestClass.class.getName()+"' />" +
        "  </transaction-context>" +
        "</environment-scopes>"
    );
    EnvironmentImpl environment = environmentFactory.openEnvironment();
    try {
      OverloadingRecorder r = (OverloadingRecorder) environment.get("l");
      assertNotNull(r);
      OverloadingTestClass a = (OverloadingTestClass) environment.get("a");
      assertNotNull(a);

      a.fire("ping", null);
      fail("expected exception");
    } catch (WireException e) {
      assertTextPresent("couldn't invoke listener method logEvent", e.getMessage());
    } finally {
      environment.close();
    }
    environmentFactory.close();
  }
}

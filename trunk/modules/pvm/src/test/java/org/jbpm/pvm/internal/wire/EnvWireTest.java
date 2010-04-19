package org.jbpm.pvm.internal.wire;

import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.env.EnvironmentFactory;
import org.jbpm.pvm.internal.processengine.ProcessEngineImpl;
import org.jbpm.pvm.internal.wire.WireContext;

/**
 * @author Guillaume Porcher
 *
 */
public class EnvWireTest extends WireTestCase {

  public void testEnvironmentWire(){
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
      "<environment-scopes>" +
      "  <process-engine-context /> " +
      "  <transaction-context>" +
      "    <env-ref name='e' />" +
      "  </transaction-context>" +
      "</environment-scopes>"
    );

    EnvironmentImpl environment = environmentFactory.openEnvironment();

    try{
      Object e = environment.get("e");

      assertNotNull(e);
      assertEquals(environment, e);
      assertEquals(e, ((EnvironmentImpl) e).get("e"));
    } finally {
      environment.close();
    }
    environmentFactory.close();
  }

  public void testEnvironmentFactoryWire(){
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
        "<environment-scopes>" +
        "  <process-engine-context />" +
        "  <transaction-context>" +
        "    <process-engine-ref name='f' />" +
        "  </transaction-context>" +
        "</environment-scopes>"
      );

      EnvironmentImpl environment = environmentFactory.openEnvironment();

      try {
        Object f = environment.get("f");

        assertNotNull(f);
        assertEquals(environmentFactory, f);
      } finally {
        environment.close();
      }
      environmentFactory.close();
  }

  public void testContextRefEnvironmentWire(){
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
        "<jbpm-configuration>" +
        "  <process-engine-context />" +
        "  <transaction-context>" +
        "    <context-ref name='c' />" +
        "  </transaction-context>" +
        "</jbpm-configuration>"
      );

      EnvironmentImpl environment = environmentFactory.openEnvironment();

      try {
        Object c = environment.get("c");

        assertNotNull(c);
        assertEquals(WireContext.class, c.getClass());
        assertEquals(environment.getContext("transaction"), c);
      } finally {
        environment.close();
      }
      environmentFactory.close();
  }

  public void testContextRefEnvironmentFactoryWire(){
    EnvironmentFactory environmentFactory = ProcessEngineImpl.parseXmlString(
        "<environment-scopes>" +
        "  <process-engine-context>" +
        "    <context-ref name='c' />" +
        "  </process-engine-context>" +
        "  <environment/>" +
        "</environment-scopes>"
      );

      EnvironmentImpl environment = environmentFactory.openEnvironment();

      try {
        Object c = environment.get("c");

        assertNotNull(c);
        assertEquals(WireContext.class, c.getClass());
        assertEquals(environment.getContext("process-engine"), c);
      } finally {
        environment.close();
      }
      environmentFactory.close();
  }
}

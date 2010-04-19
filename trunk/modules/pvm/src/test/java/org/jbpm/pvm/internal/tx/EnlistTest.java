package org.jbpm.pvm.internal.tx;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireException;
import org.jbpm.pvm.internal.wire.WireTestCase;

/**
 * @author Guillaume Porcher
 */
public class EnlistTest extends WireTestCase {

  public void setUp() throws Exception {
    super.setUp();
    MyResource.events = new ArrayList<String>();
  }

  public void tearDown() throws Exception {
    MyResource.events = null;
    super.tearDown();
  }
  
  public static class MyResource implements StandardResource {
    public static List<String> events = null;
    public void commit() { events.add("commit"); }
    public void flush() { events.add("flush"); }
    public void prepare() { events.add("prepare"); }
    public void rollback() { events.add("rollback"); }
  }

  public void testEnlist() {
    EnvironmentImpl environment = TxTests.openEnvironment(
      "<jbpm-configuration>" +
      "  <process-engine/>"+
      "  <transaction-context>" +
      "    <transaction name='tx' />" +
      "    <object name='o' class='"+ MyResource.class.getName()+"'>" +
      "      <enlist transaction='tx'/>" +
      "    </object>" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );

    try {
      Object o = environment.get("o");
      assertNotNull(o);
      StandardTransaction t = (StandardTransaction) environment.get("tx");
      t.begin();
      assertNotNull(t);
      assertTrue(t.getResources().contains(o));
      assertEquals(MyResource.events.toString(), 0, MyResource.events.size());
      t.complete();
      assertEquals(MyResource.events.toString(), 2, MyResource.events.size());
      assertEquals("prepare", MyResource.events.get(0));
      assertEquals("commit", MyResource.events.get(1));
    } finally {
      environment.close();
    }
  }

  public void testEnlistRollback() {
    EnvironmentImpl environment = TxTests.openEnvironment(
      "<jbpm-configuration>" +
      "  <process-engine/>"+
      "  <transaction-context>" +
      "    <transaction name='tx' />" +
      "    <object name='o' class='"+ MyResource.class.getName()+"'>" +
      "      <enlist transaction='tx'/>" +
      "    </object>" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );

    try {
      Object o = environment.get("o");
      assertNotNull(o);
      StandardTransaction t = (StandardTransaction) environment.get("tx");
      t.begin();
      assertNotNull(t);
      assertTrue(t.getResources().contains(o));
      assertEquals(MyResource.events.toString(), 0, MyResource.events.size());
      t.setRollbackOnly();
      t.complete();
      assertEquals(MyResource.events.toString(), 1, MyResource.events.size());
      assertEquals("rollback", MyResource.events.get(0));
    } finally {
      environment.close();
    }
  }

  public void testEnlistInOtherContext() {
    EnvironmentImpl environment = TxTests.openEnvironment(
      "<jbpm-configuration>" +
      "  <process-engine/>"+
      "  <transaction-context>" +
      "    <transaction name='tx' />" +
      "    <object name='o' class='"+ MyResource.class.getName()+"'>" +
      "      <enlist transaction='tx'/>" +
      "    </object>" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );

    try {
      WireContext context = createWireContext("<objects>" + " <object name='o' class='" + MyResource.class.getName() + "'>" + "  <enlist transaction='tx'/>"
              + " </object>" + "</objects>");
      environment.setContext(context);
      Object o = environment.get("o");
      assertNotNull(o);
      StandardTransaction t = (StandardTransaction) environment.get("tx");
      t.begin();
      assertNotNull(t);
      assertTrue(t.getResources().contains(o));
      assertEquals(MyResource.events.toString(), 0, MyResource.events.size());
      t.complete();
      assertEquals(MyResource.events.toString(), 2, MyResource.events.size());
      assertEquals("prepare", MyResource.events.get(0));
      assertEquals("commit", MyResource.events.get(1));
    } finally {
      environment.close();
    }
  }

  public void testEnlistInOtherContextWithRollback() {
    EnvironmentImpl environment = TxTests.openEnvironment(
      "<jbpm-configuration>" +
      "  <process-engine/>"+
      "  <transaction-context>" +
      "    <transaction name='tx' />" +
      "    <object name='o' class='"+ MyResource.class.getName()+"'>" +
      "      <enlist transaction='tx'/>" +
      "    </object>" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );

    try {
      WireContext context = createWireContext("<objects>" + " <object name='o' class='" + MyResource.class.getName() + "'>" + "  <enlist transaction='tx'/>"
              + " </object>" + "</objects>");
      environment.setContext(context);
      Object o = environment.get("o");
      assertNotNull(o);
      StandardTransaction t = (StandardTransaction) environment.get("tx");
      t.begin();
      assertNotNull(t);
      assertTrue(t.getResources().contains(o));
      assertEquals(MyResource.events.toString(), 0, MyResource.events.size());
      t.setRollbackOnly();
      t.complete();
      assertEquals(MyResource.events.toString(), 1, MyResource.events.size());
      assertEquals("rollback", MyResource.events.get(0));
    } finally {
      environment.close();
    }
  }

  public void testEnlistNotAResource() {
    EnvironmentImpl environment = TxTests.openEnvironment(
      "<jbpm-configuration>" +
      "  <process-engine/>"+
      "  <transaction-context>" +
      "    <transaction name='transaction' />" +
      "    <object name='o' class='java.lang.Object'>" +
      "      <enlist transaction='transaction'/>" +
      "    </object>" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );
    
    try{
      environment.get("o");
      fail("expected exeption");
    } catch (WireException e) {
      assertTextPresent("couldn't initialize object 'o':", e.getMessage());
      assertTextPresent("operation enlist can only be applied on objects that implement org.jbpm.pvm.internal.tx.StandardResource", e.getMessage());
    } finally {
      environment.close();
    }
  }

  public void testEnlistNotATransaction() {
    EnvironmentImpl environment = TxTests.openEnvironment(
      "<jbpm-configuration>" +
      "  <process-engine/>"+
      "  <transaction-context>" +
      "    <object name='o' class='"+MyResource.class.getName()+"'>" +
      "      <enlist transaction='tx'/>" +
      "    </object>" +
      "    <object name='tx' class='java.lang.Object'/>" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );

    try{
      environment.get("o");
      fail("expected exeption");
    } catch (WireException e) {
      assertTextPresent("couldn't initialize object 'o':", e.getMessage());
      assertTextPresent("couldn't find org.jbpm.pvm.internal.tx.StandardTransaction 'tx' to enlist resource ", e.getMessage());
    } finally {
      environment.close();
    }
  }

  public void testMissingTransactionName() {
    EnvironmentImpl environment = TxTests.openEnvironment(
      "<jbpm-configuration>" +
      "  <process-engine/>"+
      "  <transaction-context>" +
      "    <transaction />" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );

    try{
      assertNotNull(environment.get(Transaction.class));
      assertNotNull(environment.get(StandardTransaction.class));
    } finally {
      environment.close();
    }
  }
  
}

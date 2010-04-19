package org.jbpm.pvm.internal.wire;

import java.util.List;

import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireException;

/**
 * @author Guillaume Porcher
 */
public class DelayedInitTest extends WireTestCase {

  public static class NeedInitClass {
    private List<?> l;

    public int getSize() {
      return l.size();
    }
  }

  public void testMethodAndDelayedInit() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+NeedInitClass.class.getName()+"'>" +
          "<field name='l'>" +
          "   <list /> " +
          "</field>" +
      "  </object>" +
      "  <object name='q' factory='o' method='getSize' />" +
      "</objects>"
    );
    Object q = wireContext.get("q");

    assertNotNull(q);
    assertEquals(Integer.class, q.getClass());
  }

  public void testFactoryAndDelayedInit() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='q' method='getSize'>" +
      "    <factory>" +
      "      <object name='o' class='"+NeedInitClass.class.getName()+"'>" +
      "        <field name='l'>" +
      "          <list /> " +
      "        </field>" +
      "      </object>" +
      "    </factory>" +
      "  </object>" +
      "</objects>"
    );
    Object q = wireContext.get("q");

    assertNotNull(q);
    assertEquals(Integer.class, q.getClass());
  }

  public void testRefAndDelayedInit() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='o' class='"+NeedInitClass.class.getName()+"'>" +
          "<field name='l'>" +
          "   <list /> " +
          "</field>" +
      "  </object>" +
      "  <object name='q' method='getSize'>" +
      "    <factory>" +
      "       <ref object='o' init='required'/>" +
      "    </factory>" +
      "  </object>" +
      "</objects>"
    );
    Object q = wireContext.get("q");

    assertNotNull(q);
    assertEquals(Integer.class, q.getClass());
  }

  public static class A {
    public B b;
  }

  public static class B {
    public A a;
  }

  public void testBidirectionnalInitDefault(){
    WireContext wireContext = createWireContext(
        "<objects>" +
        "  <object name='a' class='"+A.class.getName()+"'>" +
        "   <field name='b'>" +
        "     <ref object='b'/>" +
        "   </field>" +
        "  </object>" +
        "  <object name='b' class='"+B.class.getName()+"'>" +
        "    <field name='a'>" +
        "     <ref object='a'/>" +
        "    </field>" +
        "  </object>"+
        "</objects>"
      );
      Object b = wireContext.get("b");

      assertNotNull(b);
      assertEquals(B.class, b.getClass());
      Object a = ((B)b).a;
      assertNotNull(a);
      assertEquals(A.class, a.getClass());
      assertEquals(b, ((A)((B)b).a).b);
      assertEquals(a, wireContext.get("a"));
    }

  /**
   * In this test, "b" is created, and initialized.
   * During the initialization, "a" is created and initialized.
   * As "b" is already created and we don't require it to be initialized, "a" can be initialized and the test is OK.
   */
  public void testBidirectionnalInitARefRequired(){
    WireContext wireContext = createWireContext(
        "<objects>" +
        "  <object name='a' class='"+A.class.getName()+"'>" +
        "   <field name='b'>" +
        "     <ref object='b'/>" +
        "   </field>" +
        "  </object>" +
        "  <object name='b' class='"+B.class.getName()+"'>" +
        "    <field name='a'>" +
        "     <ref object='a' init='required'/>" +
        "    </field>" +
        "  </object>"+
        "</objects>"
      );
      Object b = wireContext.get("b");

      assertNotNull(b);
      assertEquals(B.class, b.getClass());
      Object a = ((B)b).a;
      assertNotNull(a);
      assertEquals(A.class, a.getClass());
      assertEquals(b, ((A)((B)b).a).b);
      assertEquals(a, wireContext.get("a"));
    }

  /**
   * In this test, "b" is created, and initialized.
   * During the initialization, "a" is created and initialized.
   * As "b" is already created and we don't require it to be initialized,
   * "a" can be initialized and the test is OK.
   */
  public void testBidirectionnalInitARequired(){
    WireContext wireContext = createWireContext(
        "<objects>" +
        "  <object name='a' class='"+A.class.getName()+"' init='required'>" +
        "   <field name='b'>" +
        "     <ref object='b'/>" +
        "   </field>" +
        "  </object>" +
        "  <object name='b' class='"+B.class.getName()+"'>" +
        "    <field name='a'>" +
        "     <ref object='a'/>" +
        "    </field>" +
        "  </object>"+
        "</objects>"
      );
      Object b = wireContext.get("b");

      assertNotNull(b);
      assertEquals(B.class, b.getClass());
      Object a = ((B)b).a;
      assertNotNull(a);
      assertEquals(A.class, a.getClass());
      assertEquals(b, ((A)((B)b).a).b);
      assertEquals(a, wireContext.get("a"));
    }

  /**
   * Circular dependencies during initialization of 'b'
   */
  public void testBidirectionnalInitAandBRequired(){
    WireContext wireContext = createWireContext(
        "<objects>" +
        "  <object name='a' class='"+A.class.getName()+"'>" +
        "   <field name='b'>" +
        "     <ref object='b' init='required'/>" +
        "   </field>" +
        "  </object>" +
        "  <object name='b' class='"+B.class.getName()+"'>" +
        "    <field name='a'>" +
        "     <ref object='a' init='required'/>" +
        "    </field>" +
        "  </object>"+
        "</objects>"
      );

    try {
      wireContext.get("b");
      fail("expected exception");
    } catch(WireException e) {
      assertTextPresent("circular dependency", e.getMessage());
    }
  }

}

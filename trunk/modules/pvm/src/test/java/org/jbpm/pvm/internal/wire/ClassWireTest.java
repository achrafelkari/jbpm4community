package org.jbpm.pvm.internal.wire;

import java.util.List;

import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.xml.Problem;

/**
 * Tests for the ClassDescriptor
 *
 * @author Guillaume Porcher
 *
 */
public class ClassWireTest extends WireTestCase {

  /**
   * Tests if a valid definition works.
   */
  public void testClass(){
    WireContext wireContext = createWireContext(
        "<objects>" +
        "  <class name='o' class-name='"+ClassWireTest.class.getName()+"' />" +
        "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals(ClassWireTest.class, o);
  }

  public void testNoClassName() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <class name='o'/>" +
      "</objects>"
    );
    assertNotNull(problems);
    assertEquals(1, problems.size());
    assertTextPresent("class must have classname attribute: ", problems.get(0).getMsg());
  }
  /**
   * Tests the error raised when the class is not found.
   */
  public void testClassNotFound(){
    String className = "i am not a valid class";
    WireContext wireContext = createWireContext(
        "<objects>" +
        "  <class name='o' class-name='"+ className +"' />" +
        "</objects>"
    );
    try {
      wireContext.get("o");
      fail("expected exception");
    } catch (WireException e) {
      assertTextPresent(className, e.getMessage());
    }
  }

  public class InnerClass {
  }

  /**
   * Tests if inner class loading works.
   */
  public void testInnerClass(){
    WireContext wireContext = createWireContext(
        "<objects>" +
        "  <class name='o' class-name='"+ InnerClass.class.getName() +"' />" +
        "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals(InnerClass.class, o);
  }

  /**
   * Tests if loading a class defined in another package works.
   */
  public void testExternalClass() {
    WireContext wireContext = createWireContext(
        "<objects>" +
        "  <class name='o' class-name='"+EnvironmentImpl.class.getName()+"' />" +
        "</objects>"
    );

    Object o = wireContext.get("o");

    assertNotNull(o);
    assertEquals(org.jbpm.pvm.internal.env.EnvironmentImpl.class, o);
  }

}

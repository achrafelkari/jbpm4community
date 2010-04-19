package org.jbpm.pvm.internal.wire;

import java.util.List;
import java.util.Set;

import org.jbpm.pvm.internal.xml.Problem;

public class SetWireTest extends WireTestCase {

  public void testSet() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <set name='s'>" +
      "    <true />" +
      "    <string value='' />" +
      "    <null />" +
      "    <object name='o' class='"+Object.class.getName()+"' />" +
      "  </set>" +
      "</objects>"
    );
    
    Set s = (Set) wireContext.get("s");
    assertEquals(s.toString(), 4, s.size());
    assertTrue(s.contains(Boolean.TRUE));
    assertTrue(s.contains(""));
    assertTrue(s.contains(null));
    
    Object o = wireContext.get("o");
    assertNotNull(o);
    assertTrue(s.contains(o));
  }

  public void testCustomListType() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <set name='s' class='java.util.TreeSet' />" +
      "</objects>"
    );
    assertEquals(java.util.TreeSet.class, wireContext.get("s").getClass());
  }

  public void testInvalidSetType() {
    List<Problem> problems = parseProblems(
        "<objects>" +
        "  <set name='s' class='invalid-set-type'/>" +
        "</objects>"
    );

    assertTextPresent("class invalid-set-type could not be found", problems.get(0).getMsg());
  }

  public void testUnknownValue() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <set name='s'>" +
      "    <unknown-descriptor />" +
      "  </set>" +
      "</objects>"
    );
    assertNotNull(problems);
    assertTextPresent("unrecognized element: <unknown-descriptor", problems.get(0).getMsg());
  }

}

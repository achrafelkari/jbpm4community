package org.jbpm.pvm.internal.wire;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jbpm.pvm.internal.xml.Problem;

/**
 * @author Tom Baeyens
 * @author Guillaume Porcher
 */
public class MapWireTest extends WireTestCase {

  public void testMap() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <map name='m'>" +
      "    <entry>" +
      "      <key>" +
      "        <true/>" +
      "      </key>" +
      "      <value>" +
      "        <null/>" +
      "      </value>" +
      "    </entry>" +
      "    <entry>" +
      "      <key>" +
      "        <string value='a'/>" +
      "      </key>" +
      "      <value>" +
      "        <long value='5'/>" +
      "      </value>" +
      "    </entry>" +
      "  </map>" +
      "</objects>"
    );

    Map m = (Map) wireContext.get("m");
    assertEquals(m.toString(), 2, m.size());
    assertNull(m.get(Boolean.TRUE));
    assertEquals(new Long(5), m.get("a"));
  }

  public void testCustomMapType() {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <map name='m' class='java.util.TreeMap' />" +
      "</objects>"
    );
    assertEquals(TreeMap.class, wireContext.get("m").getClass());
  }

  public void testInvalidMapType() {
    List<Problem> problems = parseProblems(
        "<objects>" +
        "  <map name='m' class='invalid-map-type'/>" +
        "</objects>"
    );

    assertTextPresent("class invalid-map-type could not be found", problems.get(0).getMsg());
  }

  public void testMapWithNonEntries() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <map name='m'>" +
      "    <true />" +
      "    <string value='' />" +
      "    <null />" +
      "  </map>" +
      "</objects>"
    );
    assertNotNull(problems);
    assertEquals(problems.toString(), 3, problems.size());
    assertTextPresent("map can only contain entry elements", problems.get(0).getMsg());
    assertTextPresent("map can only contain entry elements", problems.get(1).getMsg());
    assertTextPresent("map can only contain entry elements", problems.get(2).getMsg());
  }

  public void testMapWithBadEntries() {
    List<Problem> problems = parseProblems(
      "<objects>" +
      "  <map name='m'>" +
      "    <entry/>" +
      "    <entry><key><null/></key></entry>" +
      "    <entry><value><null/></value></entry>" +
      "    <entry><key/><value><null/></value></entry>" +
      "  </map>" +
      "</objects>"
    );
    assertNotNull(problems);
    assertEquals(problems.toString(), 4, problems.size());
    assertTextPresent("entry must have key and value element with a single descriptor as contents", problems.get(0).getMsg());
    assertTextPresent("entry must have key and value element with a single descriptor as contents", problems.get(1).getMsg());
    assertTextPresent("entry must have key and value element with a single descriptor as contents", problems.get(2).getMsg());
    assertTextPresent("entry must have key and value element with a single descriptor as contents", problems.get(3).getMsg());
  }
}

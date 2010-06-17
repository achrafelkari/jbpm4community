package org.jbpm.jpdl.parsing;

import java.util.List;

import org.jbpm.pvm.internal.xml.Problem;

/**
 * JBPM-2849.
 * @author Huisheng Xu
 */
public class CacheParsingTest extends JpdlParseTestCase {

  public void testCacheDisabled() {
    String xmlString = "<process name='MyProsess' version='1' xmlns='http://jbpm.org/jpdl/4.4'>\n"
      + "  <start name='start'>\n"
      + "    <transition name='myTransition' to='end'>\n"
      + "      <event-listener class='com.comp.MyEventListener' cache='disabled'/>\n"
      + "    </transition>\n"
      + "  </start>\n"
      + "  <end name='end'/>\n"
      + "</process>\n";

    List<Problem> problems = jpdlParser.createParse()
      .setString(xmlString)
      .execute()
      .getProblems();
    assertEquals(0, problems.size());
  }

  public void testCachePowered() {
    String xmlString = "<process name='MyProsess' version='1' xmlns='http://jbpm.org/jpdl/4.4'>\n"
      + "  <start name='start'>\n"
      + "    <transition name='myTransition' to='end'>\n"
      + "      <event-listener class='com.comp.MyEventListener' cache='powered'/>\n"
      + "    </transition>\n"
      + "  </start>\n"
      + "  <end name='end'/>\n"
      + "</process>\n";

    List<Problem> problems = jpdlParser.createParse()
      .setString(xmlString)
      .execute()
      .getProblems();
    assert !problems.isEmpty();
  }
}

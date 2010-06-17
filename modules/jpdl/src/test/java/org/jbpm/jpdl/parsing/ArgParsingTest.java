package org.jbpm.jpdl.parsing;

import java.util.List;

import org.jbpm.pvm.internal.xml.Problem;

/**
 * JBPM-2715.
 * @author Huisheng Xu
 */
public class ArgParsingTest extends JpdlParseTestCase {

  public void testArgOneValue() {
    String xmlString = "<process name='MyProcess' version='1' xmlns='http://jbpm.org/jpdl/4.4'>\n"
      + "  <start name='start'>\n"
      + "    <transition name='myTransition' to='end'>\n"
      + "      <event-listener class='com.comp.MyEventListener'>\n"
      + "        <constructor>\n"
      + "          <arg type='java.lang.String'><string value='hello world'/></arg>\n"
      + "        </constructor>\n"
      + "      </event-listener>\n"
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

  public void testArgTwoValues() {
    String xmlString = "<process name='MyProcess' version='1' xmlns='http://jbpm.org/jpdl/4.4'>\n"
      + "  <start name='start'>\n"
      + "    <transition name='myTransition' to='end'>\n"
      + "      <event-listener class='com.comp.MyEventListener'>\n"
      + "        <constructor>\n"
      + "          <arg type='java.lang.String'>\n"
      + "            <string value='hello world'/>\n"
      + "            <string value='goodbye world'/>\n"
      + "          </arg>\n"
      + "        </constructor>\n"
      + "      </event-listener>\n"
      + "    </transition>\n"
      + "  </start>\n"
      + "  <end name='end'/>\n"
      + "</process>\n";

    List<Problem> problems = jpdlParser.createParse()
      .setString(xmlString)
      .execute()
      .getProblems();
    assertEquals(1, problems.size());
  }
}

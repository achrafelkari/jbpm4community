package org.jbpm.jpdl.parsing;

import java.util.List;

import org.jbpm.pvm.internal.xml.Problem;

/**
 * JBPM-2715.
 * @author Huisheng Xu
 */
public class ArgParsingTest extends JpdlParseTestCase {

  public void testArgOneValue() {
    String xmlString = "<process name='MyProcess' version='1' xmlns='http://jbpm.org/4.4/jpdl'>"
      + "  <start name='start'>"
      + "    <transition name='myTransition' to='end'>"
      + "      <event-listener class='com.comp.MyEventListener'>"
      + "        <constructor>"
      + "          <arg type='java.lang.String'><string value='hello world'/></arg>"
      + "        </constructor>"
      + "      </event-listener>"
      + "    </transition>"
      + "  </start>"
      + "  <end name='end'/>"
      + "</process>";

    List<Problem> problems = jpdlParser.createParse()
      .setString(xmlString)
      .execute()
      .getProblems();
    assertEquals(0, problems.size());
  }

  public void testArgTwoValues() {
    String xmlString = "<process name='MyProcess' version='1' xmlns='http://jbpm.org/4.4/jpdl'>\n"
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
      + "</process>";

    List<Problem> problems = jpdlParser.createParse()
      .setString(xmlString)
      .execute()
      .getProblems();
    assertEquals(1, problems.size());
    assertEquals("xml validation error: cvc-complex-type.2.4.d: "
      + "Invalid content was found starting with element 'string'. "
      + "No child element is expected at this point. [line=8 column=44 ]: "
      + "org.xml.sax.SAXParseException: cvc-complex-type.2.4.d: "
      + "Invalid content was found starting with element 'string'. "
      + "No child element is expected at this point.", problems.get(0).toString());
  }
}

package org.jbpm.jpdl.parsing;

import java.util.List;

import org.jbpm.pvm.internal.xml.Problem;

/**
 * JBPM-2843.
 * @author Huisheng Xu
 */
public class MailParsingTest extends JpdlParseTestCase {

  public void testMailParse() {
    String xmlString = "<process name='MyProsess' version='1' xmlns='http://jbpm.org/jpdl/4.4'>"
      + "  <start name='start'>"
      + "    <transition to='mail'/>"
      + "  </start>"
      + "  <mail name='mail' lang='java' class='org.MyMailProducer'>"
      + "    <transition to='end'/>"
      + "  </mail>"
      + "  <end name='end'/>"
      + "</process>";

    List<Problem> problems = jpdlParser.createParse()
      .setString(xmlString)
      .execute()
      .getProblems();
    assertEquals(0, problems.size());
  }
}

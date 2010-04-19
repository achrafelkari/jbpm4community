/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jbpm.jpdl.internal.convert;

import java.io.InputStream;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.jbpm.jpdl.internal.xml.JpdlParser;
import org.jbpm.pvm.internal.env.EnvironmentFactory;
import org.jbpm.pvm.internal.processengine.ProcessEngineImpl;
import org.jbpm.pvm.internal.xml.Problem;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.InputSource;

/**
 * Test the jpdl3Converter
 *
 */
public class Jpdl3ConverterReaderTest {
	@Test
	public void testSimpleProcesss() throws Exception {
		testConvert("simple.xml");
	}

	@Test
	public void testBusinessTrip() throws Exception {
		setUpEnviroment();
		testConvert("businesstrip.xml");
	}

	@Test
	public void testAssignment() throws Exception {
		testConvert("assignment.xml");
	}

	@Test
	public void testEvent() throws Exception {
		setUpEnviroment();
		testConvert("process-event.xml");
	}

	@Test
	public void testDescision() throws Exception {
		testConvert("testDecision.xml");
	}

	@Test
	public void testProcessState() throws Exception {
		Document convertedDoc = convert("process-state.xml");
		Element ele = convertedDoc.getRootElement();
		String subProcessKey = ele.element("sub-process").attributeValue("sub-process-key");
		Assert.assertEquals("interview", subProcessKey);
		validate(convertedDoc);
		
	}

	@Test
	public void testScript() throws Exception {
		setUpEnviroment();
		Document doc = convert("script.xml");
		Element stateEle = doc.getRootElement().element("state");
		Assert.assertEquals("async", stateEle.attributeValue("continue"));
		validate(doc);
	}

	@Test
	// Unsupported exception handler conversion test
	public void testExceptionHandler() throws Exception {
		InputStream inputStream = getClass().getClassLoader()
				.getResourceAsStream("exception-handler.xml");
		// Convert to process file to jpdl4
		InputSource ins = new InputSource(inputStream);
		Jpdl3Converter converter = new Jpdl3Converter(ins);
		Document doc = converter.readAndConvert();
		Assert.assertEquals(converter.problems.size(), 2);
		Assert.assertTrue(converter.problems.get(0).toString().indexOf("[WARNING] Unsupported exception handler conversion for element") > -1);
		Assert.assertTrue(converter.problems.get(1).toString().indexOf("[WARNING] Unsupported exception handler conversion for element") > -1);
	}

	@Test
	public void testSuperStateAndMailNode() throws Exception {
		InputStream inputStream = getClass().getClassLoader()
				.getResourceAsStream("superstate-mail.xml");
		// Convert to process file to jpdl4
		InputSource ins = new InputSource(inputStream);
		Jpdl3Converter converter = new Jpdl3Converter(ins);		
		Document doc = converter.readAndConvert();
		validate(doc);		
	}
	
	@Test
	public void testTransitionResolved() {
		InputStream inputStream = getClass().getClassLoader()
				.getResourceAsStream("test-transition-resolve.xml");
		// Convert to process file to jpdl4
		InputSource ins = new InputSource(inputStream);
		Jpdl3Converter converter = new Jpdl3Converter(ins);
		Document doc = converter.readAndConvert();
		Assert.assertEquals(converter.problems.size(), 2);
		for (org.jbpm.jpdl.internal.convert.problem.Problem pb : converter.problems) {
			System.out.println(pb);
		}
		Assert.assertTrue(converter.problems.get(0).toString().startsWith(
		"[WARNING] transition to='first2'"));
        Assert.assertTrue(converter.problems.get(1).toString().startsWith(
		"[WARNING] transition to='end2'"));
		
	}

	@Test
	public void testTimer() throws Exception {
		String xml = convert("timer.xml").asXML();
		List<Problem> problems = new JpdlParser().createParse().setString(xml)
				.execute().getProblems();
		Assert.assertEquals(2, problems.size());
		Assert.assertTrue(problems.get(0).getMsg().startsWith(
				"unrecognized event listener"));
		Assert.assertTrue(problems.get(1).getMsg().startsWith(
				"unrecognized event listener"));
	}
	
	@Test
	public void testMailNode() throws Exception {
		setUpEnviroment();
		String xml = convert("mail-node.xml").asXML();
		List<Problem> problems = new JpdlParser().createParse().setString(xml)
				.execute().getProblems();
        Assert.assertEquals(0, problems.size());
		
	}
	
	
	
	private void testConvert(String resourcefile) throws Exception {
		Document doc = convert(resourcefile);
		validate(doc);	
	}
	
	
	private Document convert(String resouceFile) throws Exception {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resouceFile);
		// Convert to process file to jpdl4
		InputSource ins = new InputSource(inputStream);
		Jpdl3Converter converter = new Jpdl3Converter(ins);
		Document doc = converter.readAndConvert();
		//System.out.println(doc.asXML());
		return doc;
	}
	
	private void validate(Document convertedDoc) throws Exception {
	  System.out.println(convertedDoc.asXML());
		List<Problem> problems = new JpdlParser().createParse().setString(convertedDoc.asXML()).execute().getProblems();
		Assert.assertEquals(problems.toString(), 0, problems.size());
	}
	
	private void setUpEnviroment() throws Exception {
		EnvironmentFactory environmentFactory = ProcessEngineImpl
				.parseXmlString("<jbpm-configuration>"
						+ "  <process-engine-context>"
						+ "    <script-manager default-expression-language='juel'"
						+ "                    default-script-language='juel'>"
						+ "      <script-language name='juel' factory='com.sun.script.juel.JuelScriptEngineFactory' />"
						+ "    </script-manager>"
						
						+ "    <mail-template name='my-template'> "
						+ "       <to addresses='${addressee}' />"
						+ "           <subject>rectify ${newspaper}</subject>"
						+ "           <text>${newspaper} ${date} ${details}</text>"
						+ "    </mail-template>"
						+ "    <mail-template name='task-notification'> "
						+ "       <to addresses='${addressee}' />"
						+ "           <subject>rectify ${newspaper}</subject>"
						+ "           <text>${newspaper} ${date} ${details}</text>"
						+ "    </mail-template>"
						+ "     <mail-template name='task-reminder'> "
                        + "       <to addresses='${addressee}' />"
                        + "           <subject>Task reminder</subject>"
                        + "           <text>Task reminder</text>"
                        + "    </mail-template>"
                        
						+ "  </process-engine-context> </jbpm-configuration>");

		environmentFactory.openEnvironment();
	}
}

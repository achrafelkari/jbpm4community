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

import java.io.File;
import java.io.PrintStream;
import java.net.URL;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test the command line parser and validation
 *
 */
public class JpdlConverterToolTest {
	
	JpdlConverterTool tool = new JpdlConverterTool();
	java.io.ByteArrayOutputStream bout = null;
	PrintStream oldOut = null;
	
	java.io.ByteArrayOutputStream errorOut = null;
	PrintStream oldError = null;
	
	@Before
	public void replaceSystemOut() {
		oldOut = System.out;
        bout = new java.io.ByteArrayOutputStream();		
		System.setOut(new java.io.PrintStream(bout));
		
		oldError = System.err;
		errorOut = new java.io.ByteArrayOutputStream();	
		System.setErr(new java.io.PrintStream(errorOut));
	}
	
	@After
	public void restoreSystemOut() {
		System.setErr(oldError);
		System.setOut(oldOut);		
	}
	
	@Test
	public void wrongArgs1() {
    	String args[] = {"-v","-t"};
		try {
			tool.parseParam(args);
			Assert.fail("No Illegal command exception thrown");
		} catch (Exception e) {
			//do nothing
		}
	}
	@Test
	public void wrongArgs2() {
		
		String args2[] = {"-v","-o"};
		try {
			tool.parseParam(args2);
			Assert.fail("No Illegal command exception thrown");
		} catch (Exception e) {
			//do nothing
		}
		
	}	
	@Test
	public void wrongArgs3() {
		String args3[] = {"-v","-o", "tmp.xml"};
		try {
			tool.parseParam(args3);
			Assert.fail("No Illegal command exception thrown");
		} catch (Exception e) {
			//do nothing
		}
	}
	
	@Test
	public void wrongArgs4() {	
		String args4[] = {"-o", "tmp.xml", "process.xml", "myprocess.xml"};
		try {
			tool.parseParam(args4);
			Assert.fail("No Illegal command exception thrown");
		} catch (Exception e) {
			//do nothing
		}	
		
	}
	
	
	@Test
	public void rightArgs() throws Exception {	
		String args4[] = {"-o", "tmp.xml", "process.xml"};
		tool.parseParam(args4);	
		String args5[] = {"process.xml"};
		tool.parseParam(args5);	
		String args6[] = {"-v","process.xml"};
		tool.parseParam(args6);
		String args7[] = {"-o","process.xml", "processed.xml"};
		tool.parseParam(args7);
	}
	
	
	@Test
	public void validateArgs1() throws Exception {	
		ConverterContext context = new ConverterContext();
		context.put(ConverterContext.PROCESS_FILE, "test.xml");
		try {
			tool.validate(context);
			Assert.fail("No Illegal command exception thrown");
		} catch (Exception e) {
			//
		}
	}
	
	@Test
	public void validateArgs2() throws Exception {
		String fileName = getResource("simple.xml");
		ConverterContext context = new ConverterContext();
		context.put(ConverterContext.PROCESS_FILE, fileName);
		context.put(ConverterContext.OUPUTFILE, "/mytest/histest");
		try {
			tool.validate(context);
			Assert.fail("No Illegal command exception thrown");
		} catch (Exception e) {
			//
		}
	}
	
	@Ignore
	//Test convert the process file under current directory
    //it will generate a tmp file under migration dir.
	//Enable this test until we find how to set the current directory
	public void runConcretProcess() throws Exception {
		String fileName = getResource("simple.xml");
		String args[] = new String[]{"-v",fileName};
		JpdlConverterTool.main(args);
		URL url = new URL(fileName);
		File tmpFile = new File(url.getFile());
		File destFile = new File(tmpFile.getParentFile(), "simple.converted.jpdl.xml");
		Assert.assertTrue(destFile.exists());		
	}
	
	@Test
	public void runConcretProcess2() throws Exception {
		String fileName = getResource("simple.xml");
		URL url = new URL(fileName);
		File tmpFile = new File(url.getFile());
		File outputfile = new File(tmpFile.getParentFile(), "tmp-output.xml");
			
		String args[] = new String[]{"-v","-o", outputfile.getAbsolutePath(),fileName};
		JpdlConverterTool.main(args);
	
		Assert.assertTrue(outputfile.exists());	
		Assert.assertTrue(outputfile.length() > 0);	
	}
	
	@Test
	public void testErrorMessage() throws Exception {
		String fileName = getResource("invalid.xml");
		String args[] = new String[]{fileName};			
		JpdlConverterTool.main(args);
		String str = new String(errorOut.toByteArray());		
		Assert.assertTrue(str.indexOf("[ERROR] cvc-complex-type.2.4.a:") > -1);				
	}
	
	
    public String getResource(String file) throws Exception {
    	URL url = getClass().getClassLoader().getResource(file);
    	return url.toString();
    }
}

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
import java.io.FileWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jbpm.jpdl.internal.convert.exception.IllegalCommandException;
import org.jbpm.jpdl.internal.convert.problem.Problem;

/**
 * Conversion command line tool main class.
 * 
 * @author Jim Ma
 */
public class JpdlConverterTool {   
  
	public static void main(String[] args) {
	    JpdlConverterTool jpdlConverterTool = new JpdlConverterTool();
	  
	    // Parse and validate the command line arguments
		ConverterContext context = null;
		try {
			context = jpdlConverterTool.parseParam(args);
			jpdlConverterTool.validate(context);
		} catch (IllegalCommandException e) {
			System.err.println(e.getMessage());
			System.err.println(jpdlConverterTool.getUsage());
			return;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return;
		}
		
		
		boolean verbose = false;
		if (context.get(ConverterContext.VERBOSE) != null) {
			verbose = true;
		}
		Jpdl3Converter jpdlConverter = new Jpdl3Converter((URL)context.get(context.PROCESS_FILE_URL));
		
		try {
			if (verbose) {
				System.out.println("Loading process file from URL [" + context.get(context.PROCESS_FILE_URL) + "]...");
			}
			Document jpdl4Doc = jpdlConverter.readAndConvert();
			
			if (verbose) {
				System.out.println("Converting the process file to jPDL4 version....");
			}			
			
			String outputFilePath = (String)context.get(context.OUPUTFILE);
			File outputFile = new File(outputFilePath);
			
			//well print xml to file
			Writer fileWriter = new FileWriter(outputFile);
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter( fileWriter, format );
			writer.write(jpdl4Doc);
			writer.close();

			
			if (verbose) {
				System.out.println("Generating converted file to:" + (outputFile.isAbsolute() ? outputFile.getAbsolutePath() : outputFile.getName()));
			}
			
		} catch (Exception e) {

	      for (Problem problem : jpdlConverter.problems) {
				if (problem.getLevel() == problem.LEVEL_WARNING) {
					System.err.println(problem);
				}
				if (problem.getLevel() < problem.LEVEL_WARNING) {
					System.err.println(problem);
					if (problem.getException() != null
							&& context.get(ConverterContext.VERBOSE) != null) {
						problem.getException().printStackTrace(System.err);
						System.err.println();
					}
				}
			}
			
			
		}
	}

	/**
	 * Parses the command line arguments and returns a context 
	 * in which the conversion will happen.
	 */
	public ConverterContext parseParam(String[] args) throws IllegalCommandException {
		ConverterContext context = new ConverterContext();
		
		if (args.length == 0) {
			throw new IllegalCommandException("No file for conversion is supplied");
		}
		
		for (int i = 0; i < args.length; i++) {
			String token = args[i];
			if ("-v".equalsIgnoreCase(token) || "-verbose".equalsIgnoreCase(token)) {
				context.put(ConverterContext.VERBOSE, ConverterContext.VERBOSE);
			} else if ("-o".equalsIgnoreCase(token)
					|| "-output".equalsIgnoreCase(token)) {
				int j = ++i;
				if (j < args.length) {
					String outputFile = args[j];					
					context.put(ConverterContext.OUPUTFILE, args[j]);
				} else {
					throw new IllegalCommandException("Missing output file name");
				}
			} else if (token.startsWith("-")) {
				throw new IllegalCommandException("Unknow flag [" + token +"]");
		    } else if(context.get(ConverterContext.PROCESS_FILE) == null) {
				context.put(ConverterContext.PROCESS_FILE, token);
			} else {
				throw new IllegalCommandException("Duplicate input process file");
			}

		}
		if (context.get(ConverterContext.PROCESS_FILE) == null) {
			throw new IllegalCommandException("No input process file defined");
		}

		return context;
	}

	public void validate(ConverterContext context) throws Exception {
		String processFile = (String) context.get(ConverterContext.PROCESS_FILE);
		URL processURL = getFileURL(processFile);
		if (processURL == null) {
			throw new IllegalCommandException("Failed to load the process file [" + processFile + "]");
		}
	    context.put(context.PROCESS_FILE_URL, processURL);
	    //handle the output filename
	    if (context.get(ConverterContext.OUPUTFILE) == null) {
	    	File tmpFile = new File(processURL.getFile());
	    	String fileName = tmpFile.getName();
	    	String baseName = fileName;
	    	
	    	int index = fileName.lastIndexOf(".");
	    	
	    	if (index > -1) {
	    		baseName = fileName.substring(0, index);
	    	} else {
	    		baseName = fileName;
	    	}
	    	
	    	String outputFileName =  baseName + ".converted.jpdl.xml";
	        File outFile = new File(".", outputFileName);
	        context.put(ConverterContext.OUPUTFILE, outFile.getAbsolutePath());
	    } else {
	    	String outputFile = (String)context.get(ConverterContext.OUPUTFILE);
	    	File file = new File(outputFile);
			if (file.isAbsolute()) {
				if (!file.getParentFile().exists()) {
					throw new IllegalCommandException("Output directory [" + file.getParent() + "] does not exist");
				}
			} else {
			    file = new File(".", outputFile);			    
			}
			if (!file.exists()) {
					file.createNewFile();				
			}
			context.put(ConverterContext.OUPUTFILE, file.getAbsolutePath());
	    }
		
	}

	public URL getFileURL(String fileName) {
		try {
			URL url = new URL(fileName);
			return url;
		} catch (MalformedURLException e1) {
			//Do nothing
		}
		File file = new File(fileName);
		if (file.exists()) {
			try {
				return file.toURL();
			} catch (MalformedURLException e) {
				// Do nothing.
			}
			
		}
		// load in current directory
		File tmpFile = new File(".", fileName);
		if (file.exists()) {
			try {
				return tmpFile.toURL();
			} catch (MalformedURLException e) {
				// Do nothing
			}
		}
		
		return null;
	}
	
	public String getUsage() {
		return "Usage : java org.jbpm.jpdl.internal.convert.JpdlConverterTool <file>\r\n"
		 +     "        java org.jbpm.jpdl.internal.convert.JpdlConverterTool -v -o <outputfile> <file>";
		
	}
	
}

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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Wraps the validated command line arguments 
 * and determines the context of the conversion.
 * 
 * @author Jim Ma
 */
public class ConverterContext {
  
	public static final String PROCESS_FILE = "processFile"; 
	public static final String PROCESS_FILE_URL = "processFileURL";
	public static final String VERBOSE = "verbose";
	public static final String OUPUTFILE = "outputFile";
	private Map<String, Object> paramMap = null;
	
	public void put(String key, Object value) {
		if (paramMap == null) {
			paramMap = new ConcurrentHashMap<String, Object>();
		}
		paramMap.put(key, value);
	}
	
	public Object get(String key) {
		if (paramMap == null) {
			return null;
		}
		return paramMap.get(key);
		
	}
		
	public void refresh() {
		paramMap = null;
	}
	
}

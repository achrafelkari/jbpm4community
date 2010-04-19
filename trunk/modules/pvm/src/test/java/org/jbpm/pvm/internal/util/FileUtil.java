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
package org.jbpm.pvm.internal.util;

import java.io.File;
import java.net.URL;
import java.util.StringTokenizer;


/**
 * @author Tom Baeyens
 */
public abstract class FileUtil {

  private static final String FILE_SEPARATOR = System.getProperty("file.separator");

  public static String getFileNameForResource(String resource) throws Exception {
    String testClassesUrl = FileUtil.class.getProtectionDomain().getCodeSource().getLocation().toString();
    URL fileUrl = new URL(testClassesUrl);
    File file = new File(fileUrl.toURI());
    String fileName = file.getAbsolutePath();

    StringTokenizer tokenizer = new StringTokenizer(resource, "/");
    while (tokenizer.hasMoreTokens()) {
      fileName = fileName+FILE_SEPARATOR+tokenizer.nextToken(); 
    }
    
    return fileName; 
  }
}

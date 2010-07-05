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
package org.jbpm.cactustool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Tom Baeyens
 */
public class CactusTestGenerator {

  private static final String excludedTests = System.getProperty("excluded.tests");
  private static final String suiteTests = System.getProperty("suite.tests");

  public static void main(String[] args) throws IOException {
    if (args == null) {
      log("syntax: java -cp ... org.jbpm.cactustool.CactusTestGenerator testdestroot testsrcroot1 testsrcroot2 ...");
    }

    if (excludedTests != null) {
      log("################################################################################################");
      log("# excluded tests: " + excludedTests);
      log("################################################################################################");
    }

    String testPackageSourceDir = args[0] + "/org/jbpm/test";
    new File(testPackageSourceDir).mkdirs();

    String testFileName = testPackageSourceDir + "/AllIntegrationTests.java";
    File testFile = new File(testFileName);
    FileOutputStream fos = new FileOutputStream(testFile);
    PrintWriter out = new PrintWriter(fos);
    log("generating java class " + testFile);

    out.println("package org.jbpm.test;");
    out.println();
    out.println("import junit.framework.Test;");
    out.println("import junit.framework.TestCase;");
    out.println("import org.apache.cactus.ServletTestSuite;");
    out.println();
    out.println("public class AllIntegrationTests extends TestCase {");
    out.println();
    out.println("  public static Test suite() {");
    out.println("    ServletTestSuite suite = new ServletTestSuite();");

    for (int i = 1; i < args.length; i++) {
      String testSrcRoot = args[i];
      scanForTestClasses(testSrcRoot, "", out);
    }

    out.println("    return suite;");
    out.println("  }");
    out.println("}");

    out.close();
  }

  private static void scanForTestClasses(String dirPath, String packageName, PrintWriter out)
    throws IOException {
    File dirFile = new File(dirPath);
    if (".svn".equals(dirFile.getName()))
      return;

    log("scanning dir " + dirFile.getAbsolutePath());
    File[] dirContentFiles = dirFile.listFiles();
    if (dirContentFiles != null) {
      for (File file : dirContentFiles) {
        String fileName = file.getName();

        if (file.isFile() && fileName.endsWith("Test.java")
          && (excludedTests == null || !excludedTests.contains(fileName))) {
          String className = packageName + "." + fileName.substring(0, fileName.length() - 5);
          log("  adding " + className);
          if (suiteTests == null || !suiteTests.contains(fileName)) {
            out.println("    suite.addTestSuite(" + className + ".class);");
          }
          else {
            out.println("    suite.addTest(" + className + ".suite());");
          }
        }
        else if (file.isDirectory()) {
          String subDirPath = dirPath + "/" + fileName;
          String subPackageName = "".equals(packageName) ? fileName : packageName + "."
            + fileName;
          scanForTestClasses(subDirPath, subPackageName, out);
        }
      }
    }
  }

  private static void log(String msg) {
    System.out.println(msg);
  }
}

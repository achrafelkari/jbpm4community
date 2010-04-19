package org.jbpm.test.reporting;/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

import junit.framework.TestCase;

import org.hibernate.cfg.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.sql.*;

/**
 * Parse the birt report templates, extract SQL statements and execute against database.
 * 
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class SQLStmtTest extends TestCase
{
  private static final String OVERALL_ACTIVITY_RPTDESIGN = "overall_activity.rptdesign";

  private static final String PROCESS_SUMMARY_RPTDESIGN = "process_summary.rptdesign";

  private Connection conn;

  static boolean skipTests;

  protected void setUp() throws Exception
  {
    // hibernate configuration is leveraged to parse the 
    // properties as plain jdk dom parsing results in 
    // in an exception when running without internet connection 
    // when the hibernate dtd being retrieved from the url, 
    Properties jdbcProps = new Configuration()
      .configure("jbpm.hibernate.cfg.xml")
      .getProperties();

    // create connection
    Class.forName(jdbcProps.getProperty("hibernate.connection.driver_class"));
    this.conn = DriverManager.getConnection(
        jdbcProps.getProperty("hibernate.connection.url"),
        jdbcProps.getProperty("hibernate.connection.username"),
        jdbcProps.getProperty("hibernate.connection.password")
    );
  }


  protected void tearDown() throws Exception
  {
    if(this.conn!=null)
    {
      conn.close();
    }
  }

  public void testOverallActivityReport_Statements() throws Exception
  {   
    InputStream in =
        Thread.currentThread().getContextClassLoader()
            .getResourceAsStream(OVERALL_ACTIVITY_RPTDESIGN);
    assertNotNull("Unable to find report:"+OVERALL_ACTIVITY_RPTDESIGN, in);

    Document doc = parseTemplate(in);

    List<Element> queryTextElements = getQueryTextElements(doc);
    assertFalse("No query strings found in template", queryTextElements.isEmpty());

    for(Element query : queryTextElements)
    {
      executeQuery(query.getTextContent());
    }
  }

  public void testProcessSummaryReport_Statements() throws Exception
  {
    if(skipTests) return;
    
    InputStream in =
        Thread.currentThread().getContextClassLoader()
            .getResourceAsStream(PROCESS_SUMMARY_RPTDESIGN);
    assertNotNull("Unable to find report:"+PROCESS_SUMMARY_RPTDESIGN, in);

    Document doc = parseTemplate(in);

    List<Element> queryTextElements = getQueryTextElements(doc);
    assertFalse("No query strings found in template", queryTextElements.isEmpty());

    for(Element query : queryTextElements)
    {
      executeQuery(query.getTextContent());
    }
  }

  private void executeQuery(String sql) throws SQLException
  {

    System.out.println("Execute:");
    System.out.println(sql);

    PreparedStatement stmt = conn.prepareStatement(sql);
    ParameterMetaData metaData = stmt.getParameterMetaData();
    for(int i=1; i<metaData.getParameterCount()+1; i++)
    {
      stmt.setString(i, "PLACEHOLDER_STRING");
    }

    // run it
    ResultSet rs = stmt.executeQuery();
    System.out.println("");
    System.out.println("Result size:" +rs.getFetchSize());
    System.out.println("--");
  }
  
  private List<Element> getQueryTextElements(Document doc)
  {
    List<Element> props = new ArrayList<Element>();
    dfsElementSearch(props, doc.getDocumentElement(),
        new Filter()
        {
          public boolean select(Element candidate)
          {
            return candidate.getNodeName().equals("property")
                && DOMUtils.getAttributeValue(candidate, "name").equals("queryText");
          }
        });
    return props;
  }

  private Document parseTemplate(InputStream in)
      throws ParserConfigurationException, SAXException, IOException
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(false);
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(in);
    return doc;
  }

  private static void dfsElementSearch(List<Element> resultBuffer, Element root, Filter filter)
  {
    if( filter.select(root) )
    {
      resultBuffer.add(root);
      return;
    }

    List<Element> children = getChildElements(root);
    for(Element child : children)
    {
      dfsElementSearch(resultBuffer, child, filter);
    }
  }

  private static List<Element> getChildElements(Node node)
  {
    List<Element> list = new LinkedList<Element>();
    NodeList nlist = node.getChildNodes();
    for (int i = 0; i < nlist.getLength(); i++)
    {
      Node child = nlist.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE)
      {
        list.add((Element)child);
      }
    }
    return list;
  }

  interface Filter
  {
    boolean select(Element candidate);
  }


}

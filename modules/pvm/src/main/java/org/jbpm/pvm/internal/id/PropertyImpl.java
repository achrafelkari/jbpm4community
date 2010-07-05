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
package org.jbpm.pvm.internal.id;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.history.model.HistoryActivityInstanceImpl;
import org.jbpm.pvm.internal.history.model.HistoryDetailImpl;
import org.jbpm.pvm.internal.history.model.HistoryTaskImpl;
import org.jbpm.pvm.internal.history.model.HistoryVariableImpl;
import org.jbpm.pvm.internal.identity.impl.GroupImpl;
import org.jbpm.pvm.internal.identity.impl.MembershipImpl;
import org.jbpm.pvm.internal.identity.impl.UserImpl;
import org.jbpm.pvm.internal.job.JobImpl;
import org.jbpm.pvm.internal.lob.Lob;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.processengine.ProcessEngineImpl;
import org.jbpm.pvm.internal.repository.DeploymentImpl;
import org.jbpm.pvm.internal.repository.DeploymentProperty;
import org.jbpm.pvm.internal.task.ParticipationImpl;
import org.jbpm.pvm.internal.task.SwimlaneImpl;
import org.jbpm.pvm.internal.task.TaskImpl;
import org.jbpm.pvm.internal.type.Variable;

/** jbpm installation properties.
 * 
 * currently there are 2 use cases for these properties:
 *  1) include the jbpm schema version into the DB
 *  2) have a record to maintain the next dbid for the id generator
 *   
 * @author Tom Baeyens
 */
public class PropertyImpl {
  
  private static Log log = Log.getLog(PropertyImpl.class.getName());
  
  public static final String DB_VERSION_KEY = "db.version";
  public static final String NEXT_DBID_KEY = "next.dbid";

  protected int version;
  protected String key;
  protected String value;
  
  protected PropertyImpl() {
  }

  public PropertyImpl(String key, String value) {
    this.key = key;
    this.value = value;
  }
  
  public static Long getNextDbid(Session session) {
    String dbidPropertyValue = getPropertyValue(session, PropertyImpl.NEXT_DBID_KEY);
    if (dbidPropertyValue!=null) {
      return Long.valueOf(dbidPropertyValue);
    }
    return null;
  }

  public static void setNextDbid(Session session, long nextDbid) {
    setPropertyValue(session, PropertyImpl.NEXT_DBID_KEY, Long.toString(nextDbid));
  }

  public static String getDbVersion(Session session) {
    String dbVersionPropertyValue = getPropertyValue(session, PropertyImpl.DB_VERSION_KEY);
    if (dbVersionPropertyValue!=null) {
      return dbVersionPropertyValue;
    }
    return null;
  }

  public static void setDbVersionToLibraryVersion(Session session) {
    setPropertyValue(session, PropertyImpl.DB_VERSION_KEY, ProcessEngineImpl.JBPM_LIBRARY_VERSION);
  }

  public static void createProperties(Session session) {
    setDbVersionToLibraryVersion(session);
    setNextDbid(session, 1);
  }

  public static void initializeNextDbid(Session session) {
    long nextDbid = getMaxDbid(session)+1;
    setNextDbid(session, nextDbid);
    log.info("nextDbid is initialized to "+nextDbid);
  }
  
  public static boolean propertiesTableExists(Session session) {
    try {
      session.createQuery("from "+PropertyImpl.class.getName())
        .setMaxResults(1)
        .uniqueResult();
      return true;
      
    } catch (Exception e) {
      return false;
    }
  }

  protected static long getMaxDbid(Session session) {
    long maxDbid = 0;
    
    List<String> persistedTypes = new ArrayList<String>();
    persistedTypes.add(DeploymentImpl.class.getName());
    persistedTypes.add(DeploymentProperty.class.getName());
    persistedTypes.add(ExecutionImpl.class.getName());
    persistedTypes.add(GroupImpl.class.getName());
    persistedTypes.add(HistoryActivityInstanceImpl.class.getName());
    persistedTypes.add(HistoryDetailImpl.class.getName());
    persistedTypes.add(HistoryTaskImpl.class.getName());
    persistedTypes.add(HistoryVariableImpl.class.getName());
    persistedTypes.add(JobImpl.class.getName());
    persistedTypes.add(Lob.class.getName());
    persistedTypes.add(MembershipImpl.class.getName());
    persistedTypes.add(ParticipationImpl.class.getName());
    persistedTypes.add(SwimlaneImpl.class.getName());
    persistedTypes.add(TaskImpl.class.getName());
    persistedTypes.add(UserImpl.class.getName());
    persistedTypes.add(Variable.class.getName());
    
    for (String persistedType: persistedTypes) {
      try {
        Long typeMaxDbid = (Long) session.createQuery(
                "select max(o.dbid) " +
                "from "+persistedType+" as o"
            ).uniqueResult();
        
        if ( (typeMaxDbid!=null)
             && (typeMaxDbid.longValue()>maxDbid) 
           ) {
          maxDbid = typeMaxDbid.longValue();
        }
      } catch (Exception e) {
        log.info("couldn't get max dbid for "+persistedType, e);
        e.printStackTrace();
      }
    }

    return maxDbid;
  }

  protected static String getPropertyValue(Session session, String propertyKey) {
    PropertyImpl dbidProperty = getProperty(session, propertyKey);
    if (dbidProperty==null) {
      return null;
    }
    return dbidProperty.getValue();
  }

  protected static void setPropertyValue(Session session, String propertyKey, String propertyValue) {
    PropertyImpl property = getProperty(session, propertyKey);
    if (property==null) {
      property = new PropertyImpl(propertyKey, propertyValue);
      session.save(property);
    } else {
      property.setValue(propertyValue);
    }
  }

  protected static PropertyImpl getProperty(Session session, String key) {
    return (PropertyImpl) session.get(PropertyImpl.class, key);
  }

  public static void setDbVersionTo41(Session session) {
    setPropertyValue(session, DB_VERSION_KEY, "4.1");
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getKey() {
    return key;
  }
  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }
}

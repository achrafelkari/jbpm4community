package org.jbpm.pvm.internal.hibernate;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import org.jbpm.api.Execution;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;
import org.jbpm.pvm.internal.util.ReflectUtil;

public class ExecutionType implements UserType, ParameterizedType {

  private static final long serialVersionUID = 1L;
  private static final Log log = Log.getLog(ExecutionType.class.getName());
  
  int[] sqlTypes = new int[]{Types.VARCHAR};
  
  protected String processResource;
  protected ProcessDefinitionImpl processDefinition;

  public void setParameterValues(Properties properties) {
  }

  public int[] sqlTypes() {
    return sqlTypes;
  }

  // JDBC - object translation 
  
  public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
    ExecutionImpl execution = createExecution(owner);
    String activityName = rs.getString(names[0]);
    ActivityImpl activity = execution.getProcessDefinition().getActivity(activityName);
    execution.setActivity(activity);
    execution.setState(Execution.STATE_ACTIVE_ROOT);
    return execution;
  }

  private ExecutionImpl createExecution(Object owner) {
    ExecutionImpl execution = new ExecutionImpl();
    execution.setProcessDefinition(getProcessDefinition(owner));
    return execution;
  }

  private synchronized ProcessDefinitionImpl getProcessDefinition(Object owner) {
    if (processDefinition!=null) {
      return processDefinition;
    }
    Class<?> ownerClass = owner.getClass();
    try {
      Method method = ownerClass.getDeclaredMethod("getProcessDefinition");
      processDefinition = (ProcessDefinitionImpl) ReflectUtil.invoke(method, null, null);
    } catch (Exception e) {
      throw new RuntimeException("couldn't get process definition for "+owner, e);
    }
    
    return processDefinition;
  }

  public void nullSafeSet(PreparedStatement st, Object owner, int index) throws HibernateException, SQLException {
    if (owner!=null) {
      ExecutionImpl execution = (ExecutionImpl) owner;
      String activityName = execution.getActivity().getName();
      log.trace("binding 'execution-state{"+activityName+"}' to parameter: "+index);
      st.setString(index, activityName);
    }
  }

  // for dirty checking ?

  public Object deepCopy(Object object) throws HibernateException {
    if (object==null) {
      return null;
    }
    
    ExecutionImpl original = (ExecutionImpl) object;

    ActivityImpl activity = new ActivityImpl();
    activity.setName(original.getActivity().getName());
    
    ExecutionImpl copy = new ExecutionImpl();
    copy.setActivity(activity);

    return copy;
  }

  public boolean equals(Object arg0, Object arg1) throws HibernateException {
    if ( (arg0==null) || (arg1==null)) return false;
    
    ExecutionImpl execution0 = (ExecutionImpl) arg0;
    ExecutionImpl execution1 = (ExecutionImpl) arg1;
    
    String activityName0 = execution0.getActivity().getName();
    String activityName1 = execution1.getActivity().getName();
    
    return activityName0.equals(activityName1);
  }

  public int hashCode(Object object) throws HibernateException {
    return object.hashCode();
  }

  public boolean isMutable() {
    return true;
  }

  public Class<?> returnedClass() {
    return Execution.class;
  }

  // merge functionality //////////////////////////////////////////////////////
  
  public Object replace(Object arg0, Object arg1, Object arg2) throws HibernateException {
    return null;
  }

  // serialization for cache //////////////////////////////////////////////////

  public Object assemble(Serializable arg0, Object arg1) throws HibernateException {
    return null;
  }

  public Serializable disassemble(Object arg0) throws HibernateException {
    return null;
  }
}

package org.jbpm.pvm.internal.job;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.job.Job;
import org.jbpm.pvm.internal.lob.Lob;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.wire.Descriptor;

public abstract class JobImpl implements Command<Boolean>, Serializable, Job {

  private static final long serialVersionUID = 1L;
  // private static final DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss,SSS");

  public static final String STATE_WAITING = "waiting";
  public static final String STATE_ACQUIRED = "acquired";
  public static final String STATE_ERROR = "error";
  public static final String STATE_SUSPENDED = "suspended";

  protected long dbid;
  protected int dbversion;

  /** date until which the command should not be executed
   * for async messages, this due date should be set to null. */
  protected Date dueDate;
  
  /** job state. */
  protected String state = STATE_WAITING;

  /** the execution (if any) for this job */  
  protected ExecutionImpl execution;

  /** the process instance */  
  protected ExecutionImpl processInstance;
  
  // execution members

  /** specifies if this jobImpl can be executed concurrently with other jobs for the 
   * same execution. */
  protected boolean isExclusive;

  /** name of the jobImpl executor name that has locked this jobImpl. */
  protected String lockOwner;

  /** the time the lock on this jobImpl expires. */
  protected Date lockExpirationTime;

  /** stack trace of the exception that occurred during command execution. */
  protected String exception;
  
  /** number of attempts left to try.  Should be decremented each time an exception 
   * occurs during command execution. */
  protected int retries = 3;
  
  protected Lob configurationBytes;

  protected Object configuration;
  
  /** a command that can be used as the behaviour of this job */ 
  protected Descriptor commandDescriptor;

  public JobImpl() {
  }

  public void setExecution(ExecutionImpl execution) {
    this.execution = execution;
    this.processInstance = execution.getProcessInstance();
  }
  
  public void acquire(String lockOwner, Date lockExpirationTime) {
    this.state = STATE_ACQUIRED;
    this.lockOwner = lockOwner;
    this.lockExpirationTime = lockExpirationTime;
  }
  public void release() {
    this.state = STATE_WAITING;
    this.lockOwner = null;
    this.lockExpirationTime = null;
  }
  public void setRetries(int retries) {
    this.retries = retries;
    if (this.retries==0) {
      this.state = STATE_ERROR;
    }
  }
  public void suspend() {
    this.state = STATE_SUSPENDED;
  }
  public void resume() {
    if (retries==0) {
      this.state = STATE_ERROR;
    } else {
      this.state = STATE_WAITING;
    }
  }
  
  public Object getConfiguration() {
    if ( (configuration==null)
         && (configurationBytes!=null)
       ) {
      try {
        byte[] bytes = configurationBytes.extractBytes();
        ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectStream = new ObjectInputStream(byteStream);
        configuration = objectStream.readObject();
      } catch (Exception e) {
        throw new JbpmException("couldn't deserialize configuration object for "+this, e);
      }
    }
    return configuration;
  }
  
  public void setConfiguration(Object configuration) {
    this.configuration = configuration;
    
    try {
      ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
      ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
      objectStream.writeObject(configuration);
      byte[] bytes = byteStream.toByteArray();
      configurationBytes = new Lob(bytes, true);
    } catch (Exception e) {
      throw new JbpmException("couldn't serialize configuration object for "+this, e);
    }
  }

  // customized getters and setters ///////////////////////////////////////////
  
  public String getId() {
    return Long.toString(dbid);
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  public long getDbid() {
    return dbid;
  }
  public void setDbid(long id) {
	  dbid = id;
  }
  public String getLockOwner() {
    return lockOwner;
  }
  public Date getDueDate() {
    return dueDate;
  }
  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }
  @Deprecated
  public Date getDuedate() {
    return getDueDate();
  }
  /** @deprecated call {@link #setDueDate(Date)} instead */
  @Deprecated
  public void setDuedate(Date duedate) {
    setDueDate(duedate);
  }
  public String getException() {
    return exception;
  }
  public int getRetries() {
    return retries;
  }
  public boolean isExclusive() {
    return isExclusive;
  }
  public ExecutionImpl getExecution() {
    return execution;
  }
  public void setExclusive(boolean isExclusive) {
    this.isExclusive = isExclusive;
  }
  public void setLockOwner(String jobExecutorName) {
    this.lockOwner = jobExecutorName;
  }
  public ExecutionImpl getProcessInstance() {
    return processInstance;
  }
  public void setException(String exception) {
    this.exception = exception;
  }
  public Date getLockExpirationTime() {
    return lockExpirationTime;
  }
  public void setLockExpirationTime(Date lockExpirationTime) {
    this.lockExpirationTime = lockExpirationTime;
  }
  public Descriptor getCommandDescriptor() {
    return commandDescriptor;
  }
  public void setCommandDescriptor(Descriptor commandDescriptor) {
    this.commandDescriptor = commandDescriptor;
  }
}

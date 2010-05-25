package org.jbpm.pvm.internal.util;

import java.util.Date;

public class Clock {
  
  private Clock() {
    // prevent instantiation
  }
  
  protected static Date explicitTime = null;
  
  public static Date getTime() {
    if (explicitTime==null) {
      return new Date();
    }
    return explicitTime;
  }
  
  public static void setExplicitTime(Date explicitTime) {
    Clock.explicitTime = explicitTime;
  }
  
  public static Date getExplicitTime() {
    return explicitTime;
  }
}

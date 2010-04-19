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
package org.jbpm.pvm.internal.cal;

import java.io.Serializable;
import java.util.Date;

/**
 * identifies a continuous set of days.
 */
public class Holiday implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  protected long oid = -1;
  protected int version = 0;
  protected Date fromDay = null;
  protected Date toDay = null;
  protected BusinessCalendarImpl businessCalendarImpl = null;

  /*
  public static List parseHolidays(Properties calendarProperties, BusinessCalendar businessCalendar) {
    List holidays = new ArrayList();
    
    DateFormat dateFormat = new SimpleDateFormat(calendarProperties.getProperty("day.format"));
    Iterator iter = calendarProperties.keySet().iterator();
    while (iter.hasNext()) {
      String key = (String) iter.next();
      if (key.startsWith("holiday")) {
        Holiday holiday = new Holiday(calendarProperties.getProperty(key), dateFormat, businessCalendar);
        holidays.add(holiday);
      }
    }

    return holidays;
  }

  public Holiday(String holidayText, DateFormat dateFormat, BusinessCalendar businessCalendar) {
    this.businessCalendar = businessCalendar;
    try {
      int separatorIndex = holidayText.indexOf('-');
      if (separatorIndex==-1) {
        fromDay = dateFormat.parse(holidayText.trim());
        toDay = fromDay;
      } else {
        String fromText = holidayText.substring(0, separatorIndex).trim();
        String toText = holidayText.substring(separatorIndex+1).trim();
        fromDay = dateFormat.parse(fromText);
        toDay = dateFormat.parse(toText);
      }
      // now we are going to set the toDay to the end of the day, rather then the beginning.
      // we take the start of the next day as the end of the toDay.
      Calendar calendar = businessCalendar.createCalendar();
      calendar.setTime(toDay);
      calendar.add(Calendar.DATE, 1);
      toDay = calendar.getTime();
      
    } catch (ParseException e) {
      throw new PvmException("couldn't parse holiday '"+holidayText+"'", e);
    }
  }
  */

  public boolean includes(Date date) {
    return ( (fromDay.getTime()<=date.getTime())
             && (date.getTime()<toDay.getTime())
           );
  }
  public BusinessCalendarImpl getBusinessCalendar() {
    return businessCalendarImpl;
  }
  public void setBusinessCalendar(BusinessCalendarImpl businessCalendarImpl) {
    this.businessCalendarImpl = businessCalendarImpl;
  }
  public Date getFromDay() {
    return fromDay;
  }
  public void setFromDay(Date fromDay) {
    this.fromDay = fromDay;
  }
  public Date getToDay() {
    return toDay;
  }
  public void setToDay(Date toDay) {
    this.toDay = toDay;
  }
}

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
import java.util.Calendar;
import java.util.Date;

/**
 * is part of a day that can for example be used to represent business hours. 
 *
 */
public class DayPart implements Serializable {

  private static final long serialVersionUID = 1L;
  
  protected long oid = -1;
  protected int version = 0;
  protected int fromHour = -1;
  protected int fromMinute = -1;
  protected int toHour = -1;
  protected int toMinute = -1;
  protected Day day = null;
  protected int index = -1;

  public Date add(Date date, long millis, boolean isBusinessTime) {
    Date end = null;
    
    BusinessCalendarImpl businessCalendarImpl = day.getBusinessCalendar();
    Calendar calendar = businessCalendarImpl.createCalendar();
    calendar.setTime(date);
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    int minute = calendar.get(Calendar.MINUTE);

    long dateMilliseconds = ((hour*60)+minute)*60*1000;
    long dayPartEndMilleseconds = ((toHour*60)+toMinute)*60*1000;
    long millisecondsInThisDayPart = dayPartEndMilleseconds - dateMilliseconds;
    
    if (millis <= millisecondsInThisDayPart) {
      end = new Date( date.getTime() + millis);
    } else {
      long remainderMillis = millis - millisecondsInThisDayPart;
      Date dayPartEndDate = new Date(date.getTime() + millis - remainderMillis);
      
      Object[] result = new Object[2];
      day.findNextDayPartStart(index+1, dayPartEndDate, result);
      Date nextDayPartStart = (Date) result[0];
      DayPart nextDayPart = (DayPart) result[1];
      
      end = nextDayPart.add(nextDayPartStart, remainderMillis, isBusinessTime);
    }
    
    return end;
  }
  
  public boolean isStartAfter(Date date) {
    Calendar calendar = day.getBusinessCalendar().createCalendar();
    calendar.setTime(date);
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    int minute = calendar.get(Calendar.MINUTE);
    
    return ( (hour<fromHour)
             || ( (hour==fromHour)
                  && (minute<=fromMinute) 
                ) 
           );
  }


  public boolean includes(Date date) {
    Calendar calendar = day.getBusinessCalendar().createCalendar();
    calendar.setTime(date);
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    int minute = calendar.get(Calendar.MINUTE);
    
    return ( ( (fromHour<hour)
               || ( (fromHour==hour)
                   && (fromMinute<=minute) 
                 )
             ) &&
             ( (hour<toHour)
               || ( (hour==toHour)
                    && (minute<=toMinute) 
                  )
             )
           );
  }

  public Date getStartTime(Date date) {
    Calendar calendar = day.getBusinessCalendar().createCalendar();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, fromHour);
    calendar.set(Calendar.MINUTE, fromMinute);
    return calendar.getTime();
  }
  
  public Day getDay() {
    return day;
  }
  public void setDay(Day day) {
    this.day = day;
  }
  public int getFromHour() {
    return fromHour;
  }
  public void setFromHour(int fromHour) {
    this.fromHour = fromHour;
  }
  public int getFromMinute() {
    return fromMinute;
  }
  public void setFromMinute(int fromMinute) {
    this.fromMinute = fromMinute;
  }
  public int getIndex() {
    return index;
  }
  public void setIndex(int index) {
    this.index = index;
  }
  public int getToHour() {
    return toHour;
  }
  public void setToHour(int toHour) {
    this.toHour = toHour;
  }
  public int getToMinute() {
    return toMinute;
  }
  public void setToMinute(int toMinute) {
    this.toMinute = toMinute;
  }
}

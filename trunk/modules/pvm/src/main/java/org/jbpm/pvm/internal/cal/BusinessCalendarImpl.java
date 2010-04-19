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
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.jbpm.api.JbpmException;
import org.jbpm.pvm.internal.util.Clock;

/**
 * a calendar that knows about business hours.
 */
public class BusinessCalendarImpl implements Serializable, BusinessCalendar {
  
  private static final long serialVersionUID = 1L;
  private static BusinessCalendarImpl instance = null;
  
  protected long oid = -1;
  protected int version = 0;
  protected TimeZone timeZone = TimeZone.getDefault();
  /** array that contains the weekdays in the index as specified by {@link Calendar#SUNDAY} (=1),
   * {@link Calendar#MONDAY} (=2),... {@link Calendar#SATURDAY} (=7).  
   */ 
  protected Day[] days = null;
  protected Holiday[] holidays = null;
  
  protected long secondInMillis = 1000; 
  protected long minuteInMillis = 60000; 
  protected long hourInMillis = 3600000; 
  protected long dayInMillis = 24*hourInMillis; 
  protected long weekInMillis = 7*dayInMillis; 
  protected long monthInMillis = 30*dayInMillis; 
  protected long yearInMillis = 365*dayInMillis; 

  protected long businessDayInMillis = 8*hourInMillis; 
  protected long businessWeekInMillis = 40*hourInMillis; 
  protected long businessMonthInMillis = 21*dayInMillis; 
  protected long businessYearInMillis = 220*dayInMillis; 
  
  /** constructor for persistence and creating an empty business calendar */
  public BusinessCalendarImpl() {
  }

  public static synchronized BusinessCalendarImpl getInstance() {
    if (instance==null) {
      instance = new BusinessCalendarImpl();
    }
    return instance;
  }

  public Date add(Date date, String duration) {
    return add(date, new Duration(duration));
  }
  
  public Date subtract(Date date, String duration) {
	if (duration.contains("business")) {
	  throw new JbpmException("Duedate subtraction not supported for business durations");
	}
    return subtract(date, new Duration(duration));
  }

  public Date subtract(Date date, Duration duration) {
	Date end = null;
    long millis = convertToMillis(duration);
    end = new Date(date.getTime()-millis);
    if (end.before(Clock.getTime())) {
    	throw new JbpmException("Duedate "+ end+ " in the past");
    }
    return end;
  }


  public Date add(Date date, Duration duration) {
    Date end = null;
    if (duration.isBusinessTime()) {
      DayPart dayPart = findDayPart(date);
      boolean isInbusinessHours = (dayPart!=null);
      if (! isInbusinessHours) {
        Object[] result = new Object[2];
        findDay(date).findNextDayPartStart(0, date, result);
        date = (Date) result[0];
        dayPart = (DayPart) result[1];
      }
      long millis = convertToMillis(duration);
      end = dayPart.add(date, millis, duration.isBusinessTime());
    } else {
      long millis = convertToMillis(duration);
      end = new Date(date.getTime()+millis);
    }
    return end;
  }

  public long convertToMillis(Duration duration){
    long millis = duration.getMillis();
    millis += duration.getSeconds() * secondInMillis;
    millis += duration.getMinutes() * minuteInMillis;
    millis += duration.getHours() * hourInMillis;
    if (duration.isBusinessTime()) {
      millis += duration.getDays() * businessDayInMillis;
      millis += duration.getWeeks() * businessWeekInMillis;
      millis += duration.getMonths() * businessMonthInMillis;
      millis += duration.getYears() * businessYearInMillis;
    } else {
      millis += duration.getDays() * dayInMillis;
      millis += duration.getWeeks() * weekInMillis;
      millis += duration.getMonths() * monthInMillis;
      millis += duration.getYears() * yearInMillis;
    }
    return millis;
  }

  public boolean isInBusinessHours(Date date) { 
    return (findDayPart(date)!=null); 
  } 

  public boolean isHoliday(Date date) {
    if (holidays!=null) {
      for(Holiday holiday: holidays) {
        if (holiday.includes(date)) {
          return true;
        }
      }
    }
    return false;
  }

  protected Date findStartOfNextDay(Date date) {
    Calendar calendar = createCalendar();
    calendar.setTime(date);
    calendar.add(Calendar.DATE, 1);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    date = calendar.getTime();
    while(isHoliday(date)) {
      calendar.setTime(date);
      calendar.add(Calendar.DATE, 1);
      date = calendar.getTime();
    }
    return date;
  }

  public Calendar createCalendar() {
    return new GregorianCalendar();
  }

  protected Day findDay(Date date) {
    Calendar calendar = createCalendar();
    calendar.setTime(date);
    int weekDayIndex = calendar.get(Calendar.DAY_OF_WEEK);
    return days[weekDayIndex];
  }

  protected DayPart findDayPart(Date date) {
    DayPart dayPart = null;
    if (! isHoliday(date)) {
      Day day = findDay(date);
      DayPart[] dayParts = day.getDayParts();
      if (dayParts!=null) {
        for (int i=0; ((i < dayParts.length) && (dayPart==null)); i++) {
          DayPart candidate = dayParts[i];
          if (candidate.includes(date)) {
            dayPart = candidate;
          }
        }
      }
    }
    return dayPart;
  }

  protected DayPart findNextDayPart(Date date) { 
    DayPart nextDayPart = null; 
    while(nextDayPart==null) { 
      nextDayPart = findDayPart(date); 
      if (nextDayPart==null) { 
        date = findStartOfNextDay(date); 
        Object result[] = new Object[2]; 
        Day day = findDay(date); 
        day.findNextDayPartStart(0, date, result); 
        nextDayPart = (DayPart) result[1]; 
      } 
    } 
    return nextDayPart; 
  }

  

  // getters and setters //////////////////////////////////////////////////////
  
  public long getBusinessDayInMillis() {
    return businessDayInMillis;
  }
  public void setBusinessDayInMillis(long businessDayInMillis) {
    this.businessDayInMillis = businessDayInMillis;
  }
  public long getBusinessMonthInMillis() {
    return businessMonthInMillis;
  }
  public void setBusinessMonthInMillis(long businessMonthInMillis) {
    this.businessMonthInMillis = businessMonthInMillis;
  }
  public long getBusinessWeekInMillis() {
    return businessWeekInMillis;
  }
  public void setBusinessWeekInMillis(long businessWeekInMillis) {
    this.businessWeekInMillis = businessWeekInMillis;
  }
  public long getBusinessYearInMillis() {
    return businessYearInMillis;
  }
  public void setBusinessYearInMillis(long businessYearInMillis) {
    this.businessYearInMillis = businessYearInMillis;
  }
  public long getDayInMillis() {
    return dayInMillis;
  }
  public void setDayInMillis(long dayInMillis) {
    this.dayInMillis = dayInMillis;
  }
  public Day[] getDays() {
    return days;
  }
  public void setDays(Day[] days) {
    this.days = days;
  }
  public Holiday[] getHolidays() {
    return holidays;
  }
  public void setHolidays(Holiday[] holidays) {
    this.holidays = holidays;
  }
  public long getHourInMillis() {
    return hourInMillis;
  }
  public void setHourInMillis(long hourInMillis) {
    this.hourInMillis = hourInMillis;
  }
  public long getMinuteInMillis() {
    return minuteInMillis;
  }
  public void setMinuteInMillis(long minuteInMillis) {
    this.minuteInMillis = minuteInMillis;
  }
  public long getMonthInMillis() {
    return monthInMillis;
  }
  public void setMonthInMillis(long monthInMillis) {
    this.monthInMillis = monthInMillis;
  }
  public long getSecondInMillis() {
    return secondInMillis;
  }
  public void setSecondInMillis(long secondInMillis) {
    this.secondInMillis = secondInMillis;
  }
  public TimeZone getTimeZone() {
    return timeZone;
  }
  public void setTimeZone(TimeZone timeZone) {
    this.timeZone = timeZone;
  }
  public long getWeekInMillis() {
    return weekInMillis;
  }
  public void setWeekInMillis(long weekInMillis) {
    this.weekInMillis = weekInMillis;
  }
  public long getYearInMillis() {
    return yearInMillis;
  }
  public void setYearInMillis(long yearInMillis) {
    this.yearInMillis = yearInMillis;
  } 
}

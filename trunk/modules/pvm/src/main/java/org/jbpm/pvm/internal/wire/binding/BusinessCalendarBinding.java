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
package org.jbpm.pvm.internal.wire.binding;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.jbpm.pvm.internal.cal.BusinessCalendarImpl;
import org.jbpm.pvm.internal.cal.Day;
import org.jbpm.pvm.internal.cal.DayPart;
import org.jbpm.pvm.internal.cal.Holiday;
import org.jbpm.pvm.internal.util.StringUtil;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.descriptor.ProvidedObjectDescriptor;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;
import org.w3c.dom.Element;


/**
 * @author Tom Baeyens
 */
public class BusinessCalendarBinding extends WireDescriptorBinding {
  
  public BusinessCalendarBinding() {
    super("business-calendar");
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    BusinessCalendarImpl businessCalendarImpl = new BusinessCalendarImpl();
    
    TimeZone timeZone = null;
    if (element.hasAttribute("timezone")) {
      timeZone = TimeZone.getTimeZone(element.getAttribute("timezone"));
    } else {
      timeZone = TimeZone.getDefault();
    }
    businessCalendarImpl.setTimeZone(timeZone);
    
    String hourFormatText = "HH:mm";
    if (element.hasAttribute("hour-format")) {
      hourFormatText = element.getAttribute("hour-format");
    }
    DateFormat hourFormat = new SimpleDateFormat(hourFormatText);

    Day[] days = new Day[8];
    days[Calendar.SUNDAY] = parseDay(element, "sunday", hourFormat, businessCalendarImpl, parse);
    days[Calendar.MONDAY] = parseDay(element, "monday", hourFormat, businessCalendarImpl, parse);
    days[Calendar.TUESDAY] = parseDay(element, "tuesday", hourFormat, businessCalendarImpl, parse);
    days[Calendar.WEDNESDAY] = parseDay(element, "wednesday", hourFormat, businessCalendarImpl, parse);
    days[Calendar.THURSDAY] = parseDay(element, "thursday", hourFormat, businessCalendarImpl, parse);
    days[Calendar.FRIDAY] = parseDay(element, "friday", hourFormat, businessCalendarImpl, parse);
    days[Calendar.SATURDAY] = parseDay(element, "saturday", hourFormat, businessCalendarImpl, parse);
    businessCalendarImpl.setDays(days);

    String dayFormatText = "dd/MM/yyyy";
    if (element.hasAttribute("day-format")) {
      dayFormatText = element.getAttribute("day-format");
    }
    DateFormat dayFormat = new SimpleDateFormat(dayFormatText);

    Holiday[] holidays = null;
    List<Element> holidayElements = XmlUtil.elements(element, "holiday");
    if (!holidayElements.isEmpty()) {
      holidays = new Holiday[holidayElements.size()];
      for (int i=0; i<holidayElements.size(); i++) {
        holidays[i] = parseHoliday(holidayElements.get(i), dayFormat, businessCalendarImpl, parse);
      }
    }
    businessCalendarImpl.setHolidays(holidays);
    
    ProvidedObjectDescriptor descriptor = new ProvidedObjectDescriptor(businessCalendarImpl, true);
    return descriptor;
  }

  private Day parseDay(Element daysElement, String dayText, DateFormat hourFormat, BusinessCalendarImpl businessCalendarImpl, Parse parse) {
    Day day = new Day();
    day.setBusinessCalendar(businessCalendarImpl);

    Element dayElement = XmlUtil.element(daysElement, dayText);
    if (dayElement!=null) {
      List<DayPart> dayParts = new ArrayList<DayPart>();
      
      if (dayElement.hasAttribute("hours")) {
        int dayPartIndex = 0;
        String hours = dayElement.getAttribute("hours");
        for (String part: StringUtil.tokenize(hours, "and")) {
          try {
            int separatorIndex = part.indexOf('-');
            if (separatorIndex==-1) throw new IllegalArgumentException("no dash (-)");
            String fromText = part.substring(0, separatorIndex).trim().toLowerCase(); 
            String toText = part.substring(separatorIndex+1).trim().toLowerCase();
            
            Date from = hourFormat.parse(fromText);
            Date to = hourFormat.parse(toText);
            
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(from);
            int fromHour = calendar.get(Calendar.HOUR_OF_DAY);
            int fromMinute = calendar.get(Calendar.MINUTE);

            calendar.setTime(to);
            int toHour = calendar.get(Calendar.HOUR_OF_DAY);
            if (toHour==0) {
              toHour=24;
            }
            int toMinute = calendar.get(Calendar.MINUTE);

            DayPart dayPart = new DayPart();
            dayPart.setDay(day);
            dayPart.setIndex(dayPartIndex);
            dayPart.setFromHour(fromHour);
            dayPart.setFromMinute(fromMinute);
            dayPart.setToHour(toHour);
            dayPart.setToMinute(toMinute);
            dayParts.add(dayPart);
            
          } catch(Exception e) {
            parse.addProblem(dayText+" has invalid hours part '"+part+"': "+e.getMessage(), dayElement);  
          }
          
          dayPartIndex++;
        }
        
      } else {
        parse.addProblem(dayText+" must have attribute 'hours'", dayElement);
      }
      
      DayPart[] dayPartArray = new DayPart[dayParts.size()];
      dayPartArray = dayParts.toArray(dayPartArray);
      day.setDayParts(dayPartArray);
    }
    return day;
  }
  
  private Holiday parseHoliday(Element holidayElement, DateFormat dayFormat, BusinessCalendarImpl businessCalendarImpl, Parse parse) {
    Holiday holiday = new Holiday();
    try {
      if (holidayElement.hasAttribute("period")) {
        String holidayPeriodText = holidayElement.getAttribute("period");
        
        int dashIndex = holidayPeriodText.indexOf('-');
        
        String fromDateText = null;
        String toDateText = null;
        if (dashIndex!=-1) {
          fromDateText = holidayPeriodText.substring(0, dashIndex).trim().toLowerCase(); 
          toDateText = holidayPeriodText.substring(dashIndex+1).trim().toLowerCase();
          
        } else {
          fromDateText = holidayPeriodText.trim().toLowerCase(); 
          toDateText = fromDateText;
        }

        
        Date fromDate = dayFormat.parse(fromDateText);
        holiday.setFromDay(fromDate);

        Date toDate = dayFormat.parse(toDateText);
        holiday.setToDay(toDate);
        
      } else {
        parse.addProblem("attribute 'period' in element business-calendar is required", holidayElement);
      }

      // now we are going to set the toDay to the end of the day, rather then the beginning.
      // we take the start of the next day as the end of the toDay.
      Calendar calendar = businessCalendarImpl.createCalendar();
      calendar.setTime(holiday.getToDay());
      calendar.add(Calendar.DATE, 1);
      Date toDay = calendar.getTime();
      holiday.setToDay(toDay);
      
    } catch (Exception e) {
      parse.addProblem("couldn't parse holiday: "+XmlUtil.toString(holidayElement), holidayElement);
    }

    return holiday;
  }
}

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
 * is a day on a business calendar.
 */
public class Day implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  protected long oid = -1;
  protected int version = 0;
  protected DayPart[] dayParts = null;
  protected BusinessCalendarImpl businessCalendarImpl = null;

  public void findNextDayPartStart(int dayPartIndex, Date date, Object[] result) {
    // if there is a day part in this day that starts after the given date
    if ( (dayParts!=null)
         && (dayPartIndex < dayParts.length)
       ) {
      if (dayParts[dayPartIndex].isStartAfter(date)) {
        result[0] = dayParts[dayPartIndex].getStartTime(date);
        result[1] = dayParts[dayPartIndex];
      } else {
        findNextDayPartStart(dayPartIndex+1, date, result);
      }
    } else {
      // descend recustively
      date = businessCalendarImpl.findStartOfNextDay(date);
      Day nextDay = businessCalendarImpl.findDay(date);
      nextDay.findNextDayPartStart(0, date, result);
    }
  }

  public BusinessCalendarImpl getBusinessCalendar() {
    return businessCalendarImpl;
  }
  public DayPart[] getDayParts() {
    return dayParts;
  }
  public void setBusinessCalendar(BusinessCalendarImpl businessCalendarImpl) {
    this.businessCalendarImpl = businessCalendarImpl;
  }
  public void setDayParts(DayPart[] dayParts) {
    this.dayParts = dayParts;
  }
}

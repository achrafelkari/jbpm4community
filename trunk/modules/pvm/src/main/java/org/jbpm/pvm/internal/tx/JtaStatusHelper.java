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
package org.jbpm.pvm.internal.tx;

import javax.transaction.Status;


/**
 * @author Tom Baeyens
 */
public abstract class JtaStatusHelper {

  public static String toString(int txStatus) {
    if (txStatus==Status.STATUS_ACTIVE) {
      return "STATUS_ACTIVE";
    } else if (txStatus==Status.STATUS_COMMITTED) {
      return "STATUS_COMMITTED";
    } else if (txStatus==Status.STATUS_COMMITTING) {
      return "STATUS_COMMITTING";
    } else if (txStatus==Status.STATUS_MARKED_ROLLBACK) {
      return "STATUS_MARKED_ROLLBACK";
    } else if (txStatus==Status.STATUS_NO_TRANSACTION) {
      return "STATUS_NO_TRANSACTION";
    } else if (txStatus==Status.STATUS_PREPARED) {
      return "STATUS_PREPARED";
    } else if (txStatus==Status.STATUS_PREPARING) {
      return "STATUS_PREPARING";
    } else if (txStatus==Status.STATUS_ROLLEDBACK) {
      return "STATUS_ROLLEDBACK";
    } else if (txStatus==Status.STATUS_ROLLING_BACK) {
      return "STATUS_ROLLING_BACK";
    } else if (txStatus==Status.STATUS_UNKNOWN) {
      return "STATUS_UNKNOWN";
    } else {
      return "unknown";
    }
  }
}

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

import org.jbpm.pvm.internal.svc.RetryInterceptor;
import org.jbpm.pvm.internal.tx.JtaRetryInterceptor;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.descriptor.ProvidedObjectDescriptor;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;
import org.w3c.dom.Element;

/** parses a descriptor for creating a {@link RetryInterceptor}.
 * 
 * See schema docs for more details.
 *
 * @author Tom Baeyens
 */
public class RetryInterceptorBinding extends WireInterceptorBinding {

  public RetryInterceptorBinding() {
    super("retry-interceptor");
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    RetryInterceptor retryInterceptor = null;
    
    String type = XmlUtil.attribute(element, "type");
    if ("jta".equals(type)) {
      retryInterceptor = new JtaRetryInterceptor();
    } else {
      retryInterceptor = new RetryInterceptor();
    }
    
    
    if (element.hasAttribute("retries")) {
      String retriesText = element.getAttribute("retries");
      try {
        int retries = Integer.parseInt(retriesText);
        retryInterceptor.setRetries(retries);
      } catch (NumberFormatException e) {
        parse.addProblem("couldn't parse retries "+retriesText, element);
      }
    }

    if (element.hasAttribute("delay")) {
      String delayText = element.getAttribute("delay");
      try {
        long delay = Long.parseLong(delayText);
        retryInterceptor.setDelay(delay);
      } catch (NumberFormatException e) {
        parse.addProblem("couldn't parse delay "+delayText, element);
      }
    }

    if (element.hasAttribute("delay-factor")) {
      String delayFactorText = element.getAttribute("delay-factor");
      try {
        long delayFactor = Long.parseLong(delayFactorText);
        retryInterceptor.setDelayFactor(delayFactor);
      } catch (NumberFormatException e) {
        parse.addProblem("couldn't parse delay-factor "+delayFactorText, element);
      }
    }

    return new ProvidedObjectDescriptor(retryInterceptor);
  }
}

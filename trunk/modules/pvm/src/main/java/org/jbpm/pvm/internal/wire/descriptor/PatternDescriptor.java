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
package org.jbpm.pvm.internal.wire.descriptor;

import java.util.regex.Pattern;

import org.jbpm.pvm.internal.wire.WireContext;

/**
 * @author Alejandro Guizar
 */
public class PatternDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;

  protected String regex;
  protected boolean literal;
  protected boolean canonEq;

  public PatternDescriptor() {
  }

  public PatternDescriptor(String regex) {
    this.regex = regex;
  }

  public boolean isLiteral() {
    return literal;
  }

  public void setLiteral(boolean literal) {
    this.literal = literal;
  }

  public boolean isCanonEq() {
    return canonEq;
  }

  public void setCanonEq(boolean canonEq) {
    this.canonEq = canonEq;
  }

  public Object construct(WireContext wireContext) {
    int flags = 0;
    if (literal) flags |= Pattern.LITERAL;
    if (canonEq) flags |= Pattern.CANON_EQ;
    return Pattern.compile(regex, flags);
  }

}

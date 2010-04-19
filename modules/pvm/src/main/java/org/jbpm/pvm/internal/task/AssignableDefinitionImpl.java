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
package org.jbpm.pvm.internal.task;

import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.model.ProcessElementImpl;
import org.jbpm.pvm.internal.util.EqualsUtil;
import org.jbpm.pvm.internal.wire.usercode.UserCodeReference;

/**
 * @author Tom Baeyens
 */
public class AssignableDefinitionImpl extends ProcessElementImpl {

  private static final long serialVersionUID = 1L;

  protected String name;
  protected Expression description;

  protected Expression assigneeExpression;
  protected Expression candidateUsersExpression;
  protected Expression candidateGroupsExpression;
  protected UserCodeReference assignmentHandlerReference;

  // equals ///////////////////////////////////////////////////////////////////
  // hack to support comparing hibernate proxies against the real objects
  // since this always falls back to ==, we don't need to overwrite the hashcode
  public boolean equals(Object o) {
    return EqualsUtil.equals(this, o);
  }

  // getters and setters //////////////////////////////////////////////////////

  public Expression getAssigneeExpression() {
    return assigneeExpression;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public Expression getDescription() {
    return description;
  }
  public void setDescription(Expression description) {
    this.description = description;
  }
  public void setAssigneeExpression(Expression assigneeExpression) {
    this.assigneeExpression = assigneeExpression;
  }
  public UserCodeReference getAssignmentHandlerReference() {
    return assignmentHandlerReference;
  }
  public void setAssignmentHandlerReference(UserCodeReference assignmentHandlerReference) {
    this.assignmentHandlerReference = assignmentHandlerReference;
  }
  public Expression getCandidateUsersExpression() {
    return candidateUsersExpression;
  }
  public void setCandidateUsersExpression(Expression candidateUsersExpression) {
    this.candidateUsersExpression = candidateUsersExpression;
  }
  public Expression getCandidateGroupsExpression() {
    return candidateGroupsExpression;
  }
  public void setCandidateGroupsExpression(Expression candidateGroupsExpression) {
    this.candidateGroupsExpression = candidateGroupsExpression;
  }
}

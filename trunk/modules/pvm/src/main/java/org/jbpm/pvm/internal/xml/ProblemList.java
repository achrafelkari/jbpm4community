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
package org.jbpm.pvm.internal.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jbpm.api.JbpmException;
import org.jbpm.pvm.internal.repository.DeploymentImpl;
import org.w3c.dom.Element;

/** list of problems.  Base class for {@link Parse} 
 * and {@link DeploymentImpl}.
 * 
 * @author Tom Baeyens
 */
public class ProblemList implements Serializable {

  private static final long serialVersionUID = 1L;

  static final String NEWLINE = System.getProperty("line.separator");

  protected List<Problem> problems;

  // problem constructor method ///////////////////////////////////////////////
  
  public void addProblem(String msg, Exception e, String severity, Element element) {
    ProblemImpl problem = new ProblemImpl(msg, e, severity);

    if (element!=null) {
      Integer line = (Integer) element.getUserData("line");
      if (line!=null) {
        problem.setLine(line);
      }
      Integer column = (Integer) element.getUserData("column");
      if (column!=null) {
        problem.setColumn(column);
      }
    }
    
    addProblem(problem);
  }
  
  // problem constructor methods with default values //////////////////////////

  /** add a problem with {@link ProblemImpl#TYPE_ERROR the default severity}.*/
  public void addProblem(String msg) {
    addProblem(msg, null, ProblemImpl.TYPE_ERROR, null);
  }

  /** add a problem with an exception cause and 
   * {@link ProblemImpl#TYPE_ERROR the default severity}.*/
  public void addProblem(String msg, Exception e) {
    addProblem(msg, e, ProblemImpl.TYPE_ERROR, null);
  }

  /** add a problem with {@link ProblemImpl#TYPE_ERROR the default severity}.*/
  public void addProblem(String msg, Element element) {
    addProblem(msg, null, ProblemImpl.TYPE_ERROR, element);
  }

  /** add a problem with an exception cause and 
   * {@link ProblemImpl#TYPE_ERROR the default severity}.*/
  public void addProblem(String msg, Exception e, Element element) {
    addProblem(msg, e, ProblemImpl.TYPE_ERROR, element);
  }

  // problem mgmt methods /////////////////////////////////////////////////////

  /** all problems encountered */
  public List<Problem> getProblems() {
    if (problems==null) {
      return Collections.emptyList();
    }
    return problems;
  }

  /** to add parsing problems during XML parsing and DOM walking. */
  public void addProblem(ProblemImpl problem) {
    if (problems==null) {
      problems = new ArrayList<Problem>();
    }
    problems.add(problem);
  }

  /** add all problems */
  public void addProblems(List<Problem> problems) {
    if (this.problems==null) {
      this.problems = new ArrayList<Problem>();
    }
    this.problems.addAll(problems);
  }

  /** indicates presence of problems */
  public boolean hasProblems() {
    return problems != null && !problems.isEmpty();
  }

  /** indicates presence of errors */
  public boolean hasErrors() {
    if (problems==null) {
      return false;
    }
    for (Problem problem: problems) {
      if (ProblemImpl.TYPE_ERROR.equals(problem.getSeverity())) {
        return true;
      }
    }
    return false;
  }

  /** allows to provide the list object that should be used to 
   * capture the parsing problems. */
  public void setProblems(List<Problem> problems) {
    this.problems = problems;
  }
  
    
  public JbpmException getJbpmException() {
    return getJbpmException(null);
  }
  
  public JbpmException getJbpmException(String message) {
    if (! hasErrors()) {
      return null;
    }
    
    Throwable cause = null;
    
    StringBuilder errorMsg = new StringBuilder();
    if (problems!=null) {
      if (message!=null) {
        errorMsg.append(message);
        errorMsg.append(": ");
      } else if (problems.size()==1) {
        Throwable singleCause = problems.get(0).getCause();
        if ( (singleCause!=null)
             && (JbpmException.class.isAssignableFrom(singleCause.getClass()))
           ) {
          return (JbpmException) singleCause;
        }
      }
      
      for (Problem p : getProblems()) {
        errorMsg.append(NEWLINE);
        errorMsg.append("  ");
        errorMsg.append(p.toString());

        if (p.getCause()!=null) {
          cause = p.getCause();
        }
      }
    }
    return new JbpmException(errorMsg.toString(), cause);
  }
}

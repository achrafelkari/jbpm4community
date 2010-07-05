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
package org.jbpm.pvm.internal.cmd;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.api.cmd.Environment;
import org.jbpm.api.history.HistoryComment;
import org.jbpm.pvm.internal.session.DbSession;

/**
 * @author Tom Baeyens
 */
public class GetTaskCommentsCmd extends AbstractCommand<List<HistoryComment>> {

  private static final long serialVersionUID = 1L;
  
  protected String taskId;
  
  public GetTaskCommentsCmd(String taskId) {
    this.taskId = taskId;
  }

  public List<HistoryComment> execute(Environment environment) throws Exception {
    DbSession dbSession = environment.get(DbSession.class);

    List<HistoryComment> comments = dbSession.findCommentsByTaskId(taskId);
    forceInitializationAndClean(comments);
    return comments;
  }

  protected void forceInitializationAndClean(List<? extends HistoryComment> comments) {
    if (comments!=null) {
      comments.size();
      List<HistoryComment> copy = new ArrayList<HistoryComment>(comments);
      for (int i=0; i<copy.size(); i++) {
        HistoryComment comment = copy.get(i);
        
        // when comments get deleted, it's possible that we 
        // get null values in the list as the indexes of the 
        // other comments are not upated.
        // So if there is a null value, we can safely delete it
        // from the persistent list and cause the indexes to 
        // be updated
        if (comment==null) {
          comments.remove(i);
        } else {
          forceInitializationAndClean(comment.getReplies());
        }
      }
    }
  }
}

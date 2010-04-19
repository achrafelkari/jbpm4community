package org.jbpm.pvm.internal.svc;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.identity.Group;
import org.jbpm.pvm.internal.identity.cmd.FindGroupsCmd;


public class FindGroupIds implements Command<List<String>> {

  private static final long serialVersionUID = 1L;

  protected String userId;

  public FindGroupIds(String userId) {
    this.userId = userId;
  }

  public List<String> execute(Environment environment) throws Exception {
    List<String> groupIds = new ArrayList<String>();
    
    FindGroupsCmd findGroupsCmd = new FindGroupsCmd(userId);
    List<Group> groups = findGroupsCmd.execute(environment);
    for (Group group: groups) {
      groupIds.add(group.getId());
    }
    
    return groupIds;
  }
  
  
}

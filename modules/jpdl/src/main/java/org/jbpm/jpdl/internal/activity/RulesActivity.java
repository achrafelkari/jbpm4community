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
package org.jbpm.jpdl.internal.activity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.drools.KnowledgeBase;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.jpdl.internal.rules.ExecutionGlobals;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.repository.RulesDeployer;


/**
 * @author Tom Baeyens
 */
public class RulesActivity extends DecisionConditionActivity {

  private static final long serialVersionUID = 1L;
  
  List<RulesFact> rulesFacts = new ArrayList<RulesFact>();  
  
  public void execute(ActivityExecution execution) throws Exception {
    ExecutionImpl executionImpl = (ExecutionImpl)execution;
    String deploymentId = executionImpl.getProcessDefinition().getDeploymentId();
    KnowledgeBase knowledgeBase = RulesDeployer.getKnowledgeBase(deploymentId);

    StatefulKnowledgeSession knowledgeSession = 
        knowledgeBase.newStatefulKnowledgeSession();
    
    ExecutionGlobals executionGlobals = new ExecutionGlobals(execution);
    knowledgeSession.getGlobals().setDelegate(executionGlobals);
   
    for (RulesFact rulesFact: rulesFacts) {
      Object fact = rulesFact.getObject(execution);
      knowledgeSession.insert(fact);
    }

    knowledgeSession.fireAllRules();
    
    super.execute(executionImpl);
  }
  
  public void addRulesFact(RulesFact rulesFact) {
    rulesFacts.add(rulesFact);
  }
}

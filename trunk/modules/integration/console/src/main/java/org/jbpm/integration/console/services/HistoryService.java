/* jboss.org */
package org.jbpm.integration.console.services;

import com.google.inject.Inject;
import org.jboss.bpm.console.client.model.HistoryActivityInstanceRef;
import org.jboss.bpm.console.client.model.HistoryProcessInstanceRef;
import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.annotations.Service;

import org.jbpm.api.JbpmException;
import org.jbpm.api.history.HistoryActivityInstance;
import org.jbpm.api.history.HistoryProcessInstance;
import org.jbpm.integration.console.JBPMIntegration;
import org.jbpm.integration.console.ModelAdaptor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Mar 15, 2010
 */
@Service("JBPM_HISTORY_SERVICE")
public class HistoryService extends JBPMIntegration implements MessageCallback
{

  private MessageBus bus;

  @Inject
  public HistoryService(MessageBus bus)
  {
    this.bus = bus;
  }

  public void callback(Message message)
  {
    switch (HistoryCommands.valueOf(message.getCommandType()))
    {
      case GET_FINISHED_PROCESS_INSTANCES:
        String definitionId = message.get(String.class, HistoryParts.PROCESS_DEFINITION_ID);
        List<HistoryProcessInstanceRef> results = getFinishedProcessInstances(definitionId);

        MessageBuilder.createConversation(message)
            .subjectProvided()
            .command(HistoryCommands.GET_FINISHED_PROCESS_INSTANCES)
            .with(HistoryParts.PROCESS_DEFINITION_ID, definitionId)
            .with(HistoryParts.INSTANCE_LIST, results)
            .errorsHandledBy(new ErrorCallback()
            {
              public boolean error(Message message, Throwable throwable)
              {
                throw new JbpmException("Failed to send message", throwable);
              }
            })
            .sendNowWith(bus);
        
        break;
      case GET_PROCESS_INSTANCE_HISTORY:
        String instanceId = message.get(String.class, HistoryParts.PROCESS_INSTANCE_ID);
        List<HistoryActivityInstanceRef> results2 = getProcessInstanceHistory(instanceId);

        MessageBuilder.createConversation(message)
            .subjectProvided()
            .command(HistoryCommands.GET_PROCESS_INSTANCE_HISTORY)
            .with(HistoryParts.PROCESS_INSTANCE_ID, instanceId)
            .with(HistoryParts.HISTORY_RECORDS, results2)
            .noErrorHandling()
            .sendNowWith(bus);

        break;
      default:
        throw new IllegalArgumentException("Unknown command "+ message.getCommandType());        

    }
  }

  private List<HistoryActivityInstanceRef> getProcessInstanceHistory(String processInstanceId) {

    org.jbpm.api.HistoryService historyService = this.processEngine.getHistoryService();

    List<HistoryActivityInstance> his = historyService.createHistoryActivityInstanceQuery().processInstanceId(processInstanceId).list();

    List<HistoryActivityInstanceRef> results = new ArrayList<HistoryActivityInstanceRef>();
    for (HistoryActivityInstance t0 : his) {

      results.add(ModelAdaptor.adoptHistoryActivity(t0));

    }

    return results;
  }

  private List<HistoryProcessInstanceRef> getFinishedProcessInstances(String definitionId) {

    org.jbpm.api.HistoryService historyService = this.processEngine.getHistoryService();

    List<HistoryProcessInstanceRef> results = new ArrayList<HistoryProcessInstanceRef>();

    List<HistoryProcessInstance> his = historyService.createHistoryProcessInstanceQuery().processDefinitionId(definitionId).list();

    for (HistoryProcessInstance t0 : his) {
      // all only finished elements
      if (t0.getEndTime() != null) {
        results.add(ModelAdaptor.adoptHistoryProcessInstance(t0));
      }
    }

    return results;
  }
  
}

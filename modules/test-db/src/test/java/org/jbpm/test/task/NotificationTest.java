package org.jbpm.test.task;

import java.io.*;
import java.util.*;
import javax.mail.Message;

import org.jbpm.api.*;
import org.jbpm.api.job.*;
import org.jbpm.api.task.*;
import org.jbpm.pvm.internal.email.spi.*;
import org.jbpm.test.*;

/**
 * @author Huisheng Xu
 */
public class NotificationTest extends JbpmTestCase {
    protected void setUp() throws Exception {
        super.setUp();
        identityService.createUser("johnsmith", "johnsmith", "johnsmith");
    }

    protected void tearDown() throws Exception {
        identityService.deleteUser("johnsmith");
        super.tearDown();
    }

    public void testDefault() throws Exception {
        deployJpdlXmlString(
            "<process name='X10ExceptionTaskTest'>"
            + "   <start name='start1' g='93,78,48,48' continue='async'>"
            + "      <transition name='to Test Task' to='Test Task' g='1,-20'/>"
            + "   </start>"
            + "   <end name='end1' g='315,236,48,48'/>"
            + "   <task name='Test Task' g='178,159,92,52' assignee='johnsmith'>"
            + "   <notification>"
            + "    <to users='${task.assignee}'/>"
            + "    <cc addresses='invalid@email@address'/>"
            + "    <subject>${task.name}</subject>"
            + "    <text>"
            + "      <![CDATA[Hi ${task.assignee},"
            + "      Task '${task.name}' has been assigned to you."
            + "      ${task.description}"
            + "      Sent by JBoss jBPM"
            + "      ]]>"
            + "    </text>"
            + "   </notification>"
            + "   <transition name='to end1' to='end1' g='-6,-22'/>"
            + " </task>" + "</process>");

        ProcessInstance processInstance = executionService
            .startProcessInstanceByKey("X10ExceptionTaskTest");
        String processInstanceId = processInstance.getId();
        int retries = 0;

        while (retries < 3) {
            retries++;

            try {
                List<Job> jobs = managementService.createJobQuery().list();
                Job job = jobs.get(0);
                managementService.executeJob(job.getId());

                break;
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        List<Task> tasks = taskService.createTaskQuery().list();
        assertEquals(0, tasks.size());
    }

    public void testSupportExpr() {
        deployJpdlXmlString(
            "<process name='NotificationTaskTest' xmlns='http://jbpm.org/4.3/jpdl'>"
            + "  <start name='start1' g='93,78,48,48'>"
            + "    <transition name='to Test Task' to='Test Task' g='1,-20'/>"
            + "  </start>"
            + "  <end name='end1' g='315,236,48,48'/>"
            + "  <task name='Test Task' g='178,159,92,52' assignee='johnsmith'>"
            + "    <notification expr='#{jbpmCustomMailProducer}'>"
            + "      <field name='templateName'><string value='planning-approval-start-notifcation'/></field>"
            + "    </notification>"
            + "    <transition name='to end1' to='end1' g='-6,-22'/>"
            + "  </task>"
            + "</process>");

        ProcessInstance processInstance = executionService
            .startProcessInstanceByKey("NotificationTaskTest",
            Collections.singletonMap("jbpmCustomMailProducer", new JbpmCustomMailProducer()));
        String processInstanceId = processInstance.getId();
    }

    public static class JbpmCustomMailProducer implements MailProducer, Serializable {
        private String templateName;
        public Collection<Message> produce(Execution execution) {
            return Collections.EMPTY_SET;
        }
    }
}

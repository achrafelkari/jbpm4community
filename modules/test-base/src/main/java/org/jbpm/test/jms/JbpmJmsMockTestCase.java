package org.jbpm.test.jms;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;

import org.jbpm.test.ejb.JbpmEjbMockTestCase;

import com.mockrunner.jms.DestinationManager;
import com.mockrunner.jms.JMSTestModule;
import com.mockrunner.mock.jms.JMSMockObjectFactory;


public class JbpmJmsMockTestCase extends JbpmEjbMockTestCase
{

  private JMSMockObjectFactory jmsMockFactory;
  private JMSTestModule jmsTestModule;
  
  private ConnectionFactory connectionFactory;
  private Queue queue;

  private JMSTestModule createJMSTestModule(JMSMockObjectFactory mockFactory)
  {
      return new JMSTestModule(mockFactory);
  }

  private JMSMockObjectFactory createJMSMockObjectFactory()
  {
      return new JMSMockObjectFactory();
  }
  
  private JMSMockObjectFactory getJMSMockObjectFactory()
  {
      synchronized(JMSMockObjectFactory.class) 
      {
          if(jmsMockFactory == null)
          {
              jmsMockFactory = createJMSMockObjectFactory();
          }
      }
      return jmsMockFactory;
  }
  
  private DestinationManager getDestinationManager()
  {
      return jmsTestModule.getDestinationManager();
  }
  
  protected ConnectionFactory getConnectionFactory() {
    if (connectionFactory == null) {
      connectionFactory = getJMSMockObjectFactory().createMockConnectionFactory();
    }
    return connectionFactory;
  }
  
  protected Queue getQueue() {
    if (queue == null) {
      queue = getDestinationManager().createQueue("testQueue");
    }
    return queue;
  }
  
  protected void setUp() throws Exception 
  {
    super.setUp();
    jmsTestModule = createJMSTestModule(getJMSMockObjectFactory());
  }

}

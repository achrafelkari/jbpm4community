package org.jbpm.test.ejb;
import org.jbpm.test.JbpmTestCase;

import com.mockrunner.ejb.EJBTestModule;
import com.mockrunner.mock.ejb.EJBMockObjectFactory;


public class JbpmEjbMockTestCase extends JbpmTestCase
{

  private EJBMockObjectFactory ejbMockFactory;
  private EJBTestModule ejbTestModule;
  
  private EJBTestModule createEJBTestModule()
  {
    return new EJBTestModule(getEJBMockObjectFactory());
  }
  
  private EJBMockObjectFactory createEJBMockObjectFactory()
  {
    return new EJBMockObjectFactory();
  }

  private EJBMockObjectFactory getEJBMockObjectFactory()
  {
      synchronized(EJBMockObjectFactory.class) 
      {
          if(ejbMockFactory == null)
          {
              ejbMockFactory = createEJBMockObjectFactory();
          }
      }
      return ejbMockFactory;
  }

  protected void bindToContext(String name, Object object) {
    ejbTestModule.bindToContext(name, object);
  }

  protected void setUp() throws Exception 
  {
    super.setUp();
    ejbTestModule = createEJBTestModule();
  }

}

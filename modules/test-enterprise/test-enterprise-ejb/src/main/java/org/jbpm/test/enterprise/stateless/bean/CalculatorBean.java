package org.jbpm.test.enterprise.stateless.bean;

import javax.ejb.Stateless;

@Stateless
public class CalculatorBean implements CalculatorRemote, CalculatorLocal
{
   public Integer add(Integer x, Integer y)
   {
      return x + y;
   }

   public Integer subtract(Integer x, Integer y)
   {
      return x - y;
   }
}

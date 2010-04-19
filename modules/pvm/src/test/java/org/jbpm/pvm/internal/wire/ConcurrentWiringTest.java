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
package org.jbpm.pvm.internal.wire;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.wire.WireContext;

/**
 * @author Tom Baeyens
 */
public class ConcurrentWiringTest extends WireTestCase {

  public static class A {
    public A() {
      sleepTight(10);
    }
  }
  
  public static class B {
    A a;
    public B() {
      sleepTight(10);
    }
    public void setA(A a) {
      sleepTight(4);
      this.a = a;
    }
  }
  
  public static class C {
    B b;
    public C() {
      sleepTight(10);
    }
    public void setB(B b) {
      sleepTight(3);
      this.b = b;
    }
  }
  
  public static class D {
    A a;
    B b;
    public D() {
      sleepTight(10);
    }
    public void setA(A a) {
      this.a = a;
    }
  }

  public class Collector extends Thread {
    WireContext wireContext;
    String objectName;
    Object result;
    public Collector(WireContext wireContext, String objectName, String name) {
      super(name);
      this.wireContext = wireContext;
      this.objectName = objectName;
    }
    public void run() {
      sleepTight(5);
      result = wireContext.get(objectName);
    }
  }

  public void testConcurrency() throws Exception {
    WireContext wireContext = createWireContext(
      "<objects>" +
      "  <object name='a' class='"+A.class.getName()+"' />" +
      "  <object name='b' class='"+B.class.getName()+"'>" +
      "    <property name='a'><ref object='a' /></property>" +
      "  </object>" +
      "  <object name='c' class='"+C.class.getName()+"'>" +
      "    <property name='b'><ref object='b' /></property>" +
      "  </object>" +
      "  <object name='d' class='"+D.class.getName()+"'>" +
      "    <property name='a'><ref object='a' /></property>" +
      "    <field name='b'><ref object='b' /></field>" +
      "  </object>" +
      "</objects>"
    );
    
    List<Collector> collectors = new ArrayList<Collector>();
    for (int i=0; i<10; i++) {
      collectors.add(new Collector(wireContext, "b", "b"+i));
      collectors.add(new Collector(wireContext, "c", "c"+i));
      collectors.add(new Collector(wireContext, "d", "d"+i));
    }
    collectors.add(new Collector(wireContext, "a", "a"));
    
    for (Thread collector: collectors) {
      collector.start();
    }

    for (Thread collector: collectors) {
      boolean isJoined = false;
      while (collector.isAlive() && !isJoined) {
        try {
          collector.join();
          isJoined = true;
        } catch (InterruptedException e) {
          log.info("ignoring interrupted exception while joining "+collector);
        }
      }
    }
    
    Object a = wireContext.get("a");
    Object b = wireContext.get("b");
    Object c = wireContext.get("c");
    Object d = wireContext.get("d");
    
    for (Collector collector: collectors) {
      if ("a".equals(collector.objectName)) {
        assertSame(a, collector.result);
      } else if ("b".equals(collector.objectName)) {
        assertSame(b, collector.result);
      } else if ("c".equals(collector.objectName)) {
        assertSame(c, collector.result);
      } else if ("d".equals(collector.objectName)) {
        assertSame(d, collector.result);
      }
    }
  }

  static void sleepTight(int millis) {
    try {
      log.debug("sleeping for "+millis);
      Thread.sleep(millis);
      log.debug("woke up");
    } catch (InterruptedException e) {
      throw new RuntimeException("interrupted exception", e);
    }
  }
  private static Log log = Log.getLog(ConcurrentWiringTest.class.getName());
}
